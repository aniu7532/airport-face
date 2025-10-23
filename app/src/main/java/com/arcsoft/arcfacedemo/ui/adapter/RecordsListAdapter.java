package com.arcsoft.arcfacedemo.ui.adapter;

import java.io.File;
import java.util.List;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.CardRecords;
import com.arcsoft.arcfacedemo.util.glide.GlideApp;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import android.content.Context;
import android.widget.ImageView;

public class RecordsListAdapter extends EasyRVAdapter<CardRecords.ListDTO> {

    public RecordsListAdapter(Context context, List<CardRecords.ListDTO> list, int... layoutIds) {
        super(context, list, layoutIds);
    }

    @Override
    protected void onBindData(EasyRVHolder viewHolder, final int position, final CardRecords.ListDTO item) {
        // tvPass tvTime
        viewHolder.setText(R.id.tvName, item.getNickname() + "    " + item.getIdCode());
        viewHolder.setText(R.id.tvBu, item.getCompanyName());
        viewHolder.setText(R.id.tvPass, item.isStatus() ? "通过" : "未通过");
        viewHolder.setText(R.id.tvTime, item.getCheckTime());
        ALog.e(item.getSitePhoto() + "");
        if (ObjectUtils.isNotEmpty(item.getCheckPhoto())) {
            if (item.getCheckPhoto().startsWith("http")) {
                Glide.with(mContext).load(item.getCheckPhoto()).into((ImageView) viewHolder.getView(R.id.img));
            } else {

                File file = new File(item.getCheckPhoto());
                ALog.i("加载图片路径: " + file.getAbsolutePath());
                if (!file.exists()) {
                    ALog.e("文件不存在: " + file.getAbsolutePath());
                    return;
                }
                // 加载本地加密文件
                GlideApp.with(Utils.getApp()).load(file)
                        // .placeholder(R.drawable.loading_placeholder) // 占位图
                        // .error(R.drawable.error_placeholder) // 错误图
                        .into(((ImageView) viewHolder.getView(R.id.img)));

                // ((ImageView)
                // viewHolder.getView(R.id.img)).setImageBitmap(ImageDownloader.loadAndDecryptImage2(item.getCheckPhoto()));
                // Glide.with(mContext).load("file://" + item.getSitePhoto()).into((ImageView)
                // viewHolder.getView(R.id.img));
            }
        }

    }
}
