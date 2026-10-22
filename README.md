# KuiklyStarRating

Kuikly 星级评分，用于商品评价和内容打分。自研 DSL 与 Compose DSL 都能用。

作者：[@FooWrites](https://github.com/FooWrites)

## 效果

<table>
  <tr>
    <td><img src="docs/screenshots/starrating-empty.png" alt="零分" width="280" /></td>
    <td><img src="docs/screenshots/starrating-rated.png" alt="已评分" width="280" /></td>
  </tr>
  <tr>
    <td align="center">零分</td>
    <td align="center">已评分</td>
  </tr>
</table>

## 安装

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

自研 DSL：

```kotlin
implementation("com.github.FooWrites.KuiklyStarRating:starrating:<tag>")
```

Compose DSL：

```kotlin
implementation("com.github.FooWrites.KuiklyStarRating:starrating-compose:<tag>")
```

把 `<tag>` 换成发布标签。`starrating-compose` 已传递依赖 `starrating`。本库基于 Kotlin 2.1.21 / Kuikly 2.4.2-2.0.21 构建，不发布到 Maven Central。

JitPack 只发布 `:starrating` 与 `:starrating-compose`。`:demo` 和 `:androidApp` 不在发布坐标里。

## 模块

| 模块 | 发布 | 说明 |
| --- | --- | --- |
| `:starrating` | 是 | 自研 DSL |
| `:starrating-compose` | 是 | Compose DSL，包装同一个 `StarRatingView` |
| `:demo` | 否 | 示例页 `star_rating_demo`、`star_rating_compose_demo` |
| `:androidApp` | 否 | 本地 Android 示例宿主，默认打开 `star_rating_demo` |

## 自研 DSL

```kotlin
StarRating {
    ref { ratingRef = it }
    attr {
        maxStars(5)
        rating(0f)
        allowHalf(true)
        readOnly(false)
        starSize(28f)
        starSpacing(6f)
        activeColor(Color(0xFFF5B400L))
        inactiveColor(Color(0xFFD8DDE6L))
    }
    event {
        onRate { value ->
            // 用户点按或拖动后的评分，半星时为 0.5 的倍数
        }
    }
}

ratingRef.view?.setRating(4f)
ratingRef.view?.clear()
```

颜色用 `com.tencent.kuikly.core.base.Color`。默认 5 颗星、星宽 28、间距 6，组件会按星数撑开宽高。`readOnly(true)` 只展示，不响应触摸。`allowHalf(false)` 时取整星。滑出左边界为 0 分。

## Compose DSL

```kotlin
val controller = remember { StarRatingController() }
var rating by remember { mutableStateOf(0f) }

StarRating(
    modifier = Modifier.width(164.dp).height(28.dp),
    maxStars = 5,
    rating = rating,
    allowHalf = true,
    readOnly = false,
    starSize = 28f,
    starSpacing = 6f,
    onRate = { value -> rating = value },
    controller = controller,
)

controller.setRating(4f)
controller.clear()
```

`StarRating` 通过 `MakeKuiklyComposeNode` 挂上同一个 `StarRatingView`，绘制逻辑不另写一份。

## sDynamic

绘制全部在 `commonMain`：用 Kuikly 声明式 `View` / `Text` 排 ★，没有 Android / iOS 原生 View，也没有 CocoaPods 依赖。可以按 sDynamic 组件接入。

## 示例

`:demo` 两页：

- `star_rating_demo`：自研 DSL，从 0 分开始，可切换只读、半星，清空或设为 4 分
- `star_rating_compose_demo`：Compose DSL

本地 Android 宿主（不发布）：

```bash
./gradlew :androidApp:assembleDebug
```

`applicationId` 为 `com.foowrites.kuikly.starrating.host`，启动页是 `star_rating_demo`。需要 JDK 17 和 Android SDK 34。

## 许可

Apache License 2.0

Copyright (c) 2026 FooWrites
