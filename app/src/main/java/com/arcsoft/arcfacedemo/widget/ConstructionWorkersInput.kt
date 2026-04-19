package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersInputBinding
import androidx.core.content.withStyledAttributes

class ConstructionWorkersInput @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var onTextChangedCb: (text: String) -> Unit = {}

    private val binding by lazy {
        ActivityConstructionWorkersInputBinding.inflate(
            LayoutInflater.from(
                context
            ), this
        )
    }

    init {
        orientation = VERTICAL
        context.withStyledAttributes(attrs, R.styleable.ConstructionWorkersInput) {
            val title = getString(R.styleable.ConstructionWorkersInput_title)
            binding.title.text = title
            binding.input.addTextChangedListener(object : TextWatcher {

                override fun afterTextChanged(p0: Editable?) {
                }

                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                    onTextChangedCb(p0.toString())
                }

            })
        }
    }

    fun addTextChangedListener(cb: (text: String) -> Unit) {
        onTextChangedCb = cb
    }

    fun clear() {
        binding.input.text?.clear()
    }

}