package com.arcsoft.arcfacedemo.ui.adapter;

import java.io.File;
import java.util.List;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.entity.CardRecords;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.glide.GlideApp;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.Utils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import android.content.Context;
import android.widget.ImageView;

/**
 * 通行记录列表适配器（非分页版），展示姓名、单位、通过状态、时间及抓拍照片。
 */
public class RecordsListAdapter extends EasyRVAdapter<CardRecords.ListDTO> {

    public RecordsListAdapter(Context context, List<CardRecords.ListDTO> list, int... layoutIds) {
        super(context, list, layoutIds);
    }

    /** 绑定通行记录文本信息并加载查验照片（网络或本地）。 */
    @Override
    protected void onBindData(EasyRVHolder viewHolder, final int position, final CardRecords.ListDTO item) {
        // tvPass tvTime
        viewHolder.setText(R.id.tvName, item.getNickname() + "    " + item.getIdCode());
        viewHolder.setText(R.id.tvBu, item.getCompanyName());
        viewHolder.setText(R.id.tvPass, item.isStatus() ? "通过" : "未通过");
        viewHolder.setText(R.id.tvTime, item.getCheckTime());
        ALog.e(item.getSitePhoto() + "");
        if (ObjectUtils.isNotEmpty(item.getCheckPhoto())) {
            if (item.getCheckPhoto().startsWith("http") || item.getCheckPhoto().startsWith("encrypted/")) {

                // 拼接基础下载地址
                String baseUrl = UrlConstants.fileStreamUrl(item.getCheckPhoto());

                // 构造带有 Authorization 头的 GlideUrl
                LazyHeaders.Builder headersBuilder = new LazyHeaders.Builder();
                // 携带 accessToken（如果存在）
                if (ApiUtils.getAccessToken() != null) {
                    headersBuilder.addHeader("Authorization", "Bearer " + ApiUtils.getAccessToken());
                }
                GlideUrl glideUrl = new GlideUrl(baseUrl, headersBuilder.build());

                Glide.with(mContext).load(glideUrl).into((ImageView) viewHolder.getView(R.id.img));
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
