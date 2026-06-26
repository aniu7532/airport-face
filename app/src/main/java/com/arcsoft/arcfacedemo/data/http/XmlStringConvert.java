package com.arcsoft.arcfacedemo.data.http;

import com.blankj.utilcode.util.ObjectUtils;
import com.lzy.okgo.convert.Converter;

import okhttp3.Response;

/**
 * XML 响应体转换器，从 XML 标签中提取内部文本内容。
 */
public class XmlStringConvert implements Converter<String> {
    /** 解析 XML 响应，提取标签内的文本内容；无效时返回 null */
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
