package com.foowrites.kuikly.starrating.host.adapter

import android.graphics.drawable.Drawable
import com.tencent.kuikly.core.render.android.adapter.HRImageLoadOption
import com.tencent.kuikly.core.render.android.adapter.IKRImageAdapter
import org.json.JSONObject

/** Stub image loader — Star rating demo does not load remote images. */
class KRImageAdapter : IKRImageAdapter {
    override val shouldWaitViewDidLoad: Boolean get() = false

    override fun fetchDrawable(
        imageLoadOption: HRImageLoadOption,
        callback: (Drawable?) -> Unit
    ) {
        callback(null)
    }

    override fun fetchDrawable(
        imageLoadOption: HRImageLoadOption,
        imageParams: JSONObject?,
        callback: (drawable: Drawable?) -> Unit
    ) {
        callback(null)
    }
}
