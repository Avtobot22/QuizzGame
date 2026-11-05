package com.quizzgame.core.session.lifecycle

import com.quizzgame.core.network.auth.CredentialsSnapshot
import com.quizzgame.core.session.storage.SessionRecord

internal enum class SessionMutationKind {
    SIGN_IN,
    REFRESH_PAIR,
    LOGOUT,
    REVOKED,
    RESTORE_REFRESH,
}

internal sealed interface PendingSessionMutation {
    data object Read : PendingSessionMutation
    data object UncertainRefresh : PendingSessionMutation

    class Write(
        val kind: SessionMutationKind,
        val before: SessionRecord,
        val target: SessionRecord,
        val marker: SessionRecord? = null,
    ) : PendingSessionMutation {
        override fun toString(): String = "PendingSessionMutation.Write($kind, <redacted>)"
    }

    class RefreshInFlight(val observed: CredentialsSnapshot, val record: SessionRecord) : PendingSessionMutation {
        override fun toString(): String = "PendingSessionMutation.RefreshInFlight(<redacted>)"
    }
}
