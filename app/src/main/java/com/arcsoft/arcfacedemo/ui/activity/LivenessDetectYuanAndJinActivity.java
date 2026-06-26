package com.arcsoft.arcfacedemo.ui.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.Serial.SerialInter;
import com.arcsoft.arcfacedemo.Serial.SerialManage;
import com.arcsoft.arcfacedemo.data.http.JsonCallback;
import com.arcsoft.arcfacedemo.databinding.ActivityLivenessDetectBinding;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.dao.LongTermRecordsDao;
import com.arcsoft.arcfacedemo.db.dao.TemporaryCardRecordsDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;
import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;
import com.arcsoft.arcfacedemo.entity.ApiResponse;
import com.arcsoft.arcfacedemo.entity.Area;
import com.arcsoft.arcfacedemo.entity.Base;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.entity.LongPassCards;
import com.arcsoft.arcfacedemo.entity.Records;
import com.arcsoft.arcfacedemo.entity.Tag;
import com.arcsoft.arcfacedemo.entity.Version;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.service.TokenRefreshJobService;
import com.arcsoft.arcfacedemo.ui.adapter.CheckLogListAdapter;
import com.arcsoft.arcfacedemo.ui.fragment.Document1;
import com.arcsoft.arcfacedemo.ui.fragment.Document2;
import com.arcsoft.arcfacedemo.ui.fragment.Document3;
import com.arcsoft.arcfacedemo.ui.model.PreviewConfig;
import com.arcsoft.arcfacedemo.ui.viewmodel.LivenessDetectViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.DateUtil;
import com.arcsoft.arcfacedemo.util.DeviceUtils;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.ImageUploader;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.arcsoft.arcfacedemo.util.SimpleTask;
import com.arcsoft.arcfacedemo.util.SmallTask;
import com.arcsoft.arcfacedemo.util.SnowFlake;
import com.arcsoft.arcfacedemo.util.VerifyFeatureSettings;
import com.arcsoft.arcfacedemo.config.ChannelConfig;
import com.arcsoft.arcfacedemo.util.CardSerialConfigUtil;
import com.arcsoft.arcfacedemo.util.TimeControlUtil;
import com.arcsoft.arcfacedemo.util.WeakHandler;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;
import com.arcsoft.arcfacedemo.util.face.FaceFeatureCallback;
import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.dialog.CustomDrawerPopupView;
import com.arcsoft.arcfacedemo.widget.dialog.CustomPopDialog;
import com.arcsoft.arcfacedemo.widget.dialog.LoadingPopDialog;
import com.arcsoft.arcfacedemo.widget.dialog.RecordsPopDialog;
import com.arcsoft.arcfacedemo.widget.dialog.UpdatePopDialog;
import com.arcsoft.face.FaceFeature;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.decard.NDKMethod.BasicOper;
import com.decard.driver.utils.HexDump;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hc.reader.AndroidSerialPort;
import com.hc.reader.Card;
import com.hc.reader.Utils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BasePopupView;
import com.lxj.xpopup.enums.PopupPosition;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.pc_rfid.api.EC_API;
import com.pc_rfid.api.RFdata;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;

import android.Manifest;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * 长距+短距双读卡器 刷卡+人脸查验 Activity。
 * <p>
 * 主流程：短距 BasicOper PSAM 外部认证读卡 + 长距 EC_API RFID 盘点读卡 → 本地库查证件 →
 * 区域/有效期/黑名单等校验 → 活体检测与人脸 1:1 比对 → 写入长期/临时通行记录并展示证件 Fragment。
 * 支持大屏（BasicOper + EC_API 双读卡器）与小屏（AndroidSerialPort）两种方案，以及二维码串口读取临时通行证。
 * </p>
 *
 * @see LivenessDetectViewModel 人脸引擎与预览帧处理
 * @see Document2 长期证详情展示
 * @see Document3 临时证 / C 类引领人详情展示
 */
public class LivenessDetectYuanAndJinActivity extends BaseActivity
        implements ViewTreeObserver.OnGlobalLayoutListener, FaceFeatureCallback {
    private static final String TAG = "LivenessDetectActivity";

    // ========== 相机与人脸引擎 ==========
    /** RGB 预览相机辅助类 */
    private DualCameraHelper rgbCameraHelper;
    /** IR 预览相机辅助类 */
    private DualCameraHelper irCameraHelper;
    /** RGB 画面人脸框坐标变换器 */
    private FaceRectTransformer rgbFaceRectTransformer;
    /** IR 画面人脸框坐标变换器 */
    private FaceRectTransformer irFaceRectTransformer;
    /** 双摄像头预览配置（前后摄 ID、旋转角度等） */
    private PreviewConfig previewConfig;
    /** 活体检测类型：RGB / IR */
    private LivenessType livenessType;
    /** 当前待比对的人脸特征（刷卡成功后从本地解密底库提取） */
    private FaceFeature faceFeature;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    /** 设备与登录信息本地存储 */
    InfoStorage infoStorage;

    Gson gson = new Gson();
    /** 现场抓拍图上传工具 */
    ImageUploader imageUploader = new ImageUploader();
    /** 当前读到的卡 RFID，防重复刷卡 */
    private static String rfid = "";

    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS =
            new String[] { Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE };
    private ActivityLivenessDetectBinding binding;
    /** 活体检测与人脸比对 ViewModel */
    private LivenessDetectViewModel livenessDetectViewModel;

    // ========== Fragment 与 UI ==========
    /** 默认待机 Fragment（Document1） */
    private Fragment fragment1;
    private Fragment fragment2;
    private Fragment fragment3;
    private View viewById;
    private View toast_verified_passed;
    private View toast_verified_fail;
    private View button_set;
    /** 当前刷卡查到的长期/临时通行证实体 */
    public LongTermPass longTermPass;
    /** 读卡成功提示音 */
    private MediaPlayer mediaGet;
    /** 验证通过提示音 */
    private MediaPlayer mediaPass;
    /** 验证失败提示音 */
    private MediaPlayer mediaReject;
    /** 现场人脸抓拍展示 */
    private ImageView iv_face;
    private ImageView iv_face1;
    private ImageView iv_face2;
    private ImageView iv_face3;
    /** 长期记录本地 ID，供临时证关联 parentId */
    private String localLongId;
    /** 刷卡完成时间戳，用于人脸比对超时判断 */
    private Long xtTime;
    /** 人脸比对是否已判定失败（防重复回调） */
    private boolean checkFailed;

    // ========== 定时任务 Handler ==========
    /** 统一定时任务：系统时间同步、证件页自动隐藏、读卡轮询、时钟刷新、版本检查等 */
    private final WeakHandler countdownHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
            case 1111:
                clickCount = 0;
                break;
            case 1:
                // 调用接口
                callApi();
                // 线程休眠一分钟
                countdownHandler.removeMessages(1);
                countdownHandler.sendEmptyMessageDelayed(1, countdownTime);
                break;
            case 2:
                try {
                    // 其他需要执行的操作
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_all, fragment1);
                    fragmentTransaction.commit();
                    iv_face.setVisibility(View.INVISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                    ALog.e(e.getMessage());
                }

                break;
            case 3:
                ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
                    @Override
                    public String doInBackground() throws Throwable {
                        boolean result = getLongPassCardID();
                        if (!result) {
                            setRfidNull();
                        }
                        countdownHandler.removeMessages(3);
                        countdownHandler.sendEmptyMessageDelayed(3, 300);// 每隔300ms调用一次
                        return null;
                    }
                });
                break;

            case 4:
                ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
                    @Override
                    public String doInBackground() throws Throwable {
                        boolean result = getCarIDMini((Card) msg.obj);
                        if (!result) {
                            setRfidNull();
                        }
                        Message message = new Message();
                        message.what = 4;
                        message.obj = msg.obj;
                        countdownHandler.removeMessages(4);
                        countdownHandler.sendMessageDelayed(message, 300);// 每隔300ms调用一次
                        return null;
                    }
                });
                break;

            case 5:
                if (ArcFaceApplication.getApplication().isOffLine()) {
                    binding.tvTis.setText("离线模式   " + areaName + (direction == 1 ? "-进" : "-出") + "查验");

                } else {
                    binding.tvTis.setText("在线模式   " + areaName + (direction == 1 ? "-进" : "-出") + "查验");
                }
                countdownHandler.removeMessages(5);
                countdownHandler.sendEmptyMessageDelayed(5, 10000L);
                break;

            case 6:
                binding.tvTime.setText(TimeUtils.getNowString(new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss")));
                countdownHandler.removeMessages(6);
                countdownHandler.sendEmptyMessageDelayed(6, 1000L);

                break;

            case 7:
                if (ArcFaceApplication.getApplication().isValid()) {
                    check();
                } else {
                    countdownHandler.removeMessages(7);
                    countdownHandler.sendEmptyMessageDelayed(7, 60 * 1000L);
                }
                break;
            case 8:
                checkFailed = true;
                chechFailed(null, 0, 0);
                // startReadLongPassCardID();
                break;
            case 10:
                binding.tvFaceSimilar.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
            }
            return false;
        }
    });
    /** 证件详情页自动隐藏倒计时（默认 1 分钟） */
    private final long countdownTime = 1 * 60 * 1000;
    /** 当前设备所属区域名称（用于顶部标题） */
    String areaName;
    /** 底部横向查验记录列表适配器 */
    private CheckLogListAdapter mListAdapter;
    /** 最近查验记录缓存，最多保留 20 条 */
    private final ArrayList<Records> checkList = new ArrayList<>();
    LinearLayout messageLayout;
    ImageView iv_icon;
    TextView tv_message;
    /** 通行方向：1 进 / -1 出 */
    int direction;
    /** Logo 连击次数，用于打开运维抽屉 */
    private int clickCount = 0;
    private static final int REQUIRED_CLICKS = 5;

    // ========== 生命周期 ==========

    /**
     * 初始化布局、双读卡器/扫码串口、Fragment、Handler 定时任务与查验记录列表。
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ALog.e("onCreate");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_liveness_detect);

        // 保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            // attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY &
            // View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        direction = SPUtils.getInstance().getInt("direction", 1);
        initModel();
        initView();
        initViewModel();

        // 读卡设备初始化
        initReadCard();
        // 扫描二维码设备初始化
        initScanCard();
        // // 本地仓库初始化
        // db = Room.databaseBuilder(getApplicationContext(), YinchuanAirportDB.class, "yinchuan-airport-database")
        // .fallbackToDestructiveMigration().build();

        // soundManager = new SoundManager(this);
        // 预加载多个音频资源
        // int[] soundResIds = {R.raw.get, R.raw.pass, R.raw.reject};
        // soundManager.preloadSounds(soundResIds);
        initSound();
        // fragment初始化
        initFragment();

        iv_face = findViewById(R.id.iv_face);
        iv_face1 = findViewById(R.id.iv_face1);
        iv_face2 = findViewById(R.id.iv_face2);
        iv_face3 = findViewById(R.id.iv_face3);
        toast_verified_passed = findViewById(R.id.toast_verified_passed);
        toast_verified_fail = findViewById(R.id.toast_verified_fail);
        button_set = findViewById(R.id.button_set);

        messageLayout = findViewById(R.id.messageLayout);
        iv_icon = findViewById(R.id.iv_icon);
        tv_message = findViewById(R.id.tv_message);

        // 点击跳转设置界面
        button_set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
            }
        });
        infoStorage = new InfoStorage(this);
        // 调度 Token 刷新任务
        // scheduleTokenRefreshJob();

        // // 每隔半小时更新调用一次获取时间接口
        // // 开启新线程
        // new Thread(new Runnable() {
        // @Override
        // public void run() {
        // while (true) {
        // try {
        // // 调用接口
        // callApi();
        // // 线程休眠一分钟
        // Thread.sleep(60 * 1000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // break;
        // }
        // }
        // }
        // }).start();
        countdownHandler.removeMessages(1);
        countdownHandler.sendEmptyMessageDelayed(1, 100L);
        // 初始化 HandlerThread 和 Handler
        // countdownThread = new HandlerThread("CountdownThread");
        // countdownThread.start();
        // countdownHandler = new Handler(countdownThread.getLooper());

        areaName = getArea();
        countdownHandler.removeMessages(5);
        countdownHandler.sendEmptyMessageDelayed(5, 50L);
        countdownHandler.removeMessages(6);
        countdownHandler.sendEmptyMessageDelayed(6, 50L);
        countdownHandler.removeMessages(7);
        countdownHandler.sendEmptyMessageDelayed(7, 1000L);
        binding.tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(getActivity()).asCustom(new RecordsPopDialog(getActivity(), direction)).show();
            }
        });
        mListAdapter = new CheckLogListAdapter(this, checkList, R.layout.list_check_log_item);
        binding.rvList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvList.setAdapter(mListAdapter);
        // getAllRecords();
        binding.imgYuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++; // 每次点击时增加计数
                // 检测点击次数是否达到目标
                if (clickCount >= REQUIRED_CLICKS) {
                    // 达到目标后可以触发某项操作
                    clickCount = 0; // 重置计数
                    new XPopup.Builder(getActivity()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                            .popupPosition(PopupPosition.Left)// 右边
                            .hasStatusBarShadow(true) // 启用状态栏阴影
                            .asCustom(new CustomDrawerPopupView(getActivity())).show();

                }
                countdownHandler.removeMessages(1111);
                countdownHandler.sendEmptyMessageDelayed(1111, 5000);
            }
        });
        mListAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener<Records>() {
            @Override
            public void onItemClick(View view, int position, Records item) {
                if (item instanceof LongTermRecords) {
                    toggleFragment(item);
                } else if (item instanceof TemporaryCardRecords) {
                    switchFragment3((TemporaryCardRecords) item);
                }
            }
        });

    }

    // ========== 网络与提示音 ==========

    /**
     * 定时调用系统时间接口，用于保持与服务端时钟同步（Handler msg=1 触发）。
     */
    private void callApi() {
        ApiUtils.get(UrlConstants.URL_GET_SYSTEM_TIME, new HashMap<String, String>(), new ApiUtils.ApiCallback() {
            @Override
            public void onSuccess(String response) {
                // ALog.i("获取系统时间: " + gson.toJson(response));
            }

            @Override
            public void onFailure(Throwable e) {
                ALog.i("获取系统时间失败: ");
            }
        });
    }

    /** 初始化读卡/验证/拒绝三类提示音 MediaPlayer */
    void initSound() {
        // 初始化MediaPlayer实例
        mediaGet = MediaPlayer.create(this, R.raw.get);
        mediaPass = MediaPlayer.create(this, R.raw.verification_successful);
        mediaReject = MediaPlayer.create(this, R.raw.validation_failed);
    }

    /**
     * 播放提示音；若已在播放则忽略。
     *
     * @param mediaPlayer 预加载的 MediaPlayer 实例
     */
    private void playAudio(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        }
    }

    /** 初始化 Document1/2/3 三个证件 Fragment，默认展示 Document1 */
    void initFragment() {
        fragment1 = new Document1();
        fragment2 = new Document2();
        fragment3 = new Document3();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_all, fragment1);
        fragmentTransaction.commit();
    }

    // ========== Fragment 切换 ==========

    /**
     * 切换到 Document2 展示长期证详情（查验通过后带相似度）。
     *
     * @param longTermPass 长期通行证
     * @param faceSimilar  人脸相似度 0~1
     */
    private void toggleFragment(LongTermPass longTermPass, float faceSimilar) {
        try {
            // 根据idCode去本地加载图片
            // String idCode = longTermPass.idCode;
            // Bitmap bitmap = ImageUtils.loadBitmapFromPath(Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode +
            // ".jpg");
            Fragment fragment2 = new Document2();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putString("idCode", longTermPass.idCode);
            bundle.putString("passid", longTermPass.id);
            bundle.putString("nickname", longTermPass.nickname);
            bundle.putString("companyName", longTermPass.companyName);
            bundle.putString("expiryDate", longTermPass.expiryDate);
            bundle.putString("startDate", longTermPass.startDate);
            bundle.putString("templateType", String.valueOf(longTermPass.templateType));
            bundle.putString("status", String.valueOf(longTermPass.status));
            bundle.putString("photo", longTermPass.photo);
            bundle.putString("faceSimilar", faceSimilar + "");
            String areaDisplay = "";
            for (String s : longTermPass.areaDisplayCode) {
                areaDisplay += s + "\n";
            }
            bundle.putString("areaDisplayCode", areaDisplay);
            fragment2.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragment_all, fragment2);
            fragmentTransaction.commit();
            hiddenCard();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e(e.getMessage());
        }

    }

    /**
     * 根据查验记录切换到 Document2 展示证件详情（点击底部记录列表时调用）。
     *
     * @param records 长期或临时通行记录
     */
    private void toggleFragment(Records records) {

        try {
            Fragment fragment2 = new Document2();
            // 根据idCode去本地加载图片
            // String idCode = longTermPass.idCode;
            // Bitmap bitmap = ImageUtils.loadBitmapFromPath(Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode +
            // ".jpg");
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Bundle bundle = new Bundle();

            String areaDisplay = "";
            if (records instanceof LongTermRecords) {
                LongTermRecords longTermRecords = (LongTermRecords) records;
                bundle.putString("idCode", longTermRecords.idCode);
                bundle.putString("passid", longTermRecords.passid);
                bundle.putString("nickname", longTermRecords.nickname);
                bundle.putString("companyName", longTermRecords.companyName);
                bundle.putString("expiryDate", longTermRecords.expiryDate);
                bundle.putString("templateType", String.valueOf(longTermRecords.templateType));
                bundle.putString("status", String.valueOf(longTermRecords.status));
                bundle.putString("photo", longTermRecords.photo);
                bundle.putString("faceSimilar", longTermRecords.faceSimilar + "");
                areaDisplay = "";
                for (String s : longTermRecords.areaDisplayCode) {
                    areaDisplay += s + "\n";
                }
            } else if (records instanceof TemporaryCardRecords) {
                TemporaryCardRecords temporaryCardRecords = (TemporaryCardRecords) records;
                bundle.putString("idCode", temporaryCardRecords.idCode);
                bundle.putString("passid", temporaryCardRecords.passid);
                bundle.putString("nickname", temporaryCardRecords.nickname);
                bundle.putString("companyName", temporaryCardRecords.companyName);
                bundle.putString("expiryDate", temporaryCardRecords.expiryDate);
                bundle.putString("templateType", String.valueOf(temporaryCardRecords.templateType));
                bundle.putString("status", String.valueOf(temporaryCardRecords.status));
                bundle.putString("photo", temporaryCardRecords.photo);
                bundle.putString("faceSimilar", temporaryCardRecords.faceSimilar + "");
                areaDisplay = "";
                for (String s : temporaryCardRecords.areaDisplayCode) {
                    areaDisplay += s + "\n";
                }
            }

            bundle.putString("areaDisplayCode", areaDisplay);
            fragment2.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragment_all, fragment2);
            fragmentTransaction.commit();
            hiddenCard();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e(e.getMessage());
        }

    }

    /**
     * 切换到 Document3 展示 C 类 / 临时证详情（无相似度）。
     *
     * @param longTermPass 通行证实体
     */
    private void switchFragment3(LongTermPass longTermPass) {
        switchFragment3(longTermPass, 0);
    }

    /**
     * 切换到 Document3 展示临时证查验记录详情。
     *
     * @param temporaryCardRecords 临时证通行记录
     */
    private void switchFragment3(TemporaryCardRecords temporaryCardRecords) {
        try {
            Fragment fragment3 = new Document3();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString("leadingPeople", temporaryCardRecords.leadingPeople);
            bundle.putString("applyId", temporaryCardRecords.applyId);
            bundle.putString("idCode", temporaryCardRecords.idCode);
            bundle.putString("passid", temporaryCardRecords.passid);
            bundle.putString("nickname", temporaryCardRecords.nickname);
            bundle.putString("companyName", temporaryCardRecords.companyName);
            bundle.putString("expiryDate", temporaryCardRecords.expiryDate);
            bundle.putString("templateType", String.valueOf(temporaryCardRecords.templateType));
            bundle.putString("status", String.valueOf(temporaryCardRecords.status));
            bundle.putString("photo", temporaryCardRecords.photo);
            bundle.putString("faceSimilar", temporaryCardRecords.faceSimilar + "");
            String areaDisplay = "";
            for (String s : temporaryCardRecords.areaDisplayCode) {
                areaDisplay += s + "\n";
            }
            bundle.putString("areaDisplayCode", areaDisplay);
            fragment3.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragment_all, fragment3);
            fragmentTransaction.commit();
            hiddenCard();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e(e.getMessage());
        }
    }

    /**
     * 切换到 Document3 展示 C 类 / 临时证详情（带人脸相似度）。
     *
     * @param longTermPass 通行证实体
     * @param faceSimilar  人脸相似度 0~1
     */
    private void switchFragment3(LongTermPass longTermPass, float faceSimilar) {
        try {
            Fragment fragment3 = new Document3();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            String areaDisplay = "";
            for (String s : longTermPass.areaDisplayCode) {
                areaDisplay = areaDisplay + s + "  ";
            }
            Bundle bundle = new Bundle();
            bundle.putString("leadingPeople", longTermPass.leadingPeople);
            bundle.putString("applyId", longTermPass.applyId);
            bundle.putString("idCode", longTermPass.idCode);
            bundle.putString("passid", longTermPass.id);
            bundle.putString("nickname", longTermPass.nickname);
            bundle.putString("areaDisplayCode", areaDisplay);
            bundle.putString("companyName", longTermPass.companyName.toString());
            bundle.putString("startDate", longTermPass.startDate.toString());// 开始时间
            bundle.putString("expiryDate", longTermPass.expiryDate.toString());// 结束时间
            bundle.putString("templateType", String.valueOf(longTermPass.templateType));
            bundle.putString("status", String.valueOf(longTermPass.status));
            bundle.putString("photo", longTermPass.photo);
            bundle.putString("faceSimilar", faceSimilar + "");
            fragment3.setArguments(bundle);
            fragmentTransaction.replace(R.id.fragment_all, fragment3);
            fragmentTransaction.commit();
            hiddenCard();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e(e.getMessage());
        }
    }

    /**
     * 证件详情展示后启动倒计时，超时后恢复默认待机 Fragment（Handler msg=2，默认 1 分钟）。
     */
    private void hiddenCard() {
        countdownHandler.removeMessages(2);
        countdownHandler.sendEmptyMessageDelayed(2, countdownTime);

        // // 开始五分钟计时
        // // 移除之前的任务
        // countdownHandler.removeCallbacks(countdownRunnable);
        //
        // // 创建新的倒计时任务
        // countdownRunnable = new Runnable() {
        // @Override
        // public void run() {
        // // 倒计时结束后的操作
        // runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // // 其他需要执行的操作
        // FragmentManager fragmentManager = getSupportFragmentManager();
        // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        // fragmentTransaction.replace(R.id.fragment_all, fragment1);
        // fragmentTransaction.commit();
        // iv_face.setVisibility(View.INVISIBLE);
        // }
        // });
        // }
        // };

        // // 延迟五分钟执行倒计时任务
        // countdownHandler.postDelayed(countdownRunnable, countdownTime);
    }

    /**
     * Activity 停止时回调（读卡轮询由 onDestroy 统一停止）。
     */
    @Override
    protected void onStop() {
        super.onStop();
        // handler.removeCallbacks(runnable); // 停止循环调用
    }

    /**
     * 释放相机、ViewModel、MediaPlayer 与 Handler，停止短距/长距读卡并关闭 EC_API 读卡器。
     */
    @Override
    protected void onDestroy() {
        ALog.e("onDestroy");
        if (irCameraHelper != null) {
            irCameraHelper.release();
            irCameraHelper = null;
        }

        if (rgbCameraHelper != null) {
            rgbCameraHelper.release();
            rgbCameraHelper = null;
        }
        // if (handlerThread != null) {
        // handlerThread.quitSafely(); // 停止 HandlerThread
        // }

        livenessDetectViewModel.destroy();

        // soundManager.release();//提示音资源释放

        if (mediaGet != null) {
            mediaGet.release();
            mediaGet = null;
        }
        if (mediaPass != null) {
            mediaPass.release();
            mediaPass = null;
        }
        if (mediaReject != null) {
            mediaReject.release();
            mediaReject = null;
        }

        // // 退出 HandlerThread
        // if (countdownThread != null) {
        // countdownThread.quitSafely();
        // countdownThread = null;
        // }
        countdownHandler.removeCallbacksAndMessages(null);
        stopReadLongPassCardID();// 近距离卡
        stopReadCarIDMini();

        // stopReadLongCard();//远距离卡
        unInitLongReader();
        super.onDestroy();
    }

    // ========== 刷卡与串口（短距 + 长距双读卡器） ==========

    /**
     * 初始化读卡串口：按屏幕宽度选择 BasicOper（大屏，含 EC_API 长距读卡器）或 AndroidSerialPort（小屏），
     * 打开成功后启动对应读卡轮询线程。
     */
    public void initReadCard() {
        ALog.i("initReadCard: 初始化读卡串口函数");
        int[] screenSize = DeviceUtils.getScreenSize(this);
        int typeDevice = 1;
        if (screenSize == null) {
            ALog.d("initReadCard: 获取屏幕尺寸失败");
            showWarningToast("获取屏幕尺寸失败");
        } else {
            typeDevice = screenSize[0] > 800 ? 1 : 2;
            // Toast.makeText(this, "获取屏幕尺寸宽度:"+screenSize[0], Toast.LENGTH_SHORT);
        }
        showInfoToast("获取屏幕尺寸宽度:" + screenSize[0]);
        ALog.d("initReadCard: 获取屏幕尺寸宽度:" + screenSize[0]);
        BasePopupView popupView = new XPopup.Builder(getActivity()).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                .asCustom(new LoadingPopDialog(getActivity(), "初始化中，请稍后......")).show();
        // typeDevice = 1;
        if (typeDevice == 1) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String path = CardSerialConfigUtil.getCardSerialPath();
                    int baudrate = CardSerialConfigUtil.getCardSerialBaudRate();
                    int portSate = BasicOper.dc_open("COM", getActivity(), path, baudrate);
                    ALog.d("读卡串口[" + path + "] portSate:" + portSate);
                    if (portSate >= 0) {
                        BasicOper.dc_beep(5);
                    }
                    initLongReader();
                    // 等待一秒
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            popupView.dismiss();
                        }
                    });
                    startReadLongPassCardID();// 近距离卡
                }
            }).start();
            // ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            // @Override
            // public String doInBackground() throws Throwable {
            //
            // int portSate = BasicOper.dc_open("COM", getActivity(), "/dev/ttyS3", 115200);
            // ALog.d("ttyS3读卡串口portSate:" + portSate);
            // if (portSate >= 0) {
            // BasicOper.dc_beep(5);
            // }
            // initLongReader();
            // // 等待一秒
            // try {
            // Thread.sleep(1000);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            // startReadLongPassCardID();// 近距离卡
            // // startReadLongCard();//远距离卡
            // // // handler.post(runnable); // 开始循环调用
            // // // 使用HandlerThread来避免主线程阻塞
            // // handlerThread = new HandlerThread("LivenessDetectHandlerThread");
            // // handlerThread.start();
            // // countdownHandler.removeMessages(3);
            // // countdownHandler.sendEmptyMessageDelayed(3, 50L);
            //
            // // Handler backgroundHandler = new Handler(handlerThread.getLooper());
            // // backgroundHandler.post(new Runnable() {
            // // @Override
            // // public void run() {
            // // // if (!isLoopPaused) {
            // // // getLongPassCardID();
            // // // }
            // // getLongPassCardID();
            // // backgroundHandler.postDelayed(this, 300); // 每隔300ms调用一次
            // // }
            // // });
            // return null;
            // }
            // });
        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    String path = CardSerialConfigUtil.getCardSerialPath();
                    int band = CardSerialConfigUtil.getCardSerialBaudRate();
                    byte protocol = 0; // 非接触式协议
                    Card card = new AndroidSerialPort(getActivity());
                    short st = card.OpenReader(path, band);
                    if (st >= 0) {
                        ALog.d("OpenReader success" + st);
                    } else {
                        ALog.d("OpenReader fail" + st);
                    }
                    st = card.rf_select_protocol(protocol);
                    if (st >= 0) {
                        ALog.d("rf_select_protocol success" + st);
                    } else {
                        ALog.d("rf_select_protocol fail" + st);
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            popupView.dismiss();
                        }
                    });

                    startReadCarIDMini(card);
                }
            }).start();
            // ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            // @Override
            // public String doInBackground() throws Throwable {
            // String path = "/dev/ttyS3";
            // int band = 115200;
            // byte protocol = 0; // 非接触式协议
            // Card card = new AndroidSerialPort(getActivity());
            // short st = card.OpenReader(path, band);
            // if (st >= 0) {
            // ALog.d("OpenReader success" + st);
            // } else {
            // ALog.d("OpenReader fail" + st);
            // }
            // st = card.rf_select_protocol(protocol);
            // if (st >= 0) {
            // ALog.d("rf_select_protocol success" + st);
            // } else {
            // ALog.d("rf_select_protocol fail" + st);
            // }
            // startReadCarIDMini(card);
            // // Message msg = new Message();
            // // msg.what = 4;
            // // msg.obj = card;
            // // countdownHandler.removeMessages(4);
            // // countdownHandler.sendMessageDelayed(msg, 50L);
            //
            // // // 使用HandlerThread来避免主线程阻塞
            // // handlerThread = new HandlerThread("LivenessDetectHandlerThread");
            // // handlerThread.start();
            // // Handler backgroundHandler = new Handler(handlerThread.getLooper());
            // // backgroundHandler.post(new Runnable() {
            // // @Override
            // // public void run() {
            // // // if (!isLoopPaused) {
            // // // getCarIDMini(card);
            // // // }
            // // getCarIDMini(card);
            // // backgroundHandler.postDelayed(this, 300); // 每隔0.3秒调用一次
            // // }
            // // });
            // return null;
            // }
            // });
        }
    }

    /** 读卡轮询线程是否运行中 */
    boolean reading = false;
    /** 是否处于人脸比对/证件校验流程中，读卡线程在此期间暂停寻卡 */
    boolean checking = false;

    /**
     * 是否正在执行人脸比对或证件校验流程。
     *
     * @return true 表示查验中，读卡线程应跳过寻卡
     */
    public boolean isChecking() {
        return checking;
    }

    /**
     * 标记进入查验流程，暂停读卡线程寻卡。
     */
    public void startChecking() {
        checking = true;
    }

    /**
     * 标记退出查验流程，读卡线程恢复寻卡。
     */
    public void stopChecking() {
        checking = false;
    }

    /**
     * 启动大屏双读卡轮询：先尝试短距 BasicOper PSAM 外部认证读卡，失败则盘点长距 EC_API 标签，
     * 循环执行直至 {@link #stopReadLongPassCardID()}。
     */
    public void startReadLongPassCardID() {
        ALog.i("startReadLongPassCardID");
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                reading = true;
                while (reading) {
                    // if (!isLoopPaused) {
                    // getLongPassCardID();
                    // }
                    if (!isChecking()) {
                        boolean result = getLongPassCardID();
                        ALog.e("result:" + result);
                        if (!result) {
                            setRfidNull();
                            Thread.sleep(100);
                            String uid = readLongCard(ecApi);// 远距离卡
                            // ALog.e("uid:" + uid);
                            if (ObjectUtils.isEmpty(uid)) {
                                setRfidNull();
                            } else {
                                getLongPassCardInfo(uid, true);// 查询id
                            }
                        }
                    }
                    Thread.sleep(ArcFaceApplication.READ_TIME);
                }
                return null;
            }
        });
    }

    /**
     * 停止大屏短距/长距读卡轮询。
     */
    public void stopReadLongPassCardID() {
        ALog.i("stopReadLongPassCardID");
        reading = false;
    }

    /**
     * 启动小屏 AndroidSerialPort 读卡轮询：循环调用 {@link #getCarIDMini(Card)}。
     *
     * @param card 已打开并选协议的读卡器实例
     */
    public void startReadCarIDMini(Card card) {
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                reading = true;
                while (reading) {
                    // if (!isLoopPaused) {
                    // getCarIDMini(card);
                    // }
                    boolean result = getCarIDMini(card);
                    if (!result) {
                        setRfidNull();
                    }
                    Thread.sleep(ArcFaceApplication.READ_TIME);
                }
                return null;
            }
        });
    }

    /**
     * 停止小屏 AndroidSerialPort 读卡轮询。
     */
    public void stopReadCarIDMini() {
        reading = false;
    }

    /**
     * 切换回默认待机 Fragment（Document1），用于验证失败或超时恢复。
     */
    public void turnFragment1() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_all, fragment1);
        fragmentTransaction.commit();
    }

    }

    /** 读卡流程 1 耗时采样（寻卡至取 UID） */
    List<Long> listTime1 = new ArrayList<>();
    /** 读卡流程 2 耗时采样（外部认证至查库） */
    List<Long> listTime2 = new ArrayList<>();

    /**
     * 短距读卡：BasicOper PSAM 外部认证流程读取长期卡 RFID。
     * <p>
     * PSAM：选择应用、PSAM 卡复位 → 加密初始化（rfid）→ 送 PSAM 加密（rand）→ 取加密结果 rand1；
     * ACPU：激活卡片得 rfid → 取随机数 rand → 外部认证（rand1），9000 表示成功。
     * </p>
     *
     * @return true 外部认证成功并已触发查库
     */
    public boolean getLongPassCardID() {
        try {

            long time = TimeUtils.getNowMills();
            Long time1;
            Long time2;
            // ALog.d( "getLongPassCardID: 开始读卡");
            ALog.e("getLongPassCardID.rfid: " + rfid);
            // toast_verified_passed.setVisibility(View.INVISIBLE);//页面更新
            // turnFragment1();//切换成普通状态
            String[] resultArr;
            // 1. psam卡复位
            BasicOper.dc_setcpu(2); // 选择PSAM卡座
            String result = BasicOper.dc_cpureset_hex(); //
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "PSAM卡复位成功", Toast.LENGTH_SHORT).show();
                ALog.d("PSAM卡复位成功: " + resultArr[1]);
            } else {
                // Toast.makeText(this, "PSAM卡复位失败", Toast.LENGTH_SHORT).show();
                ALog.e("PSAM卡复位失败: " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }
            String Psam_getterminalid = "00B0960006"; // 获取psam卡号
            String cmd = Psam_getterminalid;
            result = BasicOper.dc_cpuapduInt_hex(cmd);
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // 可以不用调用
                ALog.d("获得终端机编号成功 = " + resultArr[1]);
            } else {
                // Toast.makeText(this, "获得终端机编号失败", Toast.LENGTH_SHORT).show();
                ALog.d("获得终端机编号失败 = " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }

            String Psam_getapp = "00A40000021001";
            cmd = Psam_getapp;
            result = BasicOper.dc_cpuapduInt_hex(cmd);
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "选择应用", Toast.LENGTH_SHORT).show();
                ALog.d("选择应用成功 = " + resultArr[1]);
            } else {
                // Toast.makeText(this, "选择应用失败", Toast.LENGTH_SHORT).show();
                ALog.d("选择应用失败 = " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }

            // 2. 激活卡片,得到rfid
            result = BasicOper.dc_config_card(0x00); // 选择typeA
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "选卡类型成功", Toast.LENGTH_SHORT).show();
                // ALog.d( "选卡类型成功 ");
            } else {
                // showWarningToast("选卡类型失败");
                ALog.d("选卡类型失败 " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }

            String resultRf = BasicOper.dc_reset(); // 射频复位
            String[] resultArrRf = resultRf.split("\\|", -1);
            if (resultArrRf[0].equals("0000")) {
                // Toast.makeText(this, "射频复位成功", Toast.LENGTH_SHORT);
                ALog.d("射频复位成功" + resultArrRf[1]);
            } else {
                // Toast.makeText(this, "射频复位失败", Toast.LENGTH_SHORT);
                ALog.d("射频复位失败" + resultArrRf[0] + " error msg = " + resultArrRf[1]);
                return false;
            }
            // 寻卡请求，防卡冲突
            String result1 = BasicOper.dc_card_n_hex(0x01);
            String[] resultArr1 = result1.split("\\|", -1);
            String rfidLocal = "";
            if (resultArr1[0].equals("0000")) {
                String strInfo = null;
                strInfo = HexDump.toHexString(resultArr1[1].getBytes());
                ALog.i("strInfo: " + strInfo);
                ALog.d("uid = " + resultArr1[1]);
                rfidLocal = reorderString(resultArr1[1]);
                ALog.i("rfid: " + rfidLocal);
                if (rfidLocal == null || rfidLocal.length() == 0) {
                    ALog.d("读取卡号为空 = null");
                    return false;
                }
                if (rfidLocal.equals(rfid)) {
                    ALog.i("重复读卡: " + rfidLocal);
                    return false;
                }
                rfid = rfidLocal;
                // getLongPassCardInfo(rfidLocal);//查询id
                time1 = TimeUtils.getNowMills() - time;
                listTime1.add(time1);
                ALog.e("读写卡流程1花费时间：" + time1 + "毫秒");
                long total = 0;
                for (long item : listTime1) {
                    total += item;
                }
                ALog.e("读写卡流程1平均花费时间：" + total / listTime1.size() + "毫秒");
            } else {
                // ALog.d( "寻卡失败 = " + resultArr1[0] + " error msg = " + resultArr1[1]);
                return false;
            }

            // 内部卡验证
            // 3.取随机数rand
            String resultTora = BasicOper.dc_pro_resetInt_hex();
            resultArr = resultTora.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "复位成功:"+resultArr[1], Toast.LENGTH_SHORT).show();
                ALog.d("复位成功 = " + resultArr[1]);
            } else {
                // showWarningToast("复位失败" + resultArr[0]);
                // Toast.makeText(this, "复位失败" + resultArr[0], Toast.LENGTH_SHORT).show();
                ALog.d("复位失败 = " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }

            String resultSen = BasicOper.dc_procommandInt_hex("0084000008", 7);
            String[] resultSenArr = resultSen.split("\\|", -1);
            String RAND = "";
            if (resultSenArr[0].equals("0000")) {
                // Toast.makeText(this, "随机数 = " + resultSenArr[1], Toast.LENGTH_SHORT).show();
                RAND = resultSenArr[1].substring(0, 16);
                ALog.d("随机数成功 = " + resultSenArr[1]);
            } else {
                // showWarningToast("随机数失败" + resultSenArr[0]);
                ALog.d("随机数失败 = " + resultSenArr[0] + " error msg = " + resultSenArr[1]);
                return false;
            }
            // 4.加密初始化（参数：rfid）
            String Psam_init = "801A280008" + "0000" + "3F00"; // 4. 加密初始化
            cmd = Psam_init + rfid;
            result = BasicOper.dc_cpuapduInt_hex(cmd);
            resultArr = result.split("\\|", -1);

            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "加密初始化成功"+resultArr[1], Toast.LENGTH_SHORT).show();
                ALog.d("加密初始化成功 = " + resultArr[1]);
            } else {
                // showWarningToast("加密初始化失败" + resultArr[0]);
                ALog.d("加密初始化失败 = " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }
            // 5.送PSAM加密（参数：rand）
            String Psam_encryption = "80FA000008";
            cmd = Psam_encryption + RAND;
            result = BasicOper.dc_cpuapduInt_hex(cmd);
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "加密成功"+resultArr[1], Toast.LENGTH_SHORT).show();
                ALog.d("加密成功 = " + resultArr[1]);
            } else {
                // showWarningToast("加密失败" + resultArr[0]);
                ALog.d("加密失败 = " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }
            // 6. 取加密结果rand1
            String Psam_encryptionresult = "00C0000008";
            cmd = Psam_encryptionresult;
            result = BasicOper.dc_cpuapduInt_hex(cmd);
            resultArr = result.split("\\|", -1);
            if (resultArr[0].equals("0000")) {
                // Toast.makeText(this, "加密结果 = "+resultArr[1], Toast.LENGTH_SHORT).show();
                RAND = resultArr[1].substring(0, 16);
                ALog.d("加密结果 = " + resultArr[1]);
            } else {
                // showWarningToast("加密结果失败" + resultArr[0]);
                ALog.d("加密结果失败 = " + resultArr[0] + " error msg = " + resultArr[1]);
                return false;
            }
            // 7.外部认证
            String Cpu_outauthenticate = "0082000108";
            cmd = Cpu_outauthenticate + RAND;
            resultSen = BasicOper.dc_procommandInt_hex(cmd, 7);
            resultSenArr = resultSen.split("\\|", -1);
            if (resultSenArr[0].equals("0000") && resultSenArr[1].contains("9000")) {
                // Toast.makeText(this, "外部认证成功", Toast.LENGTH_SHORT).show();
                ALog.d("外部认证成功,卡号 = " + rfid);
                // 通过，自己的卡
                // isLoopPaused = true;
                time2 = TimeUtils.getNowMills() - time;
                ALog.e("读写卡流程2花费时间：" + time2 + "毫秒");
                listTime2.add(time2);
                long total = 0;
                for (long item : listTime2) {
                    total += item;
                }
                ALog.e("读写卡流程2平均花费时间：" + total / listTime2.size() + "毫秒");
                getLongPassCardInfo(rfid, false);// 查询id
                return true;
            } else {
                // showWarningToast("外部认证失败" + resultSenArr[0]);
                ALog.d("外部认证失败 = " + resultSenArr[0] + " error msg = " + resultSenArr[1]);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e(e.getMessage());
        }
        return false;
    }

    /**
     * 将 UID 字节序重排为标准 RFID 字符串（大端 ↔ 小端转换）。
     *
     * @param str 原始十六进制 UID
     * @return 重排后的卡号
     */
    String reorderString(String str) {
        // 获取输入字符串的字符数组
        char[] charArray = str.toCharArray();

        // 按照目标字符串的顺序重新排列字符数组
        char[] reorderedArray = new char[charArray.length];
        reorderedArray[0] = charArray[6]; // G
        reorderedArray[1] = charArray[7]; // H
        reorderedArray[2] = charArray[4]; // F
        reorderedArray[3] = charArray[5]; // E
        reorderedArray[4] = charArray[2]; // C
        reorderedArray[5] = charArray[3]; // D
        reorderedArray[6] = charArray[0]; // A
        reorderedArray[7] = charArray[1]; // B

        // 将重新排列后的字符数组转换为字符串
        return new String(reorderedArray);
    }

    /**
     * 小屏 AndroidSerialPort 非接触式 CPU 卡复位读卡，成功后以短距 cardId 查库。
     *
     * @param card 已打开并选协议的读卡器实例
     * @return true 读到新卡号并已触发查库
     */
    boolean getCarIDMini(Card card) {
        try {
            ALog.e("getCarIDMini: " + rfid);
            byte protocol = 0; // 非接触式协议
            String path = CardSerialConfigUtil.getCardSerialPath();
            int band = CardSerialConfigUtil.getCardSerialBaudRate();
            byte[] cardUID = new byte[12];
            byte[] rst = new byte[64];

            // Card card = new AndroidSerialPort(this);
            // short st = card.OpenReader(path, band);
            // if (st >= 0) {
            // ALog.d( "OpenReader success" + st);
            // } else {
            // ALog.d( "OpenReader fail" + st);
            // }
            // short st = card.rf_select_protocol(protocol);
            // if (st >= 0) {
            // ALog.d( "rf_select_protocol success" + st);
            // } else {
            // ALog.d( "rf_select_protocol fail" + st);
            // }
            short st = card.ContactlessCpuCardReset(rst, cardUID); // 非接触式读卡 rst 返回复位信息 cardUID 为卡号
            if (st >= 0) {
                ALog.d("ContactlessCpuCardReset success,cardUID" + gson.toJson(cardUID));
                try {
                    String s = Utils.byteArrayToHexString(cardUID, 1, cardUID[0]);
                    String rfidLocal = reorderString(s);
                    ALog.d("ContactlessCpuCardReset success,rfid:" + rfid);
                    if (rfidLocal == null || rfidLocal.length() == 0) {
                        ALog.d("读取卡号为空 = null");
                        return false;
                    }
                    if (rfidLocal.equals(rfid)) {
                        ALog.i("重复读卡: " + rfidLocal);
                        return false;
                    }
                    rfid = rfidLocal;
                    getLongPassCardInfo(rfidLocal, false);// 查询id
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                // ALog.d( "ContactlessCpuCardReset fail" + st);
            }

        } catch (Exception e) {
            e.printStackTrace();
            ALog.e(e.getMessage());
        }
        return false;
    }

    /**
     * 初始化二维码扫描串口（临时证扫码），收到数据后调用 {@link #getShortPassCardID(String)}。
     * 当前渠道不支持临时证时直接返回。
     */
    public void initScanCard() {
        if (!ChannelConfig.SUPPORTS_TEMPORARY_PASS) {
            return;
        }
        SerialManage.getInstance().init(new SerialInter() {
            @Override
            public void connectMsg(String path, boolean isSucc) {
                String msg = isSucc ? "成功" : "失败";
                ALog.d("串口 " + path + " -连接" + msg);
            }

            @Override
            public void readData(String path, byte[] bytes, int size) {
                // ALog.d( "readData: "+gson.toJson(bytes.toString()));
                String data = new String(bytes, 0, size);
                // 判断是否String是否有/r，有则去掉
                data = data.replace("\r", "");
                ALog.d("二维码接收到的数据: " + gson.toJson(data));
                getShortPassCardID(data);
            }
        });// 串口初始化
        SerialManage.getInstance().open();// 打开串口

    }

    /**
     * 将证件状态码转为中文描述。
     *
     * @param status 1 正常 / 2 注销 / 3 过期 / 4 挂失 / 5 停用
     * @return 状态中文文案
     */
    public String theCardIsExpired(int status) {
        String sta = "正常";
        if (status == 1) {
            sta = "正常";
        }
        if (status == 2) {
            sta = "注销";
        }
        if (status == 3) {
            sta = "过期";
        }
        if (status == 4) {
            sta = "挂失";
        }
        if (status == 5) {
            sta = "停用";
        }
        return sta;
    }

    /** 清空当前 RFID，允许再次刷同一张卡 */
    void setRfidNull() {
        rfid = "";
    }

    /**
     * 从本地存储解析设备所属区域，取最深层子区域名称用于界面展示。
     *
     * @return 区域名称，解析失败返回空字符串
     */
    public String getArea() {
        String areaDetail = infoStorage.getString("deviceAreaDetail", "");
        ALog.i("infoStorage.areaDetail: " + gson.toJson(areaDetail));
        // 解析 JSON 字符串为 Area 对象
        Area area = gson.fromJson(areaDetail, Area.class);
        String name = "";
        if (area != null) {
            name = area.getName();
            if (ObjectUtils.isNotEmpty(area.getChildren())) {
                for (Area firstChildren : area.getChildren()) {
                    name = firstChildren.getName();
                    if (ObjectUtils.isNotEmpty(firstChildren.getChildren())) {
                        for (Area secondChildren : firstChildren.getChildren()) {
                            name = secondChildren.getName();
                        }
                    }
                }
            }
        }
        return name;
    }

    /** 判断通行证 areaIds 或 areaRootIds 是否包含当前设备区域 */
    boolean isAreaPass(String[] areaIds, String[] areaRootIds) {
        if (ObjectUtils.isEmpty(areaIds) || ObjectUtils.isEmpty(areaRootIds)) {
            return false;
        }
        return isAreaIdsPass(areaIds) || isAreaRootIdsPass(areaRootIds);
    }

    /** 校验 areaIds 是否与设备绑定区域匹配 */
    boolean isAreaIdsPass(String[] areaIds) {
        if (ObjectUtils.isEmpty(areaIds)) {
            return false;
        }
        String areaDetail = infoStorage.getString("deviceAreaDetail", "");
        ALog.i("infoStorage.areaDetail: " + gson.toJson(areaDetail));
        // 解析 JSON 字符串为 Area 对象
        Area area = gson.fromJson(areaDetail, Area.class);
        if (area != null) {
            for (String id : areaIds) {
                if (ObjectUtils.isNotEmpty(area.getChildren()) && (area.getCategory() == 1)) {
                    for (Area firstChildren : area.getChildren()) {
                        if (firstChildren.getCategory() == 2) {
                            if (id.equals(firstChildren.getId())) {
                                ALog.e("firstChildren.getId()" + firstChildren.getId());
                                return true;
                            }
                        }
                        // if (ObjectUtils.isNotEmpty(firstChildren.getChildren()) && firstChildren.getCategory() == 2)
                        // {
                        // for (Area secondChildren : firstChildren.getChildren()) {
                        // if (id.equals(secondChildren.getId())) {
                        // ALog.e("secondChildren.getId()" + secondChildren.getId());
                        // return true;
                        // }
                        // }
                        // } else if (id.equals(firstChildren.getId())) {
                        // ALog.e("firstChildren.getId()" + firstChildren.getId());
                        // return true;
                        // }
                    }
                } else if (id.equals(area.getId())) {
                    ALog.e("area.getId()" + area.getId());
                    return true;
                }
            }
        }
        return false;
    }

    /** 校验 areaRootIds 是否与设备绑定根区域匹配 */
    boolean isAreaRootIdsPass(String[] areaRootIds) {
        if (ObjectUtils.isEmpty(areaRootIds)) {
            return false;
        }
        String areaDetail = infoStorage.getString("deviceAreaDetail", "");
        ALog.i("infoStorage.areaDetail: " + gson.toJson(areaDetail));
        // 解析 JSON 字符串为 Area 对象
        Area area = gson.fromJson(areaDetail, Area.class);
        if (area != null) {
            for (String id : areaRootIds) {
                if (ObjectUtils.isNotEmpty(area.getChildren()) && area.getCategory() == 1) {
                    for (Area firstChildren : area.getChildren()) {
                        if (firstChildren.getCategory() == 2) {
                            if (id.equals(firstChildren.getId())) {
                                ALog.e("firstChildren.getId()" + firstChildren.getId());
                                return true;
                            }
                        }
                        // if (ObjectUtils.isNotEmpty(firstChildren.getChildren())
                        // && (firstChildren.getCategory() == 1 || firstChildren.getCategory() == 2)) {
                        // for (Area secondChildren : firstChildren.getChildren()) {
                        // if (id.equals(secondChildren.getId())) {
                        // ALog.e("secondChildren.getId()" + secondChildren.getId());
                        // return true;
                        // }
                        // }
                        // } else if (id.equals(firstChildren.getId())) {
                        // ALog.e("firstChildren.getId()" + firstChildren.getId());
                        // return true;
                        // }
                    }
                } else if (id.equals(area.getId())) {
                    ALog.e("area.getId()" + area.getId());
                    return true;
                }
            }
        }
        return false;
    }

    /** 最近一次成功查证的通行证，用于 1500ms 内防重复查询 */
    public LongTermPass lastLongTermPass;
    /** 最近一次成功查证的时间戳 */
    long lastTime;

    // ========== 证件查询与区域校验 ==========

    /**
     * 根据 RFID 从本地 Room 库查询长期证，校验区域与引领人后进入人脸比对流程。
     * 长距读卡使用 cardIdLong 字段，短距读卡使用 cardId 字段。
     *
     * @param rfid   读卡得到的卡号
     * @param isYuan true 长距 EC_API 读卡 / false 短距 BasicOper 或 AndroidSerialPort 读卡
     */
    public void getLongPassCardInfo(String rfid, boolean isYuan) {
        ALog.i("根据rfid查询本地数据库: " + rfid + ",isYuan:" + isYuan);
        // toast_verified_passed.setVisibility(View.INVISIBLE);//页面更新
        // toast_verified_fail.setVisibility(View.INVISIBLE);
        iv_face.setVisibility(View.INVISIBLE);
        LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (isChecking()) {
                    return null;
                }

                if (ObjectUtils.isNotEmpty(lastLongTermPass) && lastTime > 0
                        && (TimeUtils.getNowMills() - lastTime < 1500)) {
                    ALog.d("lastLongTermPass:" + lastLongTermPass.toString() + "， lastTime:" + lastTime);
                    if (isYuan) {
                        if (ObjectUtils.isNotEmpty(rfid) && lastLongTermPass.cardIdLong.equals(rfid)) {
                            ALog.e("过滤1500毫秒内重复查询");
                            return null;
                        }
                    } else {
                        if (ObjectUtils.isNotEmpty(rfid) && lastLongTermPass.cardId.equals(rfid)) {
                            ALog.e("过滤1500毫秒内重复查询");
                            return null;
                        }
                    }
                }

                startChecking();
                if (isYuan) {
                    longTermPass = dao.getBycardIdLong(rfid);
                } else {
                    longTermPass = dao.getByCardId(rfid);
                }

                ALog.d("本地数据库查询" + rfid + "的数据: " + gson.toJson(longTermPass));
                if (longTermPass != null) {
                    playAudio(mediaGet);
                    toggleFragment(longTermPass, 0); // 更新fragment
                    // boolean currentTimeInRange =
                    // DeviceUtils.isCurrentTimeInRange(longTermPass.startDate, longTermPass.expiryDate);

                    if (!checkCard()) {
                        return null;
                    }

                    // long span = TimeUtils.getTimeSpan(TimeUtils.string2Millis(longTermPass.startDate),
                    // TimeUtils.getNowMills(), TimeConstants.SEC);
                    // if (span > 0) {
                    // setRfidNull();
                    // playAudio(mediaReject);
                    // showCustomDialog(2, "证件未生效");
                    // stopChecking();
                    // return null;
                    // }
                    //
                    // span = TimeUtils.getTimeSpan(TimeUtils.string2Millis(longTermPass.expiryDate),
                    // TimeUtils.getNowMills(), TimeConstants.SEC);
                    // if (span > 0) {
                    // longTermPass.status = 3;
                    // setRfidNull();
                    // playAudio(mediaReject);
                    // showCustomDialog(2, "证件过期");
                    // stopChecking();
                    // return null;
                    // }
                    //
                    // // if (!currentTimeInRange) {
                    // // longTermPass.status = 3;
                    // // setRfidNull();
                    // // playAudio(mediaReject);
                    // // showCustomDialog(2, "证件过期");
                    // // stopChecking();
                    // // // startReadLongPassCardID();
                    // // return null;
                    // // }
                    //
                    // // 判断证件是否正常
                    // if (longTermPass.status != 1) {
                    // setRfidNull();
                    // playAudio(mediaReject);
                    // showCustomDialog(2, "证件已" + theCardIsExpired(longTermPass.status));
                    // stopChecking();
                    // // startReadLongPassCardID();
                    // return null;
                    // }

                    // 判断是否拥有当前区域权限
                    // String code = jsonToArea();
                    // if (code != null && code != "")
                    if (!isAreaPass(longTermPass.areaIds, longTermPass.areaRootIds)) {
                        // if (Arrays.asList(longTermPass.areaDisplayCode).contains(code)) {
                        // ALog.i("拥有当前区域权限" + longTermPass.cardId);
                        // }
                        ALog.i("没有有当前区域权限" + longTermPass.cardId);
                        showCustomDialog(2, "无当前区域权限");
                        setRfidNull();
                        stopChecking();
                        // startReadLongPassCardID();
                        return null;
                    }
                    ALog.i("拥有当前区域权限" + longTermPass.cardId);
                    // C类卡
                    if (longTermPass.idCode.startsWith("C")) {
                        // 判断是否关联引领人
                        if (ObjectUtils.isNotEmpty(longTermPass.leadingPeopleId)) {
                            String[] leadingPeopleId = longTermPass.leadingPeopleId;
                            String linshiID = infoStorage.getString("linshiID", "");
                            ALog.i("长期证件: " + gson.toJson(linshiID));
                            if (linshiID.equals("")) {
                                showCustomDialog(2, "请先刷引领人");
                                ALog.i("设置了引领人的C类卡请先刷引领人");
                                stopChecking();
                                return null;
                            }
                            boolean contains = Arrays.asList(leadingPeopleId).contains(linshiID);
                            if (!contains) {
                                playAudio(mediaReject);
                                showCustomDialog(2, "引领人请刷卡");
                                stopChecking();
                                return null;
                            }

                            playAudio(mediaGet);
                            ALog.i("引领人审核通过: ");
                            switchFragment3(longTermPass);
                            // String s = Constants.DEFAULT_REGISTER_FACES_DIR + "/" + longTermPass.idCode + ".jpg";
                            // Bitmap bitmap = ImageUtils.loadBitmapFromPath(s);

                            if (!checkCard()) {
                                return null;
                            }
                            // if (longTermPass.status != 1) {
                            // showCustomDialog(2, "证件已" + theCardIsExpired(longTermPass.status));
                            // stopChecking();
                            // return null;
                            // }

                            // showCustomDialog(1, "设置了引领人的C类卡请验证人脸");
                        }
                    }
                    showCustomDialog(1, "请验证人脸");
                    // Bitmap bitmap =
                    // ImageDownloader.loadAndDecryptImage(longTermPass.id, getActivity());

                    Bitmap bitmap = AESUtils.decryptRegisterFileToBitmap(longTermPass.id);

                    faceFeature = livenessDetectViewModel.getFeature(bitmap);// 传递给人脸一比一接口
                    if (faceFeature == null) {
                        showCustomDialog(2, "人脸图片无效");
                        stopChecking();
                        return null;
                    }

                    checkFailed = false;
                    // countdownHandler.removeMessages(8);
                    // countdownHandler.sendEmptyMessageDelayed(8, 3000L);
                    // C卡不能引领人
                    if (!longTermPass.idCode.startsWith("C")) {
                        // 保存记录到缓存五分钟
                        saveRecord(longTermPass);
                    }
                    // 获取当前系统时间
                    xtTime = System.currentTimeMillis();
                } else {
                    setRfidNull();
                    ALog.i("getLongPassCardInfo: 本地数据库为空:" + rfid);
                    playAudio(mediaReject);
                    faceFeature = null;
                    // isLoopPaused = false; // 接受识别结果,继续开始读卡
                    showCustomDialog(2, "长期卡不存在");
                    stopChecking();
                }
                return null;
            }

            @Override
            public void onSuccess(String result) {
                // popupView.dismiss();
            }
        });
    }

    // ========== 通行记录保存与上传 ==========

    /**
     * 构建并保存长期证通行记录（含现场图加密落盘、写入 Room、更新底部列表）。
     *
     * @param longTermPass 通行证实体
     * @param bitmap       现场抓拍图
     * @param faceSimilar  人脸相似度
     * @param quality      人脸质量分
     * @param status       true 验证通过 / false 人证不匹配
     */
    public void saveLongTermRecords(LongTermPass longTermPass, Bitmap bitmap, float faceSimilar, float quality,
            boolean status) {
        float qualityvalue = livenessDetectViewModel.getFeatureValue(bitmap);
        ALog.e("qualityvalue:" + qualityvalue);
        quality = qualityvalue;
        try {
            // 获取当前时间
            long time = System.currentTimeMillis();
            LongTermRecords longTermRecords = new LongTermRecords();
            // longTermRecords.id = UUID.randomUUID().toString();
            // longTermRecords.id = SnowflakeIdUtil.getInstance().nextId() + "";
            SnowFlake worker = new SnowFlake(1, 1, 1);
            longTermRecords.needVerify = VerifyFeatureSettings.needVerifyForNewRecord();
            longTermRecords.id = worker.nextId() + "";
            longTermRecords.checkTime = TimeUtils.getNowString();
            longTermRecords.status = String.valueOf(status);
            if (!status) {
                longTermRecords.reason = "人证不匹配";
            }

            if (longTermPass.idCode.startsWith("C") && ObjectUtils.isNotEmpty(longTermPass.leadingPeopleId)) {
                longTermRecords.parentld = localLongId;
                String linshiID = infoStorage.getString("linshiID", "");
                longTermRecords.leadingPeopleld = linshiID;
            } else {
                localLongId = longTermRecords.id;
            }

            longTermRecords.passid = longTermPass.id;
            longTermRecords.idCode = longTermPass.idCode;
            longTermRecords.cardId = longTermPass.cardId;
            longTermRecords.applyId = longTermPass.applyId;
            // longTermRecords.sitePhoto = imgUrl;
            // String deviceDirection = infoStorage.getString("deviceDirection", "1");
            longTermRecords.direction = direction + "";
            longTermRecords.nickname = longTermPass.nickname;
            longTermRecords.photo = longTermPass.photo;
            longTermRecords.leadingPeople = longTermPass.leadingPeople;
            ;// 通行方向（1：进，-1出，2：核验）
            String deviceId = infoStorage.getString("deviceId", "");
            longTermRecords.deviceId = deviceId;
            String deviceName = infoStorage.getString("deviceName", "立式查验终端");
            longTermRecords.deviceName = deviceName;
            String userId = ApiUtils.userId;
            longTermRecords.checkUserId = userId;
            String loginName = infoStorage.getString("loginName", "");
            longTermRecords.checkUserName = loginName;
            // ALog.i("saveLongTermRecords查验人名字: " + longTermPass.nickname);
            // longTermRecords.checkUserName = longTermPass.nickname;
            longTermRecords.companyName = longTermPass.companyName;
            longTermRecords.expiryDate = longTermPass.expiryDate;
            longTermRecords.templateType = longTermPass.templateType;
            longTermRecords.areaDisplayCode = longTermPass.areaDisplayCode;

            longTermRecords.faceSimilar = faceSimilar + "";
            longTermRecords.faceQuality = quality + "";

            String areaDetail = infoStorage.getString("deviceAreaDetail", "");
            ALog.i("infoStorage.areaDetail: " + gson.toJson(areaDetail));
            // 解析 JSON 字符串为 Area 对象
            Area area = gson.fromJson(areaDetail, Area.class);
            if (area != null) {
                longTermRecords.area = area.getId();
                longTermRecords.areaName = area.getCode() + area.getName();
                if (area.getChildren() != null && ObjectUtils.isNotEmpty(area.getChildren())) {

                    for (Area item : area.getChildren()) {
                        longTermRecords.areaName += item.getCode() + item.getName();// "通行区域名称(通行区域编码+名称)";
                    }

                    // Area childArea = area.getChildren().get(0);
                    // if (childArea != null) {
                    //
                    // Area childArea1 = area.getChildren().get(1);
                    // if (childArea1 != null) {
                    // longTermRecords.areaName += childArea1.getCode() + childArea1.getName();
                    // }
                    // }

                }
            }

            ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
                @Override
                public String doInBackground() throws Throwable {
                    ALog.e("doInBackground");

                    // if (ArcFaceApplication.getApplication().isOffLine())
                    if (true) {
                        saveLongTermRecordsToDb(longTermRecords, bitmap);
                    } else {
                        // getAllRecords();
                        // 上传通行图片到服务器
                        if (bitmap != null) {
                            String imgUrl = imageUploader.uploadBitmap2(bitmap);
                            ALog.i("上传图片路径: " + imgUrl);
                            if (ObjectUtils.isEmpty(imgUrl)) {
                                saveLongTermRecordsToDb(longTermRecords, bitmap);
                                return null;
                            }
                            longTermRecords.sitePhoto = imgUrl;
                        }
                        // uploadBitmap(bitmap);
                        uploadLongTermRecords(longTermRecords);
                    }
                    return null;
                }

                @Override
                public void onSuccess(String result) {

                    if (checkList.size() > 20) {
                        checkList.remove(checkList.size() - 1);
                    }
                    checkList.add(0, longTermRecords);
                    ALog.e("ADD " + longTermRecords.toString());
                    mListAdapter.notifyDataSetChanged();
                    // mListAdapter.add(0, longTermRecords);
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("保存长期记录失败: " + e.getMessage());
        }
    }

    /**
     * 将长期证通行记录与加密现场图写入本地 Room 数据库。
     *
     * @param records 长期通行记录实体
     * @param bitmap  现场抓拍图，可为 null
     */
    public void saveLongTermRecordsToDb(LongTermRecords records, Bitmap bitmap) {
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SimpleTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (ObjectUtils.isNotEmpty(bitmap)) {
                    File directory = new File(getExternalFilesDir(null), "records");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    File file1 = new File(directory, "temp.jpg");
                    File file2 = new File(directory, records.id + ".jpg");
                    ALog.i("Image temp: " + file1.getAbsolutePath());
                    ALog.i("Image imageName: " + file2.getAbsolutePath());
                    // 加密并保存文件
                    try {
                        AESUtils.encryptBitmapToFile(bitmap, file2, AESUtils.generateKey());
                        // boolean result = ImageUtils.save(bitmap, file1.getAbsolutePath(),
                        // Bitmap.CompressFormat.JPEG);
                        // if (result) {
                        // FileOutputStream fileOutputStream = new FileOutputStream(file2);
                        // FileInputStream fileInputStream = new FileInputStream(file1);
                        // ImageDownloader.encrypt(fileInputStream, fileOutputStream, ImageDownloader.KEY);
                        // }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // 1. 及时回收 Bitmap（可选）
                        // if (bitmap != null && !bitmap.isRecycled()) {
                        // bitmap.recycle();
                        // }
                    }
                    records.sitePhoto = file2.getAbsolutePath();
                }

                LongTermRecordsDao dao = ArcFaceApplication.getApplication().getDb().longTermRecordsDao();
                dao.insert(records);
                return null;
            }
        });

    }

    /**
     * 上传长期证通行记录至服务端，成功后删除本地待同步记录。
     *
     * @param longTermRecords 待上传的长期通行记录
     */
    public void uploadLongTermRecords(LongTermRecords longTermRecords) {
        ALog.i("本地长期记录: " + gson.toJson(longTermRecords));
        PostRequest<Base<String>> request =
                OkGo.<Base<String>> post(UrlConstants.URL_CREATE_LONG_RECORD).tag(UrlConstants.URL_CREATE_LONG_RECORD);
        request.headers("tenant-id", UrlConstants.TENANT_ID);
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        request.upJson(gson.toJson(longTermRecords)).execute(new JsonCallback<Base<String>>() {
            @Override
            public void onSuccess(Response<Base<String>> response) {
                if (ObjectUtils.isEmpty(response.body())) {
                    showToast("uploadTemporaryRecords失败");
                    return;
                }
                Base<String> res = response.body();
                if (res.getCode() == 200) {
                    ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SimpleTask() {
                        @Override
                        public String doInBackground() throws Throwable {
                            ArcFaceApplication.getApplication().getDb().longTermRecordsDao().delete(longTermRecords);
                            return null;
                        }
                    });

                } else {
                    ALog.e("上传长期证件日志失败");
                    showWarningToast(res.getMsg());
                }
            }

            @Override
            public void onError(Response<Base<String>> response) {
                response.getException().printStackTrace();
                ALog.e("上传长期证件日志失败," + response.getException().getMessage());
                saveLongTermRecordsToDb(longTermRecords, null);

            }
        });

        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // ApiUtils.post(UrlConstants.URL_CREATE_LONG_RECORD, gson.toJson(longTermRecords),
        // new ApiUtils.ApiCallback() {
        // @Override
        // public void onSuccess(String response) {
        // ALog.d("上传长期证件成功返回: " + response);
        // }
        //
        // @Override
        // public void onFailure(Throwable e) {
        // ALog.e("上传长期证件日志失败返回: " + e.getMessage());
        // }
        // });
        // return null;
        // }
        // });
    }

    /**
     * 根据二维码/临时证 applyId 从本地库查询临时通行证并进入人脸比对流程。
     * 须先刷长期卡作为引领人（linshiID 不为空）。
     *
     * @param carID 扫码或临时证 applyId
     */
    public void getShortPassCardID(String carID) {
        if (!ChannelConfig.SUPPORTS_TEMPORARY_PASS) {
            ALog.i("当前渠道不支持临时通行证");
            return;
        }
        // toast_verified_passed.setVisibility(View.INVISIBLE);//页面更新
        // toast_verified_fail.setVisibility(View.INVISIBLE);
        iv_face.setVisibility(View.INVISIBLE);
        // turnFragment1();//切换成普通状态
        String linshiID = infoStorage.getString("linshiID", "");
        ALog.i("长期证件: " + gson.toJson(linshiID));
        if (linshiID.equals("")) {
            showCustomDialog(2, "请先刷长期卡");
            ALog.i("临时证件刷卡,请先刷长期卡: ");
            return;
        }

        LongTermPassDao dao = ArcFaceApplication.getApplication().getDb().longTermPassDao();

        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (ObjectUtils.isNotEmpty(lastLongTermPass) && lastTime > 0
                        && (TimeUtils.getNowMills() - lastTime < 1500)) {
                    ALog.d("lastLongTermPass:" + lastLongTermPass.toString() + "， lastTime:" + lastTime);
                    if (lastLongTermPass.applyId.equals(carID)) {
                        ALog.e("过滤1500毫秒内重复查询");
                        return null;
                    }
                }

                longTermPass = dao.getByApplyId(carID);

                if (longTermPass != null) {
                    ALog.i("从本地数据库获取临时卡信息: " + longTermPass.toString());
                    switchFragment3(longTermPass);
                    playAudio(mediaGet);

                    if (!checkCard()) {
                        return null;
                    }

                    if (!isAreaPass(longTermPass.areaIds, longTermPass.areaRootIds)) {
                        // if (Arrays.asList(longTermPass.areaDisplayCode).contains(code)) {
                        // ALog.i("拥有当前区域权限" + longTermPass.cardId);
                        // }
                        ALog.i("没有有当前区域权限" + longTermPass.cardId);
                        showCustomDialog(2, "无当前区域权限");
                        setRfidNull();
                        return null;
                    }
                    ALog.i("拥有当前区域权限" + longTermPass.cardId);

                    // 判断是否有长期的卡片关联
                    String[] leadingPeopleId = longTermPass.leadingPeopleId;
                    // 判断leadingPeopleId是否包含linshiID
                    if (leadingPeopleId == null && leadingPeopleId.length == 0) {
                        playAudio(mediaReject);
                        showCustomDialog(2, "请关联长期卡");
                        return null;
                    }
                    boolean contains = Arrays.asList(leadingPeopleId).contains(linshiID);
                    if (contains) {
                        playAudio(mediaGet);
                        ALog.i("引领人审核通过: ");
                        switchFragment3(longTermPass);
                        // String s = Constants.DEFAULT_REGISTER_FACES_DIR + "/" + longTermPass.idCode + ".jpg";
                        // Bitmap bitmap = ImageUtils.loadBitmapFromPath(s);
                        // if (longTermPass.status != 1) {
                        // showCustomDialog(2, "证件已" + theCardIsExpired(longTermPass.status));
                        // return null;
                        // }

                        if (!checkCard()) {
                            return null;
                        }

                        // Bitmap bitmap = ImageDownloader.loadAndDecryptImage(longTermPass.id,
                        // getActivity());

                        Bitmap bitmap = AESUtils.decryptRegisterFileToBitmap(longTermPass.id);

                        faceFeature = livenessDetectViewModel.getFeature(bitmap);// 传递给人脸一比一接口
                        if (faceFeature == null) {
                            showCustomDialog(2, "人脸图片无效");
                            return null;
                        }
                        showCustomDialog(1, "请验证人脸");
                    } else {
                        playAudio(mediaReject);
                        showCustomDialog(2, "引领人请刷卡");
                    }
                } else {
                    ALog.d("临时卡不存在: " + carID);
                    // soundManager.playSound(R.raw.reject, 1.0f, 1.0f, 0, 1.0f);
                    playAudio(mediaReject);
                    showCustomDialog(2, "临时卡不存在");
                }
                return null;
            }
        });
    }

    /**
     * 校验证件有效期、状态、黑名单、分数及通行时段等业务规则。
     *
     * @return true 全部校验通过；false 已提示用户并停止查验
     */
    public boolean checkCard() {
        if (longTermPass.type == 1 && !ChannelConfig.SUPPORTS_TEMPORARY_PASS) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "不支持临时通行证");
            stopChecking();
            return false;
        }
        long span = TimeUtils.getTimeSpan(DateUtil.string2MillisStartDate(longTermPass.startDate), TimeUtils.getNowMills(),
                TimeConstants.SEC);
        if (span > 0) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "证件未生效");
            stopChecking();
            return false;
        }

        span = TimeUtils.getTimeSpan(DateUtil.string2MillisExpiryDate(longTermPass.expiryDate), TimeUtils.getNowMills(),
                TimeConstants.SEC);
        if (span < 0) {
            longTermPass.status = 3;
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "证件过期");
            stopChecking();
            return false;
        }

        // if (!currentTimeInRange) {
        // longTermPass.status = 3;
        // setRfidNull();
        // playAudio(mediaReject);
        // showCustomDialog(2, "证件过期");
        // stopChecking();
        // // startReadLongPassCardID();
        // return null;
        // }

        // 判断证件是否正常
        if (longTermPass.status != 1) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "证件已" + theCardIsExpired(longTermPass.status));
            stopChecking();
            // startReadLongPassCardID();
            return false;

        }

        if (longTermPass.isBlacklist) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "通行证在黑名单中");
            stopChecking();
            return false;

        }

        if (longTermPass.isWithdraw) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "通行证被收回");
            stopChecking();
            return false;

        }

        if (longTermPass.isWithhold) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "通行证被暂扣");
            stopChecking();
            return false;

        }

        if (longTermPass.score <= 0) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, "通行证分数为0");
            stopChecking();
            return false;

        }

        // 检查时间控制
        TimeControlUtil.TimeControlResult timeControlResult = TimeControlUtil.checkTimeControl(longTermPass);
        if (!timeControlResult.isAllowed()) {
            setRfidNull();
            playAudio(mediaReject);
            showCustomDialog(2, timeControlResult.getErrorMessage());
            stopChecking();
            return false;
        }

        return true;
    }

    /**
     * 构建并保存临时证通行记录（含现场图加密落盘、写入 Room、更新底部列表）。
     *
     * @param longTermPass 临时通行证实体
     * @param bitmap       现场抓拍图
     * @param faceSimilar  人脸相似度
     * @param quality      人脸质量分
     * @param status       true 验证通过 / false 人证不匹配
     */
    public void saveTemporaryRecords(LongTermPass longTermPass, Bitmap bitmap, float faceSimilar, float quality,
            boolean status) {
        float qualityvalue = livenessDetectViewModel.getFeatureValue(bitmap);
        ALog.e("qualityvalue:" + qualityvalue);
        quality = qualityvalue;
        try {

            long time = System.currentTimeMillis();
            TemporaryCardRecords temporaryCardRecords = new TemporaryCardRecords();
            temporaryCardRecords.needVerify = VerifyFeatureSettings.needVerifyForNewRecord();
            // temporaryCardRecords.id = UUID.randomUUID().toString();
            // temporaryCardRecords.id = SnowflakeIdUtil.getInstance().nextId() + "";
            SnowFlake worker = new SnowFlake(1, 1, 1);
            temporaryCardRecords.id = worker.nextId() + "";
            temporaryCardRecords.checkTime = TimeUtils.getNowString();
            // temporaryCardRecords.sitePhoto = imgUrl;
            temporaryCardRecords.status = String.valueOf(status);
            if (!status) {
                temporaryCardRecords.reason = "人证不匹配";
            }
            temporaryCardRecords.passid = longTermPass.id;
            temporaryCardRecords.idCode = longTermPass.idCode;
            temporaryCardRecords.cardId = longTermPass.cardId;
            temporaryCardRecords.applyId = longTermPass.applyId;
            // String deviceDirection = infoStorage.getString("deviceDirection", "1");
            temporaryCardRecords.direction = direction + "";

            temporaryCardRecords.nickname = longTermPass.nickname;
            temporaryCardRecords.photo = longTermPass.photo;

            temporaryCardRecords.leadingPeople = longTermPass.leadingPeople;

            String deviceId = infoStorage.getString("deviceId", "");
            temporaryCardRecords.deviceId = deviceId;
            String deviceName = infoStorage.getString("deviceName", "立式查验终端");
            temporaryCardRecords.deviceName = deviceName;
            String userId = ApiUtils.userId;
            temporaryCardRecords.checkUserId = userId;
            String loginName = infoStorage.getString("loginName", "");
            // ALog.i("saveTemporaryRecords查验人名字: " + longTermPass.nickname);
            temporaryCardRecords.checkUserName = loginName;
            // temporaryCardRecords.checkUserName = longTermPass.nickname;
            temporaryCardRecords.companyName = longTermPass.companyName;
            temporaryCardRecords.expiryDate = longTermPass.expiryDate;
            temporaryCardRecords.templateType = longTermPass.templateType;
            temporaryCardRecords.areaDisplayCode = longTermPass.areaDisplayCode;

            temporaryCardRecords.parentId = localLongId;
            temporaryCardRecords.faceSimilar = faceSimilar + "";
            temporaryCardRecords.faceQuality = quality + "";

            String areaDetail = infoStorage.getString("deviceAreaDetail", "");
            // Area area = gson.fromJson(areaDetail, Area.class);
            // if (area != null) {
            // temporaryCardRecords.area = area.getId();
            // // temporaryCardRecords.areaName = "通行区域名称(通行区域编码+名称)";
            // temporaryCardRecords.areaName += area.getCode() + area.getName();
            // }
            //
            //
            // if (area.getChildren() != null && !area.getChildren().isEmpty()) {
            // Area childArea = area.getChildren().get(0);
            // if (childArea != null) {
            // temporaryCardRecords.areaName += childArea.getCode() + childArea.getName();// "通行区域名称(通行区域编码+名称)";
            // }
            // Area childArea1 = area.getChildren().get(1);
            // if (childArea1 != null) {
            // temporaryCardRecords.areaName += childArea1.getCode() + childArea1.getName();
            // }
            // }
            // 解析 JSON 字符串为 Area 对象
            Area area = gson.fromJson(areaDetail, Area.class);
            if (area != null) {
                temporaryCardRecords.area = area.getId();
                temporaryCardRecords.areaName = area.getCode() + area.getName();
                if (area.getChildren() != null && ObjectUtils.isNotEmpty(area.getChildren())) {

                    for (Area item : area.getChildren()) {
                        temporaryCardRecords.areaName += item.getCode() + item.getName();// "通行区域名称(通行区域编码+名称)";
                    }

                    // Area childArea = area.getChildren().get(0);
                    // if (childArea != null) {
                    //
                    // Area childArea1 = area.getChildren().get(1);
                    // if (childArea1 != null) {
                    // longTermRecords.areaName += childArea1.getCode() + childArea1.getName();
                    // }
                    // }

                }
            }

            String linshiID = infoStorage.getString("linshiID", "");
            temporaryCardRecords.leadingPeopleId = linshiID;
            // temporaryCardRecords.reason = String.valueOf(time);//当前时间

            ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
                @Override
                public String doInBackground() throws Throwable {
                    // TemporaryCardRecordsDao dao =
                    // ArcFaceApplication.getApplication().getDb().temporaryCardRecordsDao();
                    // dao.insert(temporaryCardRecords);
                    // uploadTemporaryRecords(temporaryCardRecords);
                    // runOnSubThread(new Runnable() {
                    // @Override
                    // public void run() {
                    // mListAdapter.add(0, temporaryCardRecords);
                    // }
                    // });
                    // if (ArcFaceApplication.getApplication().isOffLine())
                    if (true) {
                        saveTemporaryRecordsToDb(temporaryCardRecords, bitmap);
                    } else {
                        // getAllRecords();
                        if (bitmap != null) {
                            // 上传通行图片到服务器
                            String imgUrl = imageUploader.uploadBitmap2(bitmap);
                            ALog.i("上传图片路径: " + imgUrl);
                            if (ObjectUtils.isEmpty(imgUrl)) {
                                saveTemporaryRecordsToDb(temporaryCardRecords, bitmap);
                                return null;
                            }
                            temporaryCardRecords.sitePhoto = imgUrl;
                        }
                        uploadTemporaryRecords(temporaryCardRecords);
                    }
                    return null;

                }

                @Override
                public void onSuccess(String result) {
                    if (checkList.size() > 20) {
                        checkList.remove(checkList.size() - 1);
                    }
                    checkList.add(0, temporaryCardRecords);
                    ALog.e("ADD " + temporaryCardRecords.toString());
                    mListAdapter.notifyDataSetChanged();
                    // mListAdapter.add(0, temporaryCardRecords);
                }
            });
        } catch (Exception e) {
            ALog.e("保存临时记录失败: " + e.getMessage());
        }
    }

    /**
     * 将临时证通行记录与加密现场图写入本地 Room 数据库。
     *
     * @param records 临时通行记录
     * @param bitmap  现场抓拍图，可为 null
     */
    public void saveTemporaryRecordsToDb(TemporaryCardRecords records, Bitmap bitmap) {
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SimpleTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (ObjectUtils.isNotEmpty(bitmap)) {
                    File directory = new File(getExternalFilesDir(null), "records");
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }
                    File file1 = new File(directory, "temp.jpg");
                    File file2 = new File(directory, records.id + ".jpg");
                    ALog.i("Image temp: " + file1.getAbsolutePath());
                    ALog.i("Image imageName: " + file2.getAbsolutePath());
                    // 加密并保存文件
                    try {
                        AESUtils.encryptBitmapToFile(bitmap, file2, AESUtils.generateKey());
                        // boolean result = ImageUtils.save(bitmap, file1.getAbsolutePath(),
                        // Bitmap.CompressFormat.JPEG);
                        // if (result) {
                        // FileOutputStream fileOutputStream = new FileOutputStream(file2);
                        // FileInputStream fileInputStream = new FileInputStream(file1);
                        // ImageDownloader.encrypt(fileInputStream, fileOutputStream, ImageDownloader.KEY);
                        // }
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        // 1. 及时回收 Bitmap（可选）
                        // if (bitmap != null && !bitmap.isRecycled()) {
                        // bitmap.recycle();
                        // }
                    }
                    records.sitePhoto = file2.getAbsolutePath();
                }

                TemporaryCardRecordsDao dao = ArcFaceApplication.getApplication().getDb().temporaryCardRecordsDao();
                dao.insert(records);
                return null;
            }
        });

    }

    /**
     * 上传临时证通行记录至服务端，成功后删除本地待同步记录。
     *
     * @param temporaryCardRecords 待上传记录
     */
    public void uploadTemporaryRecords(TemporaryCardRecords temporaryCardRecords) {
        ALog.i("本地临时记录: " + gson.toJson(temporaryCardRecords));

        PostRequest<Base<String>> request =
                OkGo.<Base<String>> post(UrlConstants.URL_CREATE_TEMP_RECORD).tag(UrlConstants.URL_CREATE_TEMP_RECORD);
        request.headers("tenant-id", UrlConstants.TENANT_ID);
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        request.upJson(gson.toJson(temporaryCardRecords)).execute(new JsonCallback<Base<String>>() {
            @Override
            public void onSuccess(Response<Base<String>> response) {
                if (ObjectUtils.isEmpty(response.body())) {
                    showToast("uploadTemporaryRecords失败");
                    return;
                }
                Base<String> res = response.body();
                if (res.getCode() == 200) {
                    ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SimpleTask() {
                        @Override
                        public String doInBackground() throws Throwable {
                            ArcFaceApplication.getApplication().getDb().temporaryCardRecordsDao()
                                    .delete(temporaryCardRecords);
                            return null;
                        }
                    });

                } else {
                    showWarningToast(res.getMsg());
                }
            }

            @Override
            public void onError(Response<Base<String>> response) {
                response.getException().printStackTrace();
                ALog.e("uploadTemporaryRecords," + response.getException().getMessage());
                saveTemporaryRecordsToDb(temporaryCardRecords, null);

            }
        });
    }

    /**
     * 缓存当前长期证 userId 作为 C 类临时证引领人 ID（linshiID）。
     *
     * @param longTermPass 刚刷的长期证，null 或空 userId 时清除缓存
     */
    public void saveRecord(LongTermPass longTermPass) {
        if (longTermPass != null && ObjectUtils.isNotEmpty(longTermPass.userId)) {
            infoStorage.saveString("linshiID", longTermPass.userId);
        } else {
            infoStorage.saveString("linshiID", "");
        }

        // 使用 Handler 定时删除
        // new Handler().postDelayed(new Runnable() {
        // @Override
        // public void run() {
        // infoStorage.remove("linshiID");
        // }
        // }, 5 * 60 * 1000);
    }

    // ========== 初始化 ==========

    /** 从配置加载双摄像头预览参数与活体检测类型 */
    private void initModel() {
        boolean switchCamera = ConfigUtil.isSwitchCamera(this);
        previewConfig = new PreviewConfig(
                switchCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK,
                switchCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT,
                Integer.parseInt(ConfigUtil.getRgbCameraAdditionalRotation(this)),
                Integer.parseInt(ConfigUtil.getIrCameraAdditionalRotation(this)));
        String livenessTypeStr = ConfigUtil.getLivenessDetectType(this);
        if (livenessTypeStr.equals((getString(R.string.value_liveness_type_rgb)))) {
            livenessType = LivenessType.RGB;
        } else if (livenessTypeStr.equals(getString(R.string.value_liveness_type_ir))) {
            livenessType = LivenessType.IR;
        } else {
            livenessType = null;
        }
    }

    /** 初始化 LivenessDetectViewModel 并注册人脸比对回调 */
    private void initViewModel() {
        livenessDetectViewModel = new ViewModelProvider(getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(LivenessDetectViewModel.class);
        livenessDetectViewModel.init(DualCameraHelper.canOpenDualCamera());
        // add by slm 2025-01-22
        livenessDetectViewModel.setFaceFeatureCallback(this);
    }

    /** 初始化预览布局、证件 Fragment 位置与全局布局监听 */
    private void initView() {
        if (!DualCameraHelper.hasDualCamera() || livenessType != LivenessType.IR) {
            binding.flRecognizeIr.setVisibility(View.GONE);
        }
        // 在布局结束后才做初始化操作
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
        // String[] list = { "左下", "左上", "右下", "右上" };
        int checkType = SPUtils.getInstance().getInt("tipsLoc", 0);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) binding.fragmentAll.getLayoutParams();
        switch (checkType) {
            case 0:
                params.gravity = Gravity.BOTTOM | Gravity.LEFT;
                binding.fragmentAll.setLayoutParams(params);
                break;
            case 1:
                params.gravity = Gravity.TOP | Gravity.LEFT;
                binding.fragmentAll.setLayoutParams(params);
                break;
            case 2:
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                binding.fragmentAll.setLayoutParams(params);
                break;
            case 3:
                params.gravity = Gravity.TOP | Gravity.RIGHT;
                binding.fragmentAll.setLayoutParams(params);
                break;
            default:
                break;
        }
    }

    /**
     * 调整View的宽高，使2个预览同时显示
     *
     * @param previewView        显示预览数据的view
     * @param faceRectView       画框的view
     * @param previewSize        预览大小
     * @param displayOrientation 相机旋转角度
     * @return 调整后的LayoutParams
     */
    // private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView
    // faceRectView, Camera.Size previewSize, int displayOrientation, float scale) {
    // ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
    // int measuredWidth = previewView.getMeasuredWidth();
    // int measuredHeight = previewView.getMeasuredHeight();
    // float ratio = ((float) previewSize.height) / (float) previewSize.width;
    // if (ratio > 1) {
    // ratio = 1 / ratio;
    // }
    // if (displayOrientation % 180 == 0) {
    // layoutParams.width = measuredWidth;
    // layoutParams.height = (int) (measuredWidth * ratio);
    // } else {
    // layoutParams.height = measuredHeight;
    // layoutParams.width = (int) (measuredHeight * ratio);
    // }
    // if (scale < 1f) {
    // ViewGroup.LayoutParams rgbParam = rgbPreview.getLayoutParams();
    // layoutParams.width = (int) (rgbParam.width * scale);
    // layoutParams.height = (int) (rgbParam.height * scale);
    // } else {
    // layoutParams.width *= scale;
    // layoutParams.height *= scale;
    // }
    //
    //
    // DisplayMetrics metrics = new DisplayMetrics();
    // getWindowManager().getDefaultDisplay().getMetrics(metrics);
    //
    // if (layoutParams.width >= metrics.widthPixels) {
    // float viewRatio = layoutParams.width / ((float) metrics.widthPixels);
    // layoutParams.width /= viewRatio;
    // layoutParams.height /= viewRatio;
    // }
    // if (layoutParams.height >= metrics.heightPixels) {
    // float viewRatio = layoutParams.height / ((float) metrics.heightPixels);
    // layoutParams.width /= viewRatio;
    // layoutParams.height /= viewRatio;
    // }
    //
    //
    // previewView.setLayoutParams(layoutParams);
    // faceRectView.setLayoutParams(layoutParams);
    // return layoutParams;
    // }
    /**
     * 调整 IR 预览 View 尺寸为全屏，使 RGB / IR 双预览同时显示。
     *
     * @param rgbPreview         RGB 预览容器（未使用，保留参数）
     * @param previewView        待调整的预览 View
     * @param faceRectView       人脸框绘制 View
     * @param previewSize        相机预览分辨率
     * @param displayOrientation 相机旋转角度
     * @param scale              缩放比例（未使用，保留参数）
     * @return 调整后的 LayoutParams
     */
    private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView faceRectView,
            Camera.Size previewSize, int displayOrientation, float scale) {
        ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        layoutParams.width = metrics.widthPixels;
        layoutParams.height = metrics.heightPixels;

        previewView.setLayoutParams(layoutParams);
        faceRectView.setLayoutParams(layoutParams);
        return layoutParams;
    }

    // ========== 相机与人脸预览 ==========

    /** 初始化 RGB 相机预览与人脸检测帧回调 */
    private void initRgbCamera() {
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams =
                        adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb, binding.dualCameraTexturePreviewRgb,
                                binding.dualCameraFaceRectView, previewSizeRgb, displayOrientation, 1.0f);
                rgbFaceRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height,
                        layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                        ConfigUtil.isDrawRgbRectHorizontalMirror(getActivity()),
                        ConfigUtil.isDrawRgbRectVerticalMirror(getActivity()));
                FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewRgb.getParent());
                TextView textViewRgb = new TextView(getActivity(), null);
                textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                // textViewRgb.setText(
                // getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
                textViewRgb.setTextColor(Color.WHITE);
                textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                parentView.addView(textViewRgb);

                livenessDetectViewModel.onRgbCameraOpened(camera);
                livenessDetectViewModel.setRgbFaceRectTransformer(rgbFaceRectTransformer);
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                binding.dualCameraFaceRectView.clearFaceInfo();
                binding.dualCameraFaceRectViewIr.clearFaceInfo();
                // 1. 检查bitmap是否合格，提取特征数据【该方法刷卡时调用】 开始的地方 add by slm
                List<FacePreviewInfo> facePreviewInfoList =
                        livenessDetectViewModel.onPreviewFrameOnfaceFeature(nv21, faceFeature);
                if (facePreviewInfoList != null && rgbFaceRectTransformer != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
            }

            @Override
            public void onCameraClosed() {
                ALog.i("onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                ALog.i("onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                ALog.i("onCameraConfigurationChanged:" + Thread.currentThread().getName());
                if (rgbFaceRectTransformer != null) {
                    rgbFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                ALog.i("onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        rgbCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewRgb.getMeasuredWidth(),
                        binding.dualCameraTexturePreviewRgb.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(previewConfig.getRgbCameraId())
                .isMirror(ConfigUtil.isDrawRgbPreviewHorizontalMirror(this))
                .additionalRotation(Integer.parseInt(ConfigUtil.getRgbCameraAdditionalRotation(this)))
                .previewSize(livenessDetectViewModel.loadPreviewSize()).previewOn(binding.dualCameraTexturePreviewRgb)
                .cameraListener(cameraListener).build();
        rgbCameraHelper.init();
        rgbCameraHelper.start();
    }

    /** 初始化 IR 相机预览（双摄像头 + IR 活体模式时启用） */
    private void initIrCamera() {
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams =
                        adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb, binding.dualCameraTexturePreviewIr,
                                binding.dualCameraFaceRectViewIr, previewSizeIr, displayOrientation, 0.25f);
                irFaceRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height,
                        layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                        ConfigUtil.isDrawIrRectHorizontalMirror(getActivity()),
                        ConfigUtil.isDrawIrRectVerticalMirror(getActivity()));
                TextView textViewIr = new TextView(getActivity(), null);
                textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewIr
                        .setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
                textViewIr.setTextColor(Color.WHITE);
                textViewIr.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                ((FrameLayout) binding.dualCameraTexturePreviewIr.getParent()).addView(textViewIr);
                livenessDetectViewModel.onIrCameraOpened(camera);
                livenessDetectViewModel.setIrFaceRectTransformer(irFaceRectTransformer);
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                livenessDetectViewModel.refreshIrPreviewData(nv21);
            }

            @Override
            public void onCameraClosed() {
                ALog.i("onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                ALog.i("onCameraError: " + e.getMessage());
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (irFaceRectTransformer != null) {
                    irFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                ALog.i("onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        irCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewIr.getMeasuredWidth(),
                        binding.dualCameraTexturePreviewIr.getMeasuredHeight()))
                .previewSize(livenessDetectViewModel.loadPreviewSize())
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .additionalRotation(Integer.parseInt(ConfigUtil.getIrCameraAdditionalRotation(this)))
                .specificCameraId(previewConfig.getIrCameraId()).previewOn(binding.dualCameraTexturePreviewIr)
                .cameraListener(irCameraListener).isMirror(ConfigUtil.isDrawIrPreviewHorizontalMirror(this)).build();
        irCameraHelper.init();
        try {
            irCameraHelper.start();
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }

    /**
     * 绘制RGB、IR画面的实时人脸信息
     *
     * @param facePreviewInfoList RGB画面的实时人脸信息
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        if (rgbFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> rgbDrawInfoList =
                    livenessDetectViewModel.getDrawInfo(facePreviewInfoList, LivenessType.RGB);
            binding.dualCameraFaceRectView.drawRealtimeFaceInfo(rgbDrawInfoList);
        }
        if (irFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> irDrawInfoList =
                    livenessDetectViewModel.getDrawInfo(facePreviewInfoList, LivenessType.IR);
            binding.dualCameraFaceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
        }
    }

    /**
     * 权限授予后初始化 RGB / IR 相机。
     *
     * @param requestCode   请求码
     * @param isAllGranted  是否全部授权
     */
    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                initRgbCamera();
                if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                    initIrCamera();
                }
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    /**
     * 切换相机。注意：若切换相机发现检测不到人脸，则极有可能是检测角度导致的，需要销毁引擎重新创建或者在设置界面修改配置的检测角度
     *
     * @param view 触发按钮
     */
    public void switchCamera(View view) {
        try {
            if (rgbCameraHelper != null && irCameraHelper != null) {
                rgbCameraHelper.stop();
                irCameraHelper.stop();
                rgbCameraHelper.switchCameraId();
                irCameraHelper.switchCameraId();
                rgbCameraHelper.start();
                irCameraHelper.start();
                showLongToast(getString(R.string.notice_change_detect_degree));
            } else {
                showToast(getString(R.string.switch_camera_failed));
            }
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }

    /**
     * 预览布局完成后请求权限并初始化相机。
     */
    @Override
    public void onGlobalLayout() {
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            initRgbCamera();
            if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                initIrCamera();
            }
        }
    }

    /**
     * 跳转人脸识别参数配置页并结束当前 Activity。
     *
     * @param view 触发按钮
     */
    public void setting(View view) {
        navigateToNewPage(RecognizeSettingsActivity.class);
        finish();
    }

    /**
     * 恢复 RGB / IR 相机预览。
     */
    @Override
    protected void onResume() {
        super.onResume();
        resumeCamera();
        // token保活
    }

    /** 启动 RGB / IR 相机预览 */
    private void resumeCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.start();
        }
        if (irCameraHelper != null) {
            irCameraHelper.start();
        }
    }

    /**
     * 暂停 RGB / IR 相机预览。
     */
    @Override
    protected void onPause() {
        pauseCamera();
        super.onPause();
    }

    /** 停止 RGB / IR 相机预览 */
    private void pauseCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.stop();
        }
        if (irCameraHelper != null) {
            irCameraHelper.stop();
        }
    }

    // ========== 人脸比对结果 ==========

    /**
     * 人脸 1:1 比对结果回调：成功则保存记录并展示通过 UI，失败则超时 3s 后记录失败。
     *
     * @param bitmap       现场抓拍图
     * @param faceSimilar  相似度 0~1
     * @param quality      人脸质量分
     * @param result       true 比对通过
     */
    @Override
    public void onFaceFeatureAvailable(Bitmap bitmap, float faceSimilar, float quality, boolean result) {
        ALog.i("onFaceFeatureAvailable: " + result + "，faceSimilar: " + faceSimilar + "，quality: " + quality);
        ALog.i("" + bitmap.getWidth() + "x" + bitmap.getHeight());
        if (result) {
            countdownHandler.removeMessages(8);
            chechSuccesse(bitmap, faceSimilar, quality);
            // startReadLongPassCardID();
        } else {
            long localTime = System.currentTimeMillis();
            // 如果localTime>xtTime 3s 后继续读卡
            long l = localTime - xtTime;
            if (l > 3000) {
                if (!checkFailed) {// handler 没处理时进这里
                    countdownHandler.removeMessages(8);
                    chechFailed(bitmap, faceSimilar, quality);
                    // startReadLongPassCardID();
                }
            }

        }
    }

    /**
     * 人脸比对通过：保存通行记录、播放成功音、展示证件 Fragment 与相似度。
     *
     * @param bitmap       现场抓拍图
     * @param faceSimilar  相似度 0~1
     * @param quality      人脸质量分
     */
    public void chechSuccesse(Bitmap bitmap, float faceSimilar, float quality) {
        ALog.e("chechSuccesse");
        setRfidNull();
        // 识别通过，保存记录到本地数据库
        faceFeature = null;
        stopChecking();

        if (longTermPass != null) {
            if (longTermPass.type == 0) {
                playAudio(mediaPass);
                toggleFragment(longTermPass, faceSimilar); // 更新fragment

                saveLongTermRecords(longTermPass, bitmap, faceSimilar, quality, true); // 保存长期通行记录到本地数据库
            } else if (longTermPass.type == 1) {
                if (!ChannelConfig.SUPPORTS_TEMPORARY_PASS) {
                    playAudio(mediaReject);
                    showCustomDialog(2, "不支持临时通行证");
                    stopChecking();
                    return;
                }
                playAudio(mediaPass);
                switchFragment3(longTermPass, faceSimilar);
                // String s = imageUploader.uploadBitmap(bitmap);
                // ALog.i("上传图片路径: " + s);
                saveTemporaryRecords(longTermPass, bitmap, faceSimilar, quality, true); // 保存临时通信记录到本地数据库
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showSuccessDialog();
                iv_face.setImageBitmap(bitmap);
                iv_face.setVisibility(View.VISIBLE);
                binding.tvFaceSimilar.setBackgroundResource(R.drawable.face_similar_bg2);
                binding.tvFaceSimilar.setVisibility(View.VISIBLE);
                binding.tvFaceSimilar.setText(String.format("%.2f%%", faceSimilar * 100));
                countdownHandler.removeMessages(10);
                countdownHandler.sendEmptyMessageDelayed(10, 10 * 1000L);
            }
        });

        lastLongTermPass = longTermPass;
        lastTime = TimeUtils.getNowMills();
    }

    /**
     * 人脸比对失败：保存失败记录、播放拒绝音、恢复待机 Fragment。
     *
     * @param bitmap       现场抓拍图，可为 null
     * @param faceSimilar  相似度 0~1
     * @param quality      人脸质量分
     */
    public void chechFailed(Bitmap bitmap, float faceSimilar, float quality) {
        ALog.e("chechFailed");
        setRfidNull();
        if (longTermPass != null) {
            if (longTermPass.type == 0) {
                // ALog.i("上传图片路径: " + s);
                saveLongTermRecords(longTermPass, bitmap, faceSimilar, quality, false); // 保存长期通行记录到本地数据库

            } else if (longTermPass.type == 1) {
                // String s = imageUploader.uploadBitmap(bitmap);
                // ALog.i("上传图片路径: " + s);
                saveTemporaryRecords(longTermPass, bitmap, faceSimilar, quality, false);// 保存临时通信记录到本地数据库
            }
        }
        stopChecking();
        playAudio(mediaReject);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showFailedDialog();
                faceFeature = null;
                turnFragment1();// 切换成普通状态

                if (faceSimilar > 0) {
                    binding.tvFaceSimilar.setBackgroundResource(R.drawable.face_similar_bg);
                    binding.tvFaceSimilar.setVisibility(View.VISIBLE);
                    binding.tvFaceSimilar.setText(String.format("%.2f%%", faceSimilar * 100));
                    countdownHandler.removeMessages(10);
                    countdownHandler.sendEmptyMessageDelayed(10, 10 * 1000L);
                }

            }
        });

        lastLongTermPass = null;
        lastTime = 0;
    }

    /** 自定义弹窗实例（当前主要使用 messageLayout 提示条） */
    CustomPopDialog popDialog;

    // ========== UI 提示 ==========

    /**
     * 显示验证成功提示条（绿色背景）。
     */
    public void showSuccessDialog() {
        if (popDialog != null) {
            popDialog.dismiss();
        }
        showCustomDialog(1, null);
    }

    /**
     * 显示验证失败提示条（红色背景）。
     */
    public void showFailedDialog() {
        if (popDialog != null) {
            popDialog.dismiss();
        }
        showCustomDialog(2, null);
    }

    /**
     * 显示自定义顶部提示条，1 秒后自动隐藏。
     *
     * @param icon 1 成功样式 / 2 失败样式
     * @param msg  自定义文案，为空时使用默认「验证成功/失败」
     */
    public void showCustomDialog(int icon, String msg) {
        // if (popDialog != null) {
        // popDialog.dismiss();
        // }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.messageLayout.setVisibility(View.VISIBLE);

                // 设置默认图标和文字
                Drawable successIcon = getDrawable(R.drawable.ic_success);
                Drawable failureIcon = getDrawable(R.drawable.ic_failure);
                String successText = "验证成功";
                String failureText = "验证失败";

                if (icon == 1) {
                    binding.messageLayout.setBackgroundResource(R.drawable.dialog_view_bg);
                    iv_icon.setImageDrawable(successIcon);
                    tv_message.setText(successText);
                } else {
                    binding.messageLayout.setBackgroundResource(R.drawable.dialog_view_bg2);
                    iv_icon.setImageDrawable(failureIcon);
                    tv_message.setText(failureText);
                }
                if (ObjectUtils.isNotEmpty(msg)) {
                    tv_message.setText(msg);
                }
                countdownHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.messageLayout.setVisibility(View.INVISIBLE);
                    }
                }, 1000);

            }
        });

    }

    /** 定时检查应用版本更新（Handler msg=7 触发） */
    void check() {
        GetRequest<Base<Version>> getRequest =
                OkGo.<Base<Version>> get(UrlConstants.URL_GET_APP_LAST_VERSION).params("type", 3);
        getRequest.headers("tenant-id", UrlConstants.TENANT_ID);
        if (ApiUtils.accessToken != null) {
            getRequest.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        getRequest.execute(new JsonCallback<Base<Version>>() {
            @Override
            public void onError(Response<Base<Version>> response) {
                countdownHandler.removeMessages(7);
                countdownHandler.sendEmptyMessageDelayed(7, 60 * 1000L);
            }

            @Override
            public void onSuccess(Response<Base<Version>> response) {
                if (ObjectUtils.isEmpty(response.body())) {
                    showToast("check失败");
                    ALog.w("check失败");
                    return;
                }
                if (ObjectUtils.isEmpty(response.body().getData())) {
                    showToast("无更新");
                    ALog.w("无更新");
                    return;
                }
                Version version = response.body().getData();
                if (version.getVersion().compareTo(AppUtils.getAppVersionName()) <= 0) {
                    showToast("版本号小，无更新");
                    ALog.w("版本号小，无更新");
                    return;
                }
                new XPopup.Builder(getActivity()).asCustom(new UpdatePopDialog(getActivity(), response.body().getData(),
                        new UpdatePopDialog.DownloadCallback() {
                            @Override
                            public void onStart() {
                                ALog.e("onStart");
                            }

                            @Override
                            public void onProgress(float progress, long total) {
                                ALog.e("progress:" + progress + ",total:" + total);
                            }

                            @Override
                            public void onSuccess(File file) {
                                ALog.e("file:" + file.getAbsolutePath());
                                countdownHandler.removeMessages(7);
                                countdownHandler.sendEmptyMessageDelayed(7, 60 * 1000L);
                            }

                            @Override
                            public void onError(Throwable throwable) {
                                throwable.printStackTrace();
                                ALog.e("throwable:" + throwable.getMessage());
                                countdownHandler.removeMessages(7);
                                countdownHandler.sendEmptyMessageDelayed(7, 60 * 1000L);
                            }
                        })).show();
            }
        });
    }

    // ========== 长距 EC_API 读卡器 ==========

    /** EC_API 射频数据缓冲区 */
    private RFdata rf = null;
    /** 是否正在执行 EC_API 初始化命令 */
    private boolean iscmd;
    /** 长距 RFID 读卡器 EC_API 实例 */
    private final EC_API ecApi = new EC_API();
    /** 已成功连接的 EC_API 串口路径 */
    String strCom;
    /** 长距读卡轮询线程运行标志（当前由 startReadLongPassCardID 内联调用 readLongCard） */
    boolean readingLong = false;

    // public void startReadLongCard() {
    // ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
    // @Override
    // public String doInBackground() throws Throwable {
    // readingLong = true;
    // while (readingLong) {
    // String uid = readLongCard(ecApi);
    // ALog.e("uid:" + uid);
    // if (ObjectUtils.isEmpty(uid)) {
    // setRfidNull();
    // } else {
    // getLongPassCardInfo(rfid, true);//查询id
    // }
    //
    // Thread.sleep(300);
    // }
    // return null;
    // }
    // });
    // }
    //
    // public void stopReadLongCard() {
    // readingLong = false;
    // }

    /**
     * 初始化长距 EC_API 读卡器：扫描可用串口，打开设备并执行初始化命令。
     */
    public void initLongReader() {
        rf = new RFdata();
        // 设置设备类型
        if (ecApi.EC_SetDriverType("52xx")) {
            ALog.e("设置设备类型成功");
        }
        iscmd = true;
        String[] all_com = EC_API.GetAllCOM();
        for (String value : all_com) {
            ALog.e("【" + value + "】");
        }
        for (String s : all_com) {
            ALog.e("尝试与【" + s + "】通信...");
            if (s.contains("/dev/ttyS4")) {
                continue;
            }
            if (ecApi.EC_OpenCOM(s, 38400, "8E1")) {

                // 打开设备命令
                if (ecApi.EC_OpenDevice(rf)) {// 验证串口是否连接设备
                    ALog.e("（成功）与【" + s + "】建立连接。");
                    strCom = s;
                    runCmd(ecApi);// 执行命令
                    iscmd = false;
                    return;
                }
            }
        }
        ecApi.EC_Close();
        iscmd = false;
    }

    /**
     * 关闭长距 EC_API 读卡器并释放串口。
     */
    public void unInitLongReader() {
        ecApi.EC_Close();
        iscmd = false;
    }

    /**
     * 执行 EC_API 设备打开与获取设备信息命令（初始化阶段调用）。
     *
     * @param api EC_API 实例
     */
    private void runCmd(EC_API api) {
        RFdata rf = new RFdata();
        byte[] data = null;
        byte[] UID = null;
        byte b_DSFID = 0X00;
        byte b_AFI = 0X00;
        boolean isUploadAntNum = false;// 上传天线编号

        // 打开设备命令
        if (api.EC_OpenDevice(rf)) {
            ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
            ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
            ALog.e("打开设备成功");
        } else {
            ALog.e("打开设备失败");
            return;
        }

        // 获取设备信息命令
        if (api.EC_GetDeviceInfoVersion(rf)) {
            ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
            ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
            ALog.e("获取设备信息成功");
        } else {
            ALog.e("获取设备信息失败");
            return;
        }
        /*
         * //不常用的命令示例 //读配置块3命令 if(api.EC_ReadCfgBlock(rf, (byte) 3)){ log.showSendReceiveData(rf); ALog.e("读配置块3成功");
         * //记录配置块数据，保证写配置时不更改配置 data=new byte[8]; for(int k=0;k<8;k++) { data[k]=rf.RecvData[k+6]; } //记录上传天线编号标识
         * if((data[0]>>>4&0x01)==1) { isUploadAntNum=true;//网络盘点解析天线口号用到 } }else{ ALog.e("读配置块3失败"); return; }
         *
         * //写配置块3命令 if(data!=null&&api.EC_WriteCfgBlock(rf,data, (byte) 3)){ log.showSendReceiveData(rf);
         * ALog.e("写配置块3成功"); data=null; }else{ ALog.e("写配置块3失败"); return; }
         *
         * //保存配置块命令 if(api.EC_SaveCfgBlock(rf, (byte) 0)){ log.showSendReceiveData(rf); ALog.e("保存配置块成功"); }else{
         * ALog.e("保存配置块失败"); return; }
         *
         * //噪音检测命令 if(api.EC_NoiseCheck(rf,(byte)0)){ log.showSendReceiveData(rf); ALog.e("噪音检测成功"); }else{
         * ALog.e("噪音检测失败"); return; }
         *
         * //打开射频命令 if(api.EC_Open_CloseRFPower(rf,(byte)1)){ log.showSendReceiveData(rf); ALog.e("打开射频成功"); }else{
         * ALog.e("打开射频失败"); return; }
         *
         * //关闭射频命令 if(api.EC_Open_CloseRFPower(rf,(byte)0)){ log.showSendReceiveData(rf); ALog.e("关闭射频成功"); }else{
         * ALog.e("关闭射频失败"); return; }
         *
         * //蜂鸣器闪烁命令 if(api.EC_HardwareControl(rf,(byte)1,(byte)0x80,(byte)0)){ log.showSendReceiveData(rf);
         * ALog.e("蜂鸣器闪烁成功"); }else{ ALog.e("蜂鸣器闪烁失败"); return; }
         *
         * //继电器闪烁命令 if(api.EC_HardwareControl(rf,(byte)1,(byte)1,(byte)0)){ log.showSendReceiveData(rf);
         * ALog.e("继电器闪烁成功"); }else{ ALog.e("继电器闪烁失败"); return; }
         */

    }

    /**
     * 盘点 RFID 标签（长距读卡器 EC_API 盘点命令）。
     *
     * @param api    EC_API 实例对象
     * @param antNum 要打开的天线口号(0~30，单天线盘点)，0 表示不打开（多天线盘点）
     * @return 标签列表，失败返回 null
     */
    public List<Tag> ScanTab(EC_API api, int antNum) {
        List<Tag> tab_list = new ArrayList<Tag>();
        RFdata rf_1 = new RFdata();
        byte scanType = 0;
        if (antNum >= 1 && antNum <= 30) {
            // 单标签盘点,打开指定天线口
            if (api.EC_OpenAnt_One(rf_1, (byte) antNum)) {
                ALog.v("发送>>" + HexToStr(rf_1.SendData, (rf_1.SendData[1] & 0xff) + 1));
                ALog.v("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1));
                ALog.e(String.format("打开天线口%d成功！", antNum));
                scanType = 0x04;
            } else {
                return null;
            }

        }
        switch (api.GetLinkType()) {
        case EC_API.COM: {
            if (api.EC_InventoryTag(rf_1, scanType)) {
                ALog.d("发送>>" + HexToStr(rf_1.SendData, (rf_1.SendData[1] & 0xff) + 1));
                while ((rf_1.RecvData[1] & 0xff) > 7) {
                    ALog.d("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1));
                    // 从数据包中获取DSFID
                    Tag tag_info = new Tag();
                    tag_info.DSFID = rf_1.RecvData[6];
                    // 获取天线口，设置了上传天线编号才能获取到天线口
                    if (antNum == 0) {
                        if (rf_1.RecvData[1] == 0x11) {// 通过包的长度判断是否包含天线口数据，设置了上传天线编号数据包多一个字节
                            tag_info.Ant = rf_1.RecvData[15] & 0xff;
                        } else {
                            tag_info.Ant = 1;
                        }
                    } else {
                        tag_info.Ant = antNum;
                    }
                    // 获取uid
                    for (int i = 0; i < 8; i++) {
                        tag_info.UID[i] = rf_1.RecvData[i + 7];
                    }
                    tab_list.add(tag_info);
                    // 继续接收,直到接收到结束包为止
                    if (!api.EC_Receive(rf_1)) {
                        break;
                    }
                }
                ALog.d("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1));
            }
        }
            break;
        }
        return tab_list;
    }

    /**
     * 长距读卡：盘点 EC_API 标签获取 UID，去重后返回 cardIdLong 卡号。
     *
     * @param api EC_API 实例
     * @return 8 位十六进制卡号，无卡或重复读卡返回 null
     */
    public String readLongCard(EC_API api) {
        ALog.e("readLongCard.rfid: " + rfid);
        RFdata rf = new RFdata();
        byte[] data = null;
        byte[] UID = null;
        byte b_DSFID = 0X00;
        byte b_AFI = 0X00;
        // 盘点命令
        List<Tag> tag_list = ScanTab(api, 0);
        if (tag_list != null) {
            // ALog.d("盘点成功");
            int len = tag_list.size();
            // ALog.d(String.format("标签数量%d", len));
            if (len == 0) {
                // ALog.d("检测完成！");
                return null;
            }
            UID = tag_list.get(0).UID;// 记录第一个标签的UID，读写卡命令用到
            ALog.e("UID:" + ConvertUtils.bytes2HexString(UID));
        } else {
            ALog.e("盘点失败");
            return null;
        }
        String realUid = ConvertUtils.bytes2HexString(UID).substring(0, 8);
        if (realUid.equals(rfid)) {
            ALog.i("重复读卡: " + realUid);
            return null;
        }

        rfid = realUid;

        // Toast.makeText(this, "carId"+resultArr1[1], Toast.LENGTH_SHORT).show();

        // //打开天线口命令，读写标签之前要打开标签所在的天线口
        // if (api.EC_OpenAnt_One(rf, (byte) 1)) {
        // ALog.d("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.d("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // ALog.d("打开天线口1成功");
        // } else {
        // ALog.e("打开天线口1失败");
        // return null;
        // }
        //
        // //获取标签信息命令
        // if (api.EC_GetOneTagInfo(rf, UID, (byte) 1)) {
        // ALog.d("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.d("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("获取标签信息成功");
        // //记录DSFID和AFI，
        // b_DSFID = rf.RecvData[15];
        // b_AFI = rf.RecvData[16];
        // } else {
        // ALog.e("获取标签信息失败");
        // return null;
        // }
        // } else {
        // ALog.e("获取标签信息失败");
        // return null;
        // }
        //
        // //读单个数据块命令
        // if (api.EC_ReadCardOneBlock(rf, UID, (byte) 1, (byte) 0, (byte) 0)) {
        // ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("读数据块0成功");
        // //记录数据块
        // data = new byte[4];
        // for (int k = 0; k < 4; k++) {
        // data[k] = rf.RecvData[8 + k];
        // }
        // } else {
        // ALog.e("读数据块0失败");
        // return null;
        // }
        // } else {
        // ALog.e("读数据块0失败");
        // return null;
        // }
        //
        // //写单个数据块命令
        // if (data != null && api.EC_WriteCardOneBlock(rf, UID, (byte) 1, (byte) 0, data)) {
        // ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("写数据块0成功");
        // data = null;
        // } else {
        // ALog.e("写数据块0失败");
        // return null;
        // }
        // } else {
        // ALog.e("写数据块0失败");
        // return null;
        // }
        //
        // //读多个数据块命令
        // if (api.EC_ReadCardMultBlock(rf, UID, (byte) 1, (byte) 0, (byte) 2)) {
        // ALog.d("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.d("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // if (api.GetLinkType() != EC_API.NET) {
        // //非UDP传输方式，还有一个结束包
        // RFdata rf_1 = new RFdata();
        // if (api.EC_Receive(rf_1)) {
        // ALog.d("接收<<" + HexToStr(rf_1.RecvData, (rf_1.RecvData[1] & 0xff) + 1));
        // } else {
        // ALog.e("读数据块0~2失败");
        // return null;
        // }
        // }
        // ALog.d("读数据块0~2成功");
        // //记录数据块
        // data = new byte[12];
        // for (int k = 0; k < 4; k++) {
        // data[k] = rf.RecvData[k + 17];
        // data[k + 4] = rf.RecvData[k + 17 + 5];
        // data[k + 8] = rf.RecvData[k + 17 + 10];
        // }
        // } else {
        // ALog.e("读数据块0~2失败");
        // return null;
        // }
        // } else {
        // ALog.e("读数据块0~2失败");
        // return null;
        // }
        //
        // //写多个数据块命令
        // if (data != null && api.EC_WriteCardMultBlock(rf, UID, (byte) 1, (byte) 0, (byte) 2, data)) {
        // ALog.d("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.d("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.d("写数据块0~2成功");
        // } else {
        // ALog.e("写数据块0~2失败");
        // return null;
        // }
        // } else {
        // ALog.e("写数据块0~2失败");
        // return null;
        // }
        //
        // //写DSFID命令
        // if (api.EC_WriteOneTagDSFID(rf, UID, (byte) 1, b_DSFID)) {
        // ALog.d("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.d("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("写DSFID成功");
        // } else {
        // ALog.e("写DSFID失败");
        // return null;
        // }
        // } else {
        // ALog.e("写DSFID失败");
        // return null;
        // }
        //
        // //写AFI命令
        // if (api.EC_WriteOneTagAFI(rf, UID, (byte) 1, b_AFI)) {
        // ALog.d("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.d("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("写AFI成功");
        // } else {
        // ALog.e("写AFI失败");
        // return null;
        // }
        // } else {
        // ALog.e("写AFI失败");
        // return null;
        // }
        //
        // //检测EAS命令、启用EAS命令、禁用EAS命令
        // boolean isEAS;//EAS状态
        // if (api.EC_CheckOneTagEAS(rf, UID, (byte) 1)) {
        // ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("检测EAS成功");
        // if (rf.RecvData[1] == (byte) 0x0a) {//通过检测包的长度判断EAS是否禁用
        // ALog.e("EAS状态：关闭");
        // isEAS = false;
        // } else {
        // ALog.e("EAS状态：开启");
        // isEAS = true;
        // }
        // //自动调节，通过检测的值决定先禁用还是先启用，不更改标签EAS的原始值
        // for (int k = 0; k < 2; k++) {
        // if (isEAS) {
        // //禁用EAC
        // if (api.EC_BanOneTagEAS(rf, UID, (byte) 1)) {
        // ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("禁用EAS成功");
        // isEAS = false;
        // continue;
        // }
        // } else {
        // ALog.e("禁用EAS失败");
        // return null;
        // }
        //
        // } else {
        // //启用EAC
        // if (api.EC_EnableOneTagEAS(rf, UID, (byte) 1)) {
        // ALog.e("发送>>" + HexToStr(rf.SendData, (rf.SendData[1] & 0xff) + 1));
        // ALog.e("接收<<" + HexToStr(rf.RecvData, (rf.RecvData[1] & 0xff) + 1));
        // if (rf.RecvData[5] == 0) {
        // ALog.e("启用EAS成功");
        // isEAS = true;
        // continue;
        // }
        // } else {
        // ALog.e("启用EAS失败");
        // return null;
        // }
        //
        // }
        // }
        // ALog.e("检测完成！");
        // }
        // } else {
        // ALog.e("检测EAS失败");
        // return null;
        // }
        return ConvertUtils.bytes2HexString(UID).substring(0, 8);
    }

    /**
     * 字节数组转十六进制字符串（调试用）。
     *
     * @param data 字节数组
     * @param len  要转换的长度
     * @return 十六进制字符串，字节间空格分隔
     */
    private String HexToStr(byte[] data, int len) {
        String strData = "";
        for (int i = 0; i < len; i++) {
            strData = strData + String.format("%02X ", data[i]);// 十六进制
        }
        return strData;
    }
}
