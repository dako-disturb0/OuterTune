/*
 * Copyright (C) 2025 O​u​t​er​Tu​ne Project
 *
 * SPDX-License-Identifier: GPL-3.0
 *
 * For any other attributions, refer to the git commit history
 */

package com.dd3boh.outertune.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.dd3boh.outertune.constants.PlayerTimelineType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSliderTrack(
    sliderState: SliderState,
    modifier: Modifier = Modifier,
    colors: SliderColors = SliderDefaults.colors(),
    timelineType: PlayerTimelineType = PlayerTimelineType.PIN_BAR,
    trackHeight: Dp = 10.dp,
    isDragging: Boolean = false
) {
    val inactiveTrackColor = colors.inactiveTrackColor
    val activeTrackColor = colors.activeTrackColor
    val inactiveTickColor = colors.inactiveTickColor
    val activeTickColor = colors.activeTickColor

    val valueRange = sliderState.valueRange
    val canvasHeight = maxOf(trackHeight * 1.8f, 24.dp)

    Canvas(
        modifier
            .fillMaxWidth()
            .height(canvasHeight)
    ) {
        drawTrack(
            stepsToTickFractions(sliderState.steps),
            0f,
            calcFraction(
                valueRange.start,
                valueRange.endInclusive,
                sliderState.value.coerceIn(valueRange.start, valueRange.endInclusive)
            ),
            inactiveTrackColor,
            activeTrackColor,
            inactiveTickColor,
            activeTickColor,
            timelineType,
            trackHeight,
            isDragging
        )
    }
}

private fun DrawScope.drawTrack(
    tickFractions: FloatArray,
    activeRangeStart: Float,
    activeRangeEnd: Float,
    inactiveTrackColor: Color,
    activeTrackColor: Color,
    inactiveTickColor: Color,
    activeTickColor: Color,
    timelineType: PlayerTimelineType,
    trackHeight: Dp,
    isDragging: Boolean
) {
    val isRtl = layoutDirection == LayoutDirection.Rtl
    val sliderLeft = Offset(0f, center.y)
    val sliderRight = Offset(size.width, center.y)
    val sliderStart = if (isRtl) sliderRight else sliderLeft
    val sliderEnd = if (isRtl) sliderLeft else sliderRight
    val tickSize = 2.0.dp.toPx()
    val trackStrokeWidth = trackHeight.toPx()

    val sliderValueStart = Offset(
        sliderStart.x + (sliderEnd.x - sliderStart.x) * activeRangeStart,
        center.y
    )

    val sliderValueEnd = Offset(
        sliderStart.x + (sliderEnd.x - sliderStart.x) * activeRangeEnd,
        center.y
    )

    when (timelineType) {
        PlayerTimelineType.PIN_BAR -> {
            drawLine(
                color = inactiveTrackColor,
                start = sliderValueEnd,
                end = sliderEnd,
                strokeWidth = trackStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = activeTrackColor,
                start = sliderValueStart,
                end = sliderValueEnd,
                strokeWidth = trackStrokeWidth,
                cap = StrokeCap.Round
            )
            val pinRadius = maxOf(trackStrokeWidth * 1.2f, 8.dp.toPx())
            drawCircle(
                color = activeTrackColor,
                center = sliderValueEnd,
                radius = pinRadius
            )
            drawCircle(
                color = inactiveTrackColor,
                center = sliderValueEnd,
                radius = pinRadius * 0.4f
            )
        }

        PlayerTimelineType.WAVY -> {
            drawLine(
                color = inactiveTrackColor,
                start = sliderValueEnd,
                end = sliderEnd,
                strokeWidth = trackStrokeWidth,
                cap = StrokeCap.Round
            )

            val activeDist = abs(sliderValueEnd.x - sliderValueStart.x)
            if (activeDist > 0f) {
                val wavePath = Path()
                val wavelength = 20.dp.toPx()
                val amplitude = if (isDragging) trackStrokeWidth * 0.8f else trackStrokeWidth * 0.6f
                val stepPx = 2.dp.toPx()
                val direction = if (sliderValueEnd.x >= sliderValueStart.x) 1f else -1f

                wavePath.moveTo(sliderValueStart.x, center.y)
                var currentDist = 0f
                while (currentDist < activeDist) {
                    currentDist = minOf(currentDist + stepPx, activeDist)
                    val x = sliderValueStart.x + currentDist * direction
                    val y = center.y + amplitude * sin(currentDist * (2 * PI / wavelength)).toFloat()
                    wavePath.lineTo(x, y)
                }

                drawPath(
                    path = wavePath,
                    color = activeTrackColor,
                    style = Stroke(width = trackStrokeWidth, cap = StrokeCap.Round)
                )

                drawCircle(
                    color = activeTrackColor,
                    center = sliderValueEnd,
                    radius = maxOf(trackStrokeWidth * 0.8f, 5.dp.toPx())
                )
            }
        }

        PlayerTimelineType.FAT_BAR -> {
            val fatStrokeWidth = if (isDragging) trackStrokeWidth * 2.2f else trackStrokeWidth * 1.8f

            drawLine(
                color = inactiveTrackColor,
                start = sliderStart,
                end = sliderEnd,
                strokeWidth = fatStrokeWidth,
                cap = StrokeCap.Round
            )
            if (activeRangeEnd > 0f) {
                drawLine(
                    color = activeTrackColor,
                    start = sliderValueStart,
                    end = sliderValueEnd,
                    strokeWidth = fatStrokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }

        PlayerTimelineType.DYNAMIC_BAR -> {
            val activeStrokeWidth = if (isDragging) trackStrokeWidth * 2.0f else trackStrokeWidth * 1.3f
            val inactiveStrokeWidth = if (isDragging) trackStrokeWidth * 1.2f else trackStrokeWidth * 0.7f

            drawLine(
                color = inactiveTrackColor,
                start = sliderStart,
                end = sliderEnd,
                strokeWidth = inactiveStrokeWidth,
                cap = StrokeCap.Round
            )

            if (activeRangeEnd > 0f) {
                drawLine(
                    color = activeTrackColor,
                    start = sliderValueStart,
                    end = sliderValueEnd,
                    strokeWidth = activeStrokeWidth,
                    cap = StrokeCap.Round
                )
            }

            if (isDragging) {
                drawCircle(
                    color = activeTrackColor,
                    center = sliderValueEnd,
                    radius = activeStrokeWidth * 0.7f
                )
            }
        }
    }

    for (tick in tickFractions) {
        val outsideFraction = tick > activeRangeEnd || tick < activeRangeStart
        drawCircle(
            color = if (outsideFraction) inactiveTickColor else activeTickColor,
            center = Offset(lerp(sliderStart, sliderEnd, tick).x, center.y),
            radius = tickSize / 2f
        )
    }
}

private fun stepsToTickFractions(steps: Int): FloatArray {
    return if (steps == 0) floatArrayOf() else FloatArray(steps + 2) { it.toFloat() / (steps + 1) }
}

private fun calcFraction(a: Float, b: Float, pos: Float) =
    (if (b - a == 0f) 0f else (pos - a) / (b - a)).coerceIn(0f, 1f)
