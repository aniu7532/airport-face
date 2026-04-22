package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.widget.dialog.DateTimePickerDialogHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ConstructionWorkersTimeSelector @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstructionWorkersSelector(context, attrs, defStyleAttr) {

    private var onTimeChangedCb: (calendar: Calendar) -> Unit = {}

    private var currentCalendar: Calendar? = null

    private var withoutHMS: Boolean = false

    private val format: String
        get() = if (withoutHMS) "yyyy-MM-dd" else "yyyy-MM-dd HH:mm:ss"

    private lateinit var simpleDateFormat: SimpleDateFormat

    init {
        context.withStyledAttributes(attrs, R.styleable.ConstructionWorkersTimeSelector) {
            withoutHMS = getBoolean(R.styleable.ConstructionWorkersTimeSelector_withoutHMS, false)
            simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
            setOnClickListener {
                DateTimePickerDialogHelper.show(
                    context,
                    currentCalendar,
                    withoutHMS
                ) { calendar: Calendar? ->
                    if (calendar == null) return@show
                    setValue(calendar)
                    onTimeChangedCb(currentCalendar!!)
                }
            }
        }
    }

    override fun clear() {
        currentCalendar = null
        super.clear()
    }

    fun addOnTimeChangedListener(cb: (calendar: Calendar) -> Unit) {
        onTimeChangedCb = cb
    }

    fun setValue(calendar: Calendar) {
        if (calendar.time.time == currentCalendar?.time?.time) { return }
        currentCalendar = calendar.clone() as Calendar
        val format = simpleDateFormat.format(currentCalendar!!.time)
        setValue(format)
    }


}