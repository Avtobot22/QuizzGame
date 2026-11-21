package com.quizzgame.feature.play.mobile.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.quizzgame.feature.play.mobile.navigation.MobileJoinRoute
import com.quizzgame.feature.play.mobile.navigation.MobilePlayCallbacks
import com.quizzgame.feature.play.mobile.presentation.MobilePlayIntent
import com.quizzgame.feature.play.mobile.presentation.MobilePlayValidation
import com.quizzgame.feature.play.mobile.presentation.MobilePlayViewModel

@Composable
internal fun MobilePlayScreen(callbacks: MobilePlayCallbacks, draft: MobileJoinRoute? = null) {
    val viewModel: MobilePlayViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val onIntent = viewModel::onIntent
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, viewModel) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) { viewModel.observeIdentity() }
    }
    LaunchedEffect(draft) {
        if (draft != null) onIntent(MobilePlayIntent.RestoreDraft(draft.code, draft.nickname))
    }
    LaunchedEffect(state.destination) {
        val id = state.destination ?: return@LaunchedEffect
        if (callbacks.openLobby(id.toString())) onIntent(MobilePlayIntent.NavigationHandled)
    }
    val onScan = mobilePlayScanner(onIntent)
    MobilePlayContent(state, onIntent, onScan = onScan, onSignIn = {
        callbacks.signIn(state.code, state.nickname)
    })
}

@Composable
private fun mobilePlayScanner(onIntent: (MobilePlayIntent) -> Unit): () -> Unit {
    val context = LocalContext.current
    val focus = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    var showScanner by rememberSaveable { mutableStateOf(false) }
    val requestPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            showScanner = true
        } else {
            onIntent(MobilePlayIntent.CameraError(MobilePlayValidation.CAMERA_DENIED))
        }
    }
    if (showScanner) {
        MobileRoomScanner(
            onDismiss = { showScanner = false },
            onDecode = {
                showScanner = false
                onIntent(MobilePlayIntent.Qr(it))
            },
            onUnavailable = {
                showScanner = false
                onIntent(MobilePlayIntent.CameraError(MobilePlayValidation.CAMERA_UNAVAILABLE))
            },
        )
    }
    return {
        focus.clearFocus()
        keyboard?.hide()
        if (context.checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            showScanner = true
        } else {
            requestPermission.launch(Manifest.permission.CAMERA)
        }
    }
}
