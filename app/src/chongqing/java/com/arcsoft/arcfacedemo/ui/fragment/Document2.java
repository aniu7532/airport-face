package com.arcsoft.arcfacedemo.ui.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.arcsoft.arcfacedemo.R;
import com.blankj.utilcode.util.ObjectUtils;

/**
 * 重庆渠道长期证展示。
 */
public class Document2 extends AbstractDocument2 {

    private TextView access_area;
    private ImageView img_color;
    private TextView faceSimilar;

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
        img_color = view.findViewById(R.id.img_color);
        access_area = view.findViewById(R.id.access_area);
        faceSimilar = view.findViewById(R.id.faceSimilar);
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
        if (access_area != null) {
            access_area.setText(areaDisplayCode);
        }
        if (img_color != null && templateType != null && idCode != null
                && ("2".equals(templateType) || idCode.startsWith("C") || idCode.startsWith("B"))) {
            img_color.setImageResource(R.drawable.yellow_stripes);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(expiryDate);
        }
        if (faceSimilar != null) {
            if (ObjectUtils.isEmpty(similar) || similar.equals("0") || similar.equals("0.0")) {
                faceSimilar.setText("");
            } else {
                faceSimilar.setText(similar);
            }
        }
    }
}
