package com.arcsoft.arcfacedemo.util;

import com.arcsoft.face.ErrorInfo;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ErrorCodeUtilTest {
    @Test
    public void arcFaceErrors_haveChineseReasonsForActivationNetworkAndLicense() {
        assertEquals("成功", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MOK));
        assertEquals("参数无效或为空", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_INVALID_PARAM));
        assertEquals("引擎激活失败", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_ACTIVATION_FAIL));
        assertEquals("引擎已激活", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_ALREADY_ACTIVATED));
        assertEquals("引擎尚未激活", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_NOT_ACTIVATED));
        assertEquals("无法连接服务器", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_NETWORK_COULDNT_RESOLVE_HOST));
        assertEquals("网络连接失败，请检查网络", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_NETWORK_COULDNT_CONNECT_SERVER));
        assertEquals("网络连接超时", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_NETWORK_CONNECT_TIMEOUT));
        assertEquals("授权已过期", ErrorCodeUtil.arcFaceErrorCodeToFieldName(ErrorInfo.MERR_ASF_LICENSE_FILE_EXPIRED));
        assertEquals("未知错误", ErrorCodeUtil.arcFaceErrorCodeToFieldName(-12345));
    }

    @Test
    public void imageErrors_coverAllCurrentSdkValues() {
        String[] messages = {"成功", "图像数据大小不匹配", "不支持当前位图格式", "获取位图信息失败", "图像处理参数为空",
                "源图像与目标图像不能相同", "图像处理区域无效", "不支持当前图像宽高", "不支持当前旋转角度", "不支持当前图像格式"};
        for (int code = 0; code < messages.length; code++) {
            assertEquals("image code " + code, messages[code], ErrorCodeUtil.imageUtilErrorCodeToFieldName(code));
        }
        assertEquals("未知错误", ErrorCodeUtil.imageUtilErrorCodeToFieldName(999999));
    }
}
