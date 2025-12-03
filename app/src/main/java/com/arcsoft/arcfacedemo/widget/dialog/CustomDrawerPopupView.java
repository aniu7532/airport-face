package com.arcsoft.arcfacedemo.widget.dialog;

import static com.blankj.utilcode.util.ActivityUtils.startActivity;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectJinActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectYuanActivity;
import com.arcsoft.arcfacedemo.ui.activity.LivenessDetectYuanAndJinActivity;
import com.arcsoft.arcfacedemo.ui.activity.LoginActivity;
import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DialogUtils;
import com.arcsoft.arcfacedemo.util.LogUploadUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.core.DrawerPopupView;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnInputConfirmListener;
import com.lxj.xpopup.interfaces.OnSelectListener;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

/**
 * Description: 自定义抽屉弹窗
 * Create by dance, at 2018/12/20
 */
public class CustomDrawerPopupView extends DrawerPopupView {
    TextView text;

    public CustomDrawerPopupView(@NonNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_draw;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

        TextView tvPhone = findViewById(R.id.tvPhone);
        tvPhone.setText(SPUtils.getInstance().getString("mobile", ""));
        TextView tvInOut = findViewById(R.id.tvInOut);
        TextView tvChayan = findViewById(R.id.tvChayan);
		TextView tvCanshu = findViewById(R.id.tvCanshu);
        TextView tvTipsLoc = findViewById(R.id.tvTipsLoc);

        TextView tvBili = findViewById(R.id.tvBili);
        TextView tvDelete = findViewById(R.id.tvDelete);
        TextView tvWenan = findViewById(R.id.tvWenan);
        TextView tvVersion = findViewById(R.id.tvVersion);
        TextView tvUploadLog = findViewById(R.id.tvUploadLog);
        TextView tvGotoLuancher = findViewById(R.id.tvGotoLuancher);

        TextView tvGotoSetting = findViewById(R.id.tvGotoSetting);

        Button btnExit = findViewById(R.id.btnExit);
        tvVersion.setText(AppUtils.getAppVersionName().replace("-debug", ""));
        tvInOut.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] list = { "进控制区", "出控制区" };
                int direction = SPUtils.getInstance().getInt("direction", 1);
                new XPopup.Builder(getContext()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                        .asCenterList("选择进出", list, null, direction == 1 ? 0 : 1, new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                SPUtils.getInstance().put("direction", position == 0 ? 1 : -1);
                                ALog.e("direction:" + SPUtils.getInstance().getInt("direction", 0));
                                if (!(ActivityUtils.getTopActivity() instanceof LoginActivity)) {
                                    ActivityUtils.startActivity(LoginActivity.class);

                                    ActivityUtils.finishOtherActivities(LivenessDetectActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectJinActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectYuanActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectYuanAndJinActivity.class);
                                    ActivityUtils.finishOtherActivities(RegisterAndRecognizeActivity.class);
                                }
                                dismiss();
                            }
                        }).show();
            }
        });

        tvChayan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] list = { "通行证（短距）+人脸", "通行证（长距）+人脸", "通行证（长距+短距）+人脸", "人脸" };
                int checkType = SPUtils.getInstance().getInt("checkType", 0);
                new XPopup.Builder(getContext()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                        .asCenterList("查验模式", list, null, checkType, new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                SPUtils.getInstance().put("checkType", position);

                                if (!(ActivityUtils.getTopActivity() instanceof LoginActivity)) {
                                    ActivityUtils.startActivity(LoginActivity.class);

                                    ActivityUtils.finishOtherActivities(LivenessDetectActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectJinActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectYuanActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectYuanAndJinActivity.class);
                                    ActivityUtils.finishOtherActivities(RegisterAndRecognizeActivity.class);
                                }

                                dismiss();
                            }
                        }).show();
            }
        });

		tvBili.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new XPopup.Builder(getContext()).isDestroyOnDismiss(true)
					.asInputConfirm("人脸识别最小比例", "", new OnInputConfirmListener() {
						@Override
						public void onConfirm(String text) {
							boolean success = ConfigUtil.commitRecognizeScale(getContext(), text);
							if (!success) {
								ToastUtils.showShort("设置失败，请输入整数");
							} else {
								if (!(ActivityUtils.getTopActivity() instanceof LoginActivity)) {
									ActivityUtils.startActivity(LoginActivity.class);
									ActivityUtils.finishOtherActivities(LivenessDetectActivity.class);
									ActivityUtils.finishOtherActivities(LivenessDetectJinActivity.class);
									ActivityUtils.finishOtherActivities(LivenessDetectYuanActivity.class);
									ActivityUtils.finishOtherActivities(LivenessDetectYuanAndJinActivity.class);
									ActivityUtils.finishOtherActivities(RegisterAndRecognizeActivity.class);
								}
							}
						}
					})
					.show();
			}
		});

        tvTipsLoc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String[] list = { "左下", "左上", "右下", "右上" };
                int checkType = SPUtils.getInstance().getInt("tipsLoc", 0);
                new XPopup.Builder(getContext()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                        .asCenterList("证件提示窗口位置", list, null, checkType, new OnSelectListener() {
                            @Override
                            public void onSelect(int position, String text) {
                                SPUtils.getInstance().put("tipsLoc", position);
                                if (!(ActivityUtils.getTopActivity() instanceof LoginActivity)) {
                                    ActivityUtils.startActivity(LoginActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectJinActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectYuanActivity.class);
                                    ActivityUtils.finishOtherActivities(LivenessDetectYuanAndJinActivity.class);
                                    ActivityUtils.finishOtherActivities(RegisterAndRecognizeActivity.class);
                                }
                                dismiss();
                            }
                        }).show();
            }
        });

        tvCanshu.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new XPopup.Builder(getContext()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                        .asCustom(new AppKeyPopDialog(getContext())).show();
            }
        });
        tvDelete.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.startConfirmDialog(getContext(), "", "确认删除本地缓存数据吗？", new DialogUtils.ConfirmListener() {
                    @Override
                    public void onConfirm() {
                        BasePopupView popupView = new XPopup.Builder(getContext()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                                .asLoading("正在删除，请耐心等待...", LoadingPopupView.Style.ProgressBar).show();
                        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
                            @Override
                            public Boolean doInBackground() throws Throwable {
                                boolean result = FileUtils
                                        .delete("/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/photo");
                                if (result) {
                                    ALog.e("删除证件照片成功");
                                } else {
                                    ALog.e("删除证件照片失败");
                                    return false;
                                }
                                result = FileUtils.delete(
                                        "/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/register");
                                if (result) {
                                    ALog.e("删除缓存查验照片成功");
                                } else {
                                    ALog.e("删除缓存查验照片失败");
                                    return false;
                                }

                                result = FileUtils.delete(
                                        "/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/records");
                                if (result) {
                                    ALog.e("删除通行记录查验照片成功");
                                } else {
                                    ALog.e("删除通行记录查验照片失败");
                                    return false;
                                }

                                result = FileUtils.delete(
                                        "/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/Pictures/faceDB");
                                if (result) {
                                    ALog.e("删除人脸注册照片成功");
                                } else {
                                    ALog.e("删除人脸注册照片失败");
                                    return false;
                                }

                                result = FileUtils.delete(
                                        "/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/db/airportDb.db");
                                if (result) {
                                    ALog.e("删除本地数据库文件成功");
                                } else {
                                    ALog.e("删除本地数据库文件失败");
                                    return false;
                                }
                                result = FileUtils.delete(
                                        "/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/database/faceDB.db");
                                if (result) {
                                    ALog.e("删除人脸数据库文件成功");
                                } else {
                                    ALog.e("删除人脸数据库文件失败");
                                    return false;
                                }

                                // List<File> list = FileUtils.listFilesInDir(getContext().getExternalFilesDir(null));
                                // for (File file : list) {
                                // ALog.e(file.getAbsolutePath());
                                // }
                                //
                                // list =
                                // FileUtils.listFilesInDir("/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/database");
                                // for (File file : list) {
                                // ALog.e(file.getAbsolutePath());
                                // }
                                //
                                // list =
                                // FileUtils.listFilesInDir("/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/log");
                                // for (File file : list) {
                                // ALog.e(file.getAbsolutePath());
                                // }
                                //
                                // list =
                                // FileUtils.listFilesInDir("/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/db");
                                // for (File file : list) {
                                // ALog.e(file.getAbsolutePath());
                                // }
                                // list =
                                // FileUtils.listFilesInDir("/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/Pictures");
                                // for (File file : list) {
                                // ALog.e(file.getAbsolutePath());
                                // }
                                // boolean result =
                                // FileUtils.delete("/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/photo");
                                // for (File file : list) {
                                // ALog.e(file.getAbsolutePath());
                                // }
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/register
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/debugDump
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/collectLog
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/database
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/sangforlogs
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/log
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/db
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/crashLog
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/Pictures
                                /// storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/photo
                                return true;
                            }

                            @Override
                            public void onSuccess(Boolean result) {
                                if (result) {
                                    ToastUtils.showLong("删除本地缓存数据成功，请重启重新登录初始化");
                                    ThreadUtils.runOnUiThreadDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            AppUtils.exitApp();
                                        }
                                    }, 3000L);
                                } else {
                                    ToastUtils.showLong("删除本地缓存数据失败");
                                }
                                popupView.dismiss();
                            }

                            @Override
                            public void onFail(Throwable t) {
                                super.onFail(t);
                                ALog.e(t.getMessage());
                            }
                        });

                    }
                });
            }
        });

        tvWenan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                String wenan = SPUtils.getInstance().getString("wenan", "");

                DialogUtils.startInputConfirm(getContext(), "文案配置", "", wenan, "请输入文案内容",
                        new DialogUtils.OnInputListener() {
                            @Override
                            public void onConfirm(String text) {
                                SPUtils.getInstance().put("wenan", text);
                            }
                        }, new DialogUtils.CancelListener() {
                            @Override
                            public void onCancel() {

                            }
                        });

            }
        });

        tvGotoLuancher.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.startConfirmDialog(getContext(), "", "确认返回系统桌面？", new DialogUtils.ConfirmListener() {
                    @Override
                    public void onConfirm() {
                        // Intent intent =
                        // getContext().getPackageManager().getLaunchIntentForPackage("com.android.launcher3");
                        // if (intent != null) {
                        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        // startActivity(intent);
                        // }

                        try {
                            Intent intent = new Intent(Intent.ACTION_MAIN);
                            intent.addCategory(Intent.CATEGORY_HOME);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // 处理异常，例如显示一个错误消息或使用默认行为
                            Toast.makeText(getContext(), "无法找到主屏幕", Toast.LENGTH_SHORT).show();
                        }
                        // int screenSize = ScreenUtils.getScreenWidth();
                        // int typeDevice = screenSize > 800 ? 1 : 2;
                        // ALog.e("获取屏幕尺寸宽度:" + screenSize);
                        // if (typeDevice == 1) {
                        // AppUtils.launchApp("com.android.launcher3");
                        // } else {
                        // // ShellUtils.CommandResult result = ShellUtils.execCmd("adb shell dumpsys window | findstr
                        // // mCurrentFocus ", false);
                        // // ALog.e(result.toString());
                        // AppUtils.launchApp("com.android.launcher3");
                        // }
                    }
                });

            }
        });

        tvGotoSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.startConfirmDialog(getContext(), "", "确认进入系统设置？", new DialogUtils.ConfirmListener() {
                    @Override
                    public void onConfirm() {
                        Intent intent = new Intent(Settings.ACTION_SETTINGS);
                        startActivity(intent);
                    }
                });

            }
        });

        btnExit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.startConfirmDialog(getContext(), "", "确认注销登录吗？", new DialogUtils.ConfirmListener() {
                    @Override
                    public void onConfirm() {
                        if (!(ActivityUtils.getTopActivity() instanceof LoginActivity)) {
                            ActivityUtils.startActivity(LoginActivity.class);

                            ActivityUtils.finishOtherActivities(LivenessDetectActivity.class);
                            ActivityUtils.finishOtherActivities(LivenessDetectJinActivity.class);
                            ActivityUtils.finishOtherActivities(LivenessDetectYuanActivity.class);
                            ActivityUtils.finishOtherActivities(LivenessDetectYuanAndJinActivity.class);
                            ActivityUtils.finishOtherActivities(RegisterAndRecognizeActivity.class);
                        }
                        dismiss();
                    }
                });

            }
        });

        tvUploadLog.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogUtils.startConfirmDialog(getContext(), "", "确认上传本地日志？", new DialogUtils.ConfirmListener() {
                    @Override
                    public void onConfirm() {
                        ToastUtils.showLong("请稍后，正在压缩文件...");
                        // BasePopupView popupView = new XPopup.Builder(getContext()).isDestroyOnDismiss(true) //
                        // 对于只使用一次的弹窗，推荐设置这个
                        // .asLoading("正在上传本地日志，请耐心等待...", LoadingPopupView.Style.ProgressBar).show();
                        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
                            @Override
                            public Boolean doInBackground() throws Throwable {
                                LogUploadUtils.upload(getActivity());
                                return true;
                            }

                            @Override
                            public void onSuccess(Boolean result) {
                                // popupView.dismiss();
                            }
                        });
                    }
                });

            }
        });

    }

    @Override
    protected void onShow() {
        super.onShow();
    }

    @Override
    protected void onDismiss() {
        super.onDismiss();
    }
}
