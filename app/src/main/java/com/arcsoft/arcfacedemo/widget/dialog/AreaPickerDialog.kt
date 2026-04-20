package com.arcsoft.arcfacedemo.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.ProgressBar
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.data.http.JsonCallback
import com.arcsoft.arcfacedemo.entity.Area
import com.arcsoft.arcfacedemo.entity.Base
import com.arcsoft.arcfacedemo.network.ApiUtils
import com.arcsoft.arcfacedemo.network.UrlConstants
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response

/**
 * 管制区域树选择器：从 [UrlConstants.checkAreaGetDetailChannelTree] 拉取数据，
 * 支持逐级进入子区域；「选中」始终可选中当前行对应 [Area]（含非叶子节点）。
 *
 * 确定时回调 [onAreaPathSelected]，参数为 **从根到选中节点** 的 [Area] 列表（含所有父节点与当前选中项，至少 1 个元素）。
 */
@SuppressLint("ViewConstructor")
class AreaPickerDialog(
    context: Context,
    private val title: String?,
    private val onAreaPathSelected: (List<Area>) -> Unit,
    private val onCancel: (() -> Unit)? = null
) : CenterPopupView(context) {

    private var currentNodes: List<Area> = emptyList()
    private val stack: ArrayDeque<List<Area>> = ArrayDeque()
    /** 从根到当前层之前，已选中的各级父节点（与 stack 同步） */
    private val pathAncestors: ArrayDeque<Area> = ArrayDeque()
    private val pathLabels: ArrayDeque<String> = ArrayDeque()

    private lateinit var listView: ListView
    private lateinit var progress: ProgressBar
    private lateinit var tvError: AppCompatTextView
    private lateinit var layoutNav: View
    private lateinit var tvBreadcrumb: AppCompatTextView

    private val listAdapter = object : BaseAdapter() {
        override fun getCount(): Int = currentNodes.size
        override fun getItem(position: Int): Area = currentNodes[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.item_area_picker_row, parent, false)
            val area = getItem(position)
            val tvName = row.findViewById<AppCompatTextView>(R.id.tv_name)
            val tvEnter = row.findViewById<AppCompatTextView>(R.id.tv_enter)
            val btnPick = row.findViewById<AppCompatTextView>(R.id.btn_pick)
            tvName.text = area.displayLabel()
            val kids = area.filteredChildren()
            val hasKids = kids.isNotEmpty()
            tvEnter.visibility = if (hasKids) View.VISIBLE else View.GONE
            tvName.setOnClickListener {
                if (hasKids) enterLevel(area, kids) else pickArea(area)
            }
            tvEnter.setOnClickListener {
                if (hasKids) enterLevel(area, kids)
            }
            btnPick.setOnClickListener { pickArea(area) }
            return row
        }
    }

    override fun getImplLayoutId(): Int = R.layout.dialog_area_picker

    override fun onCreate() {
        super.onCreate()
        val tvTitle = findViewById<AppCompatTextView>(R.id.tv_title)
        layoutNav = findViewById(R.id.layout_nav)
        val btnBackLevel = findViewById<AppCompatTextView>(R.id.btn_back_level)
        tvBreadcrumb = findViewById(R.id.tv_breadcrumb)
        listView = findViewById(R.id.list_view)
        progress = findViewById(R.id.progress)
        tvError = findViewById(R.id.tv_error)
        val btnCancel = findViewById<AppCompatButton>(R.id.btn_cancel)

        tvTitle.text = if (title.isNullOrBlank()) "请选择区域" else title
        listView.adapter = listAdapter

        btnBackLevel.setOnClickListener { goBackLevel() }
        btnCancel.setOnClickListener {
            onCancel?.invoke()
            dismiss()
        }

        loadTree()
    }

    override fun onDetachedFromWindow() {
        OkGo.getInstance().cancelTag(UrlConstants.checkAreaGetDetailChannelTree)
        super.onDetachedFromWindow()
    }

    override fun getMaxWidth(): Int =
        (XPopupUtils.getAppWidth(context) * 0.85f).toInt()

    override fun getMaxHeight(): Int =
        (XPopupUtils.getAppWidth(context) * 0.65f).toInt()

    private fun loadTree() {
        showLoading()
        val request = OkGo.get<Base<List<Area?>?>?>(UrlConstants.checkAreaGetDetailChannelTree)
            .tag(UrlConstants.checkAreaGetDetailChannelTree)
        request.params("timestamp", System.currentTimeMillis().toString())
        request.headers("tenant-id", "1")
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken)
        }
        request.execute(object : JsonCallback<Base<List<Area?>?>?>() {
            override fun onSuccess(response: Response<Base<List<Area?>?>?>?) {
                val body = response?.body()
                if (body == null) {
                    showErrorMessage("响应为空")
                    return
                }
                val code = body.code
                if (code != 0 && code != 200) {
                    showErrorMessage(body.msg ?: "请求失败 ($code)")
                    return
                }
                val raw = body.data
                val list = raw?.filterNotNull()?.filter { it.isValidNode() } ?: emptyList()
                if (list.isEmpty()) {
                    showErrorMessage("暂无区域数据")
                    return
                }
                currentNodes = list
                stack.clear()
                pathAncestors.clear()
                pathLabels.clear()
                showList()
                listAdapter.notifyDataSetChanged()
                updateNavUi()
            }

            override fun onError(response: Response<Base<List<Area?>?>?>) {
                val msg = response.exception?.message ?: "网络错误"
                showErrorMessage(msg)
            }
        })
    }

    private fun enterLevel(area: Area, kids: List<Area>) {
        stack.addLast(currentNodes)
        pathAncestors.addLast(area)
        pathLabels.addLast(area.displayLabel())
        currentNodes = kids
        listAdapter.notifyDataSetChanged()
        updateNavUi()
    }

    private fun goBackLevel() {
        if (stack.isEmpty()) return
        currentNodes = stack.removeLast()
        pathAncestors.removeLast()
        pathLabels.removeLast()
        listAdapter.notifyDataSetChanged()
        updateNavUi()
    }

    private fun pickArea(area: Area) {
        val path = pathAncestors.toList() + area
        onAreaPathSelected(path)
        dismiss()
    }

    private fun updateNavUi() {
        layoutNav.visibility = if (stack.isEmpty()) View.GONE else View.VISIBLE
        tvBreadcrumb.text = pathLabels.joinToString(" / ")
    }

    private fun showLoading() {
        progress.visibility = View.VISIBLE
        listView.visibility = View.GONE
        tvError.visibility = View.GONE
    }

    private fun showList() {
        progress.visibility = View.GONE
        tvError.visibility = View.GONE
        listView.visibility = View.VISIBLE
    }

    private fun showErrorMessage(msg: String) {
        progress.visibility = View.GONE
        listView.visibility = View.GONE
        tvError.visibility = View.VISIBLE
        tvError.text = msg
    }

    private fun Area.isValidNode(): Boolean =
        !id.isNullOrBlank() || !name.isNullOrBlank() || !code.isNullOrBlank()

    private fun Area.displayLabel(): String = when {
        !name.isNullOrBlank() -> name
        !code.isNullOrBlank() -> code
        !id.isNullOrBlank() -> id
        else -> "—"
    }

    private fun Area.filteredChildren(): List<Area> {
        val c = children ?: return emptyList()
        return c.filter { it.isValidNode() }
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun show(
            context: Context,
            title: String?,
            onAreaPathSelected: (List<Area>) -> Unit,
            onCancel: (() -> Unit)? = null
        ) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .dismissOnTouchOutside(true)
                .asCustom(
                    AreaPickerDialog(context, title, onAreaPathSelected, onCancel)
                )
                .show()
        }
    }
}
