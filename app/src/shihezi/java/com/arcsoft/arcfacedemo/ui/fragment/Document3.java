package com.arcsoft.arcfacedemo.ui.fragment;

import android.view.View;
import android.widget.LinearLayout;

import com.arcsoft.arcfacedemo.R;

/**
 * 石河子渠道临时证展示（横版新证卡）。
 */
public class Document3 extends AbstractDocument3 {

    private LinearLayout access_area_badges;

    @Override
    protected int getLayoutResId() {
        return R.layout.document3;
    }

    @Override
    protected void bindViews(View view) {
        passers_by = view.findViewById(R.id.passers_by);
        unit = view.findViewById(R.id.unit);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        lead_people1 = view.findViewById(R.id.lead_people1);
        leading_person_unit = view.findViewById(R.id.leading_person_unit);
        card_img = view.findViewById(R.id.card_img);
        or_code = view.findViewById(R.id.or_code);
        access_area_badges = view.findViewById(R.id.access_area_badges);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    @Override
    protected void bindCardContent() {
        if (passers_by != null) {
            passers_by.setText(DocumentCardUiHelper.formatPersonWithIdCode(nickname, idCode));
        }
        if (unit != null) {
            unit.setText(companyName);
        }
        if (lead_people1 != null) {
            lead_people1.setText(
                    DocumentCardUiHelper.formatPersonWithIdCode(leadingPeople1, leadingPeople1IdCode));
        }
        if (leading_person_unit != null) {
            leading_person_unit.setText(leadingPeopleUnit);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(DocumentCardUiHelper.formatValidityPeriod(startDate, expiryDate));
        }
        DocumentCardUiHelper.bindAreaBadges(access_area_badges, areaDisplayCode,
                R.drawable.shihezi_area_badge_orange);
    }
}
