package com.kuikly.starrating

/**
 * Compose 侧的命令式入口。
 *
 * 组合还没挂上时，[setRating] / [clear] 先记住最后一次；[bind] 之后补投一次。
 * [unbind] 之后不再调用旧的回调。[rating] 在有待投递的值时返回该值，否则返回当前展示值。
 */
class StarRatingController {
    private var applier: ((Float) -> Unit)? = null
    private var pending: Float? = null
    private var displayed: Float = 0f

    fun rating(): Float = pending ?: displayed

    fun setRating(value: Float) {
        val apply = applier
        if (apply == null) {
            pending = value
            return
        }
        pending = null
        apply(value)
    }

    fun clear() {
        setRating(0f)
    }

    fun bind(applier: (Float) -> Unit) {
        this.applier = applier
        val queued = pending ?: return
        pending = null
        applier(queued)
    }

    fun unbind() {
        applier = null
    }

    fun updateDisplayed(value: Float) {
        displayed = value
    }
}
