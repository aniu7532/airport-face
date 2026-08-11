package com.arcsoft.arcfacedemo.util;

import android.text.TextUtils;

import com.arcsoft.arcfacedemo.db.entity.LongTermPass;

/**
 * 通行异常原因：证件状态异常优先于「人证不匹配」，避免误报。
 */
public final class PassAbnormalReasonUtil {

    public static final String FACE_MISMATCH = "人证不匹配";

    private PassAbnormalReasonUtil() {
    }

    /**
     * @param status 1 正常 / 2 注销 / 3 过期 / 4 挂失 / 5 停用
     */
    public static String cardStatusText(int status) {
        switch (status) {
            case 1:
                return "正常";
            case 2:
                return "注销";
            case 3:
                return "过期";
            case 4:
                return "挂失";
            case 5:
                return "停用";
            default:
                return "异常";
        }
    }

    public static String forCardStatus(int status) {
        return "证件已" + cardStatusText(status);
    }

    /**
     * 解析通行失败原因：证件非正常状态时优先返回证件状态文案。
     *
     * @param pass            通行证，可为 null
     * @param preferredReason 业务侧指定原因（如区域权限、黑名单）；证件状态异常时会被覆盖
     */
    public static String resolve(LongTermPass pass, String preferredReason) {
        if (pass != null && pass.status != 1) {
            return forCardStatus(pass.status);
        }
        if (!TextUtils.isEmpty(preferredReason)) {
            return preferredReason;
        }
        return FACE_MISMATCH;
    }

    public static String resolve(LongTermPass pass) {
        return resolve(pass, null);
    }
}
