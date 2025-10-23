package com.arcsoft.arcfacedemo.data.http;

import com.arcsoft.arcfacedemo.util.log.ALog;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;

/**
 * Created by Administrator on 2016/11/30.
 * https://github.com/hongyangAndroid/okhttputils
 */

public class HttpUtils {

    public static String get(Object tag, String url) {
        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        Call<String> call = OkGo.<String> get(url).tag(tag).converter(new StringConvert()).adapt();
        try {
            Response<String> response = call.execute();
            ALog.d("Response code:" + response.code() + ", body:" + response.body());
            if (response.code() == 200) {
                return response.body();
            } else {
                return response.getException().getMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

}
