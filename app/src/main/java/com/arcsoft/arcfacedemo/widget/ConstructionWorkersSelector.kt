package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersSelectorBinding

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

    override fun setOnClickListener(l: OnClickListener?) {
        binding.selector.setOnClickListener(l)
    }

    fun setValue(value: String) {
        binding.selectorValue.text = value
    }

    open fun clear() {
        binding.selectorValue.text = ""
    }

}