package com.arcsoft.arcfacedemo.ui.fragment;

import android.view.View;
import android.widget.LinearLayout;

import com.arcsoft.arcfacedemo.R;

/**
 * 石河子渠道长期证展示（横版新证卡）。
 */
public class Document2 extends AbstractDocument2 {

    private LinearLayout access_area_badges;

    @Override
    protected int getLayoutResId() {
        return R.layout.document2;
    }

    @Override
    protected void bindViews(View view) {
        nicknameTextView = view.findViewById(R.id.nickname);
        idCodeTextView = view.findViewById(R.id.idCode);
        companyNameTextView = view.findViewById(R.id.companyName);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        card_img = view.findViewById(R.id.card_img);
        access_area_badges = view.findViewById(R.id.access_area_badges);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    @Override
    protected void bindCardContent() {
        if (nicknameTextView != null) {
            nicknameTextView.setText(nickname);
        }
        if (idCodeTextView != null) {
            idCodeTextView.setText(idCode);
        }
        if (companyNameTextView != null) {
            companyNameTextView.setText(companyName);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(DocumentCardUiHelper.formatValidityPeriod(startDate, expiryDate));
        }
        DocumentCardUiHelper.bindAreaBadges(access_area_badges, areaDisplayCode,
                R.drawable.shihezi_area_badge_blue);
    }
}
