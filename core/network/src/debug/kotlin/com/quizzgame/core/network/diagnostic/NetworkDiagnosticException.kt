package com.quizzgame.core.network.diagnostic

import com.quizzgame.core.network.result.NetworkFailure
import java.io.IOException

open class NetworkDiagnosticException(val diagnostic: NetworkFailure.MockConfiguration) :
    IOException("Network diagnostic: $diagnostic")
