package com.arcsoft.arcfacedemo.data.http;

import com.lzy.okgo.callback.AbsCallback;

import okhttp3.Response;

public abstract class XmlStringCallback extends AbsCallback<String> {

    private XmlStringConvert convert;

    public XmlStringCallback() {
        convert = new XmlStringConvert();
    }

    @Override
    public String convertResponse(Response response) throws Throwable {
        String s = convert.convertResponse(response);
        response.close();
        return s;
    }
}
