package com.foowrites.kuikly.starrating.host.adapter

import android.app.Activity
import android.content.Context
import com.foowrites.kuikly.starrating.host.KuiklyRenderActivity
import com.tencent.kuikly.core.render.android.adapter.IKRRouterAdapter
import org.json.JSONObject

class KRRouterAdapter : IKRRouterAdapter {
    override fun openPage(context: Context, pageName: String, pageData: JSONObject) {
        KuiklyRenderActivity.start(context, pageName, pageData)
    }

    override fun closePage(context: Context) {
        (context as? Activity)?.finish()
    }
}
