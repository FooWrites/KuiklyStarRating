package com.kuikly.starrating

import kotlin.math.roundToInt

/**
 * 星级命中与取值。不依赖渲染，自研 DSL 与 Compose DSL 共用同一套计算。
 */
object StarRatingGeometry {
    const val MIN_STARS = 1
    const val MAX_STARS = 20
    const val DEFAULT_MAX_STARS = 5
    const val DEFAULT_STAR_SIZE = 28f
    const val DEFAULT_STAR_SPACING = 6f

    fun normalizeMaxStars(maxStars: Int): Int {
        return maxStars.coerceIn(MIN_STARS, MAX_STARS)
    }

    fun contentWidth(maxStars: Int, starSize: Float, starSpacing: Float): Float {
        val count = normalizeMaxStars(maxStars)
        val size = starSize.coerceAtLeast(1f)
        val spacing = starSpacing.coerceAtLeast(0f)
        return count * size + (count - 1) * spacing
    }

    fun contentHeight(starSize: Float): Float {
        return starSize.coerceAtLeast(1f)
    }

    /**
     * 把触摸点的本地 x 映射到评分。
     * x < 0 视为滑出左侧，记 0 分；落在星星上时按槽位取整星或半星。
     * 星星之间的空隙归到更近的那一侧。
     */
    fun ratingFromTouch(
        x: Float,
        maxStars: Int,
        starSize: Float,
        starSpacing: Float,
        allowHalf: Boolean,
    ): Float {
        val count = normalizeMaxStars(maxStars)
        val size = starSize.coerceAtLeast(1f)
        val spacing = starSpacing.coerceAtLeast(0f)
        if (x < 0f) {
            return 0f
        }
        val width = contentWidth(count, size, spacing)
        if (x >= width) {
            return count.toFloat()
        }
        val pitch = size + spacing
        val index = (x / pitch).toInt().coerceIn(0, count - 1)
        val local = x - index * pitch
        if (spacing == 0f || local <= size) {
            return if (allowHalf) {
                if (local < size / 2f) index + 0.5f else (index + 1).toFloat()
            } else {
                (index + 1).toFloat()
            }
        }
        val intoGap = local - size
        val closerToNext = intoGap > spacing / 2f
        val raw = if (!closerToNext) {
            (index + 1).toFloat()
        } else if (allowHalf) {
            index + 1.5f
        } else {
            (index + 2).toFloat()
        }
        return raw.coerceIn(0f, count.toFloat())
    }

    fun snapRating(value: Float, maxStars: Int, allowHalf: Boolean): Float {
        val max = normalizeMaxStars(maxStars).toFloat()
        if (value.isNaN()) {
            return 0f
        }
        val clamped = value.coerceIn(0f, max)
        return if (allowHalf) {
            ((clamped * 2f).roundToInt() / 2f).coerceIn(0f, max)
        } else {
            clamped.roundToInt().toFloat().coerceIn(0f, max)
        }
    }

    fun formatRating(value: Float): String {
        val scaled = (snapRating(value, MAX_STARS, allowHalf = true) * 2f).roundToInt()
        val whole = scaled / 2
        return if (scaled % 2 == 0) {
            whole.toString()
        } else {
            "$whole.5"
        }
    }
}
