package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.withStyledAttributes
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.databinding.ActivityConstructionWorkersSelectorBinding
import com.arcsoft.arcfacedemo.widget.dialog.DateTimePickerDialogHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ConstructionWorkersSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    private var onTimeChangedCb: (calendar: Calendar) -> Unit = {}
    private var currentCalendar: Calendar? = null

    private val binding by lazy {
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
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            binding.selector.setOnClickListener {
                DateTimePickerDialogHelper.show(
                    context,
                    currentCalendar
                ) { calendar: Calendar? ->
                    if (calendar == null) return@show
                    currentCalendar = calendar.clone() as Calendar
                    val format = simpleDateFormat.format(currentCalendar!!.time)
                    binding.selectorValue.text = format
                    onTimeChangedCb(currentCalendar!!)
                }
            }
        }
    }

    fun addOnTimeChangedListener(cb: (calendar: Calendar) -> Unit) {
        onTimeChangedCb = cb
    }

    fun clear() {
        currentCalendar = null
        binding.selectorValue.text = ""
    }

}