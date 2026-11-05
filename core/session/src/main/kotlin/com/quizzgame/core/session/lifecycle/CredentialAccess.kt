package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialUnavailableKind
import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.core.session.model.TokenPair
import com.quizzgame.core.session.storage.SessionIntent
import com.quizzgame.core.session.storage.SessionRecord
import com.quizzgame.core.session.storage.SessionStorageFailure

internal sealed interface CredentialAccess {
    class Ready(val record: SessionRecord, val expired: Boolean = false) : CredentialAccess {
        val state: SessionState.Ready = when {
            expired -> SessionState.Expired
            record.tokens != null -> SessionState.Authenticated(record.epoch)
            else -> SessionState.Guest
        }

        fun snapshot() = CredentialsSnapshot(record.tokens?.access, record.epoch, record.revision)

        fun matches(observed: CredentialsSnapshot): Boolean = record.tokens != null &&
            record.epoch == observed.sessionGeneration && record.revision == observed.credentialsRevision &&
            record.tokens.access == observed.accessToken
    }

    class Blocked(
        val kind: CredentialUnavailableKind,
        val record: SessionRecord?,
        val epoch: Long,
        val lastKnown: SessionState.Ready?,
        val storageFailure: SessionStorageFailure? = null,
    ) : CredentialAccess

    data object Closed : CredentialAccess
}

internal val CredentialAccess.record: SessionRecord? get() = when (this) {
    is CredentialAccess.Ready -> record
    is CredentialAccess.Blocked -> record
    CredentialAccess.Closed -> null
}

internal val CredentialAccess.epoch: Long get() = when (this) {
    is CredentialAccess.Ready -> record.epoch
    is CredentialAccess.Blocked -> epoch
    CredentialAccess.Closed -> 0L
}

internal val CredentialAccess.lastKnown: SessionState.Ready? get() = when (this) {
    is CredentialAccess.Ready -> state
    is CredentialAccess.Blocked -> lastKnown
    CredentialAccess.Closed -> null
}

internal fun SessionRecord.updated(
    tokens: TokenPair? = this.tokens,
    epoch: Long = this.epoch,
    revision: Long = this.revision,
    intent: SessionIntent = SessionIntent.NONE,
) = SessionRecord(
    deviceId,
    tokens,
    epoch,
    revision,
    intent,
)

internal fun SessionRecord.sameContents(other: SessionRecord): Boolean = deviceId == other.deviceId &&
    epoch == other.epoch && revision == other.revision && intent == other.intent &&
    tokens?.access == other.tokens?.access && tokens?.refresh == other.tokens?.refresh

internal fun CredentialAccess.matchingRecord(
    observed: CredentialsSnapshot,
    allowRecovery: Boolean = false,
): SessionRecord? = when (this) {
    is CredentialAccess.Ready -> record.takeIf { matches(observed) }
    is CredentialAccess.Blocked -> record?.takeIf {
        allowRecovery && kind == CredentialUnavailableKind.REFRESH_UNCERTAIN &&
            epoch == it.epoch && CredentialAccess.Ready(it).matches(observed)
    }
    CredentialAccess.Closed -> null
}
