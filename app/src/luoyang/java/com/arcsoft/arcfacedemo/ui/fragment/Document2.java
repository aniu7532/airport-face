package com.arcsoft.arcfacedemo.ui.fragment;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;

import com.arcsoft.arcfacedemo.R;

/**
 * 洛阳北郊机场渠道长期证展示（竖版）。
 */
public class Document2 extends AbstractDocument2 {

    private static final int AREA_BADGE_TEXT_COLOR = Color.parseColor("#8B2332");
    private static final int AREA_COLUMNS = 4;

    private LinearLayout accessAreaBadges;

    @Override
    protected int getLayoutResId() {
        return R.layout.document2;
    }

    /** 绑定长期证各展示控件引用。 */
    @Override
    protected void bindViews(View view) {
        nicknameTextView = view.findViewById(R.id.nickname);
        idCodeTextView = view.findViewById(R.id.idCode);
        companyNameTextView = view.findViewById(R.id.companyName);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        card_img = view.findViewById(R.id.card_img);
        accessAreaBadges = view.findViewById(R.id.access_area_badges);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    /** 将证件字段填充到长期证卡面视图，含通行区域徽章网格。 */
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
        DocumentCardUiHelper.bindLuoyangAreaBadgeGrid(accessAreaBadges, areaDisplayCode,
                R.drawable.luoyang_area_badge_outline, AREA_BADGE_TEXT_COLOR, AREA_COLUMNS);
    }
}
