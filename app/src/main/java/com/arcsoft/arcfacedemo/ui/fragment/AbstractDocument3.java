package com.arcsoft.arcfacedemo.ui.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

/**
 * 临时证 Fragment 抽象基类。
 */
public abstract class AbstractDocument3 extends Fragment {

    protected static final String TAG = "Document3";

    protected TextView passers_by;
    protected TextView unit;
    protected TextView expiryDateTextView;
    protected TextView lead_people1;
    protected TextView lead_people2;
    protected TextView leading_person_unit;
    protected ImageView card_img;
    protected ImageView or_code;
    protected View statusOverlay;
    protected TextView statusText;

    protected String idCode;
    protected String applyId;
    protected String passid;
    protected String photo;
    protected String nickname;
    protected String areaDisplayCode;
    protected String companyName;
    protected String startDate;
    protected String expiryDate;
    protected String templateType;
    protected String leadingPeople;
    protected String leadingPeople1;
    protected String leadingPeople1IdCode;
    protected String leadingPeople2;
    protected String leadingPeopleUnit;
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
        applyId = getArguments().getString("applyId");
        passid = getArguments().getString("passid");
        nickname = getArguments().getString("nickname");
        areaDisplayCode = getArguments().getString("areaDisplayCode");
        companyName = getArguments().getString("companyName");
        startDate = getArguments().getString("startDate");
        expiryDate = getArguments().getString("expiryDate");
        templateType = getArguments().getString("templateType");
        leadingPeople = getArguments().getString("leadingPeople");
        status = getArguments().getString("status");
        photo = getArguments().getString("photo");
        similar = getArguments().getString("faceSimilar");
        parseLeadingPeople(leadingPeople);
    }

    protected void parseLeadingPeople(String leadingPeopleJson) {
        if (TextUtils.isEmpty(leadingPeopleJson)) {
            return;
        }
        Gson gson = new Gson();
        Type listType = new TypeToken<List<Map<String, String>>>() {
        }.getType();
        List<Map<String, String>> result = gson.fromJson(leadingPeopleJson, listType);
        if (result == null || result.isEmpty()) {
            Log.i(TAG, "无引领人 ");
            return;
        }
        Map<String, String> map1 = result.get(0);
        if (map1 != null) {
            if (map1.get("companyName") != null) {
                leadingPeopleUnit = map1.get("companyName");
            }
            if (map1.get("nickname") != null) {
                leadingPeople1 = map1.get("nickname");
            }
            if (map1.get("idCode") != null) {
                leadingPeople1IdCode = map1.get("idCode");
            }
        }
        if (result.size() >= 2) {
            Map<String, String> map2 = result.get(1);
            if (map2 != null && map2.get("nickname") != null) {
                leadingPeople2 = map2.get("nickname");
            }
        }
    }

    public void updatePage() {
        bindCardContent();
        DocumentCardSupport.applyStatusSeal(statusOverlay, statusText, status);
        DocumentCardSupport.loadTemporaryCardPhoto(card_img, passid, photo, getActivity());
        bindQrCode();
    }

    private void bindQrCode() {
        if (or_code == null) {
            return;
        }
        final String qrText = DocumentCardUiHelper.resolveQrText(applyId, passid);
        or_code.post(() -> {
            int contentWidth = or_code.getWidth() - or_code.getPaddingLeft() - or_code.getPaddingRight();
            int contentHeight = or_code.getHeight() - or_code.getPaddingTop() - or_code.getPaddingBottom();
            int size = Math.max(Math.min(contentWidth, contentHeight), 1);
            if (size <= 1) {
                size = (int) (52 * or_code.getResources().getDisplayMetrics().density);
            }
            Bitmap qrBitmap = DocumentCardUiHelper.generateQrCodeBitmap(qrText, size);
            if (qrBitmap != null) {
                or_code.setScaleType(ImageView.ScaleType.FIT_XY);
                or_code.setImageBitmap(qrBitmap);
                or_code.setAdjustViewBounds(false);
            } else {
                Log.e(TAG, "二维码生成失败: " + qrText);
            }
        });
    }
}
