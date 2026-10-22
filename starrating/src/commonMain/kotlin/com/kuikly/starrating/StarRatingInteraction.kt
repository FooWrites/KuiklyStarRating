package com.kuikly.starrating

/**
 * 自研 DSL 的评分状态。展示、预览和回调约定见 [StarRatingGeometry]。
 */
internal class StarRatingInteraction {
    var rawRating: Float = 0f
        private set
    var maxStars: Int = StarRatingGeometry.DEFAULT_MAX_STARS
        private set
    var allowHalf: Boolean = true
        private set
    var readOnly: Boolean = false
    var starSize: Float = StarRatingGeometry.DEFAULT_STAR_SIZE
    var starSpacing: Float = StarRatingGeometry.DEFAULT_STAR_SPACING
    var isDragging: Boolean = false
        private set

    private var preview: Float? = null
    private var previousConstraints: StarRatingConstraints? = null

    val displayed: Float
        get() = preview ?: StarRatingGeometry.snapRating(rawRating, maxStars, allowHalf)

    fun applyControlled(rating: Float, maxStars: Int, allowHalf: Boolean): Float? {
        val next = StarRatingConstraints.of(maxStars, allowHalf)
        val emit = StarRatingGeometry.ratingAfterConstraintChange(
            raw = rating,
            previous = previousConstraints,
            next = next,
        )
        rawRating = rating
        this.maxStars = next.maxStars
        this.allowHalf = next.allowHalf
        previousConstraints = next
        return emit
    }

    fun setRating(value: Float): Float? {
        cancelDrag()
        val before = StarRatingGeometry.snapRating(rawRating, maxStars, allowHalf)
        rawRating = value
        val after = StarRatingGeometry.snapRating(value, maxStars, allowHalf)
        return if (after != before) after else null
    }

    fun onDown(x: Float) {
        if (readOnly) {
            return
        }
        isDragging = true
        preview = touchRating(x)
    }

    fun onMove(x: Float) {
        if (readOnly || !isDragging) {
            return
        }
        preview = touchRating(x)
    }

    fun onUp(x: Float): Float? {
        if (readOnly || !isDragging) {
            return null
        }
        val next = touchRating(x)
        val before = StarRatingGeometry.snapRating(rawRating, maxStars, allowHalf)
        isDragging = false
        preview = null
        rawRating = next
        return if (next != before) next else null
    }

    fun onCancel() {
        cancelDrag()
    }

    private fun cancelDrag() {
        isDragging = false
        preview = null
    }

    private fun touchRating(x: Float): Float {
        return StarRatingGeometry.ratingFromTouch(
            x = x,
            maxStars = maxStars,
            starSize = starSize,
            starSpacing = starSpacing,
            allowHalf = allowHalf,
        )
    }
}
