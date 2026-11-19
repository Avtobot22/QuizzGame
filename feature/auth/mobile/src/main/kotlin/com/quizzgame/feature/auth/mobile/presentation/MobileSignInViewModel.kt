package com.quizzgame.feature.auth.mobile.presentation

import com.quizzgame.shared.auth.domain.scenario.OtpSignInScenario
import com.quizzgame.shared.auth.presentation.OtpSignInEnvironment
import com.quizzgame.shared.auth.presentation.OtpSignInViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
internal class MobileSignInViewModel @Inject constructor(
    scenario: OtpSignInScenario,
    environment: OtpSignInEnvironment,
) : OtpSignInViewModel(scenario, environment)
