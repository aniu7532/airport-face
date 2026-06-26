package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.ui.activity.ConstructionWorkersActivity
import com.arcsoft.arcfacedemo.util.VerifyFeatureSettings

/**
 * 施工人员功能入口图标，根据「新记录需核实」开关动态显示或隐藏。
 */
class ConstructionWorkersEntrance @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {
    private val needVerifyListener = VerifyFeatureSettings.NeedVerifyChangeListener { needVerify ->
        updateVisibleState(needVerify)
    }

    init {
        setImageResource(R.mipmap.icon_construction_workers_entrance)
        setOnClickListener {
            context.startActivity(Intent(context, ConstructionWorkersActivity::class.java))
        }
        updateVisibleState(VerifyFeatureSettings.needVerifyForNewRecord())
    }

    /** 订阅核实开关变化，同步更新可见性。 */
    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        VerifyFeatureSettings.addNeedVerifyChangeListener(needVerifyListener)
        updateVisibleState(VerifyFeatureSettings.needVerifyForNewRecord())
    }

    /** 取消订阅，避免泄漏。 */
    override fun onDetachedFromWindow() {
        VerifyFeatureSettings.removeNeedVerifyChangeListener(needVerifyListener)
        super.onDetachedFromWindow()
    }

    private fun updateVisibleState(needVerify: Boolean) {
        visibility = if (needVerify) View.VISIBLE else View.GONE
    }
}