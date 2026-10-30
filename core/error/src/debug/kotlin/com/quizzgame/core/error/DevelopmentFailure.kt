package com.quizzgame.core.error

data class DevelopmentFailure(val kind: DevelopmentKind) : AppFailure
