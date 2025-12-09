package com.arcsoft.arcfacedemo.ui.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.data.http.JsonCallback;
import com.arcsoft.arcfacedemo.databinding.ActivityRegisterAndRecognizeBinding;
import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
import com.arcsoft.arcfacedemo.db.dao.LongTermRecordsDao;
import com.arcsoft.arcfacedemo.db.dao.TemporaryCardRecordsDao;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;
import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;
import com.arcsoft.arcfacedemo.entity.Area;
import com.arcsoft.arcfacedemo.entity.Base;
import com.arcsoft.arcfacedemo.entity.Records;
import com.arcsoft.arcfacedemo.entity.Version;
import com.arcsoft.arcfacedemo.facedb.FaceDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.ui.adapter.CheckLogListAdapter;
import com.arcsoft.arcfacedemo.ui.fragment.Document11;
import com.arcsoft.arcfacedemo.ui.fragment.Document2;
import com.arcsoft.arcfacedemo.ui.fragment.Document3;
import com.arcsoft.arcfacedemo.ui.model.CompareResult;
import com.arcsoft.arcfacedemo.ui.model.PreviewConfig;
import com.arcsoft.arcfacedemo.ui.viewmodel.RecognizeViewModel;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
import com.arcsoft.arcfacedemo.util.ImageUploader;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.arcsoft.arcfacedemo.util.PlayerUtil;
import com.arcsoft.arcfacedemo.util.SimpleTask;
import com.arcsoft.arcfacedemo.util.SmallTask;
import com.arcsoft.arcfacedemo.util.SnowFlake;
import com.arcsoft.arcfacedemo.util.WeakHandler;
import com.arcsoft.arcfacedemo.util.camera.CameraListener;
import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;
import com.arcsoft.arcfacedemo.util.face.RecognizeCallback;
import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.arcfacedemo.widget.FaceRectView;
import com.arcsoft.arcfacedemo.widget.RecognizeAreaView;
import com.arcsoft.arcfacedemo.widget.dialog.CustomDrawerPopupView;
import com.arcsoft.arcfacedemo.widget.dialog.CustomPopDialog;
import com.arcsoft.arcfacedemo.widget.dialog.RecordsPopDialog;
import com.arcsoft.arcfacedemo.widget.dialog.UpdatePopDialog;
import com.arcsoft.face.ErrorInfo;
import com.blankj.utilcode.constant.TimeConstants;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupPosition;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaPlayer;
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
 * 使用：出页面。纯人脸识别，无刷卡
 * FaceHelper类searchFace函数
 */
public class RegisterAndRecognizeActivity extends BaseActivity
        implements ViewTreeObserver.OnGlobalLayoutListener, RecognizeCallback {
    private static final String TAG = "RegisterAndRecognize";

    private DualCameraHelper rgbCameraHelper;
    private DualCameraHelper irCameraHelper;
    private FaceRectTransformer rgbFaceRectTransformer;
    private FaceRectTransformer irFaceRectTransformer;

    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;

    int actionAfterFinish = 0;
    private static final int NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY = 1;
    private static final int NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY = 2;

    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS =
            new String[] { Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE };
    private ActivityRegisterAndRecognizeBinding binding;
    private RecognizeViewModel recognizeViewModel;
    private LivenessType livenessType;
    private boolean enableLivenessDetect = false;
    private RecognizeAreaView recognizeAreaView;
    private TextView textViewRgb;
    private TextView textViewIr;
    private boolean openRectInfoDraw;
    private Fragment fragment11;
    private Fragment fragment2;
    private Fragment fragment3;
    private ImageView iv_face1;
    private ImageView iv_face2;
    private ImageView iv_face3;
	private View button_set;

    private LongTermPassDao cardDao;

    // private MediaPlayer mediaGet;
    // private MediaPlayer mediaPass;
    // private MediaPlayer mediaReject;
    // private MediaPlayer mediaFailed;
    private ImageUploader imageUploader;
    private InfoStorage infoStorage;
    private long countdownTime = 1 * 60 * 1000; // 1分钟倒计时
    private String localLongId;

    private FaceDao faceDao;

    private WeakHandler countdownHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
            case 2:
                try {
                    // 其他需要执行的操作
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_all, fragment11);
                    fragmentTransaction.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                    ALog.e(e.getMessage());
                }

                break;
            case 5:

                if (ArcFaceApplication.getApplication().isOffLine()) {
                    binding.tvTis.setText("离线模式   " + areaName + (direction == 1 ? "-进" : "-出") + "查验");

                } else {
                    binding.tvTis.setText("在线模式   " + areaName + (direction == 1 ? "-进" : "-出") + "查验");
                }
                countdownHandler.removeMessages(5);
                countdownHandler.sendEmptyMessageDelayed(5, 10000L);

                // ThreadUtils.executeByFixed(5, new ThreadUtils.SimpleTask<Boolean>() {
                // @Override
                // public Boolean doInBackground() throws Throwable {
                // ALog.d("Ping www.baidu.com 开始");
                // if (NetworkUtils.isAvailableByPing()) {
                // ALog.d("Ping www.baidu.com 成功");
                // return true;
                // } else {
                // ALog.d("Ping www.baidu.com 失败");
                // }
                // return false;
                // }
                //
                // @Override
                // public void onSuccess(Boolean result) {
                // if (result) {
                // isOffLine = false;
                // binding.tvTis.setText("在线模式 " + areaName + (direction == 1 ? "-进" : "-出") + "查验");
                // } else {
                // isOffLine = true;
                // binding.tvTis.setText("离线模式 " + areaName + (direction == 1 ? "-进" : "-出") + "查验");
                // }
                // countdownHandler.removeMessages(5);
                // countdownHandler.sendEmptyMessageDelayed(5, 10000L);
                // }
                // });
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

            case 9:
                infoStorage.remove("linshiID");
                break;

            case 10:
                binding.tvFaceSimilar.setVisibility(View.INVISIBLE);
                break;

            case 11:

                // ThreadUtils.executeByCached(new SmallTask() {
                // @Override
                // public String doInBackground() throws Throwable {
                // LongTermPass card1 = cardDao.getByNickname("小芳");
                // if (ObjectUtils.isEmpty(card1)) {
                // ALog.i("本地数据库未查询到 getByApplyId: 1874274394944454657");
                // } else {
                // ALog.e(card1.toString());
                // runOnUiThread(new Runnable() {
                // @Override
                // public void run() {
                // Glide.with(RegisterAndRecognizeActivity.this).load(AESUtils.getPhotoPath(card1.id))
                // .into(iv_face1);
                // Glide.with(RegisterAndRecognizeActivity.this)
                // .load(AESUtils.getRegisterPath(card1.id)).into(iv_face3);
                // }
                // });
                // }
                //
                // List<FaceEntity> list = faceDao.queryByName("1874274394944454657");
                // if (ObjectUtils.isEmpty(list)) {
                // ALog.i("本地数据库未查询到 queryByUserName：1874274394944454657");
                // } else {
                // for (FaceEntity item : list) {
                // ALog.e(item.toString2());
                //
                // }
                // }
                //
                // FaceEntity faceEntity = faceDao.queryByUserName("1874274394944454657");
                // if (ObjectUtils.isEmpty(faceEntity)) {
                // ALog.i("本地数据库未查询到 queryByUserName：1874274394944454657");
                // } else {
                // ALog.e(faceEntity.toString2());
                // runOnUiThread(new Runnable() {
                // @Override
                // public void run() {
                // Glide.with(RegisterAndRecognizeActivity.this).load(faceEntity.getImagePath())
                // .into(iv_face2);
                // }
                // });
                // }
                // countdownHandler.removeMessages(11);
                // countdownHandler.sendEmptyMessageDelayed(11, 30 * 1000);
                // return null;
                // }
                // });

                break;

            default:
                break;
            }
            return false;
        }
    });
    String areaName;
    private CheckLogListAdapter mListAdapter;
    private ArrayList<Records> checkList = new ArrayList<>();
    // boolean isOffLine;
    LinearLayout messageLayout;
    ImageView iv_icon;
    TextView tv_message;
    int direction;
    private int clickCount = 0; // 点击计数
    private static final int REQUIRED_CLICKS = 5; // 需要的点击次数

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ALog.e("onCreate");
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_and_recognize);

        // 保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        direction = SPUtils.getInstance().getInt("direction", 1);
        ALog.e("direction:" + direction);
        initData();

        initViewModel();
        initView();
        // fragment初始化
        initFragment();
		button_set = findViewById(R.id.button_set);
		// 点击跳转设置界面
		button_set.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), HomeActivity.class);
				startActivity(intent);
			}
		});
        iv_face1 = findViewById(R.id.iv_face1);
        iv_face2 = findViewById(R.id.iv_face2);
        iv_face3 = findViewById(R.id.iv_face3);
        messageLayout = findViewById(R.id.messageLayout);
        iv_icon = findViewById(R.id.iv_icon);
        tv_message = findViewById(R.id.tv_message);
        openRectInfoDraw = true;
        recognizeViewModel.setDrawRectInfoTextValue(true);
        infoStorage = new InfoStorage(this);
        areaName = getArea();
        countdownHandler.removeMessages(5);
        countdownHandler.sendEmptyMessageDelayed(5, 50L);
        countdownHandler.removeMessages(6);
        countdownHandler.sendEmptyMessageDelayed(6, 50L);
        countdownHandler.removeMessages(7);
        countdownHandler.sendEmptyMessageDelayed(7, 1000L);

        countdownHandler.sendEmptyMessageDelayed(11, 10 * 1000);

        infoStorage = new InfoStorage(this);
        binding.tvMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new XPopup.Builder(RegisterAndRecognizeActivity.this)
                        .asCustom(new RecordsPopDialog(RegisterAndRecognizeActivity.this, direction)).show();

            }
        });
        mListAdapter = new CheckLogListAdapter(this, checkList, R.layout.list_check_log_item);
        binding.rvList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        binding.rvList.setAdapter(mListAdapter);

        binding.imgYuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickCount++; // 每次点击时增加计数
                // 检测点击次数是否达到目标
                if (clickCount >= REQUIRED_CLICKS) {
                    // 达到目标后可以触发某项操作
                    clickCount = 0; // 重置计数
                    new XPopup.Builder(RegisterAndRecognizeActivity.this).isDestroyOnDismiss(true) // 对于只使用一次的弹窗，推荐设置这个
                            .popupPosition(PopupPosition.Left)// 右边
                            .hasStatusBarShadow(true) // 启用状态栏阴影
                            .asCustom(new CustomDrawerPopupView(RegisterAndRecognizeActivity.this)).show();

                }
                countdownHandler.removeMessages(1111);
                countdownHandler.sendEmptyMessageDelayed(1111, 5000);
            }
        });

        mListAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener<Records>() {
            @Override
            public void onItemClick(View view, int position, Records item) {
                toggleFragment(item);
            }
        });

        // getAllRecords();
        cardDao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
        faceDao = FaceDatabase.getInstance(getApplication()).faceDao();
        imageUploader = new ImageUploader();// 图片上传工具类
        infoStorage = new InfoStorage(this);
        initSound();

        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // YinchuanAirportDB db = ArcFaceApplication.getApplication().getDb();
        // // LongTermRecords records = db.longTermRecordsDao().getByLast();
        // // ALog.e("records:" + records.toString());
        // // LongTermPass records = db.longTermPassDao().getByLast();
        //
        // LongTermPass records = db.longTermPassDao().getByLastAndType(1);
        // // LongTermRecords records = db.longTermRecordsDao().getByLast();
        //
        // ALog.e("records:" + records.toString());
        // runOnUiThread(new Runnable() {
        // @Override
        // public void run() {
        // // toggleFragment(records);
        // // switchFragment3(records, 0.9f);
        // // toggleFragment(records, 0.9f);
        // }
        // });
        //
        // return null;
        // }
        // });
    }

    private void initData() {
        String livenessTypeStr = ConfigUtil.getLivenessDetectType(this);
        if (livenessTypeStr.equals((getString(R.string.value_liveness_type_rgb)))) {
            livenessType = LivenessType.RGB;
        } else if (livenessTypeStr.equals(getString(R.string.value_liveness_type_ir))) {
            livenessType = LivenessType.IR;
        } else {
            livenessType = null;
        }
        enableLivenessDetect =
                !ConfigUtil.getLivenessDetectType(this).equals(getString(R.string.value_liveness_type_disable));
    }

    private void initViewModel() {
        recognizeViewModel = new ViewModelProvider(getViewModelStore(),
                new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(RecognizeViewModel.class);

        recognizeViewModel.setLiveType(livenessType);

        recognizeViewModel.getFtInitCode().observe(this, ftInitCode -> {
            if (ftInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode,
                        ErrorCodeUtil.arcFaceErrorCodeToFieldName(ftInitCode));
                ALog.i("initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFrInitCode().observe(this, frInitCode -> {
            if (frInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode,
                        ErrorCodeUtil.arcFaceErrorCodeToFieldName(frInitCode));
                ALog.i("initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFlInitCode().observe(this, flInitCode -> {
            if (flInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode,
                        ErrorCodeUtil.arcFaceErrorCodeToFieldName(flInitCode));
                ALog.i("initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFaceItemEventMutableLiveData().observe(this, faceItemEvent -> {
            RecyclerView.Adapter adapter = binding.dualCameraRecyclerViewPerson.getAdapter();
            switch (faceItemEvent.getEventType()) {
            case REMOVED:
                if (adapter != null) {
                    adapter.notifyItemRemoved(faceItemEvent.getIndex());
                }
                break;
            case INSERTED:
                if (adapter != null) {
                    adapter.notifyItemInserted(faceItemEvent.getIndex());
                }
                break;
            default:
                break;
            }
        });

        recognizeViewModel.getRecognizeConfiguration().observe(this, recognizeConfiguration -> {
            ALog.i("initViewModel recognizeConfiguration: " + recognizeConfiguration.toString());
        });

        recognizeViewModel.setOnRegisterFinishedCallback(
                (facePreviewInfo, success) -> showToast(success ? "register success" : "register failed"));

        recognizeViewModel.getRecognizeNotice().observe(this, notice -> binding.setRecognizeNotice(notice));

        recognizeViewModel.getDrawRectInfoText().observe(this, drawRectInfoText -> {
            binding.setDrawRectInfoText(drawRectInfoText);
        });
        recognizeViewModel.setRecognizeCallback(this);
    }

    private void initView() {
        if (!DualCameraHelper.hasDualCamera() || livenessType != LivenessType.IR) {
            binding.flRecognizeIr.setVisibility(View.GONE);
        }
        // 在布局结束后才做初始化操作
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
        binding.setCompareResultList(recognizeViewModel.getCompareResultList().getValue());

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

    void initFragment() {
        fragment11 = new Document11();
        fragment2 = new Document2();
        fragment3 = new Document3();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_all, fragment11);
        fragmentTransaction.commit();
    }

    public void toggleFragment(LongTermPass longTermPass, float faceSimilar) {
        try {
            Fragment fragment2 = new Document2();
            // 根据idCode去本地加载图片
            // String idCode = longTermPass.idCode;
            // Bitmap bitmap = ImageUtils.loadBitmapFromPath(Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode +
            // ".jpg");
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Bundle bundle = new Bundle();
            bundle.putString("idCode", longTermPass.idCode);
            bundle.putString("passid", longTermPass.id);
            bundle.putString("nickname", longTermPass.nickname);
            bundle.putString("companyName", longTermPass.companyName);
            bundle.putString("expiryDate", longTermPass.expiryDate);
            bundle.putString("templateType", String.valueOf(longTermPass.templateType));
            bundle.putString("status", String.valueOf(longTermPass.status));
            bundle.putString("photo", longTermPass.photo);
            bundle.putString("faceSimilar", faceSimilar + "");

            String areaDisplay = new String();
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
                areaDisplay = new String();
                if (ObjectUtils.isNotEmpty(longTermRecords.areaDisplayCode)) {
                    for (String s : longTermRecords.areaDisplayCode) {
                        areaDisplay += s + "\n";
                    }
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
                areaDisplay = new String();
                if (ObjectUtils.isNotEmpty(temporaryCardRecords.areaDisplayCode)) {
                    for (String s : temporaryCardRecords.areaDisplayCode) {
                        areaDisplay += s + "\n";
                    }
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

    private void switchFragment3(LongTermPass longTermPass) {
        switchFragment3(longTermPass, 0);
    }

    private void switchFragment3(TemporaryCardRecords temporaryCardRecords) {
        try {
            Fragment fragment3 = new Document3();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Bundle bundle = new Bundle();
            bundle.putString("leadingPeople", temporaryCardRecords.leadingPeople);
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

    // public void toggleFragment2() {
    // try {
    // Fragment fragment2 = new Document2();
    // // 根据idCode去本地加载图片
    // // String idCode = longTermPass.idCode;
    // // Bitmap bitmap = ImageUtils.loadBitmapFromPath(Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode +
    // // ".jpg");
    // FragmentManager fragmentManager = getSupportFragmentManager();
    // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    // fragmentTransaction.replace(R.id.fragment_all, fragment2);
    // fragmentTransaction.commit();
    // hiddenCard();
    // } catch (Exception e) {
    // e.printStackTrace();
    // ALog.e(e.getMessage());
    // }
    //
    // }
    //
    private void switchFragment3(LongTermPass longTermPass, float faceSimilar) {
        try {
            Fragment fragment3 = new Document3();
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            String areaDisplay = new String();
            for (String s : longTermPass.areaDisplayCode) {
                areaDisplay = areaDisplay + s + " ";
            }
            Bundle bundle = new Bundle();
            bundle.putString("leadingPeople", longTermPass.leadingPeople);
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
     * 根据hiddenCard判断,如果为true则五分钟后隐藏
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

        recognizeViewModel.destroy();
        switch (actionAfterFinish) {
        case NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY:
            navigateToNewPage(RecognizeDebugActivity.class);
            break;
        case NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY:
            navigateToNewPage(RecognizeSettingsActivity.class);
            break;
        default:
            break;
        }
        super.onDestroy();
        countdownHandler.removeCallbacksAndMessages(null);
    }

    private void initSound() {
        // // 初始化MediaPlayer实例
        // mediaGet = MediaPlayer.create(this, R.raw.get);
        // mediaPass = MediaPlayer.create(this, R.raw.verification_successful);
        // mediaReject = MediaPlayer.create(this, R.raw.validation_failed);
        // mediaFailed = MediaPlayer.create(this, R.raw.shibie_failed);
    }

    private void stopAllAudio() {
        // stopAudio(mediaGet);
        // stopAudio(mediaPass);
        // stopAudio(mediaReject);
    }

    private void stopAudio(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
        }
    }

    private void playAudio(MediaPlayer mediaPlayer) {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            } else if (mediaPlayer.isPlaying()) {
                // mediaPlayer.stop();
                stopAllAudio();
                mediaPlayer.start();
            }
        }
    }

    public String getArea() {
        String areaDetail = infoStorage.getString("deviceAreaDetail", "");
        ALog.i("infoStorage.areaDetail: " + GsonUtils.toJson(areaDetail));
        // 解析 JSON 字符串为 Area 对象
        Area area = GsonUtils.fromJson(areaDetail, Area.class);
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

    // // 上传临时证件日志
    // public void getAllRecords() {
    // GetRequest<Base<CardRecords>> request =
    // OkGo.<Base<CardRecords>>get(UrlConstants.URL_GET_RESORD_PAGE).tag(UrlConstants.URL_GET_RESORD_PAGE);
    // request.headers("tenant-id", "1");
    // if (ApiUtils.accessToken != null) {
    // request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
    // }
    // request.params("deviceId", infoStorage.getString("deviceId", "")).params("pageNo", 1).params("pageSize", 50)
    // .params("direction", -1).execute(new JsonCallback<Base<CardRecords>>() {
    // @Override
    // public void onSuccess(Response<Base<CardRecords>> response) {
    // if (ObjectUtils.isEmpty(response.body())) {
    // showToast("getAllRecords失败");
    // return;
    // }
    // Base<CardRecords> res = response.body();
    // if (res.getCode() == 200) {
    // if (ObjectUtils.isNotEmpty(res.getData())
    // && ObjectUtils.isNotEmpty(res.getData().getList())) {
    // mListAdapter.clear();
    // mListAdapter.addAll(res.getData().getList());
    // }
    // } else {
    // showWarningToast(res.getMsg());
    // }
    // }
    //
    // @Override
    // public void onError(Response<Base<CardRecords>> response) {
    // response.getException().printStackTrace();
    // ALog.e("uploadTemporaryRecords," + response.getException().getMessage());
    // }
    // });
    // }

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
    // previewView.setLayoutParams(layoutParams);
    // faceRectView.setLayoutParams(layoutParams);
    // return layoutParams;
    // }
    private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView faceRectView,
            Camera.Size previewSize, int displayOrientation, float scale) {
        // 获取当前视图的布局参数
        ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();

        // 获取屏幕的宽度和高度
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;

        // 将布局参数的宽度和高度设置为屏幕的宽度和高度
        layoutParams.width = screenWidth;
        layoutParams.height = screenHeight;

        // 应用调整后的布局参数
        previewView.setLayoutParams(layoutParams);
        faceRectView.setLayoutParams(layoutParams);

        return layoutParams;
    }

    private void initRgbCamera() {
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                runOnUiThread(() -> {
                    Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
                    ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb,
                            binding.dualCameraTexturePreviewRgb, binding.dualCameraFaceRectView, previewSizeRgb,
                            displayOrientation, 1.0f);
                    rgbFaceRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height,
                            layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                            ConfigUtil.isDrawRgbRectHorizontalMirror(RegisterAndRecognizeActivity.this),
                            ConfigUtil.isDrawRgbRectVerticalMirror(RegisterAndRecognizeActivity.this));

                    FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewRgb.getParent());
                    if (textViewRgb == null) {
                        textViewRgb = new TextView(RegisterAndRecognizeActivity.this, null);
                    } else {
                        parentView.removeView(textViewRgb);
                    }
                    textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    // textViewRgb.setText(
                    // getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
                    textViewRgb.setTextColor(Color.WHITE);
                    textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                    parentView.addView(textViewRgb);

                    // // 父View宽度和子View一致，保持居中
                    // ViewGroup.LayoutParams parentLayoutParams = parentView.getLayoutParams();
                    // parentLayoutParams.width = layoutParams.width;
                    // parentView.setLayoutParams(parentLayoutParams);

                    // 添加recognizeAreaView，在识别区域发生变更时，更新数据给FaceHelper
                    if (ConfigUtil.isRecognizeAreaLimited(RegisterAndRecognizeActivity.this)) {
                        if (recognizeAreaView == null) {
                            recognizeAreaView = new RecognizeAreaView(RegisterAndRecognizeActivity.this);
                            recognizeAreaView.setLayoutParams(new ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        } else {
                            parentView.removeView(recognizeAreaView);
                        }
                        recognizeAreaView.setOnRecognizeAreaChangedListener(
                                recognizeArea -> recognizeViewModel.setRecognizeArea(recognizeArea));
                        parentView.addView(recognizeAreaView);
                    }

                    recognizeViewModel.onRgbCameraOpened(camera);
                    recognizeViewModel.setRgbFaceRectTransformer(rgbFaceRectTransformer);
                });
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                binding.dualCameraFaceRectView.clearFaceInfo();
                List<FacePreviewInfo> facePreviewInfoList = recognizeViewModel.onPreviewFrame(nv21, true);
                if (facePreviewInfoList != null && rgbFaceRectTransformer != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
                recognizeViewModel.clearLeftFace(facePreviewInfoList);
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

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        rgbCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewRgb.getMeasuredWidth(),
                        binding.dualCameraTexturePreviewRgb.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .additionalRotation(previewConfig.getRgbAdditionalDisplayOrientation())
                .previewSize(recognizeViewModel.loadPreviewSize()).specificCameraId(previewConfig.getRgbCameraId())
                .isMirror(ConfigUtil.isDrawRgbPreviewHorizontalMirror(this))
                .previewOn(binding.dualCameraTexturePreviewRgb).cameraListener(cameraListener).build();
        rgbCameraHelper.init();
        rgbCameraHelper.start();
    }

    /**
     * 初始化红外相机，若活体检测类型是可见光活体检测或不启用活体，则不需要启用
     */
    private void initIrCamera() {
        if (livenessType == LivenessType.RGB || !enableLivenessDetect) {
            return;
        }
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams =
                        adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb, binding.dualCameraTexturePreviewIr,
                                binding.dualCameraFaceRectViewIr, previewSizeIr, displayOrientation, 0.25f);

                irFaceRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height,
                        layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
                        ConfigUtil.isDrawIrRectHorizontalMirror(RegisterAndRecognizeActivity.this),
                        ConfigUtil.isDrawIrRectVerticalMirror(RegisterAndRecognizeActivity.this));

                FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewIr.getParent());
                if (textViewIr == null) {
                    textViewIr = new TextView(RegisterAndRecognizeActivity.this, null);
                } else {
                    parentView.removeView(textViewIr);
                }
                textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewIr
                        .setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
                textViewIr.setTextColor(Color.WHITE);
                textViewIr.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
                parentView.addView(textViewIr);

                recognizeViewModel.onIrCameraOpened(camera);
                recognizeViewModel.setIrFaceRectTransformer(irFaceRectTransformer);
            }

            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                recognizeViewModel.refreshIrPreviewData(nv21);
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
                if (irFaceRectTransformer != null) {
                    irFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                ALog.i("onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        irCameraHelper = new DualCameraHelper.Builder()
                .previewViewSize(new Point(binding.dualCameraTexturePreviewIr.getMeasuredWidth(),
                        binding.dualCameraTexturePreviewIr.getMeasuredHeight()))
                .rotation(getWindowManager().getDefaultDisplay().getRotation())
                .specificCameraId(previewConfig.getIrCameraId()).previewOn(binding.dualCameraTexturePreviewIr)
                .cameraListener(irCameraListener).isMirror(ConfigUtil.isDrawIrPreviewHorizontalMirror(this))
                .previewSize(recognizeViewModel.loadPreviewSize()) // 相机预览大小设置，RGB与IR需使用相同大小
                .additionalRotation(previewConfig.getIrAdditionalDisplayOrientation()) // 额外旋转角度
                .build();
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
                    recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.RGB, openRectInfoDraw);
            binding.dualCameraFaceRectView.drawRealtimeFaceInfo(rgbDrawInfoList);
        }
        if (irFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> irDrawInfoList =
                    recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.IR, openRectInfoDraw);
            binding.dualCameraFaceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                recognizeViewModel.init();
                initRgbCamera();
                if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                    initIrCamera();
                }
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    public void openRectInfoDraw(View view) {
        openRectInfoDraw = !openRectInfoDraw;
        recognizeViewModel.setDrawRectInfoTextValue(openRectInfoDraw);
    }

    /**
     * 将准备注册的状态置为待注册
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        recognizeViewModel.prepareRegister();
    }

    /**
     * 参数配置
     *
     * @param view
     */
    public void setting(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 识别分析界面
     *
     * @param view 注册按钮
     */
    public void recognizeDebug(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 在{@link ActivityRegisterAndRecognizeBinding#dualCameraTexturePreviewRgb}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            recognizeViewModel.init();
            initRgbCamera();
            if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                initIrCamera();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCamera();
    }

    private void resumeCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.start();
        }
        if (irCameraHelper != null) {
            irCameraHelper.start();
        }
    }

    @Override
    protected void onPause() {
        pauseCamera();
        super.onPause();
    }

    private void pauseCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.stop();
        }
        if (irCameraHelper != null) {
            irCameraHelper.stop();
        }
    }

    @Override
    public void onRecognized(CompareResult compareResult, Integer liveness, boolean similarPass) {

    }

    @Override
    public void onRecognized(Bitmap bitmap, float faceSimilar, float quality, String username, boolean result) {
        ALog.i("userName:" + username + ", faceSimilar:" + faceSimilar + ", result:" + result);
        if (result) {
            if (!isChecking()) {
                queryPassCard(username, bitmap, faceSimilar, quality);
            }
        } else {
            // toggleFragment2();
            // playAudio(mediaFailed);
        }
    }

    @Override
    public void onNoticeChanged(String notice) {
        ALog.v(notice + "");
    }

    /**
     * 判断证件是否正常
     *
     * @param status
     * @return
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

    boolean checking = false;
    public LongTermPass lastLongTermPass;
    long lastTime;

    public boolean isChecking() {
        ALog.i("checking:" + checking);
        return checking;
    }

    public void startChecking() {
        checking = true;
    }

    public void stopChecking() {
        checking = false;
    }

    /**
     * 根据名字查询通行证
     */
    public void queryPassCard(String name, Bitmap bitmap, float faceSimilar, float quality) {
        // if (ArcFaceApplication.TEST) {
        // return;
        // }
        if (null == cardDao) {
            ALog.i("dao初始化失败 ");
            return;
        }
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (isChecking()) {
                    return null;
                }

                if (ObjectUtils.isNotEmpty(lastLongTermPass) && lastTime > 0
                        && (TimeUtils.getNowMills() - lastTime < 1500)) {
                    ALog.d("lastLongTermPass:" + lastLongTermPass.toString() + "， lastTime:" + lastTime);
                    if (lastLongTermPass.id.equals(name)) {
                        ALog.e("过滤1500毫秒内重复查询");
                        lastLongTermPass = null;
                        lastTime = 0;
                        stopChecking();
                        return null;
                    }
                }
                startChecking();

                // String imgUrl = imageUploader.uploadBitmap(bitmap);
                LongTermPass card = cardDao.getById(name);

                if (ObjectUtils.isEmpty(card)) {
                    ALog.i("本地数据库未查询到: " + name);
                    // playAudio(mediaReject);
                    playAudio(R.raw.validation_failed);
                    showCustomDialog(2, "证件不存在");
                    lastLongTermPass = null;
                    lastTime = 0;
                    stopChecking();
                    // runOnUiThread(() -> ToastDialogManager.showCustomDialog(RegisterAndRecognizeActivity.this, 2,
                    // "证件不存在"));
                    return null;
                }
                ALog.i("card: " + card.toString());
                // List<LongTermPass> cards = cardDao.getAllByIdCode(name);
                // // 判断证件是否存在
                // if (cards == null || cards.size() == 0) {
                // ALog.i("本地数据库未查询到: " + name);
                // playAudio(mediaReject);
                // runOnUiThread(() -> ToastDialogManager.showCustomDialog(RegisterAndRecognizeActivity.this, 2,
                // "证件不存在"));
                // return null;
                // }
                // boolean allNotTimeInRange = false;
                // boolean allIsExpired = false;
                // boolean pass = false;
                // LongTermPass card = null;
                // for (int i = 0; i < cards.size(); i++) {
                // LongTermPass item = cards.get(i);
                // ALog.e(item.toString());
                // // 判断证件是否过期
                // boolean currentTimeInRange = DeviceUtils.isCurrentTimeInRange(item.startDate, item.expiryDate);
                // if (!currentTimeInRange) {
                // item.status = 3;
                //// playAudio(mediaReject);
                //// runOnUiThread(new Runnable() {
                //// @Override
                //// public void run() {
                //// ToastDialogManager.showCustomDialog(RegisterAndRecognizeActivity.this, 2, "证件过期");
                //// }
                //// });
                // ALog.e("证件过期");
                // savePassRecord(item, bitmap, false, imgUrl);
                // if (i == cards.size() - 1) {
                // allNotTimeInRange = true;
                // }
                // card = item;
                // continue;
                // }
                // // 判断证件是否正常
                // if (item.status != 1) {
                //// playAudio(mediaReject);
                //// runOnUiThread(new Runnable() {
                //// @Override
                //// public void run() {
                //// ToastDialogManager.showCustomDialog(RegisterAndRecognizeActivity.this, 2, "证件已" +
                // theCardIsExpired(item.status));
                //// }
                //// });
                // ALog.e("证件已" + theCardIsExpired(item.status));
                // savePassRecord(item, bitmap, false, imgUrl);
                // if (i == cards.size() - 1) {
                // allIsExpired = true;
                // }
                // card = item;
                // continue;
                // }
                // card = item;
                // break;
                // }
                // if (allNotTimeInRange) {
                // playAudio(mediaReject);
                // runOnUiThread(new Runnable() {
                // @Override
                // public void run() {
                // ToastDialogManager.showCustomDialog(RegisterAndRecognizeActivity.this, 2, "证件过期");
                // }
                // });
                //// savePassRecord(card, bitmap, false, imgUrl);
                // return null;
                // }
                // if (allIsExpired) {
                // playAudio(mediaReject);
                // LongTermPass finalCard = card;
                // runOnUiThread(new Runnable() {
                // @Override
                // public void run() {
                // ToastDialogManager.showCustomDialog(RegisterAndRecognizeActivity.this, 2, "证件已" +
                // theCardIsExpired(finalCard.status));
                // }
                // });
                //// savePassRecord(card, bitmap, false, imgUrl);
                // return null;
                // }
                // 判断证件是否过期
                // boolean currentTimeInRange = DeviceUtils.isCurrentTimeInRange(card.startDate, card.expiryDate);
                // if (!currentTimeInRange) {
                // card.status = 3;
                // // playAudio(mediaReject);
                // playAudio(R.raw.validation_failed);
                // showCustomDialog(2, "证件过期");
                // ALog.e("证件过期");
                // savePassRecord(card, bitmap, faceSimilar, quality, false);
                // lastLongTermPass = null;
                // lastTime = 0;
                // stopChecking();
                // return null;
                // }
                // // 判断证件是否正常
                // if (card.status != 1) {
                // // playAudio(mediaReject);
                // playAudio(R.raw.validation_failed);
                // showCustomDialog(2, "证件已" + theCardIsExpired(card.status));
                // ALog.e("证件已" + theCardIsExpired(card.status));
                // savePassRecord(card, bitmap, faceSimilar, quality, false);
                // lastLongTermPass = null;
                // lastTime = 0;
                // stopChecking();
                // return null;
                // }

                if (!checkCard(card)) {
                    savePassRecord(card, bitmap, faceSimilar, quality, false);
                    lastLongTermPass = null;
                    lastTime = 0;
                    stopChecking();
                    return null;
                }

                // C类卡
                if (card.idCode.startsWith("C")) {
                    // 判断是否关联引领人
                    if (ObjectUtils.isNotEmpty(card.leadingPeopleId)) {
                        String[] leadingPeopleId = card.leadingPeopleId;
                        String linshiID = infoStorage.getString("linshiID", "");
                        ALog.i("长期证件: " + GsonUtils.toJson(linshiID));
                        if (linshiID.equals("")) {
                            playAudio(R.raw.validation_failed);
                            showCustomDialog(2, "请先刷引领人");
                            ALog.i("设置了引领人的C类卡请先刷引领人");
                            stopChecking();
                            return null;
                        }
                        boolean contains = Arrays.asList(leadingPeopleId).contains(linshiID);
                        if (!contains) {
                            playAudio(R.raw.validation_failed);
                            showCustomDialog(2, "请刷引领人卡");
                            stopChecking();
                            return null;
                        }

                        ALog.i("引领人审核通过: ");
                        // switchFragment3(longTermPass);
                        // String s = Constants.DEFAULT_REGISTER_FACES_DIR + "/" + longTermPass.idCode + ".jpg";
                        // Bitmap bitmap = ImageUtils.loadBitmapFromPath(s);
                        // if (card.status != 1) {
                        // playAudio(R.raw.validation_failed);
                        // showCustomDialog(2, "证件已" + theCardIsExpired(card.status));
                        // stopChecking();
                        // return null;
                        // }

                        if (!checkCard(card)) {
                            return null;
                        }

                        // showCustomDialog(1, "设置了引领人的C类卡请验证人脸");
                    }
                }
                // // String s = imageUploader.uploadBitmap(bitmap);
                // runOnUiThread(new Runnable() {
                // @Override
                // public void run() {
                // ALog.e("photo:" + card.photo + "");
                // ALog.e("checkPhoto:" + card.checkPhoto + "");
                // Glide.with(RegisterAndRecognizeActivity.this).load(card.photo).into(iv_face1);
                // Glide.with(RegisterAndRecognizeActivity.this).load(card.checkPhoto).into(iv_face2);
                // }
                // });
                // PlayerUtil.getInstance().initPlayer(RegisterAndRecognizeActivity.this,
                // R.raw.verification_successful);
                // PlayerUtil.getInstance().startPlay();
                playAudio(R.raw.verification_successful);
                // 正常,提示成功,保存到数据库
                // playAudio(mediaPass);
                showSuccessDialog();

                if (card != null) {
                    if (card.type == 0) {
                        toggleFragment(card, faceSimilar); // 更新fragment
                    } else if (card.type == 1) {
                        switchFragment3(card, faceSimilar);
                    }
                }

                // toggleFragment(card, faceSimilar); // 更新fragment

                // uploadPassRecord(true);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.tvFaceSimilar.setBackgroundResource(R.drawable.face_similar_bg2);
                        binding.tvFaceSimilar.setVisibility(View.VISIBLE);
                        binding.tvFaceSimilar.setText(String.format("%.2f%%", faceSimilar * 100));
                        countdownHandler.removeMessages(10);
                        countdownHandler.sendEmptyMessageDelayed(10, 10 * 1000L);
                    }
                });

                if (ArcFaceApplication.TEST) {
                    return null;
                }
                // C卡不能引领人
                if (!card.idCode.startsWith("C")) {
                    // 保存记录到缓存五分钟
                    saveRecord(card);
                }
                savePassRecord(card, bitmap, faceSimilar, quality, true);
                return null;
            }
        });

    }

    public boolean checkCard(LongTermPass longTermPass) {
        long span = TimeUtils.getTimeSpan(TimeUtils.string2Millis(longTermPass.startDate), TimeUtils.getNowMills(),
                TimeConstants.SEC);
        if (span > 0) {
            playAudio(R.raw.validation_failed);
            showCustomDialog(2, "证件未生效");
            stopChecking();
            return false;
        }

        span = TimeUtils.getTimeSpan(TimeUtils.string2Millis(longTermPass.expiryDate), TimeUtils.getNowMills(),
                TimeConstants.SEC);
        if (span > 0) {
            longTermPass.status = 3;
            playAudio(R.raw.validation_failed);
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
            playAudio(R.raw.validation_failed);
            showCustomDialog(2, "证件已" + theCardIsExpired(longTermPass.status));
            stopChecking();
            // startReadLongPassCardID();
            return false;

        }

        if (longTermPass.isBlacklist) {
            playAudio(R.raw.validation_failed);
            showCustomDialog(2, "通行证在黑名单中");
            stopChecking();
            return false;

        }

        if (longTermPass.isWithdraw) {
            playAudio(R.raw.validation_failed);
            showCustomDialog(2, "通行证被收回");
            stopChecking();
            return false;

        }

        if (longTermPass.isWithhold) {
            playAudio(R.raw.validation_failed);
            showCustomDialog(2, "通行证被暂扣");
            stopChecking();
            return false;

        }

        if (longTermPass.score <= 0) {
            playAudio(R.raw.validation_failed);
            showCustomDialog(2, "通行证分数为0");
            stopChecking();
            return false;

        }
        return true;
    }

    /**
     * 保存通行记录到本地数据库
     */
    public void savePassRecord(LongTermPass card, Bitmap bitmap, float faceSimilar, float quality, Boolean isPass) {
        // uploadPassRecord();
        // boolean availableByPing = NetworkUtils.isAvailableByPing();
        // ALog.i("用 ping 判断网络是否可用：" + availableByPing);
        int type = card.type;
        if (type == 0) {// 长期卡
            saveLongRecord(card, bitmap, faceSimilar, quality, isPass);
        } else if (type == 1) {
            saveShortRecord(card, bitmap, faceSimilar, quality, isPass);
        }
    }

    /**
     * 保存长期通行记录到本地
     */
    public void saveLongRecord(LongTermPass longTermPass, Bitmap bitmap, float faceSimilar, float quality,
            Boolean isPass) {
        float qualityvalue = recognizeViewModel.getFeatureValue(bitmap);
        ALog.e("qualityvalue:" + qualityvalue);
        quality = qualityvalue;
        try {
            LongTermRecords longTermRecords = new LongTermRecords();
            // longTermRecords.id = UUID.randomUUID().toString();
            // longTermRecords.id = SnowflakeIdUtil.getInstance().nextId() + "";
            SnowFlake worker = new SnowFlake(1, 1, 1);
            longTermRecords.id = worker.nextId() + "";
            longTermRecords.status = String.valueOf(isPass);
            if (!isPass) {
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
            // longTermRecords.sitePhoto = imgUrl;// 识别图片路径

            // String deviceDirection = infoStorage.getString("deviceDirection", "-1");

            longTermRecords.direction = direction + "";// 通行方向（1：进，-1出，2：核验）
            longTermRecords.nickname = longTermPass.nickname;

            String deviceId = infoStorage.getString("deviceId", "");
            longTermRecords.deviceId = deviceId;
            String deviceName = infoStorage.getString("deviceName", "立式查验终端");
            longTermRecords.deviceName = deviceName;
            String userId = ApiUtils.userId;
            longTermRecords.checkUserId = userId;
            String loginName = infoStorage.getString("loginName", "");
            longTermRecords.checkUserName = loginName;
            // longTermRecords.checkUserName = longTermPass.nickname;
            longTermRecords.companyName = longTermPass.companyName;
            longTermRecords.expiryDate = longTermPass.expiryDate;
            longTermRecords.templateType = longTermPass.templateType;
            longTermRecords.areaDisplayCode = longTermPass.areaDisplayCode;
            longTermRecords.faceSimilar = faceSimilar + "";
            longTermRecords.faceQuality = quality + "";

            String areaDetail = infoStorage.getString("deviceAreaDetail", "");
            ALog.i("infoStorage.areaDetail: " + GsonUtils.toJson(areaDetail));
            // 解析 JSON 字符串为 Area 对象
            Area area = GsonUtils.fromJson(areaDetail, Area.class);
            if (area != null) {
                longTermRecords.area = area.getId();
                longTermRecords.areaName = area.getCode() + area.getName();
                if (area.getChildren() != null && ObjectUtils.isNotEmpty(area.getChildren())) {
                    for (Area item : area.getChildren()) {
                        longTermRecords.areaName += item.getCode() + item.getName();// "通行区域名称(通行区域编码+名称)";
                    }
                }
            }
            // if (area != null) {
            // longTermRecords.area = area.getId();
            // longTermRecords.areaName = area.getCode() + area.getName();
            // }
            // if (area.getChildren() != null && !area.getChildren().isEmpty()) {
            // Area childArea = area.getChildren().get(0);
            // if (childArea != null) {
            // longTermRecords.areaName += childArea.getCode() + childArea.getName();// "通行区域名称(通行区域编码+名称)";
            // }
            // Area childArea1 = area.getChildren().get(1);
            // if (childArea1 != null) {
            // longTermRecords.areaName += childArea1.getCode() + childArea1.getName();
            // }
            // }
            // ThreadUtils.executeByCached(new SmallTask() {
            // @Override
            // public String doInBackground() throws Throwable {
            // LongTermRecordsDao dao = ArcFaceApplication.getApplication().getDb().longTermRecordsDao();
            // dao.insert(longTermRecords);
            // mListAdapter.add(0, longTermRecords);
            // return null;
            // }
            // });
            // uploadLongRecords(longTermRecords);
            ThreadUtils.executeByCached(new SmallTask() {
                @Override
                public String doInBackground() throws Throwable {
                    // if (ArcFaceApplication.getApplication().isOffLine())
                    if (true) {
                        saveLongTermRecordsToDb(longTermRecords, bitmap);
                    } else {
                        // getAllRecords();
                        // 上传通行图片到服务器
                        String imgUrl = imageUploader.uploadBitmap2(bitmap);
                        ALog.i("上传图片路径: " + imgUrl);
                        if (ObjectUtils.isEmpty(imgUrl)) {
                            saveLongTermRecordsToDb(longTermRecords, bitmap);
                            return null;
                        }
                        longTermRecords.sitePhoto = imgUrl;
                        // uploadBitmap(bitmap);
                        uploadLongTermRecords(longTermRecords);
                    }
                    return null;
                }

                @Override
                public void onSuccess(String result) {
                    if (isPass) {
                        if (checkList.size() > 20) {
                            checkList.remove(checkList.size() - 1);
                        }
                        checkList.add(0, longTermRecords);
                        mListAdapter.notifyDataSetChanged();
                        // mListAdapter.add(0, longTermRecords);
                    }
                    stopChecking();
                    lastLongTermPass = longTermPass;
                    lastTime = TimeUtils.getNowMills();
                }
            });

        } catch (Exception e) {
            ALog.e("保存长期记录失败: " + e.getMessage());
        }
    }

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
     * 保存临时通信记录到本地
     */
    public void saveShortRecord(LongTermPass longTermPass, Bitmap bitmap, float faceSimilar, float quality,
            Boolean isPass) {
        float qualityvalue = recognizeViewModel.getFeatureValue(bitmap);
        ALog.e("qualityvalue:" + qualityvalue);
        quality = qualityvalue;
        try {
            TemporaryCardRecords temporaryCardRecords = new TemporaryCardRecords();
            // temporaryCardRecords.id = UUID.randomUUID().toString();
            // temporaryCardRecords.id = SnowflakeIdUtil.getInstance().nextId() + "";
            // temporaryCardRecords.sitePhoto = imgUrl;
            SnowFlake worker = new SnowFlake(1, 1, 1);
            temporaryCardRecords.id = worker.nextId() + "";
            temporaryCardRecords.status = String.valueOf(isPass);
            if (!isPass) {
                temporaryCardRecords.reason = "人证不匹配";
            }
            temporaryCardRecords.passid = longTermPass.id;
            temporaryCardRecords.idCode = longTermPass.idCode;
            temporaryCardRecords.cardId = longTermPass.cardId;
            temporaryCardRecords.applyId = longTermPass.applyId;
            // String deviceDirection = infoStorage.getString("deviceDirection", "-1");
            temporaryCardRecords.direction = direction + "";

            temporaryCardRecords.nickname = longTermPass.nickname;
            String deviceId = infoStorage.getString("deviceId", "");
            temporaryCardRecords.deviceId = deviceId;
            String deviceName = infoStorage.getString("deviceName", "立式查验终端");
            temporaryCardRecords.deviceName = deviceName;
            String userId = ApiUtils.userId;
            temporaryCardRecords.checkUserId = userId;
            String loginName = infoStorage.getString("loginName", "");
            temporaryCardRecords.checkUserName = loginName;
            // ALog.i("saveTemporaryLocalSQL查验人名字: " + loginName);
            // temporaryCardRecords.checkUserName = longTermPass.nickname;

            temporaryCardRecords.companyName = longTermPass.companyName;
            temporaryCardRecords.expiryDate = longTermPass.expiryDate;
            temporaryCardRecords.templateType = longTermPass.templateType;
            temporaryCardRecords.areaDisplayCode = longTermPass.areaDisplayCode;
            temporaryCardRecords.faceSimilar = faceSimilar + "";
            temporaryCardRecords.faceQuality = quality + "";

            String areaDetail = infoStorage.getString("deviceAreaDetail", "");
            Area area = GsonUtils.fromJson(areaDetail, Area.class);
            // if (area != null) {
            // temporaryCardRecords.area = area.getId();
            // // temporaryCardRecords.areaName = "通行区域名称(通行区域编码+名称)";
            // temporaryCardRecords.areaName += area.getCode() + area.getName();
            // }
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
            // ThreadUtils.executeByCached(new SmallTask() {
            // @Override
            // public String doInBackground() throws Throwable {
            // TemporaryCardRecordsDao dao = ArcFaceApplication.getApplication().getDb().temporaryCardRecordsDao();
            // dao.insert(temporaryCardRecords);
            // uploadTemporaryRecords(temporaryCardRecords);
            // return null;
            // }
            // });
            if (area != null) {
                temporaryCardRecords.area = area.getId();
                temporaryCardRecords.areaName = area.getCode() + area.getName();
                if (area.getChildren() != null && ObjectUtils.isNotEmpty(area.getChildren())) {
                    for (Area item : area.getChildren()) {
                        temporaryCardRecords.areaName += item.getCode() + item.getName();// "通行区域名称(通行区域编码+名称)";
                    }
                }
            }
            String linshiID = infoStorage.getString("linshiID", "");
            temporaryCardRecords.leadingPeopleId = linshiID;
            ThreadUtils.executeByCached(new SmallTask() {
                @Override
                public String doInBackground() throws Throwable {
                    // if (ArcFaceApplication.getApplication().isOffLine())
                    if (true) {
                        saveTemporaryRecordsToDb(temporaryCardRecords, bitmap);

                    } else {
                        // getAllRecords();
                        // 上传通行图片到服务器
                        String imgUrl = imageUploader.uploadBitmap2(bitmap);
                        ALog.i("上传图片路径: " + imgUrl);
                        if (ObjectUtils.isEmpty(imgUrl)) {
                            saveTemporaryRecordsToDb(temporaryCardRecords, bitmap);
                            return null;
                        }
                        temporaryCardRecords.sitePhoto = imgUrl;
                        uploadTemporaryRecords(temporaryCardRecords);
                    }
                    return null;

                }

                @Override
                public void onSuccess(String result) {
                    if (isPass) {
                        if (checkList.size() > 20) {
                            checkList.remove(checkList.size() - 1);
                        }
                        checkList.add(0, temporaryCardRecords);
                        mListAdapter.notifyDataSetChanged();
                        // mListAdapter.add(0, temporaryCardRecords);
                    }
                    stopChecking();

                    lastLongTermPass = longTermPass;
                    lastTime = TimeUtils.getNowMills();
                }
            });

        } catch (Exception e) {
            ALog.e("保存临时记录失败: " + e.getMessage());
        }
    }

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
     * 上传长期证件通行记录到后端服务器
     */
    public void uploadLongTermRecords(LongTermRecords longTermRecords) {
        // ALog.i("本地长期记录: " + GsonUtils.toJson(longTermRecords));
        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // ApiUtils.post(UrlConstants.URL_CREATE_LONG_RECORD, GsonUtils.toJson(longTermRecords),
        // new ApiUtils.ApiCallback() {
        // @Override
        // public void onSuccess(String response) {
        // mListAdapter.add(0, longTermRecords);
        // ALog.d("上传长期证件成功返回: " + response);
        // }
        //
        // @Override
        // public void onFailure(Throwable e) {
        // mListAdapter.add(0, longTermRecords);
        // ALog.e("上传长期证件日志失败返回: " + e.getMessage());
        // }
        // });
        // return null;
        // }
        // });

        PostRequest<Base<String>> request =
                OkGo.<Base<String>> post(UrlConstants.URL_CREATE_LONG_RECORD).tag(UrlConstants.URL_CREATE_LONG_RECORD);
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        request.upJson(GsonUtils.toJson(longTermRecords)).execute(new JsonCallback<Base<String>>() {
            @Override
            public void onSuccess(Response<Base<String>> response) {
                if (ObjectUtils.isEmpty(response.body())) {
                    showToast("uploadTemporaryRecords失败");
                    return;
                }
                Base<String> res = response.body();
                if (res.getCode() == 200) {
                    ThreadUtils.executeByCached(new SimpleTask() {
                        @Override
                        public String doInBackground() throws Throwable {
                            ArcFaceApplication.getApplication().getDb().longTermRecordsDao().delete(longTermRecords);
                            return null;
                        }
                    });

                } else {
                    ALog.e("上传长期证件日志失败");
                    // showWarningToast(res.getMsg());
                }
            }

            @Override
            public void onError(Response<Base<String>> response) {
                response.getException().printStackTrace();
                ALog.e("上传长期证件日志失败," + response.getException().getMessage());
                saveLongTermRecordsToDb(longTermRecords, null);

            }
        });
    }

    /**
     * 上传临时证件通行记录到后端服务器
     */
    public void uploadTemporaryRecords(TemporaryCardRecords temporaryCardRecords) {
        // ThreadUtils.executeByCached(new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // ApiUtils.post(UrlConstants.URL_CREATE_TEMP_RECORD, GsonUtils.toJson(temporaryCardRecords),
        // new ApiUtils.ApiCallback() {
        // @Override
        // public void onSuccess(String response) {
        // ALog.d("上传临时证件成功返回: " + response);
        // }
        //
        // @Override
        // public void onFailure(Throwable e) {
        // ALog.e("Error: " + e.getMessage());
        // }
        // });
        // return null;
        // }
        // });

        PostRequest<Base<String>> request =
                OkGo.<Base<String>> post(UrlConstants.URL_CREATE_TEMP_RECORD).tag(UrlConstants.URL_CREATE_TEMP_RECORD);
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        request.upJson(GsonUtils.toJson(temporaryCardRecords)).execute(new JsonCallback<Base<String>>() {
            @Override
            public void onSuccess(Response<Base<String>> response) {
                if (ObjectUtils.isEmpty(response.body())) {
                    showToast("uploadTemporaryRecords失败");
                    return;
                }
                Base<String> res = response.body();
                if (res.getCode() == 200) {
                    ThreadUtils.executeByCached(new SimpleTask() {
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

    public void saveRecord(LongTermPass longTermPass) {
        if (longTermPass != null && ObjectUtils.isNotEmpty(longTermPass.userId)) {
            infoStorage.saveString("linshiID", longTermPass.userId);
        } else {
            infoStorage.saveString("linshiID", "");
        }
        countdownHandler.removeMessages(9);
        countdownHandler.sendEmptyMessageDelayed(9, 5 * 60 * 1000);
        // 使用 Handler 定时删除
        // new Handler().postDelayed(new Runnable() {
        // @Override
        // public void run() {
        // infoStorage.remove("linshiID");
        // }
        // }, 5 * 60 * 1000);
    }

    CustomPopDialog popDialog;

    /**
     * 显示成功弹窗
     */
    public void showSuccessDialog() {
        if (popDialog != null) {
            popDialog.dismiss();
        }
        showCustomDialog(1, null);
    }

    /**
     * 显示成功弹窗
     */
    public void showFailedDialog() {
        if (popDialog != null) {
            popDialog.dismiss();
        }
        showCustomDialog(2, null);
    }

    /**
     * 显示成功弹窗
     */
    public void showFailedDialog(String msg) {
        if (popDialog != null) {
            popDialog.dismiss();
        }
        showCustomDialog(2, msg);
    }

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

        // popDialog = new CustomPopDialog(LivenessDetectActivity1.this, icon, msg);
        // new XPopup.Builder(LivenessDetectActivity1.this).hasStatusBarShadow(false).isRequestFocus(false)
        // .hasStatusBar(false).asCustom(popDialog).show();
    }

    void check() {
        GetRequest<Base<Version>> getRequest =
                OkGo.<Base<Version>> get(UrlConstants.URL_GET_APP_LAST_VERSION).params("type", 3);
        getRequest.headers("tenant-id", "1");
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
                new XPopup.Builder(RegisterAndRecognizeActivity.this)
                        .asCustom(new UpdatePopDialog(RegisterAndRecognizeActivity.this, response.body().getData(),
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
                                }))
                        .show();
            }
        });
    }

    public void playAudio(int res) {
        PlayerUtil.getInstance().initPlayer(RegisterAndRecognizeActivity.this, res);
        PlayerUtil.getInstance().startPlay();
    }
}
