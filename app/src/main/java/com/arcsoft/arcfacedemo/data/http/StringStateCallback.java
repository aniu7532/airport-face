package com.arcsoft.arcfacedemo.data.http;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import com.blankj.utilcode.util.ToastUtils;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.convert.StringConvert;

import okhttp3.Response;

public abstract class StringStateCallback extends AbsCallback<String> {

    private StringConvert convert;

    public StringStateCallback() {
        convert = new StringConvert();
    }

    @Override
    public String convertResponse(Response response) throws Throwable {
        String s = convert.convertResponse(response);
        response.close();
        return s;
    }

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
