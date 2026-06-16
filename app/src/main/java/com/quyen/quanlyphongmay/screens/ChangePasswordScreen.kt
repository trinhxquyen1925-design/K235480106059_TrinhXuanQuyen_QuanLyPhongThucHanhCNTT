package com.quyen.quanlyphongmay.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.model.User
import com.quyen.quanlyphongmay.repository.AuthRepository
import com.quyen.quanlyphongmay.ui.design.AppBadgeTone
import com.quyen.quanlyphongmay.ui.design.AppCard
import com.quyen.quanlyphongmay.ui.design.AppColors
import com.quyen.quanlyphongmay.ui.design.AppGradientButton
import com.quyen.quanlyphongmay.ui.design.AppOutlinedButton
import com.quyen.quanlyphongmay.ui.design.AppSectionHeader
import com.quyen.quanlyphongmay.ui.design.StatusBadge
import com.quyen.quanlyphongmay.ui.design.UserRoleBadge
import com.quyen.quanlyphongmay.ui.design.UserStatusBadge

@Composable
fun ChangePasswordScreen(
    currentUser: User,
    authRepository: AuthRepository,
    onPasswordChanged: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val safeUser = currentUser.normalizedCopy()

    var currentPassword by rememberSaveable {
        mutableStateOf("")
    }

    var newPassword by rememberSaveable {
        mutableStateOf("")
    }

    var confirmPassword by rememberSaveable {
        mutableStateOf("")
    }

    var showCurrentPassword by rememberSaveable {
        mutableStateOf(false)
    }

    var showNewPassword by rememberSaveable {
        mutableStateOf(false)
    }

    var showConfirmPassword by rememberSaveable {
        mutableStateOf(false)
    }

    var loading by rememberSaveable {
        mutableStateOf(false)
    }

    val strength = remember(newPassword) {
        PasswordStrength.fromPassword(newPassword)
    }

    fun validateInput(): String? {
        val cleanCurrentPassword = currentPassword.trim()
        val cleanNewPassword = newPassword.trim()
        val cleanConfirmPassword = confirmPassword.trim()

        return when {
            cleanCurrentPassword.isBlank() -> {
                "Vui lòng nhập mật khẩu hiện tại."
            }

            cleanNewPassword.isBlank() -> {
                "Vui lòng nhập mật khẩu mới."
            }

            cleanNewPassword.length < 6 -> {
                "Mật khẩu mới phải có ít nhất 6 ký tự."
            }

            cleanConfirmPassword.isBlank() -> {
                "Vui lòng nhập lại mật khẩu mới."
            }

            cleanNewPassword != cleanConfirmPassword -> {
                "Mật khẩu nhập lại chưa khớp."
            }

            cleanCurrentPassword == cleanNewPassword -> {
                "Mật khẩu mới không được trùng mật khẩu hiện tại."
            }

            else -> null
        }
    }

    fun submitChangePassword() {
        val error = validateInput()

        if (error != null) {
            onShowMessage(error)
            return
        }

        if (loading) {
            return
        }

        loading = true

        authRepository.changePassword(
            currentPassword = currentPassword.trim(),
            newPassword = newPassword.trim()
        ) { success, message ->
            loading = false

            if (success) {
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""

                onShowMessage(message ?: "Đổi mật khẩu thành công.")
                onPasswordChanged()
            } else {
                onShowMessage(message ?: "Không thể đổi mật khẩu. Vui lòng thử lại.")
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(
            start = 0.dp,
            end = 0.dp,
            top = 2.dp,
            bottom = 18.dp
        )
    ) {
        item {
            ChangePasswordHeaderCard(
                user = safeUser
            )
        }

        item {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(18.dp),
                shadowElevation = 10.dp
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AppSectionHeader(
                        title = "Đổi mật khẩu",
                        subtitle = "Nhập mật khẩu hiện tại và mật khẩu mới để cập nhật tài khoản."
                    )

                    PasswordInputField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                        },
                        label = "Mật khẩu hiện tại",
                        placeholder = "Nhập mật khẩu đang dùng",
                        icon = Icons.Default.Lock,
                        visible = showCurrentPassword,
                        onToggleVisible = {
                            showCurrentPassword = !showCurrentPassword
                        },
                        enabled = !loading
                    )

                    PasswordInputField(
                        value = newPassword,
                        onValueChange = {
                            newPassword = it
                        },
                        label = "Mật khẩu mới",
                        placeholder = "Tối thiểu 6 ký tự",
                        icon = Icons.Default.Key,
                        visible = showNewPassword,
                        onToggleVisible = {
                            showNewPassword = !showNewPassword
                        },
                        enabled = !loading
                    )

                    PasswordStrengthCard(
                        strength = strength
                    )

                    PasswordInputField(
                        value = confirmPassword,
                        onValueChange = {
                            confirmPassword = it
                        },
                        label = "Nhập lại mật khẩu mới",
                        placeholder = "Nhập lại để xác nhận",
                        icon = Icons.Default.LockReset,
                        visible = showConfirmPassword,
                        onToggleVisible = {
                            showConfirmPassword = !showConfirmPassword
                        },
                        enabled = !loading
                    )

                    AppGradientButton(
                        text = if (loading) "Đang đổi mật khẩu..." else "Đổi mật khẩu",
                        onClick = {
                            submitChangePassword()
                        },
                        leadingIcon = Icons.Default.CheckCircle,
                        modifier = Modifier.fillMaxWidth()
                    )

                    AppOutlinedButton(
                        text = "Xóa nội dung đã nhập",
                        onClick = {
                            currentPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                        },
                        leadingIcon = Icons.Default.LockReset,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            ChangePasswordNoteCard()
        }
    }
}

@Composable
fun ChangePasswordScreen(
    currentUser: User? = null,
    authRepository: AuthRepository = AuthRepository(),
    onPasswordChanged: () -> Unit = {},
    onShowMessage: (String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    ChangePasswordScreen(
        currentUser = currentUser?.normalizedCopy() ?: User().normalizedCopy(),
        authRepository = authRepository,
        onPasswordChanged = onPasswordChanged,
        onShowMessage = onShowMessage
    )
}

@Composable
private fun ChangePasswordHeaderCard(
    user: User
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(18.dp),
        shadowElevation = 10.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(24.dp),
                color = AppColors.BlueSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Blue.copy(alpha = 0.18f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = AppColors.Blue,
                    modifier = Modifier.padding(17.dp)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = user.displayName.ifBlank { "Tài khoản" },
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                )

                Text(
                    text = user.email.ifBlank { "Chưa có email" },
                    color = AppColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UserRoleBadge(role = user.role)
                    UserStatusBadge(status = user.status)
                }
            }
        }
    }
}

@Composable
private fun PasswordInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    visible: Boolean,
    onToggleVisible: () -> Unit,
    enabled: Boolean
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = true,
        label = {
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.Blue
            )
        },
        trailingIcon = {
            IconButton(
                onClick = onToggleVisible,
                enabled = enabled
            ) {
                Icon(
                    imageVector = if (visible) {
                        Icons.Default.VisibilityOff
                    } else {
                        Icons.Default.Visibility
                    },
                    contentDescription = null,
                    tint = AppColors.Muted
                )
            }
        },
        visualTransformation = if (visible) {
            VisualTransformation.None
        } else {
            PasswordVisualTransformation()
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.None,
            keyboardType = KeyboardType.Password,
            autoCorrect = false
        ),
        shape = RoundedCornerShape(22.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AppColors.Ink,
            unfocusedTextColor = AppColors.Ink,
            disabledTextColor = AppColors.Muted,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF3F7FB),
            focusedBorderColor = AppColors.Blue,
            unfocusedBorderColor = AppColors.Border,
            disabledBorderColor = AppColors.Border,
            cursorColor = AppColors.Blue,
            focusedLabelColor = AppColors.Blue,
            unfocusedLabelColor = AppColors.Muted
        )
    )
}

@Composable
private fun PasswordStrengthCard(
    strength: PasswordStrength
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = strength.background,
        border = BorderStroke(
            width = 1.dp,
            color = strength.border
        )
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(15.dp),
                color = Color.White.copy(alpha = 0.72f)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = strength.color,
                    modifier = Modifier.padding(10.dp)
                )
            }

            Spacer(modifier = Modifier.size(10.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Độ mạnh mật khẩu",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                )

                Text(
                    text = strength.message,
                    color = AppColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )
            }

            StatusBadge(
                text = strength.label,
                tone = strength.tone
            )
        }
    }
}

@Composable
private fun ChangePasswordNoteCard() {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(17.dp),
                color = AppColors.AmberSoft,
                border = BorderStroke(
                    width = 1.dp,
                    color = AppColors.Amber.copy(alpha = 0.18f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = AppColors.Amber,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.size(11.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Lưu ý bảo mật",
                    color = AppColors.Ink,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )

                Text(
                    text = "Sau khi đổi mật khẩu, hãy ghi nhớ mật khẩu mới. Nếu Firebase yêu cầu đăng nhập lại, hãy đăng xuất rồi đăng nhập bằng mật khẩu mới.",
                    color = AppColors.Muted,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                )
            }
        }
    }
}

@Immutable
private data class PasswordStrength(
    val label: String,
    val message: String,
    val color: Color,
    val background: Color,
    val border: Color,
    val tone: AppBadgeTone
) {
    companion object {
        fun fromPassword(password: String): PasswordStrength {
            val cleanPassword = password.trim()

            val hasLetter = cleanPassword.any { it.isLetter() }
            val hasDigit = cleanPassword.any { it.isDigit() }
            val hasSpecial = cleanPassword.any { !it.isLetterOrDigit() }
            val longEnough = cleanPassword.length >= 8

            val score = listOf(
                cleanPassword.length >= 6,
                longEnough,
                hasLetter,
                hasDigit,
                hasSpecial
            ).count { it }

            return when {
                cleanPassword.isBlank() -> {
                    PasswordStrength(
                        label = "Trống",
                        message = "Nhập mật khẩu mới để kiểm tra độ mạnh.",
                        color = AppColors.Muted,
                        background = Color(0xFFF3F7FB),
                        border = AppColors.Border,
                        tone = AppBadgeTone.Neutral
                    )
                }

                score <= 2 -> {
                    PasswordStrength(
                        label = "Yếu",
                        message = "Nên dùng ít nhất 6 ký tự, có chữ và số.",
                        color = AppColors.Rose,
                        background = AppColors.RoseSoft,
                        border = AppColors.Rose.copy(alpha = 0.18f),
                        tone = AppBadgeTone.Error
                    )
                }

                score <= 4 -> {
                    PasswordStrength(
                        label = "Ổn",
                        message = "Mật khẩu dùng được. Nên thêm ký tự đặc biệt để mạnh hơn.",
                        color = AppColors.Amber,
                        background = AppColors.AmberSoft,
                        border = AppColors.Amber.copy(alpha = 0.20f),
                        tone = AppBadgeTone.Warning
                    )
                }

                else -> {
                    PasswordStrength(
                        label = "Mạnh",
                        message = "Mật khẩu có độ an toàn tốt.",
                        color = AppColors.Green,
                        background = AppColors.GreenSoft,
                        border = AppColors.Green.copy(alpha = 0.20f),
                        tone = AppBadgeTone.Success
                    )
                }
            }
        }
    }
}