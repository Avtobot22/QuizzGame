package com.quizzgame.core.error.ui.common

import com.quizzgame.core.error.ErrorCopyKey
import com.quizzgame.core.ui.status.QuizzStatusIcon

internal fun errorCopyResources(key: ErrorCopyKey): ErrorCopyResources = when (key) {
    ErrorCopyKey.CONNECTION_PROBLEM -> ErrorCopyResources(
        R.string.quizz_error_connection_problem,
        QuizzStatusIcon.NETWORK,
    )
    ErrorCopyKey.REQUEST_TIMED_OUT -> ErrorCopyResources(
        R.string.quizz_error_request_timed_out,
        QuizzStatusIcon.NETWORK,
    )
    ErrorCopyKey.SERVER_UNAVAILABLE -> ErrorCopyResources(
        R.string.quizz_error_server_unavailable,
        QuizzStatusIcon.SERVER,
    )
    ErrorCopyKey.TOO_MANY_REQUESTS -> ErrorCopyResources(R.string.quizz_error_too_many_requests, QuizzStatusIcon.SERVER)
    ErrorCopyKey.ACCESS_DENIED -> ErrorCopyResources(R.string.quizz_error_access_denied, QuizzStatusIcon.AUTH)
    ErrorCopyKey.SIGN_IN_REQUIRED -> ErrorCopyResources(R.string.quizz_error_sign_in_required, QuizzStatusIcon.AUTH)
    ErrorCopyKey.SESSION_EXPIRED -> ErrorCopyResources(R.string.quizz_error_session_expired, QuizzStatusIcon.AUTH)
    ErrorCopyKey.UNEXPECTED_RESPONSE -> ErrorCopyResources(
        R.string.quizz_error_unexpected_response,
        QuizzStatusIcon.GENERIC,
    )
    ErrorCopyKey.STORAGE_PROBLEM -> ErrorCopyResources(R.string.quizz_error_storage_problem, QuizzStatusIcon.GENERIC)
    ErrorCopyKey.GENERIC_FAILURE -> ErrorCopyResources(R.string.quizz_error_generic_failure, QuizzStatusIcon.GENERIC)
    ErrorCopyKey.MOCK_CONFIGURATION -> ErrorCopyResources(
        R.string.quizz_error_mock_configuration,
        QuizzStatusIcon.GENERIC,
    )
}
