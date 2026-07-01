package com.arcsoft.arcfacedemo.ui.fragment;

import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;

/**
 * 洛阳北郊机场渠道长期证展示（竖版）。
 * 一类证：编号含字母，红色主题；二类证：编号全数字，青绿主题 + 「二类证」横条。
 */
public class Document2 extends AbstractDocument2 {

    private static final int AREA_COLUMNS = 4;
    private static final int TYPE1_ACCENT_COLOR = Color.parseColor("#8B2332");
    private static final int TYPE2_ACCENT_COLOR = Color.parseColor("#3e7d72");

    private LinearLayout accessAreaBadges;
    private TextView type2CategoryBar;

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
        type2CategoryBar = view.findViewById(R.id.type2_category_bar);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    /** 将证件字段填充到长期证卡面视图，含通行区域徽章网格。 */
    @Override
    protected void bindCardContent() {
        boolean isType2 = DocumentCardUiHelper.isLuoyangType2Pass(idCode);
        int accentColor = isType2 ? TYPE2_ACCENT_COLOR : TYPE1_ACCENT_COLOR;
        int badgeOutlineRes = isType2
                ? R.drawable.luoyang_area_badge_outline_type2
                : R.drawable.luoyang_area_badge_outline;

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
            expiryDateTextView.setTextColor(accentColor);
        }
        if (type2CategoryBar != null) {
            type2CategoryBar.setVisibility(isType2 ? View.VISIBLE : View.GONE);
        }
        DocumentCardUiHelper.bindLuoyangAreaBadgeGrid(accessAreaBadges, areaDisplayCode,
                badgeOutlineRes, accentColor, AREA_COLUMNS);
    }
}
