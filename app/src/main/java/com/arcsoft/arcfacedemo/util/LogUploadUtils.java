package com.arcsoft.arcfacedemo.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.data.http.JsonCallback;
import com.arcsoft.arcfacedemo.entity.Base2;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import android.app.Activity;
import android.content.Context;

public class LogUploadUtils {

    public static void upload(Activity activity) {
        if (ObjectUtils.isEmpty(ALog.getLogFiles())) {
            return;
        }
        BasePopupView popupView = new XPopup.Builder(ActivityUtils.getTopActivity())
                // .isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .asLoading("正在上传本地日志，请耐心等待...", LoadingPopupView.Style.ProgressBar).show();

        InfoStorage infoStorage = new InfoStorage(activity);
        String loginName = infoStorage.getString("loginName", "");

        String zero_trust_username = infoStorage.getString("zero_trust_username", "");

        // 日志文件的命名按日期+时间+登录账号+编号来处理？？
        String name = AppUtils.getAppPackageName() + "_" + AppUtils.getAppVersionName() + "_" + zero_trust_username
                + "_" + loginName + "_"
                + TimeUtils.getNowString(new SimpleDateFormat("MM_dd_HH_mm_ss", Locale.getDefault())) + ".zip";
        ALog.e(name);
        // zip data
        File outFile = new File(ArcFaceApplication.getApplication().getWlyCacheDir(), name);
        if (outFile.exists()) {
            outFile.delete();
        }

        boolean result = false;
        try {

            List<File> list = ALog.getLogFiles();
            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName()); // 升序排序
                }
            });
            List<File> list1 = new ArrayList<>();
            // list1.add(list.get(0));
            // list1.add(list.get(1));
            list1.addAll(list);
            while (true) {
                result = ZipUtils.zipFiles(list1, outFile);
                if (result && FileUtils.getLength(outFile) < 30 * 1024 * 1024) {
                    break;
                }
                list1.remove(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!result) {
            popupView.dismiss();
            return;
        }
        if (!outFile.exists() || !outFile.canRead() || outFile.length() <= 0) {
            popupView.dismiss();
            return;
        }
        // HttpInitUtils.init(ArcFaceApplication.getApplication(), 5 * 60 * 1000L);
        ALog.e("FileUtils.getLength:" + FileUtils.getLength(outFile));
        OkGo.<Base2<String>> post("http://116.63.141.207:6666/UploadLog/UpLogAsName").tag(activity).isMultipart(true)
                .params("fileName", name).params("filePath", "D:\\Download\\android\\Log").params("", outFile)
                .execute(new JsonCallback<Base2<String>>() {
                    @Override
                    public void onStart(Request<Base2<String>, ? extends Request> request) {
                        super.onStart(request);
                        popupView.show();
                    }

                    @Override
                    public void onSuccess(Response<Base2<String>> response) {
                        if (response.body() == null) {
                            ALog.i("上传日志失败");
                            return;
                        }
                        ALog.e(response.body().getMessage());
                        ToastUtils.showShort(response.body().getMessage());
                        if (response.body().getCode() == 1) {
                            // ToastUtils.showShort(response.body().getMessage());
                        } else {
                            // ToastUtils.showShort(response.body().getMessage());
                        }
                    }

                    @Override
                    public void uploadProgress(Progress progress) {
                        super.uploadProgress(progress);
                        ALog.e("totalSize:" + progress.totalSize + ", currentSize:" + progress.currentSize
                                + ", fraction:" + progress.fraction);
                    }

                    @Override
                    public void onError(Response<Base2<String>> response) {
                        super.onError(response);
                        ALog.e(response.getException());
                        ToastUtils.showShort("上传日志失败,网络异常");
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        popupView.dismiss();
                        outFile.delete();
                    }
                });
    }

    public static void upload(Context context) {
        if (ObjectUtils.isEmpty(ALog.getLogFiles())) {
            return;
        }
        InfoStorage infoStorage = new InfoStorage(context);
        String loginName = infoStorage.getString("loginName", "");
        String zero_trust_username = infoStorage.getString("zero_trust_username", "");
        // 日志文件的命名按日期+时间+登录账号+编号来处理？？
        String name = AppUtils.getAppPackageName() + "_" + AppUtils.getAppVersionName() + "_" + zero_trust_username
                + "_" + loginName + "_"
                + TimeUtils.getNowString(new SimpleDateFormat("MM_dd_HH_mm_ss", Locale.getDefault())) + ".zip";
        ALog.e(name);
        // zip data
        File outFile = new File(ArcFaceApplication.getApplication().getWlyCacheDir(), name);
        if (outFile.exists()) {
            outFile.delete();
        }

        boolean result = false;
        try {

            List<File> list = ALog.getLogFiles();
            Collections.sort(list, new Comparator<File>() {
                @Override
                public int compare(File file1, File file2) {
                    return file1.getName().compareTo(file2.getName()); // 升序排序
                }
            });
            List<File> list1 = new ArrayList<>();
            list1.addAll(list);
            while (true) {
                result = ZipUtils.zipFiles(list1, outFile);
                if (result && FileUtils.getLength(outFile) < 30 * 1024 * 1024) {
                    break;
                }
                list1.remove(0);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!result) {
            return;
        }
        if (!outFile.exists() || !outFile.canRead() || outFile.length() <= 0) {
            return;
        }

        ALog.e("FileUtils.getLength:" + FileUtils.getLength(outFile));
        OkGo.<Base2<String>> post("http://116.63.141.207:6666/UploadLog/UpLogAsName").tag(context).isMultipart(true)
                .params("fileName", name).params("filePath", "D:\\Download\\android\\Log").params("", outFile)
                .execute(new JsonCallback<Base2<String>>() {
                    @Override
                    public void onSuccess(Response<Base2<String>> response) {
                        if (response.body() == null) {
                            ALog.i("上传日志失败");
                            return;
                        }
                        ALog.e(response.body().getMessage());
                        if (response.body().getCode() == 1) {
                            // ToastUtils.showShort(response.body().getMessage());
                        } else {
                            // ToastUtils.showShort(response.body().getMessage());
                        }
                    }

                    @Override
                    public void uploadProgress(Progress progress) {
                        super.uploadProgress(progress);
                        ALog.e("totalSize:" + progress.totalSize + ", currentSize:" + progress.currentSize
                                + ", fraction:" + progress.fraction);
                    }

                    @Override
                    public void onError(Response<Base2<String>> response) {
                        super.onError(response);
                        ALog.e(response.getException());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        outFile.delete();
                    }
                });
    }

    public static void upload(Context context, String data) {
        if (ObjectUtils.isEmpty(data)) {
            return;
        }
        File intFile = new File(ArcFaceApplication.getApplication().getWlyCacheDir(), "temp.txt");
        FileIOUtils.writeFileFromString(intFile, data);

        // 日志文件的命名按日期+时间+登录账号+编号来处理？？
        String name = AppUtils.getAppPackageName() + "_" + AppUtils.getAppVersionName() + "_"
                + TimeUtils.getNowString(new SimpleDateFormat("MM_dd_HH_mm_ss", Locale.getDefault())) + ".zip";
        ALog.e(name);

        // zip data
        File outFile = new File(ArcFaceApplication.getApplication().getWlyCacheDir(), name);

        boolean result = false;
        try {

            result = ZipUtils.zipFile(intFile, outFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (!result) {
            return;
        }
        if (!outFile.exists() || !outFile.canRead() || outFile.length() <= 0) {
            return;
        }

        ALog.e("FileUtils.getLength:" + FileUtils.getLength(outFile));
        OkGo.<Base2<String>> post("http://116.63.141.207:6666/UploadLog/UpLogAsName").tag(context).isMultipart(true)
                .params("fileName", name).params("filePath", "D:\\Download\\android\\Log").params("", outFile)
                .execute(new JsonCallback<Base2<String>>() {
                    @Override
                    public void onSuccess(Response<Base2<String>> response) {
                        if (response.body() == null) {
                            ALog.i("上传日志失败");
                            return;
                        }
                        ALog.e(response.body().getMessage());
                        if (response.body().getCode() == 1) {
                            // ToastUtils.showShort(response.body().getMessage());
                        } else {
                            // ToastUtils.showShort(response.body().getMessage());
                        }
                    }

                    @Override
                    public void uploadProgress(Progress progress) {
                        super.uploadProgress(progress);
                        ALog.e("totalSize:" + progress.totalSize + ", currentSize:" + progress.currentSize
                                + ", fraction:" + progress.fraction);
                    }

                    @Override
                    public void onError(Response<Base2<String>> response) {
                        super.onError(response);
                        ALog.e(response.getException());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        intFile.delete();
                        outFile.delete();
                    }
                });
    }
}
