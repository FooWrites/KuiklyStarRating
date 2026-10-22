package com.kuikly.starrating.demo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.kuikly.starrating.StarRating
import com.kuikly.starrating.StarRatingController
import com.kuikly.starrating.StarRatingGeometry
import com.tencent.kuikly.compose.ComposeContainer
import com.tencent.kuikly.compose.foundation.background
import com.tencent.kuikly.compose.foundation.layout.Column
import com.tencent.kuikly.compose.foundation.layout.Row
import com.tencent.kuikly.compose.foundation.layout.Spacer
import com.tencent.kuikly.compose.foundation.layout.fillMaxSize
import com.tencent.kuikly.compose.foundation.layout.fillMaxWidth
import com.tencent.kuikly.compose.foundation.layout.height
import com.tencent.kuikly.compose.foundation.layout.padding
import com.tencent.kuikly.compose.foundation.layout.width
import com.tencent.kuikly.compose.foundation.shape.RoundedCornerShape
import com.tencent.kuikly.compose.material3.Button
import com.tencent.kuikly.compose.material3.Text
import com.tencent.kuikly.compose.setContent
import com.tencent.kuikly.compose.ui.Modifier
import com.tencent.kuikly.compose.ui.graphics.Color
import com.tencent.kuikly.compose.ui.unit.dp
import com.tencent.kuikly.compose.ui.unit.sp
import com.tencent.kuikly.core.annotations.Page
import com.tencent.kuikly.core.base.Color as KuiklyColor

private const val COMPOSE_MAX_STARS = 5
private const val COMPOSE_STAR_SIZE = 36f
private const val COMPOSE_STAR_SPACING = 8f

@Page("star_rating_compose_demo")
class StarRatingComposeDemoPage : ComposeContainer() {
    override fun willInit() {
        super.willInit()
        setContent {
            StarRatingComposeDemo()
        }
    }
}

@Composable
private fun StarRatingComposeDemo() {
    val controller = remember { StarRatingController() }
    var rating by remember { mutableStateOf(0f) }
    var readOnly by remember { mutableStateOf(false) }
    var allowHalf by remember { mutableStateOf(true) }
    var hint by remember { mutableStateOf("尚未评分") }
    val ratingWidth = StarRatingGeometry.contentWidth(
        COMPOSE_MAX_STARS,
        COMPOSE_STAR_SIZE,
        COMPOSE_STAR_SPACING,
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(text = "星级评分", fontSize = 20.sp, color = Color(0xFF1F2328))
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Compose DSL。点按或拖动星星打分。",
            fontSize = 14.sp,
            color = Color(0xFF667085),
        )
        Spacer(modifier = Modifier.height(20.dp))
        StarRating(
            modifier = Modifier
                .width(ratingWidth.dp)
                .height(COMPOSE_STAR_SIZE.dp),
            maxStars = COMPOSE_MAX_STARS,
            rating = rating,
            allowHalf = allowHalf,
            readOnly = readOnly,
            starSize = COMPOSE_STAR_SIZE,
            starSpacing = COMPOSE_STAR_SPACING,
            activeColor = KuiklyColor(0xFFF5B400L),
            inactiveColor = KuiklyColor(0xFFD8DDE6L),
            onRate = { value ->
                rating = value
                hint = "当前 ${StarRatingGeometry.formatRating(value)} 分"
            },
            controller = controller,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "当前 ${StarRatingGeometry.formatRating(rating)} / $COMPOSE_MAX_STARS",
            fontSize = 16.sp,
            color = Color(0xFF1F2328),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = hint, fontSize = 14.sp, color = Color(0xFF667085))
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DemoButton(
                title = if (readOnly) "只读：开" else "只读：关",
                filled = readOnly,
                modifier = Modifier.weight(1f),
                onClick = { readOnly = !readOnly },
            )
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton(
                title = if (allowHalf) "半星：开" else "半星：关",
                filled = allowHalf,
                modifier = Modifier.weight(1f),
                onClick = { allowHalf = !allowHalf },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DemoButton(
                title = "清空",
                filled = false,
                modifier = Modifier.weight(1f),
                onClick = {
                    controller.clear()
                    hint = "已清空"
                },
            )
            Spacer(modifier = Modifier.width(8.dp))
            DemoButton(
                title = "设为 4 分",
                filled = true,
                modifier = Modifier.weight(1f),
                onClick = { controller.setRating(4f) },
            )
        }
    }
}

@Composable
private fun DemoButton(
    title: String,
    filled: Boolean,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val background = if (filled) Color(0xFF1F6FEB) else Color(0xFFF2F4F7)
    val textColor = if (filled) Color.White else Color(0xFF1F2328)
    Button(
        onClick = onClick,
        modifier = modifier
            .height(44.dp)
            .background(background, RoundedCornerShape(8.dp)),
    ) {
        Text(text = title, fontSize = 15.sp, color = textColor)
    }
}
