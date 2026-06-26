package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.arcsoft.arcfacedemo.databinding.FoldViewBinding

/**
 * 展开/收起切换控件，点击后在「展开」「收起」文案与箭头方向间切换。
 */
class FoldView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var isFold = false
    private var onFoldCb: (isFold: Boolean)-> Unit = {}

    private val binding by lazy {
        FoldViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            false
        )
    }

    init {
        addView(binding.root)
        setOnClickListener {
            isFold = !isFold
            binding.img.rotation = if (isFold) 0f else 180f
            binding.tv.text = if (isFold) "展开" else "收起"
            onFoldCb(isFold)
        }
    }

    /** 注册折叠状态变化回调，[isFold] 为 true 表示当前为收起状态。 */
    fun setOnFoldListener(cb: (isFold: Boolean)-> Unit) {
        onFoldCb = cb
    }

}