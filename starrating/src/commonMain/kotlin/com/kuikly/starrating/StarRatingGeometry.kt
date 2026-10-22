package com.kuikly.starrating

import kotlin.math.roundToInt

/**
 * 星级命中与取值。不依赖渲染，自研 DSL 与 Compose DSL 共用同一套计算。
 *
 * 取值约定：
 * - `rating` 保存调用方传入的原始值，属性刷新时不改写。
 * - 星星画 [snapRating] 的结果。半星开着时按 0.5 取整，否则取整星。
 * - 半星开关或星数变化让展示值变了，回调一次 onRate，参数是新的展示值。
 *   第一次挂到页面上不回调。调用方把回调值写回 `rating` 后不会再回调。
 * - 按下和移动只预览，不回调。松手才提交；展示值有变化才回调一次。
 *   手势取消则回到按下前的展示，不回调。
 * - `setRating` / `clear` 写入原始值，只在展示值变化时回调展示值。
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
     * 把内容尺寸收进父约束。
     *
     * 父约束松散时用内容尺寸，不用 max。父约束把 min 和 max 收成同一个值时，保留调用方指定的尺寸。
     * [bounded] 为 false 表示该轴没有上界（例如滚动方向），这时忽略 [max]，避免把无限大当成组件尺寸。
     */
    fun resolveContentSize(content: Int, min: Int, max: Int, bounded: Boolean): Int {
        val safeContent = content.coerceAtLeast(0)
        val low = min.coerceAtLeast(0)
        val upper = if (bounded) max else safeContent
        val high = maxOf(low, upper)
        return safeContent.coerceIn(low, high)
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

    /**
     * 半星开关或星数变化后，若同一个原始值的展示结果变了，返回新的展示值；否则返回 null。
     *
     * [previous] 为 null 表示组件刚挂上，不回调。调用方把返回值写回原始值后，同一组约束不会再次返回。
     * 原始值本身不被改写，取整只影响展示和这次回调。
     */
    fun ratingAfterConstraintChange(
        raw: Float,
        previous: StarRatingConstraints?,
        next: StarRatingConstraints,
    ): Float? {
        if (previous == null) {
            return null
        }
        val normalizedPrev = StarRatingConstraints.of(previous.maxStars, previous.allowHalf)
        val normalizedNext = StarRatingConstraints.of(next.maxStars, next.allowHalf)
        if (normalizedPrev == normalizedNext) {
            return null
        }
        val before = snapRating(raw, normalizedPrev.maxStars, normalizedPrev.allowHalf)
        val after = snapRating(raw, normalizedNext.maxStars, normalizedNext.allowHalf)
        return if (before != after) after else null
    }

    fun formatRating(value: Float): String {
        val scaled = (snapRating(value, MAX_STARS, allowHalf = true) * 2f).roundToInt()
        val whole = scaled / 2
        return if (scaled % 2 == 0) {
            whole.toString()
        }         else {
            "$whole.5"
        }
    }
}

/**
 * 已经归一化之前也可以传进来。[of] 会把星数收进合法范围。
 */
data class StarRatingConstraints(
    val maxStars: Int,
    val allowHalf: Boolean,
) {
    companion object {
        fun of(maxStars: Int, allowHalf: Boolean): StarRatingConstraints {
            return StarRatingConstraints(
                StarRatingGeometry.normalizeMaxStars(maxStars),
                allowHalf,
            )
        }
    }
}
