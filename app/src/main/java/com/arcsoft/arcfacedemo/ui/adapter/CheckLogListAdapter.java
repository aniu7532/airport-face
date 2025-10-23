package com.arcsoft.arcfacedemo.ui.adapter;

import java.util.List;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;
import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;
import com.arcsoft.arcfacedemo.entity.Records;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.glide.GlideApp;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.Utils;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;
import com.yuyh.easyadapter.recyclerview.EasyRVHolder;

import android.content.Context;
import android.widget.ImageView;

public class CheckLogListAdapter extends EasyRVAdapter<Records> {

    public CheckLogListAdapter(Context context, List<Records> list, int... layoutIds) {
        super(context, list, layoutIds);
    }

    @Override
    protected void onBindData(EasyRVHolder viewHolder, final int position, final Records item) {
        ImageView imageView = ((ImageView) viewHolder.getView(R.id.img));
        if (item instanceof LongTermRecords) {
            LongTermRecords longTermRecords = (LongTermRecords) item;
            if (ObjectUtils.isNotEmpty(longTermRecords.passid)) {
                ALog.e(longTermRecords.passid);
                GlideApp.with(Utils.getApp()).load(AESUtils.getPhotoPath(longTermRecords.passid)).into(imageView);
                // ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new ThreadUtils.SimpleTask<Bitmap>() {
                // @Override
                // public Bitmap doInBackground() throws Throwable {
                //
                // return ImageDownloader.loadAndDecryptImage2(longTermRecords.passid, mContext);
                // }
                //
                // @Override
                // public void onSuccess(Bitmap result) {
                // ((ImageView) viewHolder.getView(R.id.img)).setImageBitmap(result);
                // }
                // });
            }
        } else if (item instanceof TemporaryCardRecords) {
            TemporaryCardRecords temporaryCardRecords = (TemporaryCardRecords) item;
            if (ObjectUtils.isNotEmpty(temporaryCardRecords.passid)) {
                ALog.e(temporaryCardRecords.passid);
                // 加载本地加密文件
                GlideApp.with(Utils.getApp()).load(AESUtils.getPhotoPath(temporaryCardRecords.passid)).into(imageView);

                // ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new ThreadUtils.SimpleTask<Bitmap>() {
                // @Override
                // public Bitmap doInBackground() throws Throwable {
                // return ImageDownloader.loadAndDecryptImage2(temporaryCardRecords.passid, mContext);
                // }
                //
                // @Override
                // public void onSuccess(Bitmap result) {
                // ((ImageView) viewHolder.getView(R.id.img)).setImageBitmap(result);
                // }
                // });

            }
        }

    }

}
