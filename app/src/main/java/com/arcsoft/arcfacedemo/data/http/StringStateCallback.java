package com.arcsoft.arcfacedemo.data.http;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.blankj.utilcode.util.ToastUtils;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.convert.StringConvert;

import okhttp3.Response;

/**
 * 字符串响应回调基类，统一处理网络异常并提示用户。
 */
public abstract class StringStateCallback extends AbsCallback<String> {

    private StringConvert convert;

    public StringStateCallback() {
        convert = new StringConvert();
    }

    /** 将响应体转换为字符串 */
    @Override
    public String convertResponse(Response response) throws Throwable {
        String s = convert.convertResponse(response);
        response.close();
        return s;
    }

    /** 根据异常类型向用户展示对应的网络错误提示 */
    @Override
    public void onError(com.lzy.okgo.model.Response<String> response) {
        super.onError(response);
        if (response.getException() instanceof ConnectException) {
            ToastUtils.showLong("请检查服务器地址是否正确及服务器是否正常运行！");
            return;
        } else if (response.getException() instanceof UnknownHostException) {
            ToastUtils.showLong("请检查网络连接是否正常及通畅！");
            return;
        } else if (response.getException() instanceof SocketTimeoutException) {
            ToastUtils.showLong("请求超时，请重试或稍后再试！");
            return;
        }
        ToastUtils.showLong("网络异常，请重试或稍后再试！");
    }
}
