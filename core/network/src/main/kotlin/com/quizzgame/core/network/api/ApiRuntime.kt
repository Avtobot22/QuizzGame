package com.quizzgame.core.network.api

import com.quizzgame.core.network.result.NetworkResultAdapter
import retrofit2.Retrofit

class ApiRuntime(val retrofit: Retrofit, val results: NetworkResultAdapter)
