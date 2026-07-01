package com.arcsoft.arcfacedemo.ui.fragment;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.blankj.utilcode.util.ObjectUtils;

/**
 * 洛阳北郊机场渠道临时证展示（横版，底图定制；业务逻辑与其他渠道一致）。
 */
public class Document3 extends AbstractDocument3 {

    private TextView idCodeTextView;
    private TextView accessAreaTextView;
    private TextView leadPeople1IdTextView;
    private TextView faceSimilar;
    private View faceSimilarLayout;

    @Override
    protected int getLayoutResId() {
        return R.layout.document3;
    }

    /** 绑定临时证各展示控件引用。 */
    @Override
    protected void bindViews(View view) {
        idCodeTextView = view.findViewById(R.id.idCode);
        passers_by = view.findViewById(R.id.passers_by);
        accessAreaTextView = view.findViewById(R.id.access_area);
        unit = view.findViewById(R.id.unit);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        lead_people1 = view.findViewById(R.id.lead_people1);
        leadPeople1IdTextView = view.findViewById(R.id.lead_people1_id);
        leading_person_unit = view.findViewById(R.id.leading_person_unit);
        card_img = view.findViewById(R.id.card_img);
        or_code = view.findViewById(R.id.or_code);
        lead_people2 = view.findViewById(R.id.lead_people2);
        faceSimilar = view.findViewById(R.id.faceSimilar);
        faceSimilarLayout = view.findViewById(R.id.faceSimilarLayout);
        statusOverlay = view.findViewById(R.id.statusOverlay);
        statusText = view.findViewById(R.id.statusText);
        if (or_code != null) {
            or_code.setVisibility(View.GONE);
        }
    }

    /** 洛阳临时证卡面不展示二维码，右侧框仅用于人像。 */
    @Override
    public void updatePage() {
        bindCardContent();
        DocumentCardSupport.applyStatusSeal(statusOverlay, statusText, status);
        DocumentCardSupport.loadTemporaryCardPhoto(card_img, passid, photo, getActivity());
    }

    /** 将证件字段填充到临时证卡面视图。 */
    @Override
    protected void bindCardContent() {
        if (idCodeTextView != null) {
            idCodeTextView.setText(idCode);
        }
        if (passers_by != null) {
            passers_by.setText(nickname);
        }
        if (accessAreaTextView != null) {
            accessAreaTextView.setText(formatAreaDisplay(areaDisplayCode));
        }
        if (unit != null) {
            unit.setText(companyName);
        }
        if (lead_people1 != null) {
            lead_people1.setText(leadingPeople1);
        }
        if (leadPeople1IdTextView != null) {
            leadPeople1IdTextView.setText(leadingPeople1IdCode);
        }
        if (leading_person_unit != null) {
            leading_person_unit.setText(leadingPeopleUnit);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(DocumentCardUiHelper.formatValidityPeriod(startDate, expiryDate));
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

    private static String formatAreaDisplay(String raw) {
        if (TextUtils.isEmpty(raw)) {
            return "";
        }
        String normalized = raw.replace('\n', ' ').replace('\r', ' ').trim().replaceAll("\\s+", " ");
        if (normalized.isEmpty()) {
            return "";
        }
        String[] parts = normalized.split(" ");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(parts[i]);
        }
        return builder.toString();
    }
}
