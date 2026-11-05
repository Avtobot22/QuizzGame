package com.quizzgame.core.session.refresh.api

import java.io.IOException

class RefreshException(val failure: RefreshFailure) : IOException("Refresh request failed: $failure")
