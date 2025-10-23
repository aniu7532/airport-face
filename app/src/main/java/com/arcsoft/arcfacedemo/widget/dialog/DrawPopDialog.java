package com.arcsoft.arcfacedemo.widget.dialog;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.CardRecords;
import com.bumptech.glide.Glide;
import com.lxj.xpopup.core.DrawerPopupView;

public class DrawPopDialog extends DrawerPopupView {
    CardRecords.ListDTO item;

    public DrawPopDialog(@NonNull Context context) {
        super(context);
    }

    public DrawPopDialog(Context context, CardRecords.ListDTO item) {
        super(context);
        this.item = item;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_image;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        ImageView imgBig = findViewById(R.id.imgBig);
        ImageView imgSmall = findViewById(R.id.imgSmall);

        Glide.with(getContext()).load(item.getCheckPhoto()).into(imgBig);
        Glide.with(getContext()).load(item.getSitePhoto()).into(imgSmall);
    }

    // 自适应， 最大高度为Window的0.85
    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return 0;
    }
}
