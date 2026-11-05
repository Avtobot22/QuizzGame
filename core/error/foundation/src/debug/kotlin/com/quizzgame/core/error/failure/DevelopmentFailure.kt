package com.quizzgame.core.error.failure

data class DevelopmentFailure(val kind: DevelopmentKind) : AppFailure
