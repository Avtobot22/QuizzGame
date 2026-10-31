package com.quizzgame.core.error.ui.mobile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.quizzgame.core.designsystem.mobile.QuizzMobileTheme
import com.quizzgame.core.error.AppFailure
import com.quizzgame.core.error.ConnectivityKind
import com.quizzgame.core.error.DefaultErrorDecisionPolicy
import com.quizzgame.core.error.ErrorOperationContext
import com.quizzgame.core.error.ErrorSurface
import com.quizzgame.core.error.OperationKind
import com.quizzgame.core.error.RetrySafety
import com.quizzgame.core.error.ServiceKind

private const val PREVIEW_UNAVAILABLE_STATUS = 503

private fun previewSpec(operation: OperationKind, content: Boolean, safety: RetrySafety) =
    DefaultErrorDecisionPolicy().decide(
        AppFailure.Service(ServiceKind.UNAVAILABLE, PREVIEW_UNAVAILABLE_STATUS),
        ErrorOperationContext(operation, content, safety),
    )

@Preview(name = "503 without content", locale = "ru", widthDp = 360, heightDp = 640)
@Composable
private fun BlockingPreview() {
    QuizzMobileTheme {
        QuizzMobileErrorFullScreen(
            previewSpec(OperationKind.INITIAL_LOAD, false, RetrySafety.SAFE_TO_REPEAT),
            onAction = {},
        )
    }
}

@Preview(name = "Refresh preserves content", locale = "ru", widthDp = 360)
@Composable
private fun ContentPreview() {
    QuizzMobileTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Сохранённое содержимое")
            QuizzMobileErrorBanner(
                previewSpec(OperationKind.USER_REFRESH, true, RetrySafety.SAFE_TO_REPEAT),
                onAction = {},
            )
            QuizzMobileErrorInline(
                DefaultErrorDecisionPolicy().decide(
                    AppFailure.Connectivity(ConnectivityKind.TIMEOUT),
                    ErrorOperationContext(OperationKind.AUTH_FLOW, true, RetrySafety.REQUIRES_CONFIRMATION),
                ),
                onAction = {},
            )
        }
    }
}

@Preview(name = "Prefetch stays silent", locale = "ru", widthDp = 360)
@Composable
private fun OptionalPreview() {
    QuizzMobileTheme {
        val optional = previewSpec(OperationKind.PREFETCH, false, RetrySafety.SAFE_TO_REPEAT)
        check(optional.surface == ErrorSurface.SILENT)
        Text("Обложка пока недоступна")
    }
}

@Preview(name = "Forbidden repeat has no retry", locale = "ru", widthDp = 360, heightDp = 640)
@Composable
private fun ForbiddenPreview() {
    QuizzMobileTheme {
        QuizzMobileErrorFullScreen(
            previewSpec(OperationKind.INITIAL_LOAD, false, RetrySafety.FORBIDDEN),
            onAction = {},
        )
    }
}
