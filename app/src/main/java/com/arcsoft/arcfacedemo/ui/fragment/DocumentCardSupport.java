package com.arcsoft.arcfacedemo.ui.fragment;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.glide.EncryptedGlideFile;
import com.arcsoft.arcfacedemo.util.glide.GlideApp;
import com.blankj.utilcode.util.ObjectUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import java.io.File;

/**
 * Document2 / Document3 共用的证卡展示逻辑。
 */
public final class DocumentCardSupport {

    private DocumentCardSupport() {
    }

    public static void applyStatusSeal(View statusOverlay, TextView statusText, String status) {
        if (statusOverlay == null || statusText == null) {
            return;
        }
        String sealText = null;
        if ("2".equals(status)) {
//            sealText = "已注销";
        } else if ("3".equals(status)) {
            sealText = "已过期";
        } else if ("4".equals(status)) {
//            sealText = "已挂失";
        } else if ("5".equals(status)) {
            sealText = "已停用";
        }

        if (sealText != null) {
            statusOverlay.setVisibility(View.VISIBLE);
            statusText.setText(sealText);
        } else {
            statusOverlay.setVisibility(View.GONE);
        }
    }

    /** 长期证照片加载（本地加密文件走 EncryptedGlideFile）。 */
    public static void loadLongTermCardPhoto(ImageView cardImg, String passid, String photo,
            FragmentActivity activity) {
        if (cardImg == null || activity == null) {
            return;
        }
        File file = AESUtils.getPhotoPath(passid);
        if (file.exists() && file.length() > 0) {
            GlideApp.with(activity).load(new EncryptedGlideFile(file)).into(cardImg);
            return;
        }
        if (!ObjectUtils.isEmpty(photo)) {
            loadRemotePhoto(cardImg, photo, activity);
        }
    }

    /** 临时证照片加载。 */
    public static void loadTemporaryCardPhoto(ImageView cardImg, String passid, String photo,
            FragmentActivity activity) {
        if (cardImg == null || activity == null) {
            return;
        }
        File file = AESUtils.getPhotoPath(passid);
        if (file.exists() && file.length() > 0) {
            GlideApp.with(activity).load(new EncryptedGlideFile(file)).into(cardImg);
            return;
        }
        if (!ObjectUtils.isEmpty(photo)) {
            loadRemotePhoto(cardImg, photo, activity);
        }
    }

    private static void loadRemotePhoto(ImageView cardImg, String photo, FragmentActivity activity) {
        String baseUrl = UrlConstants.fileStreamUrl(photo);
        LazyHeaders.Builder headersBuilder = new LazyHeaders.Builder();
        if (ApiUtils.getAccessToken() != null) {
            headersBuilder.addHeader("Authorization", "Bearer " + ApiUtils.getAccessToken());
        }
        GlideUrl glideUrl = new GlideUrl(baseUrl, headersBuilder.build());
        Glide.with(activity).load(glideUrl).into(cardImg);
    }
}
