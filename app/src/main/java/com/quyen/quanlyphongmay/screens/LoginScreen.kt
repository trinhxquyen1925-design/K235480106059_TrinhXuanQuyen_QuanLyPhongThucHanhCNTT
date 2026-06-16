package com.quyen.quanlyphongmay.screens

import android.util.Patterns
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Router
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.quyen.quanlyphongmay.core.AppAuthContract
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.AuthRepository
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private object PremiumLoginColors {
    val BgTop = Color(0xFFF9FDFF)
    val BgMiddle = Color(0xFFF1F8FF)
    val BgBottom = Color(0xFFE7F8FF)

    val Card = Color(0xFCFFFFFF)
    val Field = Color(0xFFFFFFFF)

    val Ink = Color(0xFF0E1A33)
    val Text = Color(0xFF20314C)
    val Muted = Color(0xFF73849B)

    val Border = Color(0xFFD9E7F5)
    val BorderSoft = Color(0xFFEAF2FB)
    val Focus = Color(0xFF2F83FF)

    val Cyan = Color(0xFF25C7D8)
    val CyanDeep = Color(0xFF15A8CB)
    val Blue = Color(0xFF4287F5)
    val BlueDeep = Color(0xFF2666EA)
    val Purple = Color(0xFF8B5CF6)
    val Mint = Color(0xFF2FD4BA)
    val Rose = Color(0xFFE64B66)

    val CyanSoft = Color(0xFFE8FCFF)
    val BlueSoft = Color(0xFFEDF5FF)
    val PurpleSoft = Color(0xFFF4EEFF)
    val MintSoft = Color(0xFFE9FFF8)
    val RoseSoft = Color(0xFFFFEEF3)
}

private enum class AuthMode {
    Login,
    Register
}

private enum class AccountRole(
    val title: String,
    val roleValue: String,
    val codeLabel: String,
    val extraLabel: String,
    val icon: ImageVector,
    val start: Color,
    val end: Color,
    val soft: Color
) {
    Student(
        title = "Sinh viên",
        roleValue = AppAuthContract.Role.Student.firestoreValue,
        codeLabel = "MSSV",
        extraLabel = "Lớp",
        icon = Icons.Default.School,
        start = PremiumLoginColors.Cyan,
        end = PremiumLoginColors.Blue,
        soft = PremiumLoginColors.CyanSoft
    ),
    Teacher(
        title = "Giáo viên",
        roleValue = AppAuthContract.Role.Teacher.firestoreValue,
        codeLabel = "Mã giảng viên",
        extraLabel = "Khoa / Bộ môn",
        icon = Icons.Default.Work,
        start = PremiumLoginColors.Purple,
        end = PremiumLoginColors.Blue,
        soft = PremiumLoginColors.PurpleSoft
    )
}

@Immutable
private data class FloatingDot(
    val x: Float,
    val y: Float,
    val radius: Float,
    val alpha: Float,
    val color: Color,
    val speed: Float
)

@Composable
fun LoginScreen(
    authRepository: AuthRepository = AuthRepository(),
    onLoginSuccess: (User) -> Unit = {},
    onPendingApproval: (User?) -> Unit = {}
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current

    var mode by rememberSaveable { mutableStateOf(AuthMode.Login) }

    var loginEmail by rememberSaveable { mutableStateOf("") }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var loginPasswordVisible by rememberSaveable { mutableStateOf(false) }

    var fullName by rememberSaveable { mutableStateOf("") }
    var registerEmail by rememberSaveable { mutableStateOf("") }
    var registerPassword by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var identifier by rememberSaveable { mutableStateOf("") }
    var department by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var registerPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var confirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var selectedRole by rememberSaveable { mutableStateOf(AccountRole.Student) }

    var forgotDialogVisible by rememberSaveable { mutableStateOf(false) }
    var forgotEmail by rememberSaveable { mutableStateOf("") }

    var loginLoading by rememberSaveable { mutableStateOf(false) }
    var registerLoading by rememberSaveable { mutableStateOf(false) }
    var forgotLoading by rememberSaveable { mutableStateOf(false) }

    var loginError by rememberSaveable { mutableStateOf("") }
    var registerError by rememberSaveable { mutableStateOf("") }

    var loginShakeKey by rememberSaveable { mutableIntStateOf(0) }
    var registerShakeKey by rememberSaveable { mutableIntStateOf(0) }

    val loginReady by remember(loginEmail, loginPassword) {
        derivedStateOf {
            loginEmail.trim().isNotBlank() && loginPassword.isNotBlank()
        }
    }

    val registerReady by remember(
        fullName,
        registerEmail,
        registerPassword,
        confirmPassword,
        identifier,
        department,
        phone
    ) {
        derivedStateOf {
            fullName.trim().isNotBlank() &&
                    registerEmail.trim().isNotBlank() &&
                    registerPassword.length >= 6 &&
                    confirmPassword == registerPassword &&
                    identifier.trim().isNotBlank() &&
                    department.trim().isNotBlank() &&
                    phone.trim().length >= 9
        }
    }

    fun showSnack(message: String) {
        scope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    fun handleLogin() {
        keyboard?.hide()
        loginError = ""

        val email = AppAuthContract.normalizeEmail(loginEmail)
        val password = loginPassword
        val validation = validateLogin(email, password)

        if (validation != null) {
            loginError = validation
            loginShakeKey++
            return
        }

        loginLoading = true

        authRepository.login(email, password) { success, message, user ->
            loginLoading = false

            if (!success) {
                loginError = AppAuthContract.normalizeAuthMessage(message)
                loginShakeKey++
                return@login
            }

            if (user == null) {
                loginError = "Không thể tải dữ liệu tài khoản."
                loginShakeKey++
                return@login
            }

            when (AppAuthContract.resolveStatusForEmail(user.email, user.status)) {
                AppAuthContract.Status.Pending -> {
                    showSnack("Tài khoản đang chờ phê duyệt.")
                    onPendingApproval(user)
                }

                AppAuthContract.Status.Rejected -> {
                    loginError = "Tài khoản của bạn đã bị từ chối."
                    loginShakeKey++
                }

                AppAuthContract.Status.Locked -> {
                    loginError = "Tài khoản của bạn đang bị khóa."
                    loginShakeKey++
                }

                AppAuthContract.Status.Approved -> {
                    onLoginSuccess(user.normalizedCopy())
                }
            }
        }
    }

    fun handleRegister() {
        keyboard?.hide()
        registerError = ""

        val cleanEmail = AppAuthContract.normalizeEmail(registerEmail)
        val validation = validateRegister(
            fullName = fullName.trim(),
            email = cleanEmail,
            password = registerPassword,
            confirmPassword = confirmPassword,
            identifier = identifier.trim(),
            department = department.trim(),
            phone = phone.trim(),
            role = selectedRole
        )

        if (validation != null) {
            registerError = validation
            registerShakeKey++
            return
        }

        registerLoading = true

        authRepository.register(
            email = cleanEmail,
            password = registerPassword,
            fullName = fullName.trim(),
            identifier = identifier.trim(),
            role = selectedRole.roleValue,
            department = department.trim(),
            phone = phone.trim()
        ) { success, message, user ->
            registerLoading = false

            if (!success) {
                registerError = AppAuthContract.normalizeAuthMessage(message)
                registerShakeKey++
                return@register
            }

            showSnack("Đăng ký thành công. Vui lòng chờ phê duyệt.")

            loginEmail = cleanEmail
            loginPassword = ""

            fullName = ""
            registerEmail = ""
            registerPassword = ""
            confirmPassword = ""
            identifier = ""
            department = ""
            phone = ""
            registerError = ""

            mode = AuthMode.Login
            onPendingApproval(user)
        }
    }

    fun handleForgotPassword() {
        keyboard?.hide()

        val email = AppAuthContract.normalizeEmail(forgotEmail)

        when {
            email.isBlank() -> {
                showSnack("Vui lòng nhập email.")
                return
            }

            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                showSnack("Email không đúng định dạng.")
                return
            }
        }

        forgotLoading = true

        authRepository.sendPasswordResetEmail(email) { success, message ->
            forgotLoading = false

            if (success) {
                forgotDialogVisible = false
                showSnack(message ?: "Đã gửi email khôi phục mật khẩu.")
            } else {
                showSnack(AppAuthContract.normalizeAuthMessage(message))
            }
        }
    }

    Scaffold(
        containerColor = PremiumLoginColors.BgMiddle,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            PremiumLoginColors.BgTop,
                            PremiumLoginColors.BgMiddle,
                            PremiumLoginColors.BgBottom
                        )
                    )
                )
                .imePadding()
        ) {
            PremiumBackground()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                PremiumHeroSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(248.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                PremiumTitleSection()

                Spacer(modifier = Modifier.height(18.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 530.dp),
                    shape = RoundedCornerShape(34.dp),
                    color = PremiumLoginColors.Card,
                    shadowElevation = 18.dp,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.96f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 18.dp, vertical = 18.dp)
                    ) {
                        AuthModeSwitch(
                            mode = mode,
                            onModeChange = {
                                mode = it
                                loginError = ""
                                registerError = ""
                            }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        AnimatedContent(
                            targetState = mode,
                            transitionSpec = {
                                val direction = if (targetState == AuthMode.Register) 1 else -1
                                (
                                        slideInHorizontally(
                                            animationSpec = tween(330, easing = FastOutSlowInEasing),
                                            initialOffsetX = { fullWidth -> fullWidth * direction }
                                        ) + fadeIn(tween(240)) + scaleIn(initialScale = 0.985f)
                                        ).togetherWith(
                                        slideOutHorizontally(
                                            animationSpec = tween(260, easing = FastOutSlowInEasing),
                                            targetOffsetX = { fullWidth -> -fullWidth * direction / 2 }
                                        ) + fadeOut(tween(200)) + scaleOut(targetScale = 0.985f)
                                    ).using(SizeTransform(clip = false))
                            },
                            label = "auth-content"
                        ) { currentMode ->
                            if (currentMode == AuthMode.Login) {
                                LoginFormContent(
                                    email = loginEmail,
                                    onEmailChange = {
                                        loginEmail = it
                                        loginError = ""
                                    },
                                    password = loginPassword,
                                    onPasswordChange = {
                                        loginPassword = it
                                        loginError = ""
                                    },
                                    passwordVisible = loginPasswordVisible,
                                    onPasswordVisibleChange = { loginPasswordVisible = it },
                                    loading = loginLoading,
                                    ready = loginReady,
                                    error = loginError,
                                    shakeKey = loginShakeKey,
                                    onForgotPassword = {
                                        forgotEmail = loginEmail
                                        forgotDialogVisible = true
                                    },
                                    onSubmit = { handleLogin() },
                                    onSwitchMode = {
                                        mode = AuthMode.Register
                                        loginError = ""
                                    }
                                )
                            } else {
                                RegisterFormContent(
                                    fullName = fullName,
                                    onFullNameChange = {
                                        fullName = it
                                        registerError = ""
                                    },
                                    email = registerEmail,
                                    onEmailChange = {
                                        registerEmail = it
                                        registerError = ""
                                    },
                                    password = registerPassword,
                                    onPasswordChange = {
                                        registerPassword = it
                                        registerError = ""
                                    },
                                    confirmPassword = confirmPassword,
                                    onConfirmPasswordChange = {
                                        confirmPassword = it
                                        registerError = ""
                                    },
                                    role = selectedRole,
                                    onRoleChange = {
                                        selectedRole = it
                                        identifier = ""
                                        department = ""
                                        registerError = ""
                                    },
                                    identifier = identifier,
                                    onIdentifierChange = {
                                        identifier = it
                                        registerError = ""
                                    },
                                    department = department,
                                    onDepartmentChange = {
                                        department = it
                                        registerError = ""
                                    },
                                    phone = phone,
                                    onPhoneChange = {
                                        phone = it
                                        registerError = ""
                                    },
                                    passwordVisible = registerPasswordVisible,
                                    onPasswordVisibleChange = { registerPasswordVisible = it },
                                    confirmPasswordVisible = confirmPasswordVisible,
                                    onConfirmPasswordVisibleChange = { confirmPasswordVisible = it },
                                    loading = registerLoading,
                                    ready = registerReady,
                                    error = registerError,
                                    shakeKey = registerShakeKey,
                                    onSubmit = { handleRegister() },
                                    onSwitchMode = {
                                        mode = AuthMode.Login
                                        registerError = ""
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                DesignerCredit()

                Spacer(modifier = Modifier.height(22.dp))
            }

            if (forgotDialogVisible) {
                ForgotPasswordDialog(
                    email = forgotEmail,
                    onEmailChange = { forgotEmail = it },
                    loading = forgotLoading,
                    onDismiss = {
                        if (!forgotLoading) {
                            forgotDialogVisible = false
                        }
                    },
                    onSubmit = { handleForgotPassword() }
                )
            }
        }
    }
}

@Composable
private fun PremiumBackground() {
    val transition = rememberInfiniteTransition(label = "background")

    val glowA by transition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(7200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow-a"
    )

    val glowB by transition.animateFloat(
        initialValue = 18f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(8500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow-b"
    )

    val gridProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(11000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid-progress"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(330.dp)
                .offset(x = (-135).dp, y = (118 + glowA).dp)
                .blur(34.dp)
                .clip(CircleShape)
                .background(PremiumLoginColors.Blue.copy(alpha = 0.10f))
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 245.dp, y = (330 + glowB).dp)
                .blur(30.dp)
                .clip(CircleShape)
                .background(PremiumLoginColors.Cyan.copy(alpha = 0.12f))
        )

        Box(
            modifier = Modifier
                .size(230.dp)
                .offset(x = 218.dp, y = (700 + glowA).dp)
                .blur(30.dp)
                .clip(CircleShape)
                .background(PremiumLoginColors.Purple.copy(alpha = 0.08f))
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            drawSoftGrid(gridProgress)
            drawSoftCircuit(gridProgress)
        }

        FloatingDots()
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSoftGrid(progress: Float) {
    val step = 70.dp.toPx()
    val move = step * progress
    val color = Color(0xFFD7E8F8).copy(alpha = 0.20f)

    var x = -step + move
    while (x < size.width + step) {
        drawLine(
            color = color,
            start = Offset(x, 0f),
            end = Offset(x, size.height),
            strokeWidth = 1f
        )
        x += step
    }

    var y = -step + move
    while (y < size.height + step) {
        drawLine(
            color = color,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += step
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSoftCircuit(progress: Float) {
    val w = size.width
    val h = size.height

    val pathA = Path().apply {
        moveTo(w * 0.07f, h * 0.18f + progress * 12f)
        lineTo(w * 0.22f, h * 0.18f + progress * 12f)
        lineTo(w * 0.22f, h * 0.28f)
        lineTo(w * 0.39f, h * 0.28f)
    }

    drawPath(
        path = pathA,
        color = PremiumLoginColors.Blue.copy(alpha = 0.10f),
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )

    val pathB = Path().apply {
        moveTo(w * 0.94f, h * 0.58f - progress * 12f)
        lineTo(w * 0.78f, h * 0.58f - progress * 12f)
        lineTo(w * 0.78f, h * 0.70f)
        lineTo(w * 0.57f, h * 0.70f)
    }

    drawPath(
        path = pathB,
        color = PremiumLoginColors.Cyan.copy(alpha = 0.09f),
        style = Stroke(width = 3f, cap = StrokeCap.Round)
    )

    drawCircle(
        color = PremiumLoginColors.Blue.copy(alpha = 0.15f),
        radius = 4.dp.toPx(),
        center = Offset(w * 0.39f, h * 0.28f)
    )

    drawCircle(
        color = PremiumLoginColors.Cyan.copy(alpha = 0.15f),
        radius = 4.dp.toPx(),
        center = Offset(w * 0.57f, h * 0.70f)
    )
}

@Composable
private fun FloatingDots() {
    val dots = remember {
        listOf(
            FloatingDot(0.10f, 0.18f, 5f, 0.30f, PremiumLoginColors.Blue, 1.0f),
            FloatingDot(0.22f, 0.34f, 4f, 0.24f, PremiumLoginColors.Cyan, 0.9f),
            FloatingDot(0.82f, 0.28f, 6f, 0.24f, PremiumLoginColors.Purple, 0.8f),
            FloatingDot(0.72f, 0.48f, 5f, 0.20f, PremiumLoginColors.Mint, 0.85f),
            FloatingDot(0.30f, 0.72f, 5f, 0.22f, PremiumLoginColors.Blue, 1.1f),
            FloatingDot(0.66f, 0.82f, 4f, 0.20f, PremiumLoginColors.Cyan, 0.95f)
        )
    }

    val transition = rememberInfiniteTransition(label = "floating-dots")
    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots-progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        dots.forEachIndexed { index, dot ->
            val dx = sin((p * 2f * PI + index).toFloat()) * 18f * dot.speed
            val dy = cos((p * 2f * PI + index).toFloat()) * 14f * dot.speed

            drawCircle(
                color = dot.color.copy(alpha = dot.alpha),
                radius = dot.radius,
                center = Offset(
                    size.width * dot.x + dx,
                    size.height * dot.y + dy
                )
            )
        }
    }
}

@Composable
private fun PremiumHeroSection(
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "hero")

    val floatY by transition.animateFloat(
        initialValue = -5f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero-float"
    )

    val orbitProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(13000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero-orbit-progress"
    )

    val pulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "hero-pulse"
    )

    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hero-wave"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h * 0.54f + floatY)

            drawCircle(
                color = PremiumLoginColors.BlueSoft.copy(alpha = 0.82f),
                radius = h * 0.40f * pulse,
                center = center
            )

            drawCircle(
                color = PremiumLoginColors.CyanSoft.copy(alpha = 0.70f),
                radius = h * 0.22f,
                center = Offset(center.x - w * 0.18f, center.y + h * 0.07f)
            )

            drawCircle(
                color = PremiumLoginColors.PurpleSoft.copy(alpha = 0.58f),
                radius = h * 0.20f,
                center = Offset(center.x + w * 0.18f, center.y - h * 0.03f)
            )

            rotate(degrees = orbitProgress * 360f, pivot = center) {
                drawOval(
                    color = PremiumLoginColors.Cyan.copy(alpha = 0.17f),
                    topLeft = Offset(center.x - w * 0.32f, center.y - h * 0.16f),
                    size = Size(w * 0.64f, h * 0.32f),
                    style = Stroke(width = 3.dp.toPx())
                )
            }

            rotate(degrees = -orbitProgress * 260f, pivot = center) {
                drawOval(
                    color = PremiumLoginColors.Blue.copy(alpha = 0.11f),
                    topLeft = Offset(center.x - w * 0.22f, center.y - h * 0.28f),
                    size = Size(w * 0.44f, h * 0.56f),
                    style = Stroke(width = 2.dp.toPx())
                )
            }

            drawHeroComputer(center = center, wave = wave)
        }

        HeroOrbitIcon(
            progress = orbitProgress,
            radiusXValue = 150f,
            radiusYValue = 70f,
            phase = 0.00f,
            icon = Icons.Outlined.Wifi,
            tint = PremiumLoginColors.Blue,
            soft = PremiumLoginColors.BlueSoft
        )

        HeroOrbitIcon(
            progress = orbitProgress,
            radiusXValue = 150f,
            radiusYValue = 70f,
            phase = 0.23f,
            icon = Icons.Outlined.Memory,
            tint = PremiumLoginColors.Purple,
            soft = PremiumLoginColors.PurpleSoft
        )

        HeroOrbitIcon(
            progress = orbitProgress,
            radiusXValue = 150f,
            radiusYValue = 70f,
            phase = 0.47f,
            icon = Icons.Outlined.Cloud,
            tint = PremiumLoginColors.CyanDeep,
            soft = PremiumLoginColors.CyanSoft
        )

        HeroOrbitIcon(
            progress = orbitProgress,
            radiusXValue = 150f,
            radiusYValue = 70f,
            phase = 0.72f,
            icon = Icons.Outlined.Storage,
            tint = PremiumLoginColors.Blue,
            soft = PremiumLoginColors.BlueSoft
        )

        HeroOrbitIcon(
            progress = 1f - orbitProgress,
            radiusXValue = 105f,
            radiusYValue = 108f,
            phase = 0.16f,
            icon = Icons.Outlined.Router,
            tint = PremiumLoginColors.CyanDeep,
            soft = PremiumLoginColors.CyanSoft
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawHeroComputer(
    center: Offset,
    wave: Float
) {
    val screenW = size.width * 0.58f
    val screenH = size.height * 0.46f
    val left = center.x - screenW / 2f
    val top = center.y - screenH / 2f - 2.dp.toPx()

    drawRoundRect(
        color = Color(0xFF173057),
        topLeft = Offset(left, top),
        size = Size(screenW, screenH),
        cornerRadius = CornerRadius(30f, 30f)
    )

    drawRoundRect(
        brush = Brush.verticalGradient(
            listOf(
                Color.White,
                Color(0xFFF8FBFF),
                Color(0xFFF0F7FF)
            )
        ),
        topLeft = Offset(left + 8f, top + 8f),
        size = Size(screenW - 16f, screenH - 16f),
        cornerRadius = CornerRadius(24f, 24f)
    )

    drawRoundRect(
        color = PremiumLoginColors.BlueSoft.copy(alpha = 0.96f),
        topLeft = Offset(left + 22f, top + 20f),
        size = Size(screenW - 44f, 18f),
        cornerRadius = CornerRadius(999f, 999f)
    )

    drawCircle(
        color = PremiumLoginColors.Blue.copy(alpha = 0.32f),
        radius = 4.5f,
        center = Offset(left + 36f, top + 29f)
    )

    drawCircle(
        color = PremiumLoginColors.Cyan.copy(alpha = 0.28f),
        radius = 4.5f,
        center = Offset(left + 52f, top + 29f)
    )

    val contentLeft = left + 28f
    val contentRight = left + screenW - 28f
    val baseLine = top + screenH * 0.60f

    repeat(4) { index ->
        val y = top + 58f + index * 24f
        drawLine(
            color = PremiumLoginColors.Border.copy(alpha = 0.58f),
            start = Offset(contentLeft, y),
            end = Offset(contentRight, y),
            strokeWidth = 1.2f
        )
    }

    val chartPath = Path().apply {
        moveTo(contentLeft, baseLine)
        lineTo(left + screenW * 0.28f, baseLine - 18f * sin(wave * PI).toFloat())
        lineTo(left + screenW * 0.45f, baseLine + 14f * cos(wave * PI).toFloat())
        lineTo(left + screenW * 0.62f, baseLine - 22f * cos(wave * PI).toFloat())
        lineTo(contentRight, baseLine - 8f)
    }

    drawPath(
        path = chartPath,
        color = PremiumLoginColors.Blue,
        style = Stroke(width = 4.4f, cap = StrokeCap.Round)
    )

    val tileTop = top + screenH * 0.72f
    val tileW = (screenW - 70f) / 3f
    val tileColors = listOf(
        PremiumLoginColors.CyanSoft,
        PremiumLoginColors.MintSoft,
        PremiumLoginColors.PurpleSoft
    )

    repeat(3) { index ->
        drawRoundRect(
            color = tileColors[index].copy(alpha = 0.90f),
            topLeft = Offset(left + 24f + index * (tileW + 12f), tileTop),
            size = Size(tileW, 22f),
            cornerRadius = CornerRadius(12f, 12f)
        )
    }

    drawRoundRect(
        color = Color(0xFFCBD8E8),
        topLeft = Offset(center.x - 18f, top + screenH + 6f),
        size = Size(36f, 30f),
        cornerRadius = CornerRadius(9f, 9f)
    )

    drawRoundRect(
        color = Color(0xFFD4DEEC),
        topLeft = Offset(center.x - 82f, top + screenH + 32f),
        size = Size(164f, 16f),
        cornerRadius = CornerRadius(999f, 999f)
    )
}

@Composable
private fun HeroOrbitIcon(
    progress: Float,
    radiusXValue: Float,
    radiusYValue: Float,
    phase: Float,
    icon: ImageVector,
    tint: Color,
    soft: Color
) {
    val loop = (progress + phase) % 1f
    val angle = (loop * 2f * PI).toFloat()
    val x = cos(angle) * radiusXValue
    val y = sin(angle) * radiusYValue
    val depth = ((sin(angle) + 1f) / 2f).coerceIn(0f, 1f)
    val smoothScale = 0.96f + depth * 0.10f
    val smoothAlpha = 0.88f + depth * 0.10f

    Box(
        modifier = Modifier
            .offset(x = x.dp, y = y.dp)
            .scale(smoothScale)
            .zIndex(depth)
            .size(58.dp)
            .clip(RoundedCornerShape(21.dp))
            .background(Color.White.copy(alpha = smoothAlpha))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.98f),
                shape = RoundedCornerShape(21.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(soft),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PremiumTitleSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 540.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quản Lý Phòng Máy",
            color = PremiumLoginColors.Ink,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 32.sp,
                letterSpacing = (-0.7).sp,
                shadow = Shadow(
                    color = PremiumLoginColors.Blue.copy(alpha = 0.12f),
                    offset = Offset(0f, 5f),
                    blurRadius = 12f
                )
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            SubtitleLine(isLeft = true)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Đăng nhập vào hệ thống quản lý",
                color = PremiumLoginColors.Muted,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            SubtitleLine(isLeft = false)
        }
    }
}

@Composable
private fun SubtitleLine(isLeft: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isLeft) {
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(PremiumLoginColors.Cyan)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(PremiumLoginColors.Cyan)
            )
        } else {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(PremiumLoginColors.Cyan)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Box(
                modifier = Modifier
                    .width(22.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(PremiumLoginColors.Cyan)
            )
        }
    }
}

@Composable
private fun AuthModeSwitch(
    mode: AuthMode,
    onModeChange: (AuthMode) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        color = Color(0xFFF7FAFF),
        shape = RoundedCornerShape(25.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.94f)),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(7.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SegmentButton(
                text = "Đăng nhập",
                icon = Icons.AutoMirrored.Filled.Login,
                selected = mode == AuthMode.Login,
                modifier = Modifier.weight(1f),
                onClick = { onModeChange(AuthMode.Login) }
            )

            SegmentButton(
                text = "Đăng ký",
                icon = Icons.Default.PersonAdd,
                selected = mode == AuthMode.Register,
                modifier = Modifier.weight(1f),
                onClick = { onModeChange(AuthMode.Register) }
            )
        }
    }
}

@Composable
private fun SegmentButton(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else PremiumLoginColors.Muted,
        animationSpec = tween(180),
        label = "segment-text"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else PremiumLoginColors.Blue,
        animationSpec = tween(180),
        label = "segment-icon"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.985f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "segment-scale"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) {
                    animatedGradient(
                        PremiumLoginColors.Cyan,
                        PremiumLoginColors.Blue,
                        PremiumLoginColors.BlueDeep
                    )
                } else {
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFFFFF),
                            Color(0xFFF6FAFF)
                        )
                    )
                }
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else PremiumLoginColors.BorderSoft,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            ShineOverlay(alpha = 0.30f, widthFraction = 0.36f)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = textColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
private fun LoginFormContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    loading: Boolean,
    ready: Boolean,
    error: String,
    shakeKey: Int,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onSwitchMode: () -> Unit
) {
    val offset by rememberShakeOffset(shakeKey)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset { offset },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "Nhập email của bạn",
            leadingIcon = Icons.Default.Email,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Mật khẩu",
            placeholder = "Nhập mật khẩu",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onTrailingClick = { onPasswordVisibleChange(!passwordVisible) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() })
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Quên mật khẩu?",
                color = PremiumLoginColors.BlueDeep,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onForgotPassword() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
            )
        }

        AnimatedError(message = error)

        Spacer(modifier = Modifier.height(18.dp))

        PremiumActionButton(
            text = "Đăng nhập",
            loading = loading,
            enabled = ready,
            onClick = onSubmit
        )

        Spacer(modifier = Modifier.height(16.dp))

        FooterSwitchText(
            leading = "Chưa có tài khoản?",
            action = "Đăng ký ngay",
            onClick = onSwitchMode
        )
    }
}

@Composable
private fun RegisterFormContent(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    role: AccountRole,
    onRoleChange: (AccountRole) -> Unit,
    identifier: String,
    onIdentifierChange: (String) -> Unit,
    department: String,
    onDepartmentChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibleChange: (Boolean) -> Unit,
    confirmPasswordVisible: Boolean,
    onConfirmPasswordVisibleChange: (Boolean) -> Unit,
    loading: Boolean,
    ready: Boolean,
    error: String,
    shakeKey: Int,
    onSubmit: () -> Unit,
    onSwitchMode: () -> Unit
) {
    val offset by rememberShakeOffset(shakeKey)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset { offset },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PremiumField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "Họ tên",
            placeholder = "Nhập họ và tên",
            leadingIcon = Icons.Default.Person,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = email,
            onValueChange = onEmailChange,
            label = "Email",
            placeholder = "Nhập email của bạn",
            leadingIcon = Icons.Default.AlternateEmail,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        RoleSelector(
            selected = role,
            onSelected = onRoleChange
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = identifier,
            onValueChange = onIdentifierChange,
            label = role.codeLabel,
            placeholder = if (role == AccountRole.Student) "Nhập mã số sinh viên" else "Nhập mã giảng viên",
            leadingIcon = Icons.Default.Badge,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = department,
            onValueChange = onDepartmentChange,
            label = role.extraLabel,
            placeholder = if (role == AccountRole.Student) "Nhập lớp" else "Nhập khoa hoặc bộ môn",
            leadingIcon = if (role == AccountRole.Student) Icons.Default.Book else Icons.Default.Business,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = phone,
            onValueChange = onPhoneChange,
            label = "Số điện thoại",
            placeholder = "Nhập số điện thoại",
            leadingIcon = Icons.Default.Phone,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = password,
            onValueChange = onPasswordChange,
            label = "Mật khẩu",
            placeholder = "Nhập mật khẩu",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onTrailingClick = { onPasswordVisibleChange(!passwordVisible) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        PremiumField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "Xác nhận mật khẩu",
            placeholder = "Nhập lại mật khẩu",
            leadingIcon = Icons.Default.Lock,
            trailingIcon = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            onTrailingClick = { onConfirmPasswordVisibleChange(!confirmPasswordVisible) },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { onSubmit() })
        )

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedError(message = error)

        Spacer(modifier = Modifier.height(18.dp))

        PremiumActionButton(
            text = "Đăng ký",
            loading = loading,
            enabled = ready,
            onClick = onSubmit
        )

        Spacer(modifier = Modifier.height(16.dp))

        FooterSwitchText(
            leading = "Đã có tài khoản?",
            action = "Đăng nhập",
            onClick = onSwitchMode
        )
    }
}

@Composable
private fun RoleSelector(
    selected: AccountRole,
    onSelected: (AccountRole) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Chọn quyền tài khoản",
            color = PremiumLoginColors.Text,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccountRole.entries.forEach { role ->
                RoleButton(
                    role = role,
                    selected = role == selected,
                    modifier = Modifier.weight(1f),
                    onClick = { onSelected(role) }
                )
            }
        }
    }
}

@Composable
private fun RoleButton(
    role: AccountRole,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else PremiumLoginColors.Muted,
        animationSpec = tween(180),
        label = "role-text"
    )
    val iconColor by animateColorAsState(
        targetValue = if (selected) Color.White else role.end,
        animationSpec = tween(180),
        label = "role-icon"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 1f else 0.985f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "role-scale"
    )

    Box(
        modifier = modifier
            .height(60.dp)
            .scale(scale)
            .clip(RoundedCornerShape(21.dp))
            .background(
                if (selected) {
                    animatedGradient(role.start, role.end, PremiumLoginColors.BlueDeep)
                } else {
                    Brush.horizontalGradient(listOf(role.soft, Color.White))
                }
            )
            .border(
                width = if (selected) 0.dp else 1.dp,
                color = if (selected) Color.Transparent else PremiumLoginColors.BorderSoft,
                shape = RoundedCornerShape(21.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            ShineOverlay(alpha = 0.25f, widthFraction = 0.42f)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        if (selected) {
                            Color.White.copy(alpha = 0.18f)
                        } else {
                            Color.White.copy(alpha = 0.80f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = role.icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(9.dp))

            Text(
                text = role.title,
                color = textColor,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
            )
        }
    }
}

@Composable
private fun PremiumField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    var focused by remember { mutableStateOf(false) }
    val active = focused || value.isNotBlank()

    val borderColor by animateColorAsState(
        targetValue = if (focused) PremiumLoginColors.Focus else PremiumLoginColors.Border,
        animationSpec = tween(200),
        label = "field-border"
    )

    val labelColor by animateColorAsState(
        targetValue = if (focused) PremiumLoginColors.BlueDeep else PremiumLoginColors.Muted,
        animationSpec = tween(200),
        label = "field-label"
    )

    val iconColor by animateColorAsState(
        targetValue = if (active) PremiumLoginColors.BlueDeep else PremiumLoginColors.Muted,
        animationSpec = tween(220),
        label = "field-icon"
    )

    val iconBackground by animateColorAsState(
        targetValue = if (active) PremiumLoginColors.BlueSoft else Color.Transparent,
        animationSpec = tween(220),
        label = "field-icon-bg"
    )

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(24.dp),
            color = PremiumLoginColors.Field,
            shadowElevation = if (focused) 9.dp else 0.dp,
            border = BorderStroke(
                width = if (focused) 1.5.dp else 1.dp,
                color = borderColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 68.dp)
                    .padding(start = 16.dp, end = if (trailingIcon == null) 18.dp else 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    textStyle = TextStyle(
                        color = PremiumLoginColors.Text,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged { focused = it.isFocused },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isBlank()) {
                                Text(
                                    text = placeholder,
                                    color = PremiumLoginColors.Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 16.sp
                                    )
                                )
                            }

                            innerTextField()
                        }
                    }
                )

                if (trailingIcon != null && onTrailingClick != null) {
                    IconButton(
                        onClick = onTrailingClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = PremiumLoginColors.Muted,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = active,
            enter = fadeIn(tween(150)) + scaleIn(initialScale = 0.94f),
            exit = fadeOut(tween(90)) + scaleOut(targetScale = 0.94f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 76.dp)
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = if (focused) 2.dp else 0.dp,
                border = BorderStroke(
                    width = if (focused) 1.dp else 0.dp,
                    color = if (focused) PremiumLoginColors.Focus.copy(alpha = 0.22f) else Color.Transparent
                )
            ) {
                Text(
                    text = label,
                    color = labelColor,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp
                    ),
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
private fun PremiumActionButton(
    text: String,
    loading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "action-scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .scale(scale)
            .clip(RoundedCornerShape(24.dp))
            .background(
                animatedGradient(
                    PremiumLoginColors.Cyan,
                    PremiumLoginColors.Blue,
                    PremiumLoginColors.BlueDeep
                )
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.50f),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interaction,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        ShineOverlay(alpha = 0.31f, widthFraction = 0.34f)

        if (loading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 3.dp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Đang xử lý",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }
        } else {
            Text(
                text = text,
                color = Color.White,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 21.sp
                )
            )
        }

        if (!enabled && !loading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.18f))
            )
        }
    }
}

@Composable
private fun ShineOverlay(
    alpha: Float,
    widthFraction: Float
) {
    val transition = rememberInfiniteTransition(label = "shine")

    val progress by transition.animateFloat(
        initialValue = -0.60f,
        targetValue = 1.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shine-progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val shineW = w * widthFraction
        val x = w * progress

        rotate(degrees = -18f, pivot = Offset(x, h / 2f)) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = alpha),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(x - shineW / 2f, -h),
                size = Size(shineW, h * 3f),
                cornerRadius = CornerRadius(999f, 999f)
            )
        }
    }
}

@Composable
private fun AnimatedError(message: String) {
    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.97f),
        exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.97f)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 2.dp),
            shape = RoundedCornerShape(18.dp),
            color = PremiumLoginColors.RoseSoft,
            border = BorderStroke(
                width = 1.dp,
                color = PremiumLoginColors.Rose.copy(alpha = 0.18f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = PremiumLoginColors.Rose,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = message,
                    color = PremiumLoginColors.Rose,
                    lineHeight = 20.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun FooterSwitchText(
    leading: String,
    action: String,
    onClick: () -> Unit
) {
    Surface(
        color = Color.White.copy(alpha = 0.70f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, PremiumLoginColors.BorderSoft)
    ) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick
                )
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = leading,
                color = PremiumLoginColors.Muted,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = action,
                color = PremiumLoginColors.BlueDeep,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
private fun DesignerCredit() {
    Surface(
        color = Color.White.copy(alpha = 0.74f),
        shape = RoundedCornerShape(999.dp),
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, PremiumLoginColors.BorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(PremiumLoginColors.Cyan)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Designer by Trịnh Quyền",
                color = PremiumLoginColors.Muted,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(PremiumLoginColors.Blue)
            )
        }
    }
}

@Composable
private fun ForgotPasswordDialog(
    email: String,
    onEmailChange: (String) -> Unit,
    loading: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                PremiumLoginColors.CyanSoft,
                                PremiumLoginColors.BlueSoft
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = PremiumLoginColors.BlueDeep
                )
            }
        },
        title = {
            Text(
                text = "Khôi phục mật khẩu",
                color = PremiumLoginColors.Text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
        },
        text = {
            Column {
                Text(
                    text = "Nhập email tài khoản để nhận liên kết đặt lại mật khẩu.",
                    color = PremiumLoginColors.Muted,
                    lineHeight = 20.sp,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(14.dp))

                PremiumField(
                    value = email,
                    onValueChange = onEmailChange,
                    label = "Email",
                    placeholder = "Nhập email của bạn",
                    leadingIcon = Icons.Default.Email,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() })
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = onSubmit
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = PremiumLoginColors.BlueDeep
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đang gửi",
                        color = PremiumLoginColors.BlueDeep,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Text(
                        text = "Gửi email",
                        color = PremiumLoginColors.BlueDeep,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                enabled = !loading,
                onClick = onDismiss
            ) {
                Text(
                    text = "Hủy",
                    color = PremiumLoginColors.Muted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun animatedGradient(
    start: Color,
    middle: Color,
    end: Color
): Brush {
    val transition = rememberInfiniteTransition(label = "animated-gradient")

    val p by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradient-progress"
    )

    return Brush.linearGradient(
        colors = listOf(start, middle, end),
        start = Offset(0f + p * 160f, 0f),
        end = Offset(900f - p * 120f, 420f),
        tileMode = TileMode.Clamp
    )
}

@Composable
private fun rememberShakeOffset(key: Int): State<IntOffset> {
    val target = if (key == 0) IntOffset.Zero else IntOffset(1, 0)

    return animateIntOffsetAsState(
        targetValue = target,
        animationSpec = keyframes {
            durationMillis = 420
            IntOffset(0, 0) at 0
            IntOffset(-18, 0) at 60
            IntOffset(16, 0) at 120
            IntOffset(-12, 0) at 180
            IntOffset(8, 0) at 240
            IntOffset(-5, 0) at 300
            IntOffset(2, 0) at 360
            IntOffset(0, 0) at 420
        },
        label = "shake-offset"
    )
}

private fun validateLogin(
    email: String,
    password: String
): String? {
    return when {
        email.isBlank() -> "Vui lòng nhập email."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không đúng định dạng."
        password.isBlank() -> "Vui lòng nhập mật khẩu."
        else -> null
    }
}

private fun validateRegister(
    fullName: String,
    email: String,
    password: String,
    confirmPassword: String,
    identifier: String,
    department: String,
    phone: String,
    role: AccountRole
): String? {
    return when {
        fullName.isBlank() -> "Vui lòng nhập họ tên."
        email.isBlank() -> "Vui lòng nhập email."
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Email không đúng định dạng."
        password.length < 6 -> "Mật khẩu cần tối thiểu 6 ký tự."
        confirmPassword != password -> "Mật khẩu xác nhận chưa khớp."
        identifier.isBlank() -> "Vui lòng nhập ${role.codeLabel.lowercase()}."
        department.isBlank() -> "Vui lòng nhập ${role.extraLabel.lowercase()}."
        phone.isBlank() -> "Vui lòng nhập số điện thoại."
        phone.length < 9 -> "Số điện thoại không hợp lệ."
        !AppAuthContract.Role.canRegister(AppAuthContract.Role.fromRaw(role.roleValue)) -> "Quyền tài khoản không hợp lệ."
        else -> null
    }
}