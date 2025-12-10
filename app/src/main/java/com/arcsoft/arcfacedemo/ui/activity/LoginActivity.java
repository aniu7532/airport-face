package com.arcsoft.arcfacedemo.ui.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.BuildConfig;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.common.Constants;
import com.arcsoft.arcfacedemo.databinding.ActivityLoginBinding;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.entity.ApiResponse;
import com.arcsoft.arcfacedemo.entity.Login;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.entity.LongPassCards;
import com.arcsoft.arcfacedemo.entity.User;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.ui.callback.BatchRegisterCallback;
import com.arcsoft.arcfacedemo.ui.viewmodel.FacePhotoViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.Converters;
import com.arcsoft.arcfacedemo.util.DeviceUtils;
import com.arcsoft.arcfacedemo.util.DialogUtils;
import com.arcsoft.arcfacedemo.util.ImageDownloader;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.arcsoft.arcfacedemo.util.LogUtils;
import com.arcsoft.arcfacedemo.util.PermissionUtils;
import com.arcsoft.arcfacedemo.util.SmallTask;
import com.arcsoft.arcfacedemo.util.WeakHandler;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.arcfacedemo.widget.dialog.CustomDrawerPopupView;
import com.arcsoft.arcfacedemo.widget.dialog.LogingPopDialog;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lxj.xpopup.interfaces.XPopupCallback;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.sangfor.sdk.SFUemSDK;
import com.sangfor.sdk.base.SFAuthResultListener;
import com.sangfor.sdk.base.SFAuthType;
import com.sangfor.sdk.base.SFBaseMessage;
import com.sangfor.sdk.base.SFConstants;
import com.sangfor.sdk.base.SFSDKExtras;
import com.sangfor.sdk.base.SFSDKFlags;
import com.sangfor.sdk.base.SFSDKMode;
import com.sangfor.sdk.base.SFSetSpaConfigListener;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * 使用：登录，获取通行证
 */

public class LoginActivity extends BaseActivity
        implements View.OnClickListener, ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "LoginActivity";
    private EditText editTextUsername;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonCancel;
    private Button button_test;
    private Button button_test2;
    private Button button_test3;
    private Button button_test4;
    private Button button_test5;
    private Dialog progressDialog;
    private ImageView to_regis;
    private ImageView btnGo;

    // private RadioGroup radioGroup;
    // private RadioButton radioIn, radioOut;
    private TextView android_id;

    // Gson gson = new Gson(); // 创建 Gson 实例
    // private YinchuanAirportDB db;
    LongTermPassDao longTermPassDao;
    private int page = 1; // 将 page 声明为成员变量
    private static final int PAGE_SIZE = 20; // 每页大小
    private CountDownLatch latch;
    private FacePhotoViewModel facePhotoViewModel;
    private static final String[] NEEDED_PERMISSIONS =
            new String[] { Manifest.permission.CAMERA, Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.INTERNET, Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_FINE_LOCATION };
    private ActivityLoginBinding binding; // 添加 binding 变量
    ProgressBar progressBar;
    TextView textViewMessage;
    private int progress = 0;
    private static int updatePage = 1;
    private static int UPDATE_PAGE_SIZE = 10;
    private static boolean updateNext = true;
    public String deviceId;
    InfoStorage infoStorage;

    private WeakHandler handler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
            case 1:
                // 保存账号和密码到全局变量
                infoStorage.saveString("zero_trust_username", editTextUsername.getText().toString().trim());
                infoStorage.saveString("zero_trust_password", editTextPassword.getText().toString().trim());
                login();// 后台登录
                break;
            case 2:
                if (!autoTicket) {
                    handler.removeMessages(1);
                    handler.sendEmptyMessageDelayed(1, 10L);
                }
                break;
            default:
                break;
            }
            return false;
        }
    });
    private Runnable runnable;

    String oldPassword;
    String newPassword;
    boolean loging;

    boolean auto;
    boolean autoTicket;
    private int clickCount = 0; // 点击计数
    private static final int REQUIRED_CLICKS = 5; // 需要的点击次数
    LogingPopDialog logingPopDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login); // 使用 DataBindingUtil 初始化 binding

        ((TextView) findViewById(R.id.tvVersion)).setText(AppUtils.getAppVersionName().replace("-debug", "") + "");
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCancel = findViewById(R.id.buttonCancel);
        button_test = findViewById(R.id.button_test);
        button_test2 = findViewById(R.id.button_test2);
        button_test4 = findViewById(R.id.button_test4);
        android_id = findViewById(R.id.android_id);
        to_regis = findViewById(R.id.to_regis);

        btnGo = findViewById(R.id.btnGo);
        buttonLogin.setOnClickListener(this);
        buttonCancel.setOnClickListener(this);
        button_test.setOnClickListener(this);
        button_test2.setOnClickListener(this);
        button_test4.setOnClickListener(this);
        to_regis.setOnClickListener(this);
        // to_regis.setOnLongClickListener(new View.OnLongClickListener() {
        // @Override
        // public boolean onLongClick(View view) {
        // new XPopup.Builder(LoginActivity.this).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
        // .popupPosition(PopupPosition.Left)// 右边
        // .hasStatusBarShadow(true) // 启用状态栏阴影
        // .asCustom(new CustomDrawerPopupView(LoginActivity.this)).show();
        // return false;
        // }
        // });

        btnGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++; // 每次点击时增加计数
                // 检测点击次数是否达到目标
                if (clickCount >= REQUIRED_CLICKS) {
                    // 达到目标后可以触发某项操作
                    clickCount = 0; // 重置计数
                    handler.removeCallbacksAndMessages(null);
                    new XPopup.Builder(LoginActivity.this).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                            .popupPosition(PopupPosition.Left)// 右边
                            .setPopupCallback(new XPopupCallback() {
                                @Override
                                public void onCreated(BasePopupView popupView) {

                                }

                                @Override
                                public void beforeShow(BasePopupView popupView) {

                                }

                                @Override
                                public void onShow(BasePopupView popupView) {

                                }

                                @Override
                                public void onDismiss(BasePopupView popupView) {
                                    startLogin(100);
                                }

                                @Override
                                public void beforeDismiss(BasePopupView popupView) {

                                }

                                @Override
                                public boolean onBackPressed(BasePopupView popupView) {
                                    return false;
                                }

                                @Override
                                public void onKeyBoardStateChanged(BasePopupView popupView, int height) {

                                }

                                @Override
                                public void onDrag(BasePopupView popupView, int value, float percent,
                                        boolean upOrLeft) {

                                }

                                @Override
                                public void onClickOutside(BasePopupView popupView) {

                                }
                            }).hasStatusBarShadow(true) // 启用状态栏阴影
                            .asCustom(new CustomDrawerPopupView(LoginActivity.this)).show();

                }
                handler.removeCallbacksAndMessages(null);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        clickCount = 0;
                    }
                }, 5000);
            }
        });

        // button_test5.setOnClickListener(this);
        // editTextUsername.setText("ls001");//默认用户名为ls001
        // editTextPassword.setText("admin123");//默认密码为admin123

		if (BuildConfig.DEBUG) {
			deviceId = "a4835903298640a0";
		} else {
			deviceId = DeviceUtils.getDeviceId(this);
		}

        android_id.setText(deviceId);
        ALog.d("Android ID: " + deviceId);
        String deviceModel = DeviceUtils.getDeviceModel();
        ALog.e("deviceModel: " + deviceModel);
        int[] screenSize = DeviceUtils.getScreenSize(this);
        ALog.d("屏幕宽度: " + screenSize[0] + "px，屏幕高度: " + screenSize[1] + "px");

        // 零信任初始化监听
        initZeroTrust();

        // SharedPreferences工具类
        infoStorage = new InfoStorage(this);
        String zero_trust_username = infoStorage.getString("zero_trust_username", Constants.ZERO_USERNAME);
        editTextUsername.setText(zero_trust_username);

        String zero_trust_password = infoStorage.getString("zero_trust_password", Constants.ZERO_PASSWORD);
        editTextPassword.setText(zero_trust_password);

        // 获取存储权限
        obtainStoragePermissions();

        initRadio();// 初始化按钮

        copyAndroidID();// 复制deviceId到粘贴板
        // 写入日志
        LogUtils.writeLogToFile(this, "这是一条测试日志");
        // handler = new Handler(Looper.getMainLooper());
        // handler.postDelayed(new Runnable() {
        // @Override
        // public void run() {
        // // // ALog.e("AppUtils.relaunchApp(true)");
        // // // AppUtils.relaunchApp(true);
        // // ALog.e("AppUtils.relaunchApp(true)");
        // // // AppUtils.relaunchApp(true);
        // // Intent intent = IntentUtils.getLaunchAppIntent(Utils.getApp().getPackageName());
        // // if (intent == null) {
        // // Log.e("AppUtils", "Didn't exist launcher activity.");
        // // return;
        // // }
        // // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
        // // | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // // intent.putExtra("auto", true);
        // // Utils.getApp().startActivity(intent);
        // // android.os.Process.killProcess(android.os.Process.myPid());
        // // System.exit(0);
        //
        // // String apkPath = "/sdcard/v1.apk";
        // // if (!(new File(apkPath)).exists()) {
        // // Toast.makeText(LoginActivity.this, "apk not exist", Toast.LENGTH_SHORT).show();
        // // return;
        // // }
        // // File file = new File(apkPath);
        // // Uri uri = Uri.fromFile(file);
        // // if (uri != null) {
        // // Intent intent = new Intent(Intent.ACTION_VIEW);
        // // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        // // String authority = LoginActivity.this.getPackageName();
        // // Uri contentUri =
        // // FileProvider.getUriForFile(LoginActivity.this, authority + ".fileprovider", file);
        // // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        // // intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        // // } else {
        // // intent.setDataAndType(uri, "application/vnd.android.package-archive");
        // // }
        // // LoginActivity.this.startActivity(intent);
        // // }
        //
        // // // 安装 test.apk
        // // MyManager manager = MyManager.getInstance(LoginActivity.this);
        // // manager.reboot();
        // // 静默安装 YiShentTest.apk,安装成功后打开 apk
        //
        // int screenSize = ScreenUtils.getScreenWidth();
        // int typeDevice = screenSize > 800 ? 1 : 2;
        // ALog.d("获取屏幕尺寸宽度:" + screenSize);
        // if (typeDevice == 1) {
        // // 声明manager对象
        // @SuppressLint("WrongConstant")
        // ZysjSystemManager manager = (ZysjSystemManager) getSystemService("zysj");
        // int result = manager.zYRebootSys();
        // ALog.e("zYRebootSys result:" + result);
        // } else {
        // // 安装 test.apk
        // MyManager manager = MyManager.getInstance(LoginActivity.this);
        // manager.reboot();
        // }
        // }
        // }, 30000L);

        // editTextUsername.setText("18683256800");// 默认用户名为ls001
        // editTextPassword.setText("pia_123456");// 默认密码为admin123

        if (getIntent().hasExtra("auto") && getIntent().getBooleanExtra("auto", false)) {
            ALog.e("auto");
            ActivityUtils.finishAllActivitiesExceptNewest();
        }

        // handler.postDelayed(new Runnable() {
        // @Override
        // public void run() {
        // auto = true;
        // buttonLogin.performClick();
        // logingPopDialog = new LogingPopDialog(getActivity());
        // new XPopup.Builder(getActivity()).dismissOnTouchOutside(true).asCustom(logingPopDialog).show();
        // }
        // }, 10 * 1000L);
        //

        startLogin(10 * 1000);

        // showInfoToast("10秒后自动登录,可点击取消按钮取消");
        // check();
        // String spa = SPUtils.getInstance().getString("spa", "y121-fbcq-BPXz");
        // DialogUtils.startInputConfirm(LoginActivity.this, "安全码验证失败", "请核对后再次验证", spa, "请输入文案内容",
        // new DialogUtils.OnInputListener() {
        // @Override
        // public void onConfirm(String text) {
        // SPUtils.getInstance().put("spa", text);
        // buttonLogin.performClick();
        // }
        // }, new DialogUtils.CancelListener() {
        // @Override
        // public void onCancel() {
        //
        // }
        // });
        binding.inputLayout.setVisibility(View.INVISIBLE);
        ALog.e("getAppId:" + ConfigUtil.getAppId(this));
        ALog.e("getSdkKey:" + ConfigUtil.getSdkKey(this));
        ALog.e("getActiveKey:" + ConfigUtil.getActiveKey(this));

        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // YinchuanAirportDB db = ArcFaceApplication.getApplication().getDb();
        // int count = db.longTermPassDao().getCount();
        // ALog.e("通行证数量 count:" + count);
        //
        // String time = db.longTermPassDao().getMaxUpdateTime();
        // ALog.e("通行证数量 getMaxUpdateTime:" + time);
        //
        // LongTermPass card = db.longTermPassDao().getById("1907693397930926081");
        //
        // if (ObjectUtils.isEmpty(card)) {
        // ALog.i("本地数据库未查询到: getById 1907693397930926081");
        // } else {
        // ALog.e(card.toString());
        // }
        //
        // LongTermPass card1 = db.longTermPassDao().getByApplyId("1907685091850051586");
        //
        // if (ObjectUtils.isEmpty(card1)) {
        // ALog.i("本地数据库未查询到 getByApplyId: 1907685091850051586");
        // } else {
        // ALog.e(card1.toString());
        // }
        //
        // LongTermPass card3 = db.longTermPassDao().getByApplyId("1909876155616845826");
        // if (ObjectUtils.isEmpty(card3)) {
        // ALog.i("本地数据库未查询到 getByApplyId: 1909876155616845826");
        // } else {
        // ALog.e(card3.toString());
        // }
        // card3 = db.longTermPassDao().getByNickname("方振");
        // if (ObjectUtils.isEmpty(card3)) {
        // ALog.i("本地数据库未查询到 getByNickname: 方振");
        // } else {
        // ALog.e(card3.toString());
        // }
        // card3 = db.longTermPassDao().getByNickname("杨振飞");
        // if (ObjectUtils.isEmpty(card3)) {
        // ALog.i("本地数据库未查询到 getByNickname: 杨振飞");
        // } else {
        // ALog.e(card3.toString());
        // }
        //
        // card3 = db.longTermPassDao().getByNickname("林旭");
        // if (ObjectUtils.isEmpty(card3)) {
        // ALog.i("本地数据库未查询到 getByNickname: 林旭");
        // } else {
        // ALog.e(card3.toString());
        // }
        //
        // card3 = db.longTermPassDao().getByNickname("张高伟");
        // if (ObjectUtils.isEmpty(card3)) {
        // ALog.i("本地数据库未查询到 getByNickname: 张高伟");
        // } else {
        // ALog.e(card3.toString());
        // }
        // card3 = db.longTermPassDao().getByNickname("李锐强");
        // if (ObjectUtils.isEmpty(card3)) {
        // ALog.i("本地数据库未查询到 getByNickname: 李锐强");
        // } else {
        // ALog.e(card3.toString());
        // }
        // return null;
        // }
        // });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initRadio();
    }

    @Override
    protected void onDestroy() {
        // if (facePhotoViewModel != null) {
        // facePhotoViewModel.release();
        // facePhotoViewModel = null;
        // }
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        auto = false;
        if (binding != null) {
            binding.unbind(); // 解除绑定
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        // 只有走回收流程的时候的那种onPause，isFinishing才为true
        if (isFinishing()) {
            // 取消认证回调
            /**
             * 注意： 清除回调建议放到onPause()方法而不是onDestroy()中，
             * 避免出现onDestroy()在onCreate()之后执行，onCreate注册的认证回调被onDestory清空的问题
             */
            SFUemSDK.getInstance().setAuthResultListener(null);
        }
    }

    @Override
    public void onGlobalLayout() {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            ALog.e("onGlobalLayout: 未授权");
        } else {
            ALog.d("onGlobalLayout: yij授权");
        }
    }

    void copyAndroidID() {

        // 获取 ClipboardManager
        ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        // 创建 ClipData
        ClipData clip = ClipData.newPlainText("label", deviceId);
        // 设置 ClipData 到 ClipboardManager
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
        }
    }

    /**
     * 获取MANAGE_EXTERNAL_STORAGE权限
     */
    void obtainStoragePermissions() {
        boolean b = PermissionUtils.hasManageExternalStoragePermission(this);
        if (!b) {
            PermissionUtils.requestManageExternalStoragePermission(this);
        }
    }

    private void showProgressDialog(String message) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        progressDialog = new Dialog(this);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.setCancelable(false); // 防止用户点击取消弹窗
        progressBar = progressDialog.findViewById(R.id.progressBar);
        textViewMessage = progressDialog.findViewById(R.id.textViewMessage);
        textViewMessage.setText(message);
        progressDialog.show();
    }

    private void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    void initRadio() {
        // // 获取单选按钮控件
        // radioGroup = findViewById(R.id.radioGroup);
        // radioIn = findViewById(R.id.radioIn);
        // radioOut = findViewById(R.id.radioOut);
        // String deviceDirection = infoStorage.getString("deviceDirection", "1");
        // if (deviceDirection.equals("-1")) {
        // radioOut.setChecked(true);
        // } else if (deviceDirection.equals("1")) {
        // radioIn.setChecked(true);
        // }

        int direction = SPUtils.getInstance().getInt("direction", 1);
        // if (direction == -1) {
        // radioOut.setChecked(true);
        // } else if (direction == 1) {
        // radioIn.setChecked(true);
        // }
        // // 监听选择变化
        // radioGroup.setOnCheckedChangeListener(this);
    }

    // @Override
    // public void onCheckedChanged(RadioGroup group, int checkedId) {
    // if (checkedId == R.id.radioIn) {
    // SPUtils.getInstance().put("direction", 1);
    // infoStorage.saveString("deviceDirection", "1");
    // } else if (checkedId == R.id.radioOut) {
    // SPUtils.getInstance().put("direction", -1);
    // infoStorage.saveString("deviceDirection", "-1");
    // }
    // }

    public void startLogin(int time) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                auto = true;
                buttonLogin.performClick();
                logingPopDialog = new LogingPopDialog(getActivity());
                new XPopup.Builder(getActivity()).dismissOnTouchOutside(true).asCustom(logingPopDialog).show();
            }
        }, time);
    }

    @Override
    public void onClick(View v) {
        handler.removeCallbacksAndMessages(null);
        if (v.getId() == R.id.buttonLogin) {
            handler.removeCallbacksAndMessages(null);
            if (!checkPermissions(NEEDED_PERMISSIONS)) {
                showToast("请允许权限");
                ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
                return;
            }
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            if (loging) {
                return;
            }
            loging = true;
            Toast.makeText(this, "零信任登录中", Toast.LENGTH_SHORT).show();

            ThreadUtils.executeByCached(new SmallTask() {
                @Override
                public String doInBackground() throws Throwable {
                    // if (NetworkUtils.isAvailableByDns()) {
                    // showWarningToast("无法访问互联网！");
                    // return null;
                    // }
                    // {"loginAddress":"https://10.242.4.236","spasecret":"fc14116030915409"}
                    // String spa = SPUtils.getInstance().getString("spa", "ZUVj-Lj9N-mWiw");
                    String spa = "ZUVj-Lj9N-mWiw";
                    // String spaConfig =
                    // "{\"loginAddress\":\"https://171.221.216.108:44433\",\"spasecret\":\"" + spa + "\"}";
                    String spaConfig =
                            "{\"loginAddress\":\"https://kzqtxzvpn.caacsri.com:9998\",\"spaSecret\":\"" + spa + "\"}";
                    ALog.e("spaConfig:" + spaConfig);
                    SFUemSDK.setSpaConfig(spaConfig, new SFSetSpaConfigListener() {
                        /**
                         * 设置SPA的结果回调
                         * @param result 对SPA配置解析到的结果, 登录的URL地址
                         * @param error 如果error.mErrCode不为0,代表设置配置遇到了错误
                         */
                        @Override
                        public void onSetSpaConfig(String result, SFBaseMessage error) {
                            ALog.e("result:" + result + ",error:" + error.toString());
                            switch ((int) error.mErrCode) {
                            case 0:
                                ALog.e("auto：" + auto);
                                if (auto) {
                                    autoTicket = false;
                                    // handler.removeMessages(2);
                                    // handler.sendEmptyMessageDelayed(2, 5000L);
                                    boolean autoTicketSuccess = SFUemSDK.getInstance().startAutoTicket();
                                    autoTicket = true;
                                    // handler.removeMessages(2);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            logingPopDialog.dismiss();
                                        }
                                    });

                                    if (autoTicketSuccess) {
                                        showInfoToast("免密登陆成功 ");
                                        // 上线成功，此时用户就可以访问资源了
                                        ALog.e("免密登陆成功 ");
                                        handler.removeMessages(1);
                                        handler.sendEmptyMessageDelayed(1, 10L);
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                binding.inputLayout.setVisibility(View.VISIBLE);
                                            }
                                        });
                                        loging = false;
                                        auto = false;
                                        showErrorToast("免密登陆失败 ");
                                        // 上线失败需要调主认证
                                        ALog.e("免密登陆失败: ");
                                    }
                                    return;
                                }

                                String username = editTextUsername.getText().toString().trim();
                                String password = editTextPassword.getText().toString().trim();
                                String user = username.isEmpty() ? Constants.ZERO_USERNAME : username;
                                String pwd = password.isEmpty() ? Constants.ZERO_PASSWORD : password;
                                ALog.i("user: " + user + ",pwd:" + pwd);
                                SFUemSDK.getInstance().startPasswordAuth(Constants.BASE_VPN, user, pwd);// 零信任登录
                                return;
                            case 11011:
                                showErrorToast("不支持的参数");
                                String spa = SPUtils.getInstance().getString("spa", "y121-fbcq-BPXz");
                                DialogUtils.startInputConfirm(LoginActivity.this, "安全码验证失败", "请核对后再次验证", spa, "请输入文案内容",
                                        new DialogUtils.OnInputListener() {
                                            @Override
                                            public void onConfirm(String text) {
                                                SPUtils.getInstance().put("spa", text);
                                            }
                                        }, new DialogUtils.CancelListener() {
                                            @Override
                                            public void onCancel() {

                                            }
                                        });
                                break;
                            case 11012:
                                showErrorToast("请输入正确的安全码");
                                break;
                            case 75599999:
                                showErrorToast("不支持的参数");
                                break;
                            default:
                                break;
                            }
                            auto = false;
                            binding.inputLayout.setVisibility(View.VISIBLE);
                            String spa = SPUtils.getInstance().getString("spa", "y121-fbcq-BPXz");
                            DialogUtils.startInputConfirm(LoginActivity.this, "安全码验证失败", "请核对后再次验证", spa, "请输入文案内容",
                                    new DialogUtils.OnInputListener() {
                                        @Override
                                        public void onConfirm(String text) {
                                            SPUtils.getInstance().put("spa", text);
                                            buttonLogin.performClick();
                                        }
                                    }, new DialogUtils.CancelListener() {
                                        @Override
                                        public void onCancel() {

                                        }
                                    });
                        }
                    });
                    // login();
                    return null;
                }
            });

        } else if (v.getId() == R.id.buttonCancel) {
            handler.removeCallbacksAndMessages(null);
            // startPeriodicTask();
            // 显示成功弹窗
            // ToastDialogManager.showSuccess(this);
            // finish();
            // registerFace();
        } else if (v.getId() == R.id.button_test) {
            Executors.newSingleThreadExecutor().execute(new Runnable() {
                @Override
                public void run() {
                    // // 执行数据库操作
                    // LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
                    // dao.deleteAll();
                }
            });
        } else if (v.getId() == R.id.button_test2) {
            LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
            Executors.newSingleThreadExecutor().execute(() -> {
                List<LongTermPass> longTermPassList = dao.getAll();
                Gson gson = new Gson();
                ALog.d("本地数据库查询数据: " + gson.toJson(longTermPassList.get(0)));
                if (longTermPassList != null) {
                    showInfoToast("通行证数量: " + (longTermPassList == null ? 0 : longTermPassList.size()));
                }
            });
        } else if (v.getId() == R.id.button_test4) {
            // 跳转到 HomeActivity
            Intent intent = new Intent(LoginActivity.this, LivenessDetectJinActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.to_regis) {
            handler.removeCallbacksAndMessages(null);
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivityForResult(intent, 100);

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
        case 100:
            startLogin(100);
            break;
        default:
            break;
        }
    }
    // /**
    // * 每隔一分钟获取通行证，更新到本地数据库.
    // */
    // private void startPeriodicTask() {
    // handler = new WeakHandler();
    // runnable = new Runnable() {
    // @Override
    // public void run() {
    // // 执行任务逻辑
    // ALog.d("更新通行证任务执行中...");
    //
    // // new Thread(() -> {
    // // List<LongTermPass> all = db.longTermPassDao().getAll();
    // // ALog.d("查询本地数据库数据: "+gson.toJson(all));
    // // }).start();
    // // updateNext = true;
    // updatePage = 1;
    // getLongPassCardsUpdate();
    // // 每隔1分钟执行一次
    // handler.postDelayed(this, 1 * 60 * 1000);
    // // handler.postDelayed(this, 1000);
    // }
    // };
    // handler.post(runnable);
    // }
    //
    // public void stopPeriodicTask() {
    // if (handler != null && runnable != null) {
    // handler.removeCallbacks(runnable);
    // }
    // }

    /**
     * 初始化零信任
     */
    void initZeroTrust() {
        // 初始化零信任
        Map<SFSDKExtras, String> extra = new HashMap<>();
        int sdkFlags = SFSDKFlags.FLAGS_HOST_APPLICATION;
        sdkFlags |= SFSDKFlags.FLAGS_VPN_MODE_TCP;
        SFUemSDK.getInstance().initSDK(this, SFSDKMode.MODE_SUPPORT_MUTABLE, sdkFlags, extra);
        // 监听认证结果
        SFUemSDK.getInstance().setAuthResultListener(new SFAuthResultListener() {
            @Override
            public void onAuthSuccess(SFBaseMessage sfBaseMessage) {
							ALog.d("零信任认证成功: " + GsonUtils.toJson(sfBaseMessage));
							// 保存账号和密码到全局变量
							infoStorage.saveString("zero_trust_username", editTextUsername.getText().toString().trim());
							infoStorage.saveString("zero_trust_password", editTextPassword.getText().toString().trim());
							if(SFAuthType.AUTH_TYPE_RENEW_PASSWORD == sfBaseMessage.currentAuthType) {
								editTextPassword.setText("");
								infoStorage.saveString("zero_trust_password", "");
								return;
							}
							login();// 后台登录
            }

            @Override
            public void onAuthFailed(SFBaseMessage sfBaseMessage) {
                Toast.makeText(LoginActivity.this, sfBaseMessage.mErrStr, Toast.LENGTH_SHORT).show();
                ALog.e("零信任认证失败: " + GsonUtils.toJson(sfBaseMessage));
                loging = false;
            }

            @Override
            public void onAuthProgress(SFAuthType sfAuthType, SFBaseMessage sfBaseMessage) {
                ALog.i("零信任认证进度: " + GsonUtils.toJson(sfBaseMessage) + "类型：" + sfAuthType);
                loging = false;
                // 账号第一次登录时，需要输入新密码
                if (sfAuthType == SFAuthType.AUTH_TYPE_RENEW_PASSWORD) {
                    nextZeroLogin(sfAuthType);
                    return;
                }
            }

        });
    }

    void nextZeroLogin(SFAuthType sfAuthType) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        final EditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        final EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        // String text = editTextUsername.getText().toString().trim();
        // etOldPassword.setText(text);
        Button btnConfirm = dialogView.findViewById(R.id.btn_confirm);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        final AlertDialog dialog = builder.create();

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPassword = etOldPassword.getText().toString().trim();
                newPassword = etNewPassword.getText().toString().trim();
                if (TextUtils.isEmpty(oldPassword) || TextUtils.isEmpty(newPassword)) {
                    Toast.makeText(LoginActivity.this, "旧密码和新密码均不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 这里可以添加验证旧密码和更新新密码的逻辑
                Toast.makeText(LoginActivity.this, "旧密码: " + oldPassword + "，新密码: " + newPassword, Toast.LENGTH_SHORT)
                        .show();
                Map<String, String> map = new HashMap<>();
                map.put(SFConstants.AUTH_KEY_RENEW_OLD_PASSWORD, oldPassword);
                map.put(SFConstants.AUTH_KEY_REWNEW_NEW_PASSWORD, newPassword);
                SFUemSDK.getInstance().doSecondaryAuth(sfAuthType, map);// 再次登录
                dialog.dismiss();
            }
        });
        dialog.show();
        // Map<String, String> map = new HashMap<>();
        // map.put(SFConstants.AUTH_KEY_RENEW_OLD_PASSWORD, Constants.ZERO_PASSWORD);
        // map.put(SFConstants.AUTH_KEY_REWNEW_NEW_PASSWORD, "6822078aA@");
        // SFUemSDK.getInstance().doSecondaryAuth(sfAuthType,map);
    }

    public void initFaceServer() {
        facePhotoViewModel = new ViewModelProvider(getViewModelStore(), new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                if (modelClass == FacePhotoViewModel.class) {
                    return (T) new FacePhotoViewModel();
                }
                throw new IllegalArgumentException(
                        modelClass.getName() + " is not " + FacePhotoViewModel.class.getName());
            }
        }).get(FacePhotoViewModel.class);
        runOnSubThread(() -> {
            facePhotoViewModel.init();
        });
    }

    // 登录
    private void login() {
        showProgressDialog("初始化...");
        ALog.i("系统登录");
        Map<String, String> params = new HashMap<>();
        // params.put("username", Constants.ZERO_USERNAME);
        // 获取输入框账号和密码
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        params.put("username", username.isEmpty() ? "LS001" : username);
        // params.put("username", "ls001");
        params.put("clientSecret", "admin123");
        params.put("clientId", UrlConstants.URL_ClIENTID);
        String jsonPost = GsonUtils.toJson(params);
        // 这里可以添加登录逻辑，例如调用登录接口等
        // Toast.makeText(this, "正在登录...", Toast.LENGTH_SHORT).show();
        ApiUtils.post(UrlConstants.URL_LOGIN, jsonPost, new ApiUtils.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                // ALog.d("Response: " + response);
                ApiResponse resData;
                try {
                    // 解析 JSON 数据
                    resData = GsonUtils.fromJson(response, ApiResponse.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    showWarningToast("接口返回异常，解析错误");
                    loging = false;
                    dismissProgressDialog();
                    return;
                }

                if (resData.getCode() == 200) {
                    // runOnUiThread(new Runnable() {
                    // @Override
                    // public void run() {
                    // // 更新 UI 或显示成功消息
                    // dismissProgressDialog();// 关闭加载弹窗
                    // infoStorage.saveBoolean("isFirstStart", false);
                    // showSuccessToast("数据插入成功");
                    // // 开启定时更新
                    // ArcFaceApplication.getApplication().startPeriodicTask();
                    // // 批量注册人脸
                    // registerFace();
                    // // 跳转到 IndexActivity
                    // // Intent intent = new Intent(LoginActivity.this, LivenessDetectActivity.class);
                    // // startActivity(intent);
                    // // finish();
                    // }
                    // });

                    Login login = GsonUtils.fromJson(GsonUtils.toJson(resData.getData()), Login.class);
                    String accessToken = login.getAccessToken();
                    String refreshToken = login.getRefreshToken();
                    String userId = login.getUserId();
                    ApiUtils.userId = userId;
                    infoStorage.saveString("userId", userId);
                    ApiUtils.setRefreshToken(refreshToken);
                    ApiUtils.setAccessToken(accessToken);

                    showSuccessToast("后台登录成功");

                    // 跳转到 IndexActivity
                    // Intent intent = new Intent(LoginActivity.this, RegisterAndRecognizeActivity.class);
                    // startActivity(intent);
                    // finish();
                    // 加载查验方式
                    // getCheckMethod();
                    ThreadUtils.executeByCached(new SmallTask() {
                        @Override
                        public String doInBackground() throws Throwable {
                            // 获取设备详情
                            boolean result = getMACDetail();
                            if (!result) {
                                dismissProgressDialog();
                                showWarningToast("初始化失败，获取设备详情失败");
                                loging = false;
                                return null;
                            }

                            // 获取配置信息
                            result = getConfigInfo();
                            if (!result) {
                                dismissProgressDialog();
                                showWarningToast("初始化失败，获取配置信息失败");
                                loging = false;
                                return null;
                            }

                            // 获取用户详情
                            result = getUserDetail(login.getUserId());
                            if (!result) {
                                dismissProgressDialog();
                                showWarningToast("初始化失败，获取用户详情失败");
                                loging = false;
                                return null;
                            }

                            boolean isFirstStart = infoStorage.getBoolean("isFirstStart", true);
                            List<LongTermPass> list =
                                    ArcFaceApplication.getApplication().getDb().longTermPassDao().getAll();
                            ALog.e(ObjectUtils.isEmpty(list) ? "list.size() 0 " : "list.size():" + list.size());
                            if (isFirstStart || ObjectUtils.isEmpty(list)) {
                                // 在线激活
                                // 111
                                // 获取通信证信息
                                getLongPassCards();

                            } else {
                                dismissProgressDialog();// 关闭加载弹窗
                                // 定时上传通行纪录
                                ArcFaceApplication.getApplication().startUpDataToServer();
                                // // 获取更新的通行证数据
                                // ArcFaceApplication.getApplication().startPeriodicTask();
                                gotoActivity();// 跳转页面
                            }
                            loging = false;
                            return null;
                        }
                    });
                } else {
                    showWarningToast(resData.getMsg());
                    dismissProgressDialog();
                }
                loging = false;
            }

            @Override
            public void onFailure(Throwable e) {
                ALog.e("Error: " + e.getMessage());
                loging = false;
                showWarningToast("登录失败，" + e.getMessage());
                dismissProgressDialog();
                binding.inputLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    void gotoActivity() {
        // String deviceDirection = infoStorage.getString("deviceDirection", "1");
        // LogUploadUtils.upload(this);

        ALog.e("checkType:" + SPUtils.getInstance().getInt("checkType", 0));
        ALog.e("direction:" + SPUtils.getInstance().getInt("direction", 0));
        Intent intent = null;
        int checkType = SPUtils.getInstance().getInt("checkType", 0);
        switch (checkType) {
        case 0:
            intent = new Intent(LoginActivity.this, LivenessDetectJinActivity.class);
            break;
        case 1:
            intent = new Intent(LoginActivity.this, LivenessDetectYuanActivity.class);
            break;
        case 2:
            intent = new Intent(LoginActivity.this, LivenessDetectYuanAndJinActivity.class);
            break;
        case 3:
            intent = new Intent(LoginActivity.this, RegisterAndRecognizeActivity.class);
            break;
        default:
            break;
        }
        if (facePhotoViewModel != null) {
            ALog.e("facePhotoViewModel != null");
            facePhotoViewModel.release();
            facePhotoViewModel = null;
        }

        // 开启定时更新
        ArcFaceApplication.getApplication().startPeriodicTask();
        startActivity(intent);
        finish();

        // if (deviceDirection.equals("1")) {
        // // Intent intent = new Intent(LoginActivity.this, LivenessDetectActivity.class);
        // Intent intent = null;
        // int checkType = SPUtils.getInstance().getInt("checkType", 0);
        // switch (checkType) {
        // case 0:
        // intent = new Intent(LoginActivity.this, LivenessDetectJinActivity.class);
        // break;
        // case 1:
        // intent = new Intent(LoginActivity.this, LivenessDetectYuanActivity.class);
        // break;
        // case 2:
        // intent = new Intent(LoginActivity.this, LivenessDetectYuanAndJinActivity.class);
        // break;
        // case 3:
        // intent = new Intent(LoginActivity.this, RegisterAndRecognizeActivity.class);
        // break;
        // default:
        // break;
        // }
        // startActivity(intent);
        // finish();
        // } else if (deviceDirection.equals("-1")) {
        // Intent intent = new Intent(LoginActivity.this, RegisterAndRecognizeActivity.class);
        // startActivity(intent);
        // finish();
        // }
    }

    // 获取查验方式
    private void getCheckMethod() {
        ALog.d("获取查验方式: ");
        Map<String, String> params = new HashMap<>();
        params.put("clientId", UrlConstants.URL_ClIENTID);
        // String jsonPost = gson.toJson(params);
        ApiUtils.get(UrlConstants.URL_GETCHECKMETH, params, new ApiUtils.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                ALog.d("Response: " + response);
                // 解析 JSON 数据
                ApiResponse resData = GsonUtils.fromJson(response, ApiResponse.class);

                if (resData.getCode() == 200) {
                    Map<String, Integer> map = GsonUtils.fromJson(GsonUtils.toJson(resData.getData()),
                            new TypeToken<Map<String, Integer>>() {
                            }.getType());
                    // 打印解析结果
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        ALog.d("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    }
                    showSuccessToast("获得查验方式成功");
                } else {
                    showWarningToast(resData.getMsg());
                    ALog.e("获取查验方式失败: " + GsonUtils.toJson(resData));
                }

            }

            @Override
            public void onFailure(Throwable e) {
                Toast.makeText(LoginActivity.this, "获得查验方式失败", Toast.LENGTH_SHORT).show();
                ALog.e("Error: " + e.getMessage());
            }
        });
    }

    // 获取用户详情
    private boolean getUserDetail(String userId) {
        ALog.d("获得用户详情: ");
        // Map<String, String> params = new HashMap<>();
        // params.put("id", userId);
        // ApiUtils.get(UrlConstants.URL_GET_USER_DETAIL, params, new ApiUtils.ApiCallback() {
        //
        // @Override
        // public void onSuccess(String response) {
        // ALog.d("获获取用户详情Response: " + response);
        // Response resData = gson.fromJson(response, Response.class);
        // if (resData.getCode() == 200) {
        // // 解析 JSON 字符串为 User 对象
        // User login = gson.fromJson(gson.toJson(resData.getData()), User.class);
        // String nickname = login.getNickname();
        // infoStorage.saveString("loginName", nickname);
        // } else {
        // showWarningToast(resData.getMsg());
        // ALog.e("获获取用户详情Response失败: " + gson.toJson(resData));
        // }
        // }
        //
        // @Override
        // public void onFailure(Throwable e) {
        // Toast.makeText(LoginActivity.this, "获获取用户详情失败", Toast.LENGTH_SHORT).show();
        // ALog.e("Error: " + e.getMessage());
        // }
        // });
        GetRequest<String> request =
                OkGo.<String> get(UrlConstants.URL_GET_USER_DETAIL).tag(UrlConstants.URL_GET_USER_DETAIL);
        request.params("timestamp", String.valueOf(System.currentTimeMillis()));
        request.params("id", userId);
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }

        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        Call<String> call = request.converter(new StringConvert()).adapt();
        try {
            com.lzy.okgo.model.Response<String> res = call.execute();
            ALog.d("获获取用户详情Response");
            ApiResponse resData = GsonUtils.fromJson(res.body(), ApiResponse.class);
            if (resData.getCode() == 200) {
                // 解析 JSON 字符串为 User 对象
                User login = GsonUtils.fromJson(GsonUtils.toJson(resData.getData()), User.class);
                String nickname = login.getNickname();
                infoStorage.saveString("loginName", nickname);
                SPUtils.getInstance().put("mobile", login.getMobile());
                return true;
            } else {
                showWarningToast(resData.getMsg());
                ALog.e("获获取用户详情Response失败: " + GsonUtils.toJson(resData));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, "获获取用户详情失败", Toast.LENGTH_SHORT).show();
            ALog.e("Error: " + e.getMessage());
        }
        return false;
    }

    // 获得设备详细信息
    private boolean getMACDetail() {
        ALog.d("获得设备详细信息: ");
        ALog.d("MACAddress: " + deviceId);
        // // String macAdress = DeviceUtils.getMACAdress(this);
        // Map<String, String> params = new HashMap<>();
        // params.put("mac", deviceId);
        // // params.put("mac", "aeceeec5577e34f2");
        // ALog.d("MACAddress: " + deviceId);
        // // String jsonPost = gson.toJson(params);
        // ApiUtils.get(UrlConstants.URL_GET_MAC_DETAIL, params, new ApiUtils.ApiCallback() {
        // @Override
        // public void onSuccess(String response) {
        // ALog.d("获得设备详细信息");
        // // 解析 JSON 数据
        // Response resData = gson.fromJson(response, Response.class);
        //
        // if (resData.getCode() == 200) {
        // Type type = new TypeToken<Map<String, Object>>() {
        // }.getType();
        // Map<String, Object> map = gson.fromJson(gson.toJson(resData.getData()), type);
        // // 打印解析结果
        // for (Map.Entry<String, Object> entry : map.entrySet()) {
        // ALog.d("Key: " + entry.getKey() + ", Value: " + entry.getValue());
        // if (entry.getKey().equals("id")) {
        // infoStorage.saveString("deviceId", String.valueOf(entry.getValue()));
        // }
        // if (entry.getKey().equals("name")) {
        // infoStorage.saveString("deviceName", String.valueOf(entry.getValue()));
        // }
        // if (entry.getKey().equals("mac")) {
        // infoStorage.saveString("deviceMac", String.valueOf(entry.getValue()));
        // }
        // if (entry.getKey().equals("areaDetail")) {
        // infoStorage.saveString("deviceAreaDetail", String.valueOf(entry.getValue()));
        // }
        // ALog.e("deviceId:" + infoStorage.getString("deviceId", ""));
        // ALog.e("deviceName:" + infoStorage.getString("deviceName", ""));
        // ALog.e("deviceMac:" + infoStorage.getString("deviceMac", ""));
        // ALog.e("deviceAreaDetail:" + infoStorage.getString("deviceAreaDetail", ""));
        // }
        // showSuccessToast("获得查验方式成功");
        // } else {
        // showWarningToast(resData.getMsg());
        // ALog.e("获得设备详细信息失败: " + gson.toJson(resData));
        // }
        //
        // }
        //
        // @Override
        // public void onFailure(Throwable e) {
        // Toast.makeText(LoginActivity.this, "获得查验方式失败", Toast.LENGTH_SHORT).show();
        // ALog.e("Error: " + e.getMessage());
        // }
        // });

        GetRequest<String> request =
                OkGo.<String> get(UrlConstants.URL_GET_MAC_DETAIL).tag(UrlConstants.URL_GET_MAC_DETAIL);
        request.params("timestamp", String.valueOf(System.currentTimeMillis()));
        request.params("mac", deviceId);
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }

        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        Call<String> call = request.converter(new StringConvert()).adapt();
        try {
            com.lzy.okgo.model.Response<String> res = call.execute();
            ALog.d("获得设备详细信息");
            // 解析 JSON 数据
            ApiResponse resData = GsonUtils.fromJson(res.body(), ApiResponse.class);
            if (resData.getCode() == 200) {
                Map<String, Object> map =
                        GsonUtils.fromJson(GsonUtils.toJson(resData.getData()), new TypeToken<Map<String, Object>>() {
                        }.getType());
                // 打印解析结果
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    ALog.d("Key: " + entry.getKey() + ", Value: " + entry.getValue());
                    if (entry.getKey().equals("id")) {
                        infoStorage.saveString("deviceId", String.valueOf(entry.getValue()));
                    }
                    if (entry.getKey().equals("name")) {
                        infoStorage.saveString("deviceName", String.valueOf(entry.getValue()));
                    }
                    if (entry.getKey().equals("mac")) {
                        infoStorage.saveString("deviceMac", String.valueOf(entry.getValue()));
                    }
                    if (entry.getKey().equals("areaDetail")) {
                        infoStorage.saveString("deviceAreaDetail", String.valueOf(entry.getValue()));
                    }
                    ALog.e("deviceId:" + infoStorage.getString("deviceId", ""));
                    ALog.e("deviceName:" + infoStorage.getString("deviceName", ""));
                    ALog.e("deviceMac:" + infoStorage.getString("deviceMac", ""));
                    ALog.e("deviceAreaDetail:" + infoStorage.getString("deviceAreaDetail", ""));
                }
                showSuccessToast("获得查验方式成功");
                return true;
            } else {
                showWarningToast(resData.getMsg());
                ALog.e("获得设备详细信息失败: " + GsonUtils.toJson(resData));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(LoginActivity.this, "获得查验方式失败", Toast.LENGTH_SHORT).show();
            ALog.e("Error: " + e.getMessage());
        }
        return false;
    }

    // 获取配置信息
    private boolean getConfigInfo() {
        ALog.d("获取配置信息: ");
        // // URL_GETCONFIGINFO
        // Map<String, String> params = new HashMap<>();
        // params.put("type", "5");
        // ApiUtils.get(UrlConstants.URL_GETCONFIGINFO, params, new ApiUtils.ApiCallback() {
        // @Override
        // public void onSuccess(String response) {
        // ALog.d("获取配置信息Response: " + response);
        // // 解析 JSON 数据
        // Response resData = gson.fromJson(response, Response.class);
        //
        // if (resData.getCode() == 200) {
        //
        // ConfigInfo data = gson.fromJson(gson.toJson(resData.getData()), ConfigInfo.class);
        // ALog.d("Type: " + data.type);
        // ALog.d("Enter: " + data.params.enter);
        // ALog.d("Out: " + data.params.out);
        // infoStorage.saveString("devicesType", String.valueOf(data.type));
        // infoStorage.saveString("devicesEnter", String.valueOf(data.params.enter));
        // infoStorage.saveString("devicesOut", String.valueOf(data.params.out));
        //
        // showSuccessToast("获得查验方式成功");
        // } else {
        // showWarningToast(resData.getMsg());
        // ALog.e("获取配置信息失败: " + gson.toJson(resData));
        // }
        //
        // }
        //
        // @Override
        // public void onFailure(Throwable e) {
        // showSuccessToast("获得查验方式失败");
        // ALog.e("Error: " + e.getMessage());
        // }
        // });

        GetRequest<String> request =
                OkGo.<String> get(UrlConstants.URL_GETCONFIGINFO).tag(UrlConstants.URL_GETCONFIGINFO);
        request.params("timestamp", String.valueOf(System.currentTimeMillis()));
        request.params("type", "5");
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }

        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        Call<String> call = request.converter(new StringConvert()).adapt();
        try {
            com.lzy.okgo.model.Response<String> res = call.execute();
            ALog.d("获取配置信息Response");
            // 解析 JSON 数据
            ApiResponse resData = GsonUtils.fromJson(res.body(), ApiResponse.class);
            if (resData.getCode() == 200) {
                ConfigInfo data = GsonUtils.fromJson(GsonUtils.toJson(resData.getData()), ConfigInfo.class);
                ALog.d("Type: " + data.type);
                ALog.d("Enter: " + data.params.enter);
                ALog.d("Out: " + data.params.out);
                infoStorage.saveString("devicesType", String.valueOf(data.type));
                infoStorage.saveString("devicesEnter", String.valueOf(data.params.enter));
                infoStorage.saveString("devicesOut", String.valueOf(data.params.out));
                showSuccessToast("获取配置信息成功");
            } else {
                showWarningToast(resData.getMsg());
                ALog.e("获取配置信息失败: " + GsonUtils.toJson(resData));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            showSuccessToast("获取配置信息失败");
            ALog.e("Error: " + e.getMessage());
            return false;
        }

        request = OkGo.<String> get(UrlConstants.URL_GETCONFIGINFO).tag(UrlConstants.URL_GETCONFIGINFO);
        request.params("timestamp", String.valueOf(System.currentTimeMillis()));
        request.params("type", "6");
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }

        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        call = request.converter(new StringConvert()).adapt();
        try {
            com.lzy.okgo.model.Response<String> res = call.execute();
            ALog.d("获取配置信息Response");
            // 解析 JSON 数据
            ApiResponse resData = GsonUtils.fromJson(res.body(), ApiResponse.class);
            if (resData.getCode() == 200) {
                ConfigInfo data = GsonUtils.fromJson(GsonUtils.toJson(resData.getData()), ConfigInfo.class);
                ALog.d("Type: " + data.type);
                ALog.d("interval: " + data.params.interval);
                infoStorage.saveInt("interval", data.params.interval);
                showSuccessToast("获取配置信息成功");
                return true;
            } else {
                showWarningToast(resData.getMsg());
                ALog.e("获取配置信息失败: " + GsonUtils.toJson(resData));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showSuccessToast("获取配置信息失败");
            ALog.e("Error: " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取通信证数据,获取所有
     */
    private void getLongPassCards() {
        List<LongPassCard> longPassCardList = new ArrayList<>();
        longTermPassDao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
        Map<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("pageNo", String.valueOf(page));
        params.put("pageSize", String.valueOf(PAGE_SIZE));
        // 初始化 CountDownLatch
        latch = new CountDownLatch(1);
        final AtomicBoolean shouldContinue = new AtomicBoolean(true); // 使用 AtomicBoolean 来控制循环

        Snackbar snackbar = showIndefiniteSnackBar(binding.getRoot(), getString(R.string.registering_please_wait),
                getString(R.string.stop), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // 提前结束
                        finish();
                    }
                });

        AsyncTask.execute(() -> {

            while (true) {
                GetRequest<String> request =
                        OkGo.<String> get(UrlConstants.URL_GetLongPass).tag(UrlConstants.URL_GetLongPass);
                if (params != null) {
                    // 更新或添加 timestamp 参数
                    params.put("timestamp", String.valueOf(System.currentTimeMillis()));
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        request.params(entry.getKey(), entry.getValue());
                    }
                }
                request.headers("tenant-id", "1");
                // 检查是否有 accessToken，如果有则添加 Authorization 头
                if (ApiUtils.accessToken != null) {
                    request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
                }

                // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
                Call<String> call = request.converter(new StringConvert()).adapt();
                try {
                    Response<String> res = call.execute();
                    // ALog.d("Response code:" + res.code() + ", body:" + res.body());
                    if (res.code() == 200) {
                        ApiResponse<LongPassCards> response =
                                GsonUtils.fromJson(res.body(), new TypeToken<ApiResponse<LongPassCards>>() {
                                }.getType());
                        // ALog.d("获取通信证接口数据response: " + gson.toJson(response));

                        if (response.getCode() == 200) {
                            LongPassCards longPassCards = response.getData();
                            if (longPassCards != null && longPassCards.getList() != null
                                    && !longPassCards.getList().isEmpty()) {
                                // 下载图片到本地

                                for (LongPassCard longPassCard : longPassCards.list) {
                                    ALog.i("下载图片:" + longPassCard.nickname);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            snackbar.show();
                                        }
                                    });
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            snackbar.setText("正在下载：" + longPassCard.nickname + "，第" + page + "页");
                                        }
                                    });

                                    File directory1 = new File(getActivity().getExternalFilesDir(null), "register");// 应用的私有目录
                                    if (!directory1.exists()) {
                                        directory1.mkdirs();
                                    }
                                    boolean result = ImageDownloader.downloadImage(directory1, longPassCard.checkPhoto,
                                            longPassCard.id, longPassCard.nickname);
                                    if (!result) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // snackbar.dismiss();
                                                snackbar.setText("下载失敗：" + longPassCard.nickname + "，第" + page + "页");
                                            }
                                        });
                                        // break;
                                    }
                                    File directory2 = new File(getActivity().getExternalFilesDir(null), "photo");// 应用的私有目录
                                    if (!directory2.exists()) {
                                        directory2.mkdirs();
                                    }
                                    result = ImageDownloader.downloadImage(directory2, longPassCard.photo,
                                            longPassCard.id, longPassCard.nickname);
                                    if (!result) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                // snackbar.dismiss();
                                                snackbar.setText("下载失敗：" + longPassCard.nickname + "，第" + page + "页");
                                            }
                                        });
                                        // break;
                                    } else {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                snackbar.setText("下载成功：" + longPassCard.nickname + "，第" + page + "页");
                                            }
                                        });
                                    }
                                }

                                // ImageDownloader.downloadImages(longPassCards.list, LoginActivity.this);
                                longPassCardList.addAll(longPassCards.getList());
                                page++; // 修改成员变量 page
                                params.put("pageNo", String.valueOf(page));
                            } else {
                                // shouldContinue.set(false); // 设置标志位为 false，终止循环
                                break;
                            }
                        } else {
                            showWarningToast(response.getMsg());
                            ALog.d("接口非200: " + response.getMsg());
                            // shouldContinue.set(false); // 设置标志位为 false，终止循环
                            page = 1;
                            longPassCardList.clear();
                            break;
                        }
                        // 计数器减一
                        latch.countDown();
                        continue;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ALog.e("获取通信证接口数据失败", e);
                    showWarningToast("获取通信证接口数据失败");
                    ALog.d("接口报错: " + e.getMessage());
                    // shouldContinue.set(false); // 设置标志位为 false，终止循环
                    page = 1;
                    longPassCardList.clear();
                    // 计数器减一
                    latch.countDown();
                    break;
                }
                break;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    snackbar.dismiss();
                }
            });
            if (longPassCardList.size() > 0) {
                page = 1;
                insertDataToLocalDb(longPassCardList);
            }
        });
    }

    // 将通行证数据存入本地数据库
    private void insertDataToLocalDb(List<LongPassCard> longPassCards) {
        List<LongTermPass> longTermPassList = new ArrayList<>();
        ALog.d("调用了存入本地数据库函数 ");

        ThreadUtils.executeByCached(new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {

                for (LongPassCard item : longPassCards) {
                    // ALog.d("接口数据longPassCard: "+gson.toJson(longPassCard));
                    LongTermPass longTermPass = Converters.convertToLongTermPass(item);
                    longTermPassList.add(longTermPass);
                }

                // longPassCards.forEach(longPassCard -> {
                // // ALog.d("接口数据longPassCard: "+gson.toJson(longPassCard));
                // LongTermPass longTermPass = Converters.convertToLongTermPass(longPassCard);
                // longTermPassList.add(longTermPass);
                // });

                // 将数据插入到本地数据库
                longTermPassDao.insertAll(longTermPassList);
                // 保存当前时间到本地
                infoStorage.saveString("startDate", DeviceUtils.getCurrentTime());
                // 切换回主线程更新 UI
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 更新 UI 或显示成功消息
                        dismissProgressDialog();// 关闭加载弹窗
                        infoStorage.saveBoolean("isFirstStart", false);
                        showSuccessToast("数据插入成功");

                        // 人脸初始化
                        initFaceServer();

                        // 批量注册人脸
                        registerFace();

                        // 跳转到 IndexActivity
                        // Intent intent = new Intent(LoginActivity.this, LivenessDetectActivity.class);
                        // startActivity(intent);
                        // finish();
                    }
                });
                return null;
            }
        });
    }

    /**
     * 批量注册人脸
     */
    public void registerFace() {
        // registerFromFile(new File(Constants.DEFAULT_REGISTER_FACES_DIR));
        // 修改为私有目录
        File directory = new File(LoginActivity.this.getExternalFilesDir(null), "register");
        registerFromFile(directory);
    }

    // /**
    // * 单个注册人脸
    // */
    // public void registerFaceByBitmap(Bitmap bitmap) {
    // ThreadUtils.executeByCached(new SmallTask() {
    // @Override
    // public String doInBackground() throws Throwable {
    // facePhotoViewModel.registerFace(bitmap, (facePreviewInfo, success) -> {
    // // showLongSnackBar(binding.fabAdd, getString(success ? R.string.register_success :
    // // R.string.register_failed));
    // ALog.i("单个注册人脸: " + success);
    // });
    // return null;
    // }
    // });
    // }

    /**
     * 批量注册
     *
     * @param dir 批量注册的文件夹
     */
    public void registerFromFile(File dir) {
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
            return;
        }
        Snackbar snackbar = showIndefiniteSnackBar(binding.getRoot(), getString(R.string.registering_please_wait),
                getString(R.string.stop), v -> {
                    // 提前结束，则显示已注册人脸
                    if (facePhotoViewModel != null && facePhotoViewModel.stopRegisterIfDoing()) {
                        showLongSnackBar(binding.getRoot(), getString(R.string.stopped));
                        runOnSubThread(() -> facePhotoViewModel.loadData(true));
                    }
                });

        facePhotoViewModel.registerFromDecryptFile(getApplicationContext(), dir, new BatchRegisterCallback() {

            @Override
            public void onProcess(int current, int failed, int total) {
                runOnUiThread(() -> snackbar.setText(getString(R.string.register_progress, current, failed, total)));
                if (current == total) {
                    gotoActivity();
                }
            }

            @Override
            public void onFinish(int current, int failed, int total, String errMsg) {
                if (errMsg != null) {
                    showLongToast(errMsg);
                    ALog.d("错误信息: " + errMsg);
                }
                snackbar.dismiss();
                gotoActivity();

            }
        });
    }

    class ConfigInfo {
        int type;
        Params params;

        class Params {
            int interval;
            int enter;
            int out;
        }
    }

    // void check() {
    // UpdateUtils.update(this, new OnUpdateFailureListener() {
    // @Override
    // public void onFailure(UpdateError error) {
    // ALog.e(error.toString());
    // // 对不同错误进行处理
    // if (error.getCode() == UpdateError.ERROR.CHECK_NO_NEW_VERSION) {
    //// AppClient.get().postEvent(new AppInfoEvent(AppInfoEvent.UPDATE_NO_NEWER));
    // } else if ((error.getCode() == UpdateError.ERROR.DOWNLOAD_FAILED
    // || error.getCode() == UpdateError.ERROR.DOWNLOAD_PERMISSION_DENIED)
    // && error.getUpdateEntity() != null && error.getUpdateEntity().isForce()) {
    // showToast(error.toString());
    // AppUtils.exitApp();
    // } else if (error.getCode() == UpdateError.ERROR.PROMPT_CANCEL
    // || error.getCode() == UpdateError.ERROR.PROMPT_IGNORE
    // || error.getCode() == UpdateError.ERROR.CHECK_IGNORED_VERSION) {
    // // showToast(getString(R.string.update_tips));
    //// AppClient.get().postEvent(new AppInfoEvent(AppInfoEvent.CANCLE));
    // } else {
    //// AppClient.get().postEvent(new AppInfoEvent(AppInfoEvent.ERROR));
    // showToast(error.getMessage());
    // }
    // }
    // }, new DefaultInstallListener() {
    // @Override
    // public void onInstallApkSuccess() {
    // super.onInstallApkSuccess();
    // finish();
    // }
    // });
    // }

}
