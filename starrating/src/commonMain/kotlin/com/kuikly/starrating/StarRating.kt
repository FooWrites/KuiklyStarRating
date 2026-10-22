package com.kuikly.starrating

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vbind
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

val DefaultStarActiveColor = Color(0xFFF5B400L)
val DefaultStarInactiveColor = Color(0xFFD8DDE6L)

private const val STAR_GLYPH = "★"

/**
 * 星级评分。全部用自研 DSL 的 View / Text 绘制，不依赖原生 View。
 * 半星是把亮色 ★ 裁到左半边，叠在灰色 ★ 上。
 */
class StarRatingView : ViewContainer<StarRatingAttr, StarRatingEvent>() {
    private var maxStarsCount: Int = 0
    private var starSizePx: Float = 0f
    private var starSpacingPx: Float = 0f
    private var allowHalfFlag: Boolean = true
    private var readOnlyFlag: Boolean = false
    private var shownRating: Float = -1f
    private var activeColorValue: Color = DefaultStarActiveColor
    private var inactiveColorValue: Color = DefaultStarInactiveColor
    private var renderToken by observable(0)

    override fun createAttr(): StarRatingAttr {
        val size = StarRatingGeometry.DEFAULT_STAR_SIZE
        val spacing = StarRatingGeometry.DEFAULT_STAR_SPACING
        val count = StarRatingGeometry.DEFAULT_MAX_STARS
        return StarRatingAttr().apply {
            width(StarRatingGeometry.contentWidth(count, size, spacing))
            height(StarRatingGeometry.contentHeight(size))
            overflow(true)
        }
    }

    override fun createEvent(): StarRatingEvent = StarRatingEvent()

    override fun viewName(): String = ViewConst.TYPE_VIEW

    override fun didInit() {
        super.didInit()
        attr.onStyleChanged = { syncStyle() }
        syncStyle()
        mountSurface()
    }

    fun currentRating(): Float = shownRating.coerceAtLeast(0f)

    fun setRating(value: Float) {
        val before = currentRating()
        attr.rating(value)
        if (currentRating() != before) {
            getViewEvent().emit(currentRating())
        }
    }

    fun clear() {
        setRating(0f)
    }

    private fun syncStyle() {
        val maxStars = StarRatingGeometry.normalizeMaxStars(attr.maxStars)
        val size = attr.starSize.coerceAtLeast(1f)
        val spacing = attr.starSpacing.coerceAtLeast(0f)
        val half = attr.allowHalf
        val rating = StarRatingGeometry.snapRating(attr.ratingValue, maxStars, half)
        val activeColor = attr.active
        val inactiveColor = attr.inactive
        readOnlyFlag = attr.readOnly
        val visualChanged = maxStars != maxStarsCount ||
            size != starSizePx ||
            spacing != starSpacingPx ||
            half != allowHalfFlag ||
            rating != shownRating ||
            activeColor != activeColorValue ||
            inactiveColor != inactiveColorValue
        maxStarsCount = maxStars
        starSizePx = size
        starSpacingPx = spacing
        allowHalfFlag = half
        shownRating = rating
        attr.ratingValue = rating
        activeColorValue = activeColor
        inactiveColorValue = inactiveColor
        if (visualChanged) {
            getViewAttr().width(StarRatingGeometry.contentWidth(maxStars, size, spacing))
            getViewAttr().height(StarRatingGeometry.contentHeight(size))
            renderToken++
        }
    }

    private fun mountSurface() {
        val ctx = this
        View {
            attr {
                flexDirectionRow()
            }
            vbind({ ctx.renderToken }) {
                View {
                    attr {
                        flexDirectionRow()
                        height(ctx.starSizePx)
                    }
                    val count = ctx.maxStarsCount.coerceAtLeast(1)
                    val size = ctx.starSizePx
                    val spacing = ctx.starSpacingPx
                    for (index in 0 until count) {
                        val fill = (ctx.shownRating - index).coerceIn(0f, 1f)
                        val gap = if (index == count - 1) 0f else spacing
                        View {
                            attr {
                                width(size)
                                height(size)
                                marginRight(gap)
                                overflow(true)
                            }
                            Text {
                                attr {
                                    text(STAR_GLYPH)
                                    fontSize(size)
                                    lineHeight(size)
                                    textAlignCenter()
                                    color(ctx.inactiveColorValue)
                                    width(size)
                                    height(size)
                                }
                            }
                            if (fill > 0f) {
                                View {
                                    attr {
                                        absolutePosition(top = 0f, left = 0f)
                                        width(size * fill)
                                        height(size)
                                        overflow(true)
                                    }
                                    Text {
                                        attr {
                                            absolutePosition(top = 0f, left = 0f)
                                            text(STAR_GLYPH)
                                            fontSize(size)
                                            lineHeight(size)
                                            textAlignCenter()
                                            color(ctx.activeColorValue)
                                            width(size)
                                            height(size)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        View {
            attr {
                absolutePositionAllZero()
                backgroundColor(Color.TRANSPARENT)
                touchEnable(true)
                consumeTouchDown(true)
            }
            event {
                touchDown(true) { params ->
                    ctx.handleTouch(params.x)
                }
                touchMove(true) { params ->
                    ctx.handleTouch(params.x)
                }
            }
        }
    }

    private fun handleTouch(x: Float) {
        if (readOnlyFlag) {
            return
        }
        val next = StarRatingGeometry.ratingFromTouch(
            x = x,
            maxStars = maxStarsCount,
            starSize = starSizePx,
            starSpacing = starSpacingPx,
            allowHalf = allowHalfFlag,
        )
        if (next == shownRating) {
            return
        }
        shownRating = next
        attr.ratingValue = next
        renderToken++
        getViewEvent().emit(next)
    }
}

class StarRatingAttr : ContainerAttr() {
    internal var maxStars: Int = StarRatingGeometry.DEFAULT_MAX_STARS
    internal var ratingValue: Float = 0f
    internal var allowHalf: Boolean = true
    internal var readOnly: Boolean = false
    internal var starSize: Float = StarRatingGeometry.DEFAULT_STAR_SIZE
    internal var starSpacing: Float = StarRatingGeometry.DEFAULT_STAR_SPACING
    internal var active: Color = DefaultStarActiveColor
    internal var inactive: Color = DefaultStarInactiveColor
    internal var onStyleChanged: (() -> Unit)? = null

    fun maxStars(count: Int) {
        maxStars = count
        onStyleChanged?.invoke()
    }

    fun rating(value: Float) {
        ratingValue = value
        onStyleChanged?.invoke()
    }

    fun allowHalf(enabled: Boolean) {
        allowHalf = enabled
        onStyleChanged?.invoke()
    }

    fun readOnly(enabled: Boolean) {
        readOnly = enabled
        onStyleChanged?.invoke()
    }

    fun starSize(size: Float) {
        starSize = size
        onStyleChanged?.invoke()
    }

    fun starSpacing(spacing: Float) {
        starSpacing = spacing
        onStyleChanged?.invoke()
    }

    fun activeColor(color: Color) {
        active = color
        onStyleChanged?.invoke()
    }

    fun inactiveColor(color: Color) {
        inactive = color
        onStyleChanged?.invoke()
    }
}

class StarRatingEvent : com.tencent.kuikly.core.base.event.Event() {
    internal var onRateHandler: ((Float) -> Unit)? = null

    fun onRate(handler: (Float) -> Unit) {
        onRateHandler = handler
    }

    internal fun emit(value: Float) {
        onRateHandler?.invoke(value)
    }
}

fun ViewContainer<*, *>.StarRating(init: StarRatingView.() -> Unit) {
    addChild(StarRatingView(), init)
}
