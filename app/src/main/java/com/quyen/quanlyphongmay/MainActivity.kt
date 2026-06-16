package com.quyen.quanlyphongmay

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.google.firebase.FirebaseApp
import com.quyen.quanlyphongmay.core.AppAuthContract
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.navigation.QuanLyPhongMayAppShell
import com.quyen.quanlyphongmay.repository.AuthRepository
import com.quyen.quanlyphongmay.screens.LoginScreen
import com.quyen.quanlyphongmay.ui.design.AppScreenContainer
import com.quyen.quanlyphongmay.ui.design.LoadingStateCard

class MainActivity : ComponentActivity() {

    private val authRepository: AuthRepository by lazy {
        AuthRepository()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        setContent {
            MaterialTheme {
                QuanLyPhongMayRoot(
                    authRepository = authRepository
                )
            }
        }
    }
}

private sealed class AppEntryState {
    data object CheckingSession : AppEntryState()
    data object SignedOut : AppEntryState()
    data class Authenticated(val user: User) : AppEntryState()
}

@Composable
private fun QuanLyPhongMayRoot(
    authRepository: AuthRepository
) {
    var entryState by remember {
        mutableStateOf<AppEntryState>(AppEntryState.CheckingSession)
    }

    LaunchedEffect(Unit) {
        authRepository.getCurrentUser { success, message, user ->
            val normalizedUser = user?.normalizedCopy()

            entryState = if (
                success &&
                normalizedUser != null &&
                AppAuthContract.canUserAccessApp(
                    email = normalizedUser.email,
                    rawStatus = normalizedUser.status
                )
            ) {
                AppEntryState.Authenticated(normalizedUser)
            } else {
                authRepository.logout()
                AppEntryState.SignedOut
            }
        }
    }

    when (val state = entryState) {
        AppEntryState.CheckingSession -> {
            CheckingSessionContent()
        }

        AppEntryState.SignedOut -> {
            LoginScreen(
                authRepository = authRepository,
                onLoginSuccess = { user ->
                    val normalizedUser = user.normalizedCopy()

                    if (
                        AppAuthContract.canUserAccessApp(
                            email = normalizedUser.email,
                            rawStatus = normalizedUser.status
                        )
                    ) {
                        entryState = AppEntryState.Authenticated(normalizedUser)
                    } else {
                        authRepository.logout()
                        entryState = AppEntryState.SignedOut
                    }
                },
                onPendingApproval = {
                    authRepository.logout()
                    entryState = AppEntryState.SignedOut
                }
            )
        }

        is AppEntryState.Authenticated -> {
            QuanLyPhongMayAppShell(
                user = state.user,
                authRepository = authRepository,
                onSignedOut = {
                    entryState = AppEntryState.SignedOut
                }
            )
        }
    }
}

@Composable
private fun CheckingSessionContent() {
    AppScreenContainer {
        LoadingStateCard(
            message = "Đang kiểm tra phiên đăng nhập",
            modifier = Modifier.fillMaxWidth()
        )
    }
}