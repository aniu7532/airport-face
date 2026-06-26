package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersSelectorBinding

/**
 * 施工人员表单通用选择器：标题 + 可点击取值区域，支持 XML 配置标题与占位提示。
 */
open class ConstructionWorkersSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    protected val binding by lazy {
        ActivityConstructionWorkersSelectorBinding.inflate(
            LayoutInflater.from(
                context
            ), this
        )
    }

    init {
        orientation = VERTICAL
        context.withStyledAttributes(attrs, R.styleable.ConstructionWorkersSelector) {
            val title = getString(R.styleable.ConstructionWorkersSelector_title)
            binding.title.text = title
            val placeholder = getString(R.styleable.ConstructionWorkersSelector_placeholder)
            binding.selectorValue.hint = placeholder
        }
    }

    /** 将点击事件绑定到内部选择区域。 */
    override fun setOnClickListener(l: OnClickListener?) {
        binding.selector.setOnClickListener(l)
    }

    /** 设置当前选中展示文案。 */
    fun setValue(value: String) {
        binding.selectorValue.text = value
    }

    /** 清空已选内容。 */
    open fun clear() {
        binding.selectorValue.text = ""
    }

}