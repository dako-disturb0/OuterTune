package com.dd3boh.outertune.ui.component.button

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.ui.utils.expressiveClickable

@Composable
fun IconLabelButton(
    text: String,
    icon: ImageVector,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
        .clip(CircleShape)
        .background(background, CircleShape)
        .expressiveClickable(pressedScale = 0.94f) { onClick() }
        .padding(horizontal = 12.dp, vertical = 6.dp)
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.padding(2.dp)
    )
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}

@Composable
fun IconLabelButton(
    text: String,
    painter: Painter,
    background: Color = MaterialTheme.colorScheme.secondaryContainer,
    tint: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
        .clip(CircleShape)
        .background(background, CircleShape)
        .expressiveClickable(pressedScale = 0.94f) { onClick() }
        .padding(horizontal = 12.dp, vertical = 6.dp)
) {
    Icon(
        painter = painter,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.padding(2.dp)
    )
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 4.dp)
    )
}