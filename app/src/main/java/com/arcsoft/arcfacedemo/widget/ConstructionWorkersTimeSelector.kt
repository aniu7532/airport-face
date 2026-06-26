package com.arcsoft.arcfacedemo.widget

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.widget.dialog.DateTimePickerDialogHelper
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * 施工人员时间选择器，继承 [ConstructionWorkersSelector]，
 * 点击弹出日期时间选择框，支持仅日期、仅到小时等展示格式。
 */
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

    /** 清空已选时间。 */
    override fun clear() {
        currentCalendar = null
        super.clear()
    }

    /** 注册时间选择完成后的回调。 */
    fun addOnTimeChangedListener(cb: (calendar: Calendar) -> Unit) {
        onTimeChangedCb = cb
    }

    /** 以 [Calendar] 设置选中时间并格式化展示。 */
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