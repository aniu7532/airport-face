package com.arcsoft.arcfacedemo.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.ui.model.CompareResult;
import com.arcsoft.arcfacedemo.widget.FaceSearchResultAdapter;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * DataBinding 适配器工具类，为布局中的 ImageView、RecyclerView、TextView 提供自定义绑定逻辑。
 */
public class BindingUtil {

    /**
     * 通过 Glide 加载图片路径到 ImageView。
     */
    @BindingAdapter("imgPath")
    public static void setImagePath(ImageView imageView, String path) {
        Glide.with(imageView.getContext())
                .load(path)
                .into(imageView);
    }

    /**
     * 为人脸比对结果列表配置网格布局的 RecyclerView 适配器。
     */
    @BindingAdapter("compareResultList")
    public static void setCompareResultList(RecyclerView recyclerView, List<CompareResult> compareResultList) {
        Context context = recyclerView.getContext();
        FaceSearchResultAdapter adapter = new FaceSearchResultAdapter(compareResultList, context);
        recyclerView.setAdapter(adapter);
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int spanCount = dm.widthPixels /
                (context.getResources().getDimensionPixelSize(R.dimen.item_head_image_padding) * 2 +
                        context.getResources().getDimensionPixelSize(R.dimen.item_image_size));
        recyclerView.setLayoutManager(new GridLayoutManager(context, spanCount));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private static final SimpleDateFormat REGISTER_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * 将时间戳格式化为 yyyy-MM-dd 并显示在 TextView 上。
     */
    @BindingAdapter("date")
    public static void setDate(TextView textView, long date) {
        synchronized (REGISTER_DATE_FORMAT) {
            textView.setText(REGISTER_DATE_FORMAT.format(date));
        }
    }
}
