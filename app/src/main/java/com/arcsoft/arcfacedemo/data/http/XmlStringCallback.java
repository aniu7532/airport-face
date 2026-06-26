package com.arcsoft.arcfacedemo.data.http;

import com.lzy.okgo.callback.AbsCallback;

import okhttp3.Response;

/**
 * XML 包裹的字符串响应回调基类，剥离 XML 标签后返回内部文本内容。
 */
public abstract class XmlStringCallback extends AbsCallback<String> {

    private XmlStringConvert convert;

    public XmlStringCallback() {
        convert = new XmlStringConvert();
    }

    /** 剥离 XML 包裹层并提取内部字符串 */
    @Override
    public String convertResponse(Response response) throws Throwable {
        String s = convert.convertResponse(response);
        response.close();
        return s;
    }
}
