package com.arcsoft.arcfacedemo.data.http;

import com.blankj.utilcode.util.ObjectUtils;
import com.lzy.okgo.convert.Converter;

import okhttp3.Response;

public class XmlStringConvert implements Converter<String> {
    @Override
    public String convertResponse(Response response) throws Throwable {
        String data = response.body().string();
        if (ObjectUtils.isNotEmpty(data) && data.length() > 85) {
            data = data.substring(data.indexOf("\">") + 2, data.indexOf("</"));
            if (!"null".equals(data)) {
                return data;
            }
        }
        return null;
    }

}
