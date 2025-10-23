package com.arcsoft.arcfacedemo.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.arcsoft.arcfacedemo.R;


public class Document11 extends Fragment {


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.document11, container, false);
    }

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
