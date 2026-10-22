package com.kuikly.starrating.demo

import com.kuikly.starrating.StarRating
import com.kuikly.starrating.StarRatingGeometry
import com.kuikly.starrating.StarRatingView
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color
import com.tencent.kuikly.core.base.ViewBuilder
import com.tencent.kuikly.core.base.ViewContainer
import com.tencent.kuikly.core.base.ViewRef
import com.tencent.kuikly.core.pager.Pager
import com.tencent.kuikly.core.reactive.handler.observable
import com.tencent.kuikly.core.views.Text
import com.tencent.kuikly.core.views.View

private const val DEMO_MAX_STARS = 5
private const val DEMO_STAR_SIZE = 36f
private const val DEMO_STAR_SPACING = 8f

@Page("star_rating_demo")
class StarRatingDemoPage : Pager() {
    private var ratingRef: ViewRef<StarRatingView>? = null
    private var rating by observable(0f)
    private var readOnly by observable(false)
    private var allowHalf by observable(true)
    private var hint by observable("尚未评分")

    override fun body(): ViewBuilder {
        val ctx = this
        return {
            attr {
                flex(1f)
                flexDirectionColumn()
                backgroundColor(Color.WHITE)
                padding(16f)
            }
            Text {
                attr {
                    text("星级评分")
                    fontSize(20f)
                    fontWeightBold()
                    color(Color(0xFF1F2328L))
                }
            }
            Text {
                attr {
                    marginTop(6f)
                    text("自研 DSL。拖动只预览，松手后记分。")
                    fontSize(14f)
                    color(Color(0xFF667085L))
                }
            }
            StarRating {
                ref { ctx.ratingRef = it }
                attr {
                    marginTop(20f)
                    maxStars(DEMO_MAX_STARS)
                    rating(ctx.rating)
                    allowHalf(ctx.allowHalf)
                    readOnly(ctx.readOnly)
                    starSize(DEMO_STAR_SIZE)
                    starSpacing(DEMO_STAR_SPACING)
                    activeColor(Color(0xFFF5B400L))
                    inactiveColor(Color(0xFFD8DDE6L))
                }
                event {
                    onRate { value ->
                        ctx.rating = value
                        ctx.hint = "当前 ${StarRatingGeometry.formatRating(value)} 分"
                    }
                }
            }
            Text {
                attr {
                    marginTop(12f)
                    text("当前 ${StarRatingGeometry.formatRating(ctx.rating)} / $DEMO_MAX_STARS")
                    fontSize(16f)
                    color(Color(0xFF1F2328L))
                }
            }
            Text {
                attr {
                    marginTop(4f)
                    text(ctx.hint)
                    fontSize(14f)
                    color(Color(0xFF667085L))
                }
            }
            View {
                attr {
                    marginTop(16f)
                    flexDirectionRow()
                }
                actionButton(
                    title = { if (ctx.readOnly) "只读：开" else "只读：关" },
                    filled = { ctx.readOnly },
                    marginRight = 8f,
                ) {
                    ctx.readOnly = !ctx.readOnly
                }
                actionButton(
                    title = { if (ctx.allowHalf) "半星：开" else "半星：关" },
                    filled = { ctx.allowHalf },
                    marginRight = 0f,
                ) {
                    ctx.allowHalf = !ctx.allowHalf
                }
            }
            View {
                attr {
                    marginTop(8f)
                    flexDirectionRow()
                }
                actionButton(
                    title = { "清空" },
                    filled = { false },
                    marginRight = 8f,
                ) {
                    ctx.ratingRef?.view?.clear()
                    ctx.hint = "已清空"
                }
                actionButton(
                    title = { "设为 4 分" },
                    filled = { true },
                    marginRight = 0f,
                ) {
                    ctx.ratingRef?.view?.setRating(4f)
                }
            }
        }
    }
}

private fun ViewContainer<*, *>.actionButton(
    title: () -> String,
    filled: () -> Boolean,
    marginRight: Float,
    onClick: () -> Unit,
) {
    View {
        attr {
            flex(1f)
            height(44f)
            marginRight(marginRight)
            borderRadius(8f)
            backgroundColor(if (filled()) Color(0xFF1F6FEBL) else Color(0xFFF2F4F7L))
            allCenter()
        }
        event {
            click {
                onClick()
            }
        }
        Text {
            attr {
                text(title())
                fontSize(15f)
                color(if (filled()) Color.WHITE else Color(0xFF1F2328L))
            }
        }
    }
}
