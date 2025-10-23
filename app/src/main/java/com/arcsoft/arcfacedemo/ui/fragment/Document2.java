package com.arcsoft.arcfacedemo.ui.fragment;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.glide.GlideApp;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.bumptech.glide.Glide;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Document2 extends Fragment {
    private static final String TAG = "Document2";
    private TextView nicknameTextView;
    private TextView idCodeTextView;
    private TextView companyNameTextView;
    private TextView expiryDateTextView;
    private ImageView card_img;
    private ImageView img_color;
    private TextView access_area;
    private TextView card_status;
    private TextView faceSimilar;
    private View faceSimilarLayout;

    String idCode;
    String passid;
    String photo;

    String nickname;
    String companyName;
    String expiryDate;
    String templateType;
    String areaDisplayCode;
    String status;
    String similar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document2, container, false);
        nicknameTextView = view.findViewById(R.id.nickname);
        idCodeTextView = view.findViewById(R.id.idCode);
        companyNameTextView = view.findViewById(R.id.companyName);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        card_img = view.findViewById(R.id.card_img);
        img_color = view.findViewById(R.id.img_color);
        access_area = view.findViewById(R.id.access_area);
        card_status = view.findViewById(R.id.card_status);
        faceSimilar = view.findViewById(R.id.faceSimilar);
        faceSimilarLayout = view.findViewById(R.id.faceSimilarLayout);
        if (getArguments() != null) {
            idCode = getArguments().getString("idCode");
            passid = getArguments().getString("passid");
            nickname = getArguments().getString("nickname");
            companyName = getArguments().getString("companyName");
            expiryDate = getArguments().getString("expiryDate");
            templateType = getArguments().getString("templateType");
            areaDisplayCode = getArguments().getString("areaDisplayCode");
            status = getArguments().getString("status");
            photo = getArguments().getString("photo");
            similar = getArguments().getString("faceSimilar");
            Log.d("Document2", "nickname: " + nickname);
        }
        updatePage();
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // if (getArguments() == null) {
        // Log.e("Document2", "Arguments are null");
        // return;
        // }
        // if (getArguments() != null) {
        // idCode = getArguments().getString("idCode");
        // nickname = getArguments().getString("nickname");
        // companyName = getArguments().getString("companyName");
        // updateTime = getArguments().getString("updateTime");
        // templateType = getArguments().getString("templateType");
        // areaDisplayCode = getArguments().getString("areaDisplayCode");
        // Log.d("Document2", "nickname: "+nickname);
        //
        // // 使用 message 数据
        //// Toast.makeText(getContext(), nickname, Toast.LENGTH_SHORT).show();
        // }
    }

    public void updatePage() {
        // 更新页面信息
        if (access_area != null) {
            access_area.setText(areaDisplayCode);
        }
        if (templateType == "2" || idCode.startsWith("C") || idCode.startsWith("B")) {
            img_color.setImageResource(R.drawable.yellow_stripes);
        }
        if (nicknameTextView != null) {
            nicknameTextView.setText(nickname);
        }
        if (idCodeTextView != null) {
            idCodeTextView.setText(idCode);
        }
        if (companyNameTextView != null) {
            companyNameTextView.setText(companyName);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(expiryDate);
        }
        if (card_status != null) {
            // status==1 正常 status==2 注销 status==3 过期 status==4 挂失
            String status = "正常";
            if (status == "1")
                status = "正常";
            if (status == "2")
                status = "注销";
            if (status == "3")
                status = "过期";
            if (status == "4")
                status = "挂失";
            card_status.setText(status);
        }
        if (ObjectUtils.isEmpty(similar) || similar.equals("0") || similar.equals("0.0")) {
            // faceSimilarLayout.setVisibility(View.GONE);
            faceSimilar.setText("");
        } else {
            faceSimilar.setText(similar);
        }

        // // 根据 idCode 去本地加载图片
        // // String s = Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode + ".jpg";
        // // Bitmap bitmap = ImageUtils.loadBitmapFromPath(s);
        // Bitmap bitmap = ImageDownloader.loadAndDecryptImage2(passid, getActivity());
        // if (bitmap != null) {
        // Log.d(TAG, "获取图片 bitmap: " + bitmap.toString());
        // if (card_img != null) {
        // card_img.setImageBitmap(bitmap);
        // }
        // } else {
        // Log.e(TAG, "图片加载失败: " + passid);
        // }

        // File directory = new File(getActivity().getExternalFilesDir(null), "photo");
        // File file = new File(directory, passid + ".jpg");
        // GlideApp.with(getActivity()).load(file).into(card_img);

        ALog.e(ArcFaceApplication.getApplication().isOffLine());
        if (ArcFaceApplication.getApplication().isOffLine() || ObjectUtils.isEmpty(photo)) { // 加载本地加密文件
            GlideApp.with(getActivity()).load(AESUtils.getPhotoPath(passid)).into(card_img);
        } else {
            Glide.with(getActivity()).load(photo).into(card_img);
        }
    }

}
