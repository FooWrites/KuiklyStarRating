package com.kuikly.starrating

import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ContainerAttr
import com.tencent.kuikly.core.base.ViewConst
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.directives.vbind
import com.tencent.kuikly.core.layout.valueEquals
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

val DefaultStarActiveColor = Color(0xFFF5B400L)
val DefaultStarInactiveColor = Color(0xFFD8DDE6L)

private const val STAR_GLYPH = "★"

/**
 * 星级评分。全部用自研 DSL 的 View / Text 绘制，不依赖原生 View。
 * 半星是把亮色 ★ 裁到左半边，叠在灰色 ★ 上。
 * 取值约定见 [StarRatingGeometry]。
 */
class StarRatingView : ViewContainer<StarRatingAttr, StarRatingEvent>() {
    private val interaction = StarRatingInteraction()
    private var activeColorValue: Color = DefaultStarActiveColor
    private var inactiveColorValue: Color = DefaultStarInactiveColor
    private var acceptsTouch by observable(true)
    private var paintedKey: PaintKey? = null
    private var renderToken by observable(0)

    override fun createAttr(): StarRatingAttr = StarRatingAttr()

    override fun createEvent(): StarRatingEvent = StarRatingEvent()

    override fun viewName(): String = ViewConst.TYPE_VIEW

    override fun didInit() {
        super.didInit()
        getViewAttr().overflow(true)
        attr.onStyleChanged = { syncStyle() }
        syncStyle()
        mountSurface()
    }

    fun currentRating(): Float = interaction.displayed

    fun setRating(value: Float) {
        val emit = interaction.setRating(value)
        publish()
        if (emit != null) {
            getViewEvent().emit(emit)
        }
    }

    fun clear() {
        setRating(0f)
    }

    private fun syncStyle() {
        interaction.starSize = attr.starSize.coerceAtLeast(1f)
        interaction.starSpacing = attr.starSpacing.coerceAtLeast(0f)
        interaction.readOnly = attr.readOnly
        if (interaction.readOnly) {
            interaction.onCancel()
        }
        val emit = interaction.applyControlled(
            rating = attr.ratingValue,
            maxStars = attr.maxStars,
            allowHalf = attr.allowHalf,
        )
        activeColorValue = attr.active
        inactiveColorValue = attr.inactive
        publish()
        if (emit != null) {
            getViewEvent().emit(emit)
        }
    }

    private fun publish() {
        applyOwnedSize()
        syncTouch()
        val key = PaintKey(
            rating = interaction.displayed,
            stars = interaction.maxStars,
            size = interaction.starSize,
            spacing = interaction.starSpacing,
            active = activeColorValue.hexColor,
            inactive = inactiveColorValue.hexColor,
        )
        if (key != paintedKey) {
            paintedKey = key
            renderToken++
        }
    }

    private fun applyOwnedSize() {
        val width = StarRatingGeometry.contentWidth(
            interaction.maxStars,
            interaction.starSize,
            interaction.starSpacing,
        )
        val height = StarRatingGeometry.contentHeight(interaction.starSize)
        if (!attr.userSetWidth && !flexNode.styleWidth.valueEquals(width)) {
            flexNode.styleWidth = width
        }
        if (!attr.userSetHeight && !flexNode.styleHeight.valueEquals(height)) {
            flexNode.styleHeight = height
        }
    }

    private fun syncTouch() {
        val enabled = !interaction.readOnly
        if (acceptsTouch != enabled) {
            acceptsTouch = enabled
        }
        getViewAttr().touchEnable(enabled)
    }

    private fun mountSurface() {
        val ctx = this
        View {
            attr {
                flexDirectionRow()
                touchEnable(false)
            }
            vbind({ ctx.renderToken }) {
                View {
                    attr {
                        flexDirectionRow()
                        height(ctx.interaction.starSize)
                        touchEnable(false)
                    }
                    val count = ctx.interaction.maxStars.coerceAtLeast(1)
                    val size = ctx.interaction.starSize
                    val spacing = ctx.interaction.starSpacing
                    for (index in 0 until count) {
                        val fill = (ctx.interaction.displayed - index).coerceIn(0f, 1f)
                        val gap = if (index == count - 1) 0f else spacing
                        View {
                            attr {
                                width(size)
                                height(size)
                                marginRight(gap)
                                overflow(true)
                                touchEnable(false)
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
                                        touchEnable(false)
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
                touchEnable(ctx.acceptsTouch)
                consumeTouchDown(ctx.acceptsTouch)
            }
            event {
                touchDown(true) { params ->
                    ctx.handleDown(params.x)
                }
                touchMove(true) { params ->
                    ctx.handleMove(params.x)
                }
                touchUp(true) { params ->
                    ctx.handleUp(params.x)
                }
                touchCancel(true) {
                    ctx.handleCancel()
                }
            }
        }
    }

    private fun handleDown(x: Float) {
        interaction.onDown(x)
        publish()
    }

    private fun handleMove(x: Float) {
        interaction.onMove(x)
        publish()
    }

    private fun handleUp(x: Float) {
        val emit = interaction.onUp(x)
        publish()
        if (emit != null) {
            getViewEvent().emit(emit)
        }
    }

    private fun handleCancel() {
        interaction.onCancel()
        publish()
    }
}

private data class PaintKey(
    val rating: Float,
    val stars: Int,
    val size: Float,
    val spacing: Float,
    val active: Long,
    val inactive: Long,
)

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
    internal var userSetWidth: Boolean = false
    internal var userSetHeight: Boolean = false

    override fun width(width: Float): com.tencent.kuikly.core.base.Attr {
        userSetWidth = true
        return super.width(width)
    }

    override fun height(height: Float): com.tencent.kuikly.core.base.Attr {
        userSetHeight = true
        return super.height(height)
    }

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
