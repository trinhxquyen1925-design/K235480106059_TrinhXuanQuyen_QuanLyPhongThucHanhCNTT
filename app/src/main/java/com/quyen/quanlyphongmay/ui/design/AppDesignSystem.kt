@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.quyen.quanlyphongmay.ui.design

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quyen.quanlyphongmay.core.AppAuthContract
import com.quyen.quanlyphongmay.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Stable
object AppColors {
    val BgTop = Color(0xFFF9FDFF)
    val BgMiddle = Color(0xFFF1F8FF)
    val BgBottom = Color(0xFFE7F8FF)

    val Card = Color(0xFCFFFFFF)
    val CardSolid = Color(0xFFFFFFFF)
    val Field = Color(0xFFFFFFFF)

    val Ink = Color(0xFF0E1A33)
    val Text = Color(0xFF20314C)
    val Muted = Color(0xFF73849B)
    val SoftText = Color(0xFF96A7BA)

    val Border = Color(0xFFD9E7F5)
    val BorderSoft = Color(0xFFEAF2FB)
    val Focus = Color(0xFF2F83FF)

    val Cyan = Color(0xFF25C7D8)
    val CyanDeep = Color(0xFF15A8CB)
    val Blue = Color(0xFF4287F5)
    val BlueDeep = Color(0xFF2666EA)
    val Purple = Color(0xFF8B5CF6)
    val Mint = Color(0xFF2FD4BA)
    val Amber = Color(0xFFF5A524)
    val Rose = Color(0xFFE64B66)
    val Green = Color(0xFF22C55E)

    val CyanSoft = Color(0xFFE8FCFF)
    val BlueSoft = Color(0xFFEDF5FF)
    val PurpleSoft = Color(0xFFF4EEFF)
    val MintSoft = Color(0xFFE9FFF8)
    val AmberSoft = Color(0xFFFFF7E6)
    val RoseSoft = Color(0xFFFFEEF3)
    val GreenSoft = Color(0xFFEAFBF1)
}

@Stable
object AppDimens {
    val ScreenPadding = 18.dp
    val CardRadius = 30.dp
    val ItemRadius = 22.dp
    val ButtonRadius = 24.dp
    val FieldRadius = 24.dp
    val BottomBarRadius = 28.dp
}

@Immutable
data class AppNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
)

@Immutable
data class AppDrawerItem(
    val route: String,
    val title: String,
    val icon: ImageVector,
    val enabled: Boolean = true,
    val badgeText: String = ""
)

enum class AppBadgeTone {
    Info,
    Success,
    Warning,
    Error,
    Neutral,
    Purple
}

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopCenter,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        AppColors.BgTop,
                        AppColors.BgMiddle,
                        AppColors.BgBottom
                    )
                )
            ),
        contentAlignment = contentAlignment
    ) {
        BackgroundGlow()
        content()
    }
}

@Composable
private fun BoxScope.BackgroundGlow() {
    val transition = rememberInfiniteTransition(label = "app-background")

    val glowA by transition.animateFloat(
        initialValue = -14f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(6800, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow-a"
    )

    val glowB by transition.animateFloat(
        initialValue = 14f,
        targetValue = -14f,
        animationSpec = infiniteRepeatable(
            animation = tween(7600, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow-b"
    )

    Box(
        modifier = Modifier
            .size(330.dp)
            .align(Alignment.TopStart)
            .padding(start = 0.dp, top = (90 + glowA).dp)
            .blur(30.dp)
            .clip(CircleShape)
            .background(AppColors.Blue.copy(alpha = 0.08f))
    )

    Box(
        modifier = Modifier
            .size(280.dp)
            .align(Alignment.BottomEnd)
            .padding(end = 0.dp, bottom = (120 + glowB).dp)
            .blur(30.dp)
            .clip(CircleShape)
            .background(AppColors.Cyan.copy(alpha = 0.10f))
    )
}

@Composable
fun AppScreenContainer(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        start = AppDimens.ScreenPadding,
        end = AppDimens.ScreenPadding,
        top = 14.dp,
        bottom = 18.dp
    ),
    content: @Composable () -> Unit
) {
    AppBackground(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(AppDimens.CardRadius),
    backgroundColor: Color = AppColors.Card,
    borderColor: Color = Color.White.copy(alpha = 0.94f),
    shadowElevation: Dp = 14.dp,
    contentPadding: PaddingValues = PaddingValues(18.dp),
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = backgroundColor,
        shadowElevation = shadowElevation,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier.padding(contentPadding)
        ) {
            content()
        }
    }
}

@Composable
fun AppSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    actionText: String = "",
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                color = AppColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                    letterSpacing = (-0.3).sp
                )
            )

            if (subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    color = AppColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
            }
        }

        if (actionText.isNotBlank() && onActionClick != null) {
            Spacer(modifier = Modifier.width(12.dp))

            Surface(
                shape = RoundedCornerShape(999.dp),
                color = AppColors.BlueSoft,
                border = BorderStroke(1.dp, AppColors.BorderSoft)
            ) {
                Text(
                    text = actionText,
                    color = AppColors.BlueDeep,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 13.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
fun AppGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: ImageVector? = null,
    height: Dp = 62.dp
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "app-button-scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .clip(RoundedCornerShape(AppDimens.ButtonRadius))
            .background(AppAnimatedGradient())
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.50f),
                shape = RoundedCornerShape(AppDimens.ButtonRadius)
            )
            .clickable(
                enabled = enabled && !loading,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        ButtonShineOverlay()

        if (loading) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 3.dp
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.width(9.dp))
                }

                Text(
                    text = text,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                )
            }
        }

        if (!enabled && !loading) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.White.copy(alpha = 0.20f))
            )
        }
    }
}

@Composable
fun AppOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    height: Dp = 56.dp
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "app-outlined-button-scale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .scale(scale)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(AppDimens.ButtonRadius),
        color = Color.White.copy(alpha = if (enabled) 0.92f else 0.54f),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) AppColors.Border else AppColors.BorderSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (enabled) AppColors.BlueDeep else AppColors.SoftText,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(9.dp))
            }

            Text(
                text = text,
                color = if (enabled) AppColors.BlueDeep else AppColors.SoftText,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )
        }
    }
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minHeight: Dp = 66.dp,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    val focusedState = remember {
        mutableStateOf(false)
    }

    val focused by focusedState
    val active = focused || value.isNotBlank()

    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> AppColors.BorderSoft
            focused -> AppColors.Focus
            else -> AppColors.Border
        },
        animationSpec = tween(200),
        label = "app-field-border"
    )

    val labelColor by animateColorAsState(
        targetValue = if (focused) AppColors.BlueDeep else AppColors.Muted,
        animationSpec = tween(200),
        label = "app-field-label"
    )

    val iconColor by animateColorAsState(
        targetValue = if (active) AppColors.BlueDeep else AppColors.Muted,
        animationSpec = tween(220),
        label = "app-field-icon"
    )

    val iconBackground by animateColorAsState(
        targetValue = if (active) AppColors.BlueSoft else Color.Transparent,
        animationSpec = tween(220),
        label = "app-field-icon-background"
    )

    val shadowElevation by animateDpAsState(
        targetValue = if (focused) 8.dp else 0.dp,
        animationSpec = tween(220),
        label = "app-field-shadow"
    )

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
            shape = RoundedCornerShape(AppDimens.FieldRadius),
            color = if (enabled) AppColors.Field else Color(0xFFF7FAFF),
            shadowElevation = shadowElevation,
            border = BorderStroke(
                width = if (focused) 1.5.dp else 1.dp,
                color = borderColor
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = minHeight)
                    .padding(
                        start = if (leadingIcon == null) 18.dp else 14.dp,
                        end = if (trailingIcon == null) 18.dp else 8.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
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
                }

                BasicTextField(
                    value = value,
                    onValueChange = {
                        if (enabled) {
                            onValueChange(it)
                        }
                    },
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    textStyle = TextStyle(
                        color = if (enabled) AppColors.Text else AppColors.Muted,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .onFocusChanged {
                            focusedState.value = it.isFocused
                        },
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (value.isBlank()) {
                                Text(
                                    text = placeholder,
                                    color = AppColors.Muted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 15.sp
                                    )
                                )
                            }

                            innerTextField()
                        }
                    }
                )

                if (trailingIcon != null && onTrailingClick != null) {
                    IconButton(
                        enabled = enabled,
                        onClick = onTrailingClick,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = trailingIcon,
                            contentDescription = null,
                            tint = AppColors.Muted,
                            modifier = Modifier.size(23.dp)
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
                .padding(start = if (leadingIcon == null) 22.dp else 76.dp)
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(999.dp),
                shadowElevation = if (focused) 2.dp else 0.dp,
                border = BorderStroke(
                    width = if (focused) 1.dp else 0.dp,
                    color = if (focused) AppColors.Focus.copy(alpha = 0.22f) else Color.Transparent
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
fun StatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    tone: AppBadgeTone = AppBadgeTone.Neutral
) {
    val colors = badgeColors(tone)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = colors.background,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Text(
            text = text,
            color = colors.content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            ),
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun UserRoleBadge(
    role: String,
    modifier: Modifier = Modifier
) {
    val roleEnum = AppAuthContract.Role.fromRaw(role)
    val tone = when (roleEnum) {
        AppAuthContract.Role.Admin -> AppBadgeTone.Purple
        AppAuthContract.Role.Teacher -> AppBadgeTone.Info
        AppAuthContract.Role.Student -> AppBadgeTone.Success
    }

    StatusBadge(
        text = roleEnum.vietnameseName,
        tone = tone,
        modifier = modifier
    )
}

@Composable
fun UserStatusBadge(
    status: String,
    modifier: Modifier = Modifier
) {
    val statusEnum = AppAuthContract.Status.fromRaw(status)
    val tone = when (statusEnum) {
        AppAuthContract.Status.Pending -> AppBadgeTone.Warning
        AppAuthContract.Status.Approved -> AppBadgeTone.Success
        AppAuthContract.Status.Rejected -> AppBadgeTone.Error
        AppAuthContract.Status.Locked -> AppBadgeTone.Neutral
    }

    StatusBadge(
        text = statusEnum.vietnameseName,
        tone = tone,
        modifier = modifier
    )
}

@Composable
fun EmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionText: String = "",
    onActionClick: (() -> Unit)? = null
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(66.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    AppColors.CyanSoft,
                                    AppColors.BlueSoft
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = AppColors.BlueDeep,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Text(
                text = title,
                color = AppColors.Ink,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(7.dp))

            Text(
                text = message,
                color = AppColors.Muted,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )

            if (actionText.isNotBlank() && onActionClick != null) {
                Spacer(modifier = Modifier.height(18.dp))

                AppGradientButton(
                    text = actionText,
                    onClick = onActionClick,
                    height = 54.dp
                )
            }
        }
    }
}

@Composable
fun ErrorBanner(
    message: String,
    modifier: Modifier = Modifier,
    title: String = "Có lỗi xảy ra",
    visible: Boolean = message.isNotBlank()
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)) + scaleIn(initialScale = 0.97f),
        exit = fadeOut(tween(100)) + scaleOut(targetScale = 0.97f),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = AppColors.RoseSoft,
            border = BorderStroke(
                width = 1.dp,
                color = AppColors.Rose.copy(alpha = 0.18f)
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp)
            ) {
                Text(
                    text = title,
                    color = AppColors.Rose,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = message,
                    color = AppColors.Rose,
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
fun ConfirmDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissText: String = "Hủy",
    danger: Boolean = false,
    loading: Boolean = false
) {
    if (!visible) {
        return
    }

    AlertDialog(
        modifier = modifier,
        onDismissRequest = {
            if (!loading) {
                onDismiss()
            }
        },
        title = {
            Text(
                text = title,
                color = AppColors.Ink,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
        },
        text = {
            Text(
                text = message,
                color = AppColors.Muted,
                lineHeight = 21.sp,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
        },
        confirmButton = {
            TextButton(
                enabled = !loading,
                onClick = onConfirm
            ) {
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = if (danger) AppColors.Rose else AppColors.BlueDeep
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = if (loading) "Đang xử lý" else confirmText,
                    color = if (danger) AppColors.Rose else AppColors.BlueDeep,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        },
        dismissButton = {
            TextButton(
                enabled = !loading,
                onClick = onDismiss
            ) {
                Text(
                    text = dismissText,
                    color = AppColors.Muted,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun FeatureCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    badgeText: String = "",
    tone: AppBadgeTone = AppBadgeTone.Info
) {
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.985f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "feature-card-scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(26.dp),
        color = if (enabled) AppColors.Card else Color(0xFFF7FAFF),
        shadowElevation = if (enabled) 10.dp else 0.dp,
        border = BorderStroke(1.dp, if (enabled) Color.White.copy(alpha = 0.92f) else AppColors.BorderSoft)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            if (enabled) {
                                Brush.horizontalGradient(
                                    listOf(
                                        AppColors.CyanSoft,
                                        AppColors.BlueSoft
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFF2F6FB),
                                        Color(0xFFF8FAFD)
                                    )
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (enabled) AppColors.BlueDeep else AppColors.SoftText,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (badgeText.isNotBlank()) {
                    Spacer(modifier = Modifier.weight(1f))

                    StatusBadge(
                        text = badgeText,
                        tone = tone
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                color = if (enabled) AppColors.Ink else AppColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = subtitle,
                color = AppColors.Muted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp
                )
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    tone: AppBadgeTone = AppBadgeTone.Info
) {
    val colors = badgeColors(tone)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(26.dp),
        color = AppColors.Card,
        shadowElevation = 10.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(19.dp))
                    .background(colors.background),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.content,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = value,
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 23.sp
                    )
                )

                Text(
                    text = title,
                    color = AppColors.Muted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                )

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        color = AppColors.SoftText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun NotificationCard(
    title: String,
    message: String,
    timeText: String,
    modifier: Modifier = Modifier,
    unread: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = onClick != null,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onClick?.invoke() }
            ),
        shape = RoundedCornerShape(24.dp),
        color = if (unread) AppColors.BlueSoft else AppColors.Card,
        shadowElevation = if (unread) 8.dp else 4.dp,
        border = BorderStroke(
            width = 1.dp,
            color = if (unread) AppColors.Blue.copy(alpha = 0.18f) else AppColors.BorderSoft
        )
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (unread) AppColors.BlueDeep else AppColors.Border)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = title,
                        color = AppColors.Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    if (timeText.isNotBlank()) {
                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = timeText,
                            color = AppColors.Muted,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = message,
                    color = AppColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
            }
        }
    }
}

@Composable
fun WorkItemCard(
    title: String,
    description: String,
    statusText: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    tone: AppBadgeTone = AppBadgeTone.Warning,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = onClick != null,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = { onClick?.invoke() }
            ),
        shape = RoundedCornerShape(24.dp),
        color = AppColors.Card,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.92f))
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                val colors = badgeColors(tone)

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .background(colors.background),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.content,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = description,
                    color = AppColors.Muted,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp
                    )
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            StatusBadge(
                text = statusText,
                tone = tone
            )
        }
    }
}

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String = "",
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
    actionIcon: ImageVector? = null,
    actionBadgeCount: Int = 0,
    onActionClick: (() -> Unit)? = null,
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val ignoredScrollBehavior = scrollBehavior
    ignoredScrollBehavior?.state

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppDimens.ScreenPadding, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (navigationIcon != null && onNavigationClick != null) {
                HeaderIconButton(
                    icon = navigationIcon,
                    onClick = onNavigationClick
                )

                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = AppColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.35).sp
                    )
                )

                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = subtitle,
                        color = AppColors.Muted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            if (actionIcon != null && onActionClick != null) {
                Spacer(modifier = Modifier.width(12.dp))

                Box {
                    HeaderIconButton(
                        icon = actionIcon,
                        onClick = onActionClick
                    )

                    if (actionBadgeCount > 0) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(if (actionBadgeCount > 9) 24.dp else 20.dp),
                            shape = CircleShape,
                            color = AppColors.Rose,
                            border = BorderStroke(1.dp, Color.White)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (actionBadgeCount > 99) "99+" else actionBadgeCount.toString(),
                                    color = Color.White,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(46.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(17.dp),
        color = Color.White.copy(alpha = 0.88f),
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, AppColors.BorderSoft)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColors.BlueDeep,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun AppBottomBar(
    items: List<AppNavItem>,
    currentRoute: String,
    onItemClick: (AppNavItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        shape = RoundedCornerShape(AppDimens.BottomBarRadius),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 14.dp,
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.96f))
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(72.dp)
        ) {
            items.forEach { item ->
                val selected = item.route == currentRoute

                NavigationBarItem(
                    selected = selected,
                    onClick = { onItemClick(item) },
                    icon = {
                        Box {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )

                            if (item.badgeCount > 0) {
                                Surface(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(if (item.badgeCount > 9) 22.dp else 18.dp),
                                    shape = CircleShape,
                                    color = AppColors.Rose,
                                    border = BorderStroke(1.dp, Color.White)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                            color = Color.White,
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 9.sp
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 11.sp
                            )
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AppColors.BlueDeep,
                        selectedTextColor = AppColors.BlueDeep,
                        indicatorColor = AppColors.BlueSoft,
                        unselectedIconColor = AppColors.Muted,
                        unselectedTextColor = AppColors.Muted
                    )
                )
            }
        }
    }
}

@Composable
fun AppDrawer(
    user: User,
    drawerItems: List<AppDrawerItem>,
    currentRoute: String,
    onItemClick: (AppDrawerItem) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier.fillMaxWidth(0.84f),
        drawerContainerColor = Color.Transparent
    ) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(16.dp)
            ) {
                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 10.dp,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(21.dp))
                                    .background(AppAnimatedGradient()),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user.displayName.take(1).uppercase(),
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.width(13.dp))

                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = user.displayName,
                                    color = AppColors.Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 17.sp
                                    )
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    UserRoleBadge(role = user.role)
                                    UserStatusBadge(status = user.status)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                AppCard(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Column {
                        drawerItems.forEach { item ->
                            DrawerMenuItem(
                                item = item,
                                selected = item.route == currentRoute,
                                onClick = { onItemClick(item) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                AppOutlinedButton(
                    text = "Đăng xuất",
                    onClick = onLogoutClick,
                    height = 56.dp
                )
            }
        }
    }
}

@Composable
private fun DrawerMenuItem(
    item: AppDrawerItem,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background by animateColorAsState(
        targetValue = if (selected) AppColors.BlueSoft else Color.Transparent,
        animationSpec = tween(180),
        label = "drawer-item-background"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(
                enabled = item.enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = when {
                !item.enabled -> AppColors.SoftText
                selected -> AppColors.BlueDeep
                else -> AppColors.Muted
            },
            modifier = Modifier.size(23.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = item.title,
            color = when {
                !item.enabled -> AppColors.SoftText
                selected -> AppColors.BlueDeep
                else -> AppColors.Text
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp
            ),
            modifier = Modifier.weight(1f)
        )

        if (item.badgeText.isNotBlank()) {
            Spacer(modifier = Modifier.width(8.dp))

            StatusBadge(
                text = item.badgeText,
                tone = AppBadgeTone.Warning
            )
        }
    }
}

fun closeDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    scope.launch {
        drawerState.close()
    }
}

fun openDrawer(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    scope.launch {
        drawerState.open()
    }
}

@Composable
fun LoadingStateCard(
    message: String,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        contentPadding = PaddingValues(22.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color = AppColors.BlueDeep,
                strokeWidth = 3.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = message,
                color = AppColors.Text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
            )
        }
    }
}

@Composable
fun PermissionDeniedCard(
    modifier: Modifier = Modifier,
    message: String = "Bạn không có quyền thực hiện thao tác này."
) {
    EmptyState(
        title = "Không có quyền truy cập",
        message = message,
        modifier = modifier
    )
}

@Composable
fun AppAnimatedGradient(): Brush {
    val transition = rememberInfiniteTransition(label = "app-gradient")

    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "app-gradient-progress"
    )

    return Brush.linearGradient(
        colors = listOf(
            AppColors.Cyan,
            AppColors.Blue,
            AppColors.BlueDeep
        ),
        start = Offset(0f + progress * 160f, 0f),
        end = Offset(900f - progress * 120f, 420f),
        tileMode = TileMode.Clamp
    )
}

@Composable
private fun ButtonShineOverlay(
    alpha: Float = 0.31f,
    widthFraction: Float = 0.34f
) {
    val transition = rememberInfiniteTransition(label = "app-button-shine")

    val progress by transition.animateFloat(
        initialValue = -0.60f,
        targetValue = 1.60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1850, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "app-button-shine-progress"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val shineWidth = canvasWidth * widthFraction
        val x = canvasWidth * progress

        rotate(degrees = -18f, pivot = Offset(x, canvasHeight / 2f)) {
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        Color.White.copy(alpha = alpha),
                        Color.Transparent
                    )
                ),
                topLeft = Offset(x - shineWidth / 2f, -canvasHeight),
                size = Size(shineWidth, canvasHeight * 3f),
                cornerRadius = CornerRadius(999f, 999f)
            )
        }
    }
}

@Composable
private fun badgeColors(
    tone: AppBadgeTone
): BadgeColorSet {
    return when (tone) {
        AppBadgeTone.Info -> BadgeColorSet(
            background = AppColors.BlueSoft,
            border = AppColors.Blue.copy(alpha = 0.18f),
            content = AppColors.BlueDeep
        )

        AppBadgeTone.Success -> BadgeColorSet(
            background = AppColors.GreenSoft,
            border = AppColors.Green.copy(alpha = 0.18f),
            content = AppColors.Green
        )

        AppBadgeTone.Warning -> BadgeColorSet(
            background = AppColors.AmberSoft,
            border = AppColors.Amber.copy(alpha = 0.20f),
            content = AppColors.Amber
        )

        AppBadgeTone.Error -> BadgeColorSet(
            background = AppColors.RoseSoft,
            border = AppColors.Rose.copy(alpha = 0.18f),
            content = AppColors.Rose
        )

        AppBadgeTone.Neutral -> BadgeColorSet(
            background = Color(0xFFF3F7FB),
            border = AppColors.Border,
            content = AppColors.Muted
        )

        AppBadgeTone.Purple -> BadgeColorSet(
            background = AppColors.PurpleSoft,
            border = AppColors.Purple.copy(alpha = 0.18f),
            content = AppColors.Purple
        )
    }
}

@Immutable
private data class BadgeColorSet(
    val background: Color,
    val border: Color,
    val content: Color
)