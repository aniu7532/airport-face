package com.arcsoft.arcfacedemo.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.arcsoft.arcfacedemo.R
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils

/**
 * 字符串列表弹窗选择器，传入标题与字符串数组（或列表），点击某项回调下标与文案并关闭。
 */
@SuppressLint("ViewConstructor")
class StringListPickerDialog(
    context: Context,
    private val title: String?,
    private val items: List<String>,
    private val onItemSelected: (index: Int, text: String) -> Unit,
    private val onCancel: (() -> Unit)? = null
) : CenterPopupView(context) {

    constructor(
        context: Context,
        title: String?,
        items: Array<out String>,
        onItemSelected: (index: Int, text: String) -> Unit,
        onCancel: (() -> Unit)? = null
    ) : this(context, title, items.asList(), onItemSelected, onCancel)

    override fun getImplLayoutId(): Int = R.layout.dialog_string_list_picker

    override fun onCreate() {
        super.onCreate()
        val tvTitle = findViewById<AppCompatTextView>(R.id.tv_title)
        val listView = findViewById<ListView>(R.id.list_view)
        val btnCancel = findViewById<AppCompatButton>(R.id.btn_cancel)

        tvTitle.text = if (title.isNullOrBlank()) "请选择" else title

        val adapter = ArrayAdapter(
            context,
            R.layout.item_string_picker_row,
            R.id.tv_text,
            items
        )
        listView.adapter = adapter
        listView.setOnItemClickListener { _, _, position, _ ->
            onItemSelected(position, items[position])
            dismiss()
        }

        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }
    }

    override fun getMaxWidth(): Int =
        (XPopupUtils.getAppWidth(context) * 0.85f).toInt()

    override fun getMaxHeight() =  (XPopupUtils.getAppWidth(context) * 0.5f).toInt()

    companion object {
        @JvmStatic
        @JvmOverloads
        fun show(
            context: Context,
            title: String?,
            items: Array<out String>,
            onItemSelected: (index: Int, text: String) -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .dismissOnTouchOutside(true)
                .asCustom(
                    StringListPickerDialog(
                        context,
                        title,
                        items,
                        onItemSelected,
                        onCancel
                    )
                )
                .show()
        }

        @JvmStatic
        @JvmOverloads
        fun show(
            context: Context,
            title: String?,
            items: List<String>,
            onItemSelected: (index: Int, text: String) -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .dismissOnTouchOutside(true)
                .asCustom(
                    StringListPickerDialog(
                        context,
                        title,
                        items,
                        onItemSelected,
                        onCancel
                    )
                )
                .show()
        }
    }
}
