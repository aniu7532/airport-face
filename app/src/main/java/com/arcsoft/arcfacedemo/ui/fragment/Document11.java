package com.arcsoft.arcfacedemo.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.arcsoft.arcfacedemo.R;

/**
 * 证件展示 Fragment（布局 document11），用于接收并展示传入的 message 参数。
 */
public class Document11 extends Fragment {


    /** 加载 document11 布局。 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.document11, container, false);
    }

    /** 读取 Arguments 中的 message 并以 Toast 展示。 */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String message = getArguments().getString("message");
            // 使用 message 数据
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}
