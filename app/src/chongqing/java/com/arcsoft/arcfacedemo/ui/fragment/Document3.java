package com.arcsoft.arcfacedemo.ui.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.blankj.utilcode.util.ObjectUtils;

/**
 * 重庆渠道临时证展示。
 */
public class Document3 extends AbstractDocument3 {

    private TextView access_area;
    private ImageView img_color;
    private TextView faceSimilar;
    private View faceSimilarLayout;

    @Override
    protected int getLayoutResId() {
        return R.layout.document3;
    }

    @Override
    protected void bindViews(View view) {
        passers_by = view.findViewById(R.id.passers_by);
        access_area = view.findViewById(R.id.access_area);
        unit = view.findViewById(R.id.unit);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        lead_people1 = view.findViewById(R.id.lead_people1);
        lead_people2 = view.findViewById(R.id.lead_people2);
        leading_person_unit = view.findViewById(R.id.leading_person_unit);
        card_img = view.findViewById(R.id.card_img);
        or_code = view.findViewById(R.id.or_code);
        img_color = view.findViewById(R.id.img_color);
        faceSimilar = view.findViewById(R.id.faceSimilar);
        faceSimilarLayout = view.findViewById(R.id.faceSimilarLayout);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
    }

    @Override
    protected void bindCardContent() {
        if (leading_person_unit != null) {
            leading_person_unit.setText(leadingPeopleUnit);
        }
        if (leadingPeople1 != null && lead_people1 != null) {
            lead_people1.setText(leadingPeople1);
        }
        if (leadingPeople2 != null && lead_people2 != null) {
            lead_people2.setText(leadingPeople2);
        }
        if (img_color != null && "2".equals(templateType)) {
            img_color.setImageResource(R.drawable.yellow_stripes);
        }
        if (passers_by != null) {
            passers_by.setText(nickname);
        }
        if (access_area != null) {
            access_area.setText(areaDisplayCode);
        }
        if (unit != null) {
            unit.setText(companyName);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(startDate + "-" + expiryDate);
        }
        if (faceSimilarLayout != null && faceSimilar != null) {
            if (ObjectUtils.isEmpty(similar) || similar.equals("0") || similar.equals("0.0")) {
                faceSimilarLayout.setVisibility(View.GONE);
            } else {
                faceSimilarLayout.setVisibility(View.VISIBLE);
                faceSimilar.setText(similar);
            }
        }
    }
}
