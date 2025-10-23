package com.arcsoft.arcfacedemo.ui.fragment;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.glide.GlideApp;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

public class Document3 extends Fragment {
    private static final String TAG = "Document3";
    private TextView passers_by;
    private TextView access_area;
    private TextView unit;
    private TextView expiryDateTextView;
    private TextView lead_people1;
    private TextView lead_people2;
    private TextView leading_person_unit;
    private ImageView card_img;
    private ImageView or_code;
    private ImageView img_color;
    private TextView card_status;
    private TextView faceSimilar;
    private View faceSimilarLayout;
    String idCode;
    String passid;
    String photo;
    String nickname;
    String areaDisplayCode;
    String companyName;
    String startDate;
    String expiryDate;
    String templateType;
    String leadingPeople;
    String leadingPeople1;
    String leadingPeople2;
    String leadingPeopleUnit;
    String status;
    String similar;
    // private OnDataReceivedListener listener;
    //
    // public interface OnDataReceivedListener {
    // void onDataReceived(String data);
    // }
    //
    // @Override
    // public void onAttach(@NonNull Context context) {
    // super.onAttach(context);
    // try {
    // listener = (OnDataReceivedListener) context;
    // } catch (ClassCastException e) {
    // throw new ClassCastException(context.toString() + " must implement OnDataReceivedListener");
    // }
    // }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.document3, container, false);
        passers_by = view.findViewById(R.id.passers_by);
        access_area = view.findViewById(R.id.access_area);

        unit = view.findViewById(R.id.unit);
        expiryDateTextView = view.findViewById(R.id.expiryDate);
        lead_people1 = view.findViewById(R.id.lead_people1);
        lead_people2 = view.findViewById(R.id.lead_people2);
        leading_person_unit = view.findViewById(R.id.leading_person_unit);
        card_img = view.findViewById(R.id.card_img);
        or_code = view.findViewById(R.id.or_code);
        img_color = view.findViewById(R.id.img_color);
        card_status = view.findViewById(R.id.card_status);
        faceSimilar = view.findViewById(R.id.faceSimilar);
        faceSimilarLayout = view.findViewById(R.id.faceSimilarLayout);
        // if (listener != null) {
        // listener.onDataReceived(""); // 触发接口回调
        // }

        if (getArguments() != null) {
            idCode = getArguments().getString("idCode");
            passid = getArguments().getString("passid");
            nickname = getArguments().getString("nickname");
            areaDisplayCode = getArguments().getString("areaDisplayCode");
            companyName = getArguments().getString("companyName");
            startDate = getArguments().getString("startDate");
            expiryDate = getArguments().getString("expiryDate");
            templateType = getArguments().getString("templateType");
            leadingPeople = getArguments().getString("leadingPeople");
            status = getArguments().getString("status");
            photo = getArguments().getString("photo");
            similar = getArguments().getString("faceSimilar");
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {
            }.getType();
            List<Map<String, String>> result = gson.fromJson(leadingPeople, listType);
            for (Map<String, String> map : result) {
                for (Map.Entry<String, String> entry : map.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
            if (result.size() == 0) {
                Log.i(TAG, "无引领人 ");
            } else if (result.size() == 1) {
                Map<String, String> map1 = result.get(0);
                if (map1 != null) {
                    String companyName = map1.get("companyName");
                    String nickname = map1.get("nickname");
                    if (companyName != null) {
                        leadingPeopleUnit = companyName;
                    }
                    if (nickname != null) {
                        leadingPeople1 = nickname;
                    }
                }
            } else if (result.size() == 2) {
                Map<String, String> map1 = result.get(0);
                if (map1 != null) {
                    String companyName = map1.get("companyName");
                    String nickname = map1.get("nickname");
                    if (companyName != null) {
                        leadingPeopleUnit = companyName;
                    }
                    if (nickname != null) {
                        leadingPeople1 = nickname;
                    }
                }
                Map<String, String> map2 = result.get(1);
                if (map2 != null) {
                    String companyName = map2.get("companyName");
                    String nickname = map2.get("nickname");
                    if (companyName != null) {
                        leadingPeopleUnit = companyName;
                    }
                    if (nickname != null) {
                        leadingPeople2 = nickname;
                    }
                }
            }
        }
        updatePage();
        return view;
    }

    // @Override
    // public void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // if (getArguments() == null) {
    // Log.e("Document2", "Arguments are null");
    // return;
    // }
    // if (getArguments() != null) {
    // idCode = getArguments().getString("idCode");
    // nickname = getArguments().getString("nickname");
    // areaDisplayCode = getArguments().getString("areaDisplayCode");
    // companyName = getArguments().getString("companyName");
    // startDate = getArguments().getString("startDate");
    // expiryDate = getArguments().getString("expiryDate");
    // templateType = getArguments().getString("templateType");
    // leadingPeople = getArguments().getString("leadingPeople");
    //
    // Gson gson = new Gson();
    //
    //
    // Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
    //
    //
    // List<Map<String, String>> result = gson.fromJson(leadingPeople, listType);
    //
    //
    // for (Map<String, String> map : result) {
    // for (Map.Entry<String, String> entry : map.entrySet()) {
    // System.out.println(entry.getKey() + ": " + entry.getValue());
    // }
    // }
    //
    // if (result.size()==0){
    // Log.i(TAG, "无引领人 ");
    // }else if (result.size()==1){
    // Map<String, String> map1 = result.get(0);
    // if (map1 != null){
    // String companyName = map1.get("companyName");
    // String nickname = map1.get("nickname");
    // if (companyName != null){
    // leadingPeopleUnit=companyName;
    // }
    // if (nickname != null){
    // leadingPeople1=nickname;
    // }
    // }
    // }else if (result.size()==2){
    // Map<String, String> map2 = result.get(1);
    // if (map2 != null){
    // String nickname = map2.get("nickname");
    // if (nickname != null){
    // leadingPeople2=nickname;
    // }
    // }
    // }
    //
    //
    //
    // Log.d("Document2", "nickname: "+nickname);
    // // 使用 message 数据
    //// Toast.makeText(getContext(), nickname, Toast.LENGTH_SHORT).show();
    // }
    // }

    public void updatePage() {
        // 更新页面信息
        if (leading_person_unit != null) {
            leading_person_unit.setText(leadingPeopleUnit);
        }
        if (leadingPeople1 != null) {
            lead_people1.setText(leadingPeople1);
        }
        if (leadingPeople2 != null) {
            lead_people2.setText(leadingPeople2);
        }
        if (templateType == "2") {
            img_color.setImageResource(R.drawable.yellow_stripes);
        }
        if (passers_by != null) {
            passers_by.setText(nickname);
        }
        if (access_area != null) {
            access_area.setText(areaDisplayCode);
        }
        if (unit != null) {
            unit.setText(companyName);
        }
        if (expiryDateTextView != null) {
            expiryDateTextView.setText(startDate + "-" + expiryDate);
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
            faceSimilarLayout.setVisibility(View.GONE);
        } else {
            faceSimilar.setText(similar);
        }

        // 根据 idCode 去本地加载图片
        // String s = Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode + ".jpg";
        // Bitmap bitmap = ImageUtils.loadBitmapFromPath(s);

        // Bitmap bitmap = ImageDownloader.loadAndDecryptImage(passid, getActivity());

        // GlideApp.with(getActivity()).load(AESUtils.getPhotoPath(passid)).into(card_img);

        ALog.e(ArcFaceApplication.getApplication().isOffLine());
        if (ArcFaceApplication.getApplication().isOffLine() || ObjectUtils.isEmpty(photo)) { // 加载本地加密文件
            GlideApp.with(getActivity()).load(AESUtils.getPhotoPath(passid)).into(card_img);
        } else {
            Glide.with(getActivity()).load(photo).into(card_img);
        }
        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // File directory2 = new File(ArcFaceApplication.getApplication().getExternalFilesDir(null), "photo");// 应用的私有目录
        // if (!directory2.exists()) {
        // directory2.mkdirs();
        // }
        // boolean result = ImageDownloader.downloadImage(directory2,
        // "https://obs-digitalpass-prod.caacsri.com/08102108e20b3cfdd01f47d300ad52ae93d5f7243e9e1ab981f3e095093dabea.jpg",
        // "1872189413787062274", "赵瑞娟");
        // if (!result) {
        // ALog.e("下载失敗 photo：赵瑞娟");
        // return null;
        // }
        //
        // // Glide.with(getActivity()).load(
        // //
        // "https://obs-digitalpass-prod.caacsri.com/08102108e20b3cfdd01f47d300ad52ae93d5f7243e9e1ab981f3e095093dabea.jpg")
        // // .into(card_img);
        //
        // // GlideApp.with(getActivity()).load(AESUtils.getPhotoPath("1872189413787062274")).into(card_img);
        // return null;
        // }
        //
        // @Override
        // public void onSuccess(String result) {
        // // Glide.with(getActivity()).load(AESUtils.getPhotoPath("1872189413787062274")).into(card_img);
        // GlideApp.with(getActivity()).load(AESUtils.getPhotoPath("1872189413787062274")).into(card_img);
        // }
        // });

        // Bitmap bitmap = AESUtils.decryptPhotoFileToBitmap(passid);
        // if (bitmap != null) {
        // Log.d(TAG, "获取图片 bitmap: " + bitmap.toString());
        // if (card_img != null) {
        // card_img.setImageBitmap(bitmap);
        // }
        // } else {
        // Log.e(TAG, "图片加载失败: " + passid);
        // }

        Bitmap bitmap1 = generateQRCodeBitmap(passid, 500);
        if (bitmap1 != null) {
            if (or_code != null) {
                or_code.setImageBitmap(bitmap1);
            }
        } else {
            Log.e(TAG, "二维码生成失败: " + passid);
        }
    }

    private Bitmap generateQRCodeBitmap(String text, int size) {
        try {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, size, size);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y,
                            bitMatrix.get(x, y) ? android.graphics.Color.BLACK : android.graphics.Color.WHITE);
                }
            }

            return bitmap;
        } catch (WriterException e) {
            Log.e("MainActivity", "Error generating QR code", e);
            return null;
        }
    }
}
