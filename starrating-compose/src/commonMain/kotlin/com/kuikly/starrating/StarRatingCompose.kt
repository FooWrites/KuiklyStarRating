package com.kuikly.starrating

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tencent.kuikly.compose.extension.MakeKuiklyComposeNode
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.core.base.Color

class StarRatingController {
    internal var ratingView: StarRatingView? = null
    private var pending: Float = 0f

    fun rating(): Float = ratingView?.currentRating() ?: pending

    fun setRating(value: Float) {
        pending = value
        val view = ratingView
        if (view == null) {
            return
        }
        view.setRating(value)
        pending = view.currentRating()
    }

    fun clear() {
        setRating(0f)
    }
}

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
    onRate: (Float) -> Unit = {},
    controller: StarRatingController = remember { StarRatingController() },
) {
    MakeKuiklyComposeNode(
        factory = { StarRatingView() },
        modifier = modifier,
        viewUpdate = { view ->
            controller.ratingView = view
            view.getViewAttr().apply {
                maxStars(maxStars)
                rating(rating)
                allowHalf(allowHalf)
                readOnly(readOnly)
                starSize(starSize)
                starSpacing(starSpacing)
                activeColor(activeColor)
                inactiveColor(inactiveColor)
            }
            view.getViewEvent().onRate(onRate)
        },
    )
}
