package com.arcsoft.arcfacedemo.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * 长期证 Fragment 抽象基类：参数解析、状态盖章、照片加载由各渠道子类实现 layout 与字段绑定。
 */
public abstract class AbstractDocument2 extends Fragment {

    protected static final String TAG = "Document2";

    protected TextView nicknameTextView;
    protected TextView idCodeTextView;
    protected TextView companyNameTextView;
    protected TextView expiryDateTextView;
    protected ImageView card_img;
    protected View statusOverlay;
    protected TextView statusText;

    protected String idCode;
    protected String passid;
    protected String photo;
    protected String nickname;
    protected String companyName;
    protected String startDate;
    protected String expiryDate;
    protected String templateType;
    protected String areaDisplayCode;
    protected String status;
    protected String similar;

    protected abstract int getLayoutResId();

    protected abstract void bindViews(View view);

    protected abstract void bindCardContent();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);
        bindViews(view);
        readArguments();
        updatePage();
        return view;
    }

    protected void readArguments() {
        if (getArguments() == null) {
            return;
        }
        idCode = getArguments().getString("idCode");
        passid = getArguments().getString("passid");
        nickname = getArguments().getString("nickname");
        companyName = getArguments().getString("companyName");
        startDate = getArguments().getString("startDate");
        expiryDate = getArguments().getString("expiryDate");
        templateType = getArguments().getString("templateType");
        areaDisplayCode = getArguments().getString("areaDisplayCode");
        status = getArguments().getString("status");
        photo = getArguments().getString("photo");
        similar = getArguments().getString("faceSimilar");
        Log.d(TAG, "nickname: " + nickname);
    }

    public void updatePage() {
        bindCardContent();
        DocumentCardSupport.applyStatusSeal(statusOverlay, statusText, status);
        DocumentCardSupport.loadLongTermCardPhoto(card_img, passid, photo, getActivity());
    }
}
