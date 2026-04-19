package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
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

    init {
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        setOnClickListener {
            DateTimePickerDialogHelper.show(
                context,
                currentCalendar
            ) { calendar: Calendar? ->
                if (calendar == null) return@show
                currentCalendar = calendar.clone() as Calendar
                val format = simpleDateFormat.format(currentCalendar!!.time)
                setValue(format)
                onTimeChangedCb(currentCalendar!!)
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


}