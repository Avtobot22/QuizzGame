package com.quizzgame.core.session.api

import java.io.IOException

class RefreshException(val failure: RefreshFailure) : IOException("Refresh request failed: $failure")
