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

    private var hourOnly: Boolean = false

    /** hourOnly 且为结束时间时，对齐到该小时 59:59 */
    private var snapToEndOfHour: Boolean = false

    private val format: String
        get() = when {
            withoutHMS -> "yyyy-MM-dd"
            hourOnly -> "yyyy-MM-dd HH"
            else -> "yyyy-MM-dd HH:mm:ss"
        }

    private lateinit var simpleDateFormat: SimpleDateFormat

    init {
        context.withStyledAttributes(attrs, R.styleable.ConstructionWorkersTimeSelector) {
            withoutHMS = getBoolean(R.styleable.ConstructionWorkersTimeSelector_withoutHMS, false)
            hourOnly = getBoolean(R.styleable.ConstructionWorkersTimeSelector_hourOnly, false)
            snapToEndOfHour = getBoolean(
                R.styleable.ConstructionWorkersTimeSelector_snapToEndOfHour,
                false,
            )
            simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
            setOnClickListener {
                DateTimePickerDialogHelper.show(
                    context,
                    currentCalendar,
                    withoutHMS,
                    hourOnly,
                ) { calendar: Calendar? ->
                    if (calendar == null) return@show
                    setValue(normalizePickedTime(calendar))
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
        val normalized = normalizePickedTime(calendar)
        if (normalized.timeInMillis == currentCalendar?.timeInMillis) return
        currentCalendar = normalized
        setValue(simpleDateFormat.format(currentCalendar!!.time))
    }

    private fun normalizePickedTime(calendar: Calendar): Calendar {
        val cal = calendar.clone() as Calendar
        if (!hourOnly || withoutHMS) return cal
        if (snapToEndOfHour) {
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
        } else {
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
        }
        return cal
    }
}