package com.kuikly.starrating

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.tencent.kuikly.compose.foundation.gestures.awaitEachGesture
import com.tencent.kuikly.compose.foundation.gestures.awaitFirstDown
import com.tencent.kuikly.compose.foundation.layout.Arrangement
import com.tencent.kuikly.compose.foundation.layout.Box
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.requiredSize
import com.tencent.kuikly.compose.foundation.layout.size
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.ui.Alignment
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.draw.clipToBounds
import com.tencent.kuikly.compose.ui.input.pointer.pointerInput
import com.tencent.kuikly.compose.ui.layout.Layout
import com.tencent.kuikly.compose.ui.text.style.TextAlign
import com.tencent.kuikly.compose.ui.text.style.TextOverflow
import com.tencent.kuikly.compose.ui.unit.Constraints
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.compose.ui.graphics.Color as ComposeColor

/**
 * Compose 星级评分。用 Row / Box / Text 绘制，不经过 MakeKuiklyComposeNode。
 *
 * Kuikly 2.4.2 的 ComposeContainer.ignoreLayout 会跳过 flex。
 * 把 StarRatingView 包进 MakeKuiklyComposeNode 后，内部 flex 子节点帧是 0，星星空白也不能点。
 * 取值约定与自研 DSL 相同，见 [StarRatingGeometry]。
 * 字形由 [starGlyph] 指定，默认 ★，空字符串回落到 ★。半星仍按宽度裁切亮色字形。
 */
@Composable
fun StarRating(
    modifier: Modifier = Modifier,
    maxStars: Int = StarRatingGeometry.DEFAULT_MAX_STARS,
    rating: Float = 0f,
    allowHalf: Boolean = true,
    readOnly: Boolean = false,
    starSize: Float = StarRatingGeometry.DEFAULT_STAR_SIZE,
    starSpacing: Float = StarRatingGeometry.DEFAULT_STAR_SPACING,
    activeColor: Color = DefaultStarActiveColor,
    inactiveColor: Color = DefaultStarInactiveColor,
    starGlyph: String = DefaultStarGlyph,
    onRate: (Float) -> Unit = {},
    controller: StarRatingController = remember { StarRatingController() },
) {
    val constraints = StarRatingConstraints.of(maxStars, allowHalf)
    val size = starSize.coerceAtLeast(1f)
    val spacing = starSpacing.coerceAtLeast(0f)
    val contentWidth = StarRatingGeometry.contentWidth(constraints.maxStars, size, spacing)
    val contentHeight = StarRatingGeometry.contentHeight(size)
    val active = activeColor.toComposeColor()
    val inactive = inactiveColor.toComposeColor()
    val glyph = normalizeStarGlyph(starGlyph)

    var preview by remember { mutableStateOf<Float?>(null) }
    var appliedConstraints by remember { mutableStateOf<StarRatingConstraints?>(null) }
    val shown = preview ?: StarRatingGeometry.snapRating(
        rating,
        constraints.maxStars,
        constraints.allowHalf,
    )
    val constraintEmit = StarRatingGeometry.ratingAfterConstraintChange(
        raw = rating,
        previous = appliedConstraints,
        next = constraints,
    )

    val gesture = remember { GestureBridge() }
    gesture.rating = rating
    gesture.maxStars = constraints.maxStars
    gesture.allowHalf = constraints.allowHalf
    gesture.starSize = size
    gesture.starSpacing = spacing
    gesture.readOnly = readOnly
    gesture.onRate = onRate
    gesture.preview = { value ->
        preview = value
        controller.updateDisplayed(value)
    }
    gesture.clearPreview = {
        preview = null
    }

    SideEffect {
        controller.updateDisplayed(shown)
        if (appliedConstraints != constraints) {
            appliedConstraints = constraints
            if (constraintEmit != null) {
                onRate(constraintEmit)
            }
        }
    }

    DisposableEffect(controller) {
        controller.bind { value ->
            val snapped = StarRatingGeometry.snapRating(
                value,
                gesture.maxStars,
                gesture.allowHalf,
            )
            val committed = StarRatingGeometry.snapRating(
                gesture.rating,
                gesture.maxStars,
                gesture.allowHalf,
            )
            gesture.clearPreview()
            controller.updateDisplayed(snapped)
            if (snapped != committed) {
                gesture.onRate(snapped)
            }
        }
        onDispose { controller.unbind() }
    }

    val pointerModifier = if (readOnly) {
        Modifier
    } else {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                val down = awaitFirstDown(requireUnconsumed = false)
                if (gesture.readOnly) {
                    return@awaitEachGesture
                }
                var commitX: Float? = null
                try {
                    down.consume()
                    gesture.preview(down.position.x.toDp().value)
                    while (true) {
                        val event = awaitPointerEvent()
                        if (event.nativeEvent == "cancel") {
                            break
                        }
                        val change = event.changes.firstOrNull { it.id == down.id } ?: continue
                        if (!change.pressed) {
                            if (!change.isConsumed) {
                                commitX = change.position.x.toDp().value
                            }
                            break
                        }
                        gesture.preview(change.position.x.toDp().value)
                        change.consume()
                    }
                } finally {
                    val x = commitX
                    if (x == null) {
                        gesture.clearPreview()
                        controller.updateDisplayed(
                            StarRatingGeometry.snapRating(
                                gesture.rating,
                                gesture.maxStars,
                                gesture.allowHalf,
                            ),
                        )
                    } else {
                        controller.updateDisplayed(gesture.commit(x))
                    }
                }
            }
        }
    }

    Layout(
        modifier = modifier.clipToBounds().then(pointerModifier),
        content = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(spacing.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(constraints.maxStars) { index ->
                    val fill = (shown - index).coerceIn(0f, 1f)
                    StarGlyph(
                        glyph = glyph,
                        starSize = size,
                        fill = fill,
                        active = active,
                        inactive = inactive,
                    )
                }
            }
        },
        measurePolicy = { measurables, incoming ->
            val desiredWidth = contentWidth.dp.roundToPx().coerceAtLeast(0)
            val desiredHeight = contentHeight.dp.roundToPx().coerceAtLeast(0)
            val width = StarRatingGeometry.resolveContentSize(
                content = desiredWidth,
                min = incoming.minWidth,
                max = incoming.maxWidth,
                bounded = incoming.hasBoundedWidth,
            )
            val height = StarRatingGeometry.resolveContentSize(
                content = desiredHeight,
                min = incoming.minHeight,
                max = incoming.maxHeight,
                bounded = incoming.hasBoundedHeight,
            )
            val childConstraints = Constraints(
                minWidth = 0,
                maxWidth = desiredWidth,
                minHeight = 0,
                maxHeight = desiredHeight,
            )
            val placeables = measurables.map { it.measure(childConstraints) }
            layout(width, height) {
                placeables.forEach { placeable ->
                    val y = ((height - placeable.height) / 2).coerceAtLeast(0)
                    placeable.place(0, y)
                }
            }
        },
    )
}

@Composable
private fun StarGlyph(
    glyph: String,
    starSize: Float,
    fill: Float,
    active: ComposeColor,
    inactive: ComposeColor,
) {
    Box(modifier = Modifier.size(starSize.dp).clipToBounds()) {
        StarLabel(
            glyph = glyph,
            color = inactive,
            starSize = starSize,
            modifier = Modifier.size(starSize.dp),
        )
        if (fill > 0f) {
            Box(
                modifier = Modifier
                    .width((starSize * fill).dp)
                    .height(starSize.dp)
                    .clipToBounds(),
            ) {
                StarLabel(
                    glyph = glyph,
                    color = active,
                    starSize = starSize,
                    modifier = Modifier.requiredSize(starSize.dp),
                )
            }
        }
    }
}

@Composable
private fun StarLabel(
    glyph: String,
    color: ComposeColor,
    starSize: Float,
    modifier: Modifier,
) {
    Text(
        text = glyph,
        color = color,
        fontSize = starSize.sp,
        lineHeight = starSize.sp,
        textAlign = TextAlign.Center,
        maxLines = 1,
        softWrap = false,
        overflow = TextOverflow.Clip,
        modifier = modifier,
    )
}

private class GestureBridge {
    var rating: Float = 0f
    var maxStars: Int = StarRatingGeometry.DEFAULT_MAX_STARS
    var allowHalf: Boolean = true
    var starSize: Float = StarRatingGeometry.DEFAULT_STAR_SIZE
    var starSpacing: Float = StarRatingGeometry.DEFAULT_STAR_SPACING
    var readOnly: Boolean = false
    var onRate: (Float) -> Unit = {}
    var preview: (Float) -> Unit = {}
    var clearPreview: () -> Unit = {}

    fun commit(xDp: Float): Float {
        val next = StarRatingGeometry.ratingFromTouch(
            x = xDp,
            maxStars = maxStars,
            starSize = starSize,
            starSpacing = starSpacing,
            allowHalf = allowHalf,
        )
        val before = StarRatingGeometry.snapRating(rating, maxStars, allowHalf)
        clearPreview()
        if (next != before) {
            onRate(next)
            return next
        }
        return before
    }
}

private fun Color.toComposeColor(): ComposeColor {
    return ComposeColor(hexColor and 0xFFFFFFFFL)
}
