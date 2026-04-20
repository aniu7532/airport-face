package com.arcsoft.arcfacedemo.widget.dialog

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import com.arcsoft.arcfacedemo.R
import com.arcsoft.arcfacedemo.util.VerifyFeatureSettings
import com.blankj.utilcode.util.SPUtils
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.core.CenterPopupView
import com.lxj.xpopup.util.XPopupUtils

@SuppressLint("ViewConstructor")
class VerifyFeatureSettingsDialog(context: Context) : CenterPopupView(context) {

    override fun getImplLayoutId(): Int = R.layout.dialog_verify_feature_settings

    override fun onCreate() {
        super.onCreate()
        val sp = SPUtils.getInstance()
        val switchMaster = findViewById<SwitchCompat>(R.id.switch_verify_master)
        val switchPassage = findViewById<SwitchCompat>(R.id.switch_require_passage)
        val switchTime = findViewById<SwitchCompat>(R.id.switch_require_time)
        val switchDevice = findViewById<SwitchCompat>(R.id.switch_require_device)
        val switchRemark = findViewById<SwitchCompat>(R.id.switch_require_remark)
        val group = findViewById<View>(R.id.layout_require_group)
        val btnClose = findViewById<AppCompatButton>(R.id.btn_close)

        fun load() {
            switchMaster.isChecked = sp.getBoolean(VerifyFeatureSettings.KEY_VERIFY_FEATURE_ENABLED, false)
            switchPassage.isChecked = sp.getBoolean(VerifyFeatureSettings.KEY_REQUIRE_PASSAGE, false)
            switchTime.isChecked = sp.getBoolean(VerifyFeatureSettings.KEY_REQUIRE_PASS_TIME, false)
            switchDevice.isChecked = sp.getBoolean(VerifyFeatureSettings.KEY_REQUIRE_DEVICE, false)
            switchRemark.isChecked = sp.getBoolean(VerifyFeatureSettings.KEY_REQUIRE_REMARK, false)
            updateSubEnabled(switchMaster.isChecked, group, switchPassage, switchTime, switchDevice, switchRemark)
        }

        load()

        switchMaster.setOnCheckedChangeListener { _, checked ->
            sp.put(VerifyFeatureSettings.KEY_VERIFY_FEATURE_ENABLED, checked)
            updateSubEnabled(checked, group, switchPassage, switchTime, switchDevice, switchRemark)
        }
        switchPassage.setOnCheckedChangeListener { _, v ->
            sp.put(VerifyFeatureSettings.KEY_REQUIRE_PASSAGE, v)
        }
        switchTime.setOnCheckedChangeListener { _, v ->
            sp.put(VerifyFeatureSettings.KEY_REQUIRE_PASS_TIME, v)
        }
        switchDevice.setOnCheckedChangeListener { _, v ->
            sp.put(VerifyFeatureSettings.KEY_REQUIRE_DEVICE, v)
        }
        switchRemark.setOnCheckedChangeListener { _, v ->
            sp.put(VerifyFeatureSettings.KEY_REQUIRE_REMARK, v)
        }

        btnClose.setOnClickListener { dismiss() }
    }

    private fun updateSubEnabled(
        masterOn: Boolean,
        group: View,
        vararg switches: SwitchCompat
    ) {
        group.alpha = if (masterOn) 1f else 0.45f
        switches.forEach { it.isEnabled = masterOn }
    }

    override fun getMaxWidth(): Int =
        (XPopupUtils.getAppWidth(context) * 0.88f).toInt()

    companion object {
        @JvmStatic
        fun show(context: Context) {
            XPopup.Builder(context)
                .isDestroyOnDismiss(true)
                .dismissOnTouchOutside(true)
                .asCustom(VerifyFeatureSettingsDialog(context))
                .show()
        }
    }
}
