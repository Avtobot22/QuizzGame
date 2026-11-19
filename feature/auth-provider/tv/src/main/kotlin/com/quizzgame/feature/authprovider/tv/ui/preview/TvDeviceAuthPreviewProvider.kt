package com.quizzgame.feature.authprovider.tv.ui.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.quizzgame.feature.authprovider.tv.presentation.TvDeviceAuthState
import com.quizzgame.shared.auth.domain.entity.AuthFailure
import com.quizzgame.shared.auth.domain.entity.device.DeviceAuthorization

internal class TvDeviceAuthPreviewProvider : PreviewParameterProvider<TvDeviceAuthState> {
    private val authorization = DeviceAuthorization(
        "ABCD-2345",
        "https://auth.example.test/device",
        "https://auth.example.test/device?user_code=ABCD-2345",
    )

    private val examples = listOf(
        "Загрузка кода" to TvDeviceAuthState.Starting,
        "Ожидание подтверждения" to TvDeviceAuthState.Waiting(authorization),
        "Код подтверждён" to TvDeviceAuthState.Confirming(authorization),
        "Код истёк" to TvDeviceAuthState.Failed(AuthFailure.Expired),
        "Вход отклонён" to TvDeviceAuthState.Failed(AuthFailure.ProviderRejected),
        "Ошибка сохранения сессии" to TvDeviceAuthState.Failed(AuthFailure.StorageRecovery),
    )

    override val values = examples.asSequence().map { it.second }

    override fun getDisplayName(index: Int) = examples.getOrNull(index)?.first
}
