package com.quizzgame.core.network

import java.io.IOException

open class NetworkDiagnosticException(val diagnostic: NetworkFailure.MockConfiguration) :
    IOException("Network diagnostic: $diagnostic")
