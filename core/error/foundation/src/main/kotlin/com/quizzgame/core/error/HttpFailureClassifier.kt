package com.quizzgame.core.error

internal fun classifyServiceFailure(status: Int, reasonCode: Int?): AppFailure.Service {
    val kind = when (status) {
        HTTP_FORBIDDEN                      -> ServiceKind.ACCESS_DENIED
        HTTP_TOO_MANY_REQUESTS              -> ServiceKind.RATE_LIMITED
        in HTTP_SERVER_MIN..HTTP_SERVER_MAX -> ServiceKind.UNAVAILABLE
        in HTTP_CLIENT_MIN..HTTP_CLIENT_MAX -> ServiceKind.CLIENT_REJECTED
        else                                -> ServiceKind.OTHER_HTTP
    }
    return AppFailure.Service(kind, status, reasonCode)
}

private const val HTTP_FORBIDDEN = 403
private const val HTTP_TOO_MANY_REQUESTS = 429
private const val HTTP_CLIENT_MIN = 400
private const val HTTP_CLIENT_MAX = 499
private const val HTTP_SERVER_MIN = 500
private const val HTTP_SERVER_MAX = 599
