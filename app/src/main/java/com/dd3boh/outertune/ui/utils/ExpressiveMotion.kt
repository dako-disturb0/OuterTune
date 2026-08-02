package com.dd3boh.outertune.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.dd3boh.outertune.ui.theme.ExpressiveSpringSpec

/**
 * Material 3 Expressive press scale modifier.
 * Scales down smoothly on press with low bouncy spring physics.
 */
@Composable
fun Modifier.expressiveClickable(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    pressedScale: Float = 0.94f,
    onClick: () -> Unit
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) pressedScale else 1.0f,
        animationSpec = ExpressiveSpringSpec,
        label = "expressivePressScale"
    )

    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled,
            onClickLabel = onClickLabel,
            onClick = onClick
        )
}

/**
 * Material 3 Expressive press scale effect for existing clickables.
 */
@Composable
fun Modifier.expressiveScaleOnPress(
    interactionSource: MutableInteractionSource,
    pressedScale: Float = 0.94f
): Modifier {
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) pressedScale else 1.0f,
        animationSpec = ExpressiveSpringSpec,
        label = "expressiveScaleOnPress"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
