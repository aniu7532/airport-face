package com.arcsoft.arcfacedemo.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;

/**
 * 石河子渠道临时证展示（横版新证卡）。
 */
public class Document3 extends AbstractDocument3 {

    private LinearLayout access_area_badges;
    private TextView labelEscort2;

    @Override
    protected int getLayoutResId() {
        return R.layout.document3;
    }

    /** 绑定临时证各展示控件引用。 */
    @Override
    protected void bindViews(View view) {
        passers_by = view.findViewById(R.id.passers_by);
        unit = view.findViewById(R.id.unit);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        lead_people1 = view.findViewById(R.id.lead_people1);
        labelEscort2 = view.findViewById(R.id.label_escort2);
        lead_people2 = view.findViewById(R.id.lead_people2);
        leading_person_unit = view.findViewById(R.id.leading_person_unit);
        card_img = view.findViewById(R.id.card_img);
        or_code = view.findViewById(R.id.or_code);
        access_area_badges = view.findViewById(R.id.access_area_badges);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    /** 将证件字段填充到临时证卡面视图，含陪同人员与通行区域。 */
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
        boolean hasSecondEscort = !TextUtils.isEmpty(leadingPeople2);
        if (labelEscort2 != null) {
            labelEscort2.setVisibility(hasSecondEscort ? View.VISIBLE : View.GONE);
        }
        if (lead_people2 != null) {
            lead_people2.setVisibility(hasSecondEscort ? View.VISIBLE : View.GONE);
            if (hasSecondEscort) {
                lead_people2.setText(
                        DocumentCardUiHelper.formatPersonWithIdCode(leadingPeople2, leadingPeople2IdCode));
            }
        }
        if (leading_person_unit != null) {
            leading_person_unit.setText(DocumentCardUiHelper.formatEscortUnits(
                    leadingPeopleUnit, leadingPeople2Unit, hasSecondEscort));
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(DocumentCardUiHelper.formatValidityPeriod(startDate, expiryDate));
        }
        DocumentCardUiHelper.bindShiheziAreaBadges(access_area_badges, areaDisplayCode,
                R.drawable.shihezi_area_badge_orange);
    }
}
