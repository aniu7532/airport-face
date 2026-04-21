package com.arcsoft.arcfacedemo.util

import com.blankj.utilcode.util.SPUtils
import java.util.concurrent.CopyOnWriteArraySet

/**
 * 核销功能：总开关 [isVerifyFeatureEnabled] 为 true 时，新存通行记录 [needVerify] 应为 true。
 * 核验表单各字段是否必填由下方开关控制（仅总开关开启时生效）。
 */
object VerifyFeatureSettings {

    const val KEY_VERIFY_FEATURE_ENABLED = "verify_feature_enabled"
    const val KEY_REQUIRE_PASSAGE = "verify_required_passage"
    const val KEY_REQUIRE_PASS_TIME = "verify_required_pass_time"
    const val KEY_REQUIRE_DEVICE = "verify_required_device"
    const val KEY_REQUIRE_REMARK = "verify_required_remark"
    private val needVerifyChangeListeners = CopyOnWriteArraySet<NeedVerifyChangeListener>()

    fun interface NeedVerifyChangeListener {
        fun onNeedVerifyChanged(needVerify: Boolean)
    }

    @JvmStatic
    fun isVerifyFeatureEnabled(): Boolean =
        SPUtils.getInstance().getBoolean(KEY_VERIFY_FEATURE_ENABLED, false)

    @JvmStatic
    fun setVerifyFeatureEnabled(enabled: Boolean) {
        val old = isVerifyFeatureEnabled()
        SPUtils.getInstance().put(KEY_VERIFY_FEATURE_ENABLED, enabled)
        if (old != enabled) {
            notifyNeedVerifyChanged(enabled)
        }
    }

    @JvmStatic
    fun isRequirePassage(): Boolean =
        SPUtils.getInstance().getBoolean(KEY_REQUIRE_PASSAGE, false)

    @JvmStatic
    fun isRequirePassTime(): Boolean =
        SPUtils.getInstance().getBoolean(KEY_REQUIRE_PASS_TIME, false)

    @JvmStatic
    fun isRequireDevice(): Boolean =
        SPUtils.getInstance().getBoolean(KEY_REQUIRE_DEVICE, false)

    @JvmStatic
    fun isRequireRemark(): Boolean =
        SPUtils.getInstance().getBoolean(KEY_REQUIRE_REMARK, false)

    /** 新存通行记录是否标记为需核销（与总开关一致） */
    @JvmStatic
    fun needVerifyForNewRecord(): Boolean = isVerifyFeatureEnabled()

    @JvmStatic
    fun addNeedVerifyChangeListener(listener: NeedVerifyChangeListener) {
        needVerifyChangeListeners.add(listener)
    }

    @JvmStatic
    fun removeNeedVerifyChangeListener(listener: NeedVerifyChangeListener) {
        needVerifyChangeListeners.remove(listener)
    }

    private fun notifyNeedVerifyChanged(needVerify: Boolean) {
        needVerifyChangeListeners.forEach { it.onNeedVerifyChanged(needVerify) }
    }
}
