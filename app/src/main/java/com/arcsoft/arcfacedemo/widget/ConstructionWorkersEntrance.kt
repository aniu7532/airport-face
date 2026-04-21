package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.ui.activity.ConstructionWorkersActivity
import com.arcsoft.arcfacedemo.util.VerifyFeatureSettings

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

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        VerifyFeatureSettings.addNeedVerifyChangeListener(needVerifyListener)
        updateVisibleState(VerifyFeatureSettings.needVerifyForNewRecord())
    }

    override fun onDetachedFromWindow() {
        VerifyFeatureSettings.removeNeedVerifyChangeListener(needVerifyListener)
        super.onDetachedFromWindow()
    }

    private fun updateVisibleState(needVerify: Boolean) {
        visibility = if (needVerify) View.VISIBLE else View.GONE
    }
}