package com.arcsoft.arcfacedemo.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.data.http.JsonCallback
import com.arcsoft.arcfacedemo.entity.Area
import com.arcsoft.arcfacedemo.entity.Base
import com.arcsoft.arcfacedemo.entity.CardRecords
import com.arcsoft.arcfacedemo.entity.DeviceResult
import com.arcsoft.arcfacedemo.network.ApiUtils
import com.arcsoft.arcfacedemo.network.UrlConstants
import com.arcsoft.arcfacedemo.widget.ConstructionWorkersSelector
import com.arcsoft.arcfacedemo.widget.ConstructionWorkersTimeSelector
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import android.widget.Toast
import com.blankj.utilcode.util.GsonUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@SuppressLint("ViewConstructor")
class VerifyAndConfirmDialog(@JvmField val context: Context, val result: CardRecords.ListDTO, val successCb: ()-> Unit) :
    CenterPopupView(context) {

    private lateinit var etMark: AppCompatEditText
    private lateinit var selectorDeviceCode: ConstructionWorkersSelector
    private var devices = emptyList<DeviceResult>()
    private var device: DeviceResult? = null
    private var calendar: Calendar? = null
    /** 自根节点至选中节点的完整路径（含父级与当前选中项） */
    private var selectedAreaPath: List<Area> = emptyList()

    override fun getImplLayoutId(): Int {
        return R.layout.verify_and_confirm_dialog
    }

    override fun onCreate() {
        super.onCreate()
        val tvTitle = findViewById<AppCompatTextView>(R.id.tv_title)
        val selectorArea = findViewById<ConstructionWorkersSelector>(R.id.selector_area)
        val selectorTime = findViewById<ConstructionWorkersTimeSelector>(R.id.selector_time)
        selectorDeviceCode = findViewById(R.id.selector_device_code)
        etMark = findViewById(R.id.et_mark)
        val btnCancel = findViewById<AppCompatButton>(R.id.btn_cancel)
        val btnSure = findViewById<AppCompatButton>(R.id.btn_sure)

        tvTitle.text = "确认将 ${result.nickname} 标记为“已核实”吗？可补充核实备注，备注为非必填。"

        selectorTime.addOnTimeChangedListener {
            calendar = it
        }

        selectorDeviceCode.setOnClickListener {
            showDeviceCodeDialog()
        }

        selectorArea.setOnClickListener {
            AreaPickerDialog.show(context, "请选择通行区域", { path ->
                selectedAreaPath = path
                selectorArea.setValue(
                    path.joinToString(" / ") { formatAreaLabel(it) }
                )
            })
        }

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSure.setOnClickListener {
            submit()
        }

    }

    fun showDeviceCodeDialog() {
        if (devices.isEmpty()) {
            getDevices()
            return
        }
        StringListPickerDialog.show(
            context,
            "标题",
            devices.mapNotNull { it.name }.toList(),
            { index: Int?, text: String? ->
                if (index == null) return@show
                device = devices[index]
                selectorDeviceCode.setValue(device?.name ?: "")
            },
            null
        )
    }

    private fun formatAreaLabel(area: Area): String {
        val n = area.name
        if (!n.isNullOrBlank()) return n
        val c = area.code
        if (!c.isNullOrBlank()) return c
        return area.id.orEmpty()
    }

    private fun getDevices() {
        val popupView = XPopup.Builder(activity).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
            .asCustom(LoadingPopDialog(activity, "初始化中，请稍后......")).show()
        val request =
            OkGo.get<Base<List<DeviceResult?>?>?>(UrlConstants.checkDeviceList)
                .tag(UrlConstants.checkDeviceList)
        request.headers("tenant-id", "1")
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken)
        }
        request.execute(object : JsonCallback<Base<List<DeviceResult?>?>?>() {
            override fun onSuccess(response: Response<Base<List<DeviceResult?>?>?>?) {
                devices = response?.body()?.data?.filterNotNull() ?: emptyList()
                popupView.dismiss()
                showDeviceCodeDialog()
            }

            override fun onError(response: Response<Base<List<DeviceResult?>?>?>) {
                popupView.dismiss()
            }
        })
    }

    private fun submit() {
        val recordId = result.id?.trim().orEmpty()
        if (recordId.isEmpty()) {
            Toast.makeText(context, "记录 id 无效，无法提交", Toast.LENGTH_SHORT).show()
            return
        }
        val popupView = XPopup.Builder(activity).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
            .asCustom(LoadingPopDialog(activity, "提交中，请稍后......")).show()
        val request =
            OkGo.put<Base<Boolean?>?>(UrlConstants.checkRecordVerify)
                .tag(UrlConstants.checkRecordVerify)
        request.headers("tenant-id", "1")
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken)
        }
        val body = LinkedHashMap<String, Any>()
        body["id"] = recordId
        etMark.text?.toString()?.trim()?.takeIf { it.isNotEmpty() }?.let { body["verifyRemark"] = it }
        device?.id?.trim()?.takeIf { it.isNotEmpty() }?.let { body["deviceId"] = it }
        device?.name?.trim()?.takeIf { it.isNotEmpty() }?.let { body["deviceName"] = it }
        formatCheckTime(calendar)?.let { body["checkTime"] = it }
        val selectedArea = selectedAreaPath.lastOrNull()
        selectedArea?.id?.trim()?.takeIf { it.isNotEmpty() }?.let { body["area"] = it }
        formatAreaNameForApi(selectedAreaPath)?.trim()?.takeIf { it.isNotEmpty() }?.let {
            body["areaName"] = it
        }
        request.upJson(GsonUtils.toJson(body))
        request.execute(object : JsonCallback<Base<Boolean?>?>() {
            override fun onSuccess(response: Response<Base<Boolean?>?>?) {
                println(response)
                popupView.dismiss()
                dismiss()
                successCb()
            }

            override fun onError(response: Response<Base<Boolean?>?>) {
                popupView.dismiss()
                dismiss()
            }
        })
    }

    /** 与 [ConstructionWorkersTimeSelector] 展示一致：yyyy-MM-dd HH:mm:ss */
    private fun formatCheckTime(cal: Calendar?): String? {
        if (cal == null) return null
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(cal.time)
    }

    /** 整段路径的名称（每层「编码 名称」，层间用 /），末级仍含编码要求 */
    private fun formatAreaNameForApi(path: List<Area>): String? {
        if (path.isEmpty()) return null
        val segments = path.mapNotNull { segmentNameWithCode(it) }
        if (segments.isEmpty()) return null
        return segments.joinToString("")
    }

    private fun segmentNameWithCode(area: Area): String? {
        val name = area.name?.trim().orEmpty()
        val code = area.code?.trim().orEmpty()
        return when {
            name.isNotEmpty() && code.isNotEmpty() -> "$code$name"
            name.isNotEmpty() -> name
            code.isNotEmpty() -> code
            else -> area.id?.trim()?.takeIf { it.isNotEmpty() }
        }
    }

}