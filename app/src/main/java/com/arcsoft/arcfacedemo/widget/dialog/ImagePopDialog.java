package com.arcsoft.arcfacedemo.widget.dialog;

import android.content.Context;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.CardRecords;
import com.bumptech.glide.Glide;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * 居中图片预览弹窗，展示单条查验记录的证件照与现场照。
 */
public class ImagePopDialog extends CenterPopupView {
    CardRecords.ListDTO item;

    public ImagePopDialog(@NonNull Context context) {
        super(context);
    }

    public ImagePopDialog(Context context, CardRecords.ListDTO item) {
        super(context);
        this.item = item;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_image;
    }

    /** 使用 Glide 加载记录中的大图与小图。 */
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
