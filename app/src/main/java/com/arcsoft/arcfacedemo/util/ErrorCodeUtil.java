package com.arcsoft.arcfacedemo.util;

import com.arcsoft.face.ErrorInfo;
import com.arcsoft.imageutil.ArcSoftImageUtilError;

/** ArcFace / ArcSoft 错误码的中文说明，保留原方法名兼容现有调用。 */
public class ErrorCodeUtil {
    public static String arcFaceErrorCodeToFieldName(int code) {
        switch (code) {
            case ErrorInfo.MOK: return "成功";
            case ErrorInfo.MERR_INVALID_PARAM:
            case ErrorInfo.MERR_ASF_PARAM_NULL: return "参数无效或为空";
            case ErrorInfo.MERR_UNSUPPORTED: return "不支持此操作";
            case ErrorInfo.MERR_NO_MEMORY: return "内存不足";
            case ErrorInfo.MERR_BAD_STATE: return "引擎状态异常";
            case ErrorInfo.MERR_USER_CANCEL: return "操作已取消";
            case ErrorInfo.MERR_EXPIRED:
            case ErrorInfo.MERR_ASF_ACTIVEKEY_EXPIRED:
            case ErrorInfo.MERR_ASF_LICENSE_FILE_EXPIRED: return "授权已过期";
            case ErrorInfo.MERR_NO_DISKSPACE: return "存储空间不足";
            case ErrorInfo.MERR_FSDK_INVALID_APP_ID: return "应用标识无效";
            case ErrorInfo.MERR_FSDK_INVALID_SDK_ID: return "SDK 密钥无效";
            case ErrorInfo.MERR_FSDK_INVALID_ID_PAIR:
            case ErrorInfo.MERR_FSDK_MISMATCH_ID_AND_SDK:
            case ErrorInfo.MERR_ASF_APPID_APPKEY_SDK_MISMATCH: return "应用标识与 SDK 密钥不匹配";
            case ErrorInfo.MERR_FSDK_SYSTEM_VERSION_UNSUPPORTED:
            case ErrorInfo.MERR_ASF_VERSION_NOT_SUPPORT: return "不支持当前系统或引擎版本";
            case ErrorInfo.MERR_FSDK_FR_INVALID_IMAGE_INFO:
            case ErrorInfo.MERR_ASF_EX_INVALID_IMAGE_INFO: return "图像信息无效";
            case ErrorInfo.MERR_FSDK_FR_INVALID_FACE_INFO:
            case ErrorInfo.MERR_ASF_EX_INVALID_FACE_INFO: return "人脸信息无效";
            case ErrorInfo.MERR_FSDK_FR_MISMATCHED_FEATURE_LEVEL:
            case ErrorInfo.MERR_FSDK_FACEFEATURE_INVALID_FORMAT: return "人脸特征格式不匹配";
            case ErrorInfo.MERR_FSDK_FACEFEATURE_LOW_CONFIDENCE_LEVEL:
            case ErrorInfo.MERR_ASF_SEARCH_LOW_CONFIDENCE: return "人脸质量过低，请重新采集";
            case ErrorInfo.MERR_FSDK_FACEFEATURE_MISSFACE:
            case ErrorInfo.MERR_FSDK_FACEFEATURE_NO_FACE: return "未检测到人脸";
            case ErrorInfo.MERR_ASF_EX_FEATURE_UNINITED: return "检测功能尚未初始化";
            case ErrorInfo.MERR_ASF_EX_FEATURE_UNPROCESSED: return "尚未完成人脸检测";
            case ErrorInfo.MERR_ASF_EX_FEATURE_UNSUPPORTED_ON_INIT:
            case ErrorInfo.MERR_ASF_EX_FEATURE_UNSUPPORTED_ON_PROCESS: return "不支持当前检测功能";
            case ErrorInfo.MERR_ASF_ACTIVATION_FAIL: return "引擎激活失败";
            case ErrorInfo.MERR_ASF_ALREADY_ACTIVATED: return "引擎已激活";
            case ErrorInfo.MERR_ASF_NOT_ACTIVATED: return "引擎尚未激活";
            case ErrorInfo.MERR_ASF_DEVICE_MISMATCH: return "授权与当前设备不匹配";
            case ErrorInfo.MERR_ASF_ACTIVEFILE_SDKTYPE_MISMATCH:
            case ErrorInfo.MERR_ASF_ACTIVEFILE_SDK_MISMATCH:
            case ErrorInfo.MERR_ASF_ACTIVEKEY_SDK_FILE_MISMATCH: return "激活文件与 SDK 不匹配";
            case ErrorInfo.MERR_ASF_COLOR_SPACE_NOT_SUPPORT: return "不支持当前图像颜色格式";
            case ErrorInfo.MERR_ASF_IMAGE_WIDTH_HEIGHT_NOT_SUPPORT: return "不支持当前图像尺寸";
            case ErrorInfo.MERR_ASF_READ_PHONE_STATE_DENIED: return "未授予设备信息读取权限";
            case ErrorInfo.MERR_ASF_INTERNET_DENIED: return "未授予网络访问权限";
            case ErrorInfo.MERR_ASF_ACTIVATION_DATA_DESTROYED:
            case ErrorInfo.MERR_ASF_LICENSE_FILE_DATA_DESTROYED: return "激活或授权文件已损坏";
            case ErrorInfo.MERR_ASF_LOCAL_TIME_NOT_CALIBRATED:
            case ErrorInfo.MERR_ASF_CURRENT_DEVICE_TIME_INCORRECT: return "设备时间不正确，请校准时间";
            case ErrorInfo.MERR_ASF_ACTIVE_FILE_NO_EXIST:
            case ErrorInfo.MERR_ASF_LICENSE_FILE_NOT_EXIST: return "未找到激活或授权文件";
            case ErrorInfo.MERR_ASF_NETWORK_COULDNT_RESOLVE_HOST: return "无法连接服务器";
            case ErrorInfo.MERR_ASF_NETWORK_COULDNT_CONNECT_SERVER:
            case ErrorInfo.MERR_ASF_ACTIVEKEY_COULDNT_CONNECT_SERVER: return "网络连接失败，请检查网络";
            case ErrorInfo.MERR_ASF_NETWORK_CONNECT_TIMEOUT: return "网络连接超时";
            case ErrorInfo.MERR_ASF_NETWORK_UNKNOWN_ERROR: return "网络请求失败，请稍后重试";
            case ErrorInfo.MERR_ASF_SERVER_UNKNOWN_ERROR:
            case ErrorInfo.MERR_ASF_ACTIVEKEY_SERVER_SYSTEM_ERROR: return "激活服务器异常，请稍后重试";
            case ErrorInfo.MERR_ASF_ACTIVEKEY_POST_PARM_ERROR:
            case ErrorInfo.MERR_ASF_ACTIVEKEY_PARM_MISMATCH: return "激活参数无效或不匹配";
            case ErrorInfo.MERR_ASF_ACTIVEKEY_ACTIVEKEY_ACTIVATED: return "激活码已被使用";
            case ErrorInfo.MERR_ASF_ACTIVEKEY_ACTIVEKEY_FORMAT_ERROR: return "激活码格式错误";
            case ErrorInfo.MERR_ASF_ACTIVEKEY_APPID_PARM_MISMATCH: return "激活码与应用标识不匹配";
            default: return "未知错误";
        }
    }

    public static String imageUtilErrorCodeToFieldName(int code) {
        switch (code) {
            case ArcSoftImageUtilError.CODE_SUCCESS: return "成功";
            case ArcSoftImageUtilError.CODE_SIZE_MISMATCH: return "图像数据大小不匹配";
            case ArcSoftImageUtilError.CODE_UNSUPPORTED_BITMAP_FORMAT: return "不支持当前位图格式";
            case ArcSoftImageUtilError.CODE_GET_BITMAP_INFO_FAILED: return "获取位图信息失败";
            case ArcSoftImageUtilError.CODE_NULL_PARAMS: return "图像处理参数为空";
            case ArcSoftImageUtilError.CODE_SAME_OBJECT: return "源图像与目标图像不能相同";
            case ArcSoftImageUtilError.CODE_INVALID_AREA: return "图像处理区域无效";
            case ArcSoftImageUtilError.CODE_WIDTH_HEIGHT_UNSUPPORTED: return "不支持当前图像宽高";
            case ArcSoftImageUtilError.CODE_ROTATE_DEGREE_UNSUPPORTED: return "不支持当前旋转角度";
            case ArcSoftImageUtilError.CODE_IMAGE_FORMAT_UNSUPPORTED: return "不支持当前图像格式";
            default: return "未知错误";
        }
    }
}
