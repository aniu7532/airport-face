// package com.arcsoft.arcfacedemo.ui.activity;
//
// import android.Manifest;
// import android.content.pm.ActivityInfo;
// import android.graphics.Color;
// import android.graphics.Point;
// import android.hardware.Camera;
// import android.os.Build;
// import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
// import android.util.DisplayMetrics;
// import android.view.View;
// import android.view.ViewGroup;
// import android.view.ViewTreeObserver;
// import android.view.WindowManager;
// import android.widget.FrameLayout;
// import android.widget.TextView;
//
// import androidx.annotation.NonNull;
// import androidx.core.app.ActivityCompat;
// import androidx.databinding.DataBindingUtil;
// import androidx.fragment.app.Fragment;
// import androidx.fragment.app.FragmentManager;
// import androidx.fragment.app.FragmentTransaction;
// import androidx.lifecycle.ViewModelProvider;
// import androidx.recyclerview.widget.LinearLayoutManager;
// import androidx.recyclerview.widget.RecyclerView;
//
// import com.arcsoft.arcfacedemo.R;
// import com.arcsoft.arcfacedemo.data.http.JsonCallback;
// import com.arcsoft.arcfacedemo.databinding.ActivityRegisterAndRecognizeBinding;
// import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
// import com.arcsoft.arcfacedemo.entity.Area;
// import com.arcsoft.arcfacedemo.entity.Base;
// import com.arcsoft.arcfacedemo.entity.CardRecords;
// import com.arcsoft.arcfacedemo.network.ApiUtils;
// import com.arcsoft.arcfacedemo.network.UrlConstants;
// import com.arcsoft.arcfacedemo.ui.adapter.CheckLogListAdapter;
// import com.arcsoft.arcfacedemo.ui.fragment.Document11;
// import com.arcsoft.arcfacedemo.ui.fragment.Document2;
// import com.arcsoft.arcfacedemo.ui.fragment.Document3;
// import com.arcsoft.arcfacedemo.ui.model.PreviewConfig;
// import com.arcsoft.arcfacedemo.ui.viewmodel.RecognizeViewModel;
// import com.arcsoft.arcfacedemo.util.ConfigUtil;
// import com.arcsoft.arcfacedemo.util.ErrorCodeUtil;
// import com.arcsoft.arcfacedemo.util.FaceRectTransformer;
// import com.arcsoft.arcfacedemo.util.InfoStorage;
// import com.arcsoft.arcfacedemo.util.WeakHandler;
// import com.arcsoft.arcfacedemo.util.camera.CameraListener;
// import com.arcsoft.arcfacedemo.util.camera.DualCameraHelper;
// import com.arcsoft.arcfacedemo.util.face.constants.LivenessType;
// import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
// import com.arcsoft.arcfacedemo.util.log.ALog;
// import com.arcsoft.arcfacedemo.widget.FaceRectView;
// import com.arcsoft.arcfacedemo.widget.RecognizeAreaView;
// import com.arcsoft.arcfacedemo.widget.dialog.RecordsPopDialog;
// import com.arcsoft.face.ErrorInfo;
// import com.blankj.utilcode.util.GsonUtils;
// import com.blankj.utilcode.util.NetworkUtils;
// import com.blankj.utilcode.util.ObjectUtils;
// import com.blankj.utilcode.util.ThreadUtils;
// import com.blankj.utilcode.util.TimeUtils;
// import com.lxj.xpopup.XPopup;
// import com.lzy.okgo.OkGo;
// import com.lzy.okgo.model.Response;
// import com.lzy.okgo.request.GetRequest;
//
// import java.text.SimpleDateFormat;
// import java.util.ArrayList;
// import java.util.List;
//
/// **
// * 使用：出页面。纯人脸识别，无刷卡
// * FaceHelper类searchFace函数
// */
// public class RegisterAndRecognizeActivity2 extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {
// private static final String TAG = "RegisterAndRecognize";
//
// private DualCameraHelper rgbCameraHelper;
// private DualCameraHelper irCameraHelper;
// private FaceRectTransformer rgbFaceRectTransformer;
// private FaceRectTransformer irFaceRectTransformer;
//
// private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
//
// int actionAfterFinish = 0;
// private static final int NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY = 1;
// private static final int NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY = 2;
//
// /**
// * 所需的所有权限信息
// */
// private static final String[] NEEDED_PERMISSIONS =
// new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE};
// private ActivityRegisterAndRecognizeBinding binding;
// private RecognizeViewModel recognizeViewModel;
// private LivenessType livenessType;
// private boolean enableLivenessDetect = false;
// private RecognizeAreaView recognizeAreaView;
// private TextView textViewRgb;
// private TextView textViewIr;
// private boolean openRectInfoDraw;
// InfoStorage infoStorage;
// private Fragment fragment11;
// private Fragment fragment2;
// private Fragment fragment3;
// private long countdownTime = 1 * 60 * 1000; // 1分钟倒计时
// private WeakHandler countdownHandler = new WeakHandler(new Handler.Callback() {
// @Override
// public boolean handleMessage(@NonNull Message msg) {
// switch (msg.what) {
// case 2:
// // 其他需要执行的操作
// FragmentManager fragmentManager = getSupportFragmentManager();
// FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
// fragmentTransaction.replace(R.id.fragment_all, fragment11);
// fragmentTransaction.commit();
// break;
// case 5:
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
// binding.tvTis.setText("在线模式 " + areaName + "-出查验");
// } else {
// isOffLine = true;
// binding.tvTis.setText("离线模式 " + areaName + "-出查验");
// }
// countdownHandler.removeMessages(5);
// countdownHandler.sendEmptyMessageDelayed(5, 10000L);
// }
// });
// break;
//
// case 6:
// binding.tvTime.setText(TimeUtils.getNowString(new SimpleDateFormat("yyyy年MM月dd日HH:mm:ss")));
// countdownHandler.removeMessages(6);
// countdownHandler.sendEmptyMessageDelayed(6, 1000L);
// break;
//
// default:
// break;
// }
// return false;
// }
// });
// String areaName;
// private CheckLogListAdapter mListAdapter;
// boolean isOffLine;
//
// @Override
// protected void onCreate(Bundle savedInstanceState) {
// super.onCreate(savedInstanceState);
// binding = DataBindingUtil.setContentView(this, R.layout.activity_register_and_recognize);
//
// // 保持亮屏
// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//
// if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
// WindowManager.LayoutParams attributes = getWindow().getAttributes();
// attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
// getWindow().setAttributes(attributes);
// }
//
// // Activity启动后就锁定为启动时的方向
// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
// initData();
//
// initViewModel();
// initView();
// // fragment初始化
// initFragment();
// openRectInfoDraw = true;
// recognizeViewModel.setDrawRectInfoTextValue(true);
// infoStorage = new InfoStorage(this);
// areaName = getArea();
// countdownHandler.removeMessages(5);
// countdownHandler.sendEmptyMessageDelayed(5, 50L);
// countdownHandler.removeMessages(6);
// countdownHandler.sendEmptyMessageDelayed(6, 50L);
// infoStorage = new InfoStorage(this);
// binding.tvMore.setOnClickListener(new View.OnClickListener() {
// @Override
// public void onClick(View v) {
// new XPopup.Builder(RegisterAndRecognizeActivity2.this)
// .asCustom(new RecordsPopDialog(RegisterAndRecognizeActivity2.this))
// .show();
//
// }
// });
// mListAdapter = new CheckLogListAdapter(this, new ArrayList<>(), R.layout.list_check_log_item);
// binding.rvList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
// binding.rvList.setAdapter(mListAdapter);
// getAllRecords();
// }
//
// private void initData() {
// String livenessTypeStr = ConfigUtil.getLivenessDetectType(this);
// if (livenessTypeStr.equals((getString(R.string.value_liveness_type_rgb)))) {
// livenessType = LivenessType.RGB;
// } else if (livenessTypeStr.equals(getString(R.string.value_liveness_type_ir))) {
// livenessType = LivenessType.IR;
// } else {
// livenessType = null;
// }
// enableLivenessDetect =
// !ConfigUtil.getLivenessDetectType(this).equals(getString(R.string.value_liveness_type_disable));
// }
//
// private void initViewModel() {
// recognizeViewModel = new ViewModelProvider(getViewModelStore(),
// new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(RecognizeViewModel.class);
//
// recognizeViewModel.setLiveType(livenessType);
//
// recognizeViewModel.getFtInitCode().observe(this, ftInitCode -> {
// if (ftInitCode != ErrorInfo.MOK) {
// String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode,
// ErrorCodeUtil.arcFaceErrorCodeToFieldName(ftInitCode));
// ALog.i("initEngine: " + error);
// showToast(error);
// }
// });
// recognizeViewModel.getFrInitCode().observe(this, frInitCode -> {
// if (frInitCode != ErrorInfo.MOK) {
// String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode,
// ErrorCodeUtil.arcFaceErrorCodeToFieldName(frInitCode));
// ALog.i("initEngine: " + error);
// showToast(error);
// }
// });
// recognizeViewModel.getFlInitCode().observe(this, flInitCode -> {
// if (flInitCode != ErrorInfo.MOK) {
// String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode,
// ErrorCodeUtil.arcFaceErrorCodeToFieldName(flInitCode));
// ALog.i("initEngine: " + error);
// showToast(error);
// }
// });
// recognizeViewModel.getFaceItemEventMutableLiveData().observe(this, faceItemEvent -> {
// RecyclerView.Adapter adapter = binding.dualCameraRecyclerViewPerson.getAdapter();
// switch (faceItemEvent.getEventType()) {
// case REMOVED:
// if (adapter != null) {
// adapter.notifyItemRemoved(faceItemEvent.getIndex());
// }
// break;
// case INSERTED:
// if (adapter != null) {
// adapter.notifyItemInserted(faceItemEvent.getIndex());
// }
// break;
// default:
// break;
// }
// });
//
// recognizeViewModel.getRecognizeConfiguration().observe(this, recognizeConfiguration -> {
// ALog.i("initViewModel recognizeConfiguration: " + recognizeConfiguration.toString());
// });
//
// recognizeViewModel.setOnRegisterFinishedCallback(
// (facePreviewInfo, success) -> showToast(success ? "register success" : "register failed"));
//
// recognizeViewModel.getRecognizeNotice().observe(this, notice -> binding.setRecognizeNotice(notice));
//
// recognizeViewModel.getDrawRectInfoText().observe(this, drawRectInfoText -> {
// binding.setDrawRectInfoText(drawRectInfoText);
// });
// }
//
// private void initView() {
// if (!DualCameraHelper.hasDualCamera() || livenessType != LivenessType.IR) {
// binding.flRecognizeIr.setVisibility(View.GONE);
// }
// // 在布局结束后才做初始化操作
// binding.dualCameraTexturePreviewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
// binding.setCompareResultList(recognizeViewModel.getCompareResultList().getValue());
// }
//
// void initFragment() {
// fragment11 = new Document11();
// fragment2 = new Document2();
// fragment3 = new Document3();
// FragmentManager fragmentManager = getSupportFragmentManager();
// FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
// fragmentTransaction.add(R.id.fragment_all, fragment11);
// fragmentTransaction.commit();
// }
//
// public void toggleFragment(LongTermPass longTermPass) {
// Fragment fragment2 = new Document2();
// // 根据idCode去本地加载图片
// // String idCode = longTermPass.idCode;
// // Bitmap bitmap = ImageUtils.loadBitmapFromPath(Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode + ".jpg");
// FragmentManager fragmentManager = getSupportFragmentManager();
// FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
// Bundle bundle = new Bundle();
// bundle.putString("idCode", longTermPass.idCode);
// bundle.putString("nickname", longTermPass.nickname);
// bundle.putString("companyName", longTermPass.companyName);
// bundle.putString("expiryDate", longTermPass.expiryDate);
// bundle.putString("templateType", String.valueOf(longTermPass.templateType));
// bundle.putString("status", String.valueOf(longTermPass.status));
// String areaDisplay = new String();
// for (String s : longTermPass.areaDisplayCode) {
// areaDisplay += s + "\n";
// }
// bundle.putString("areaDisplayCode", areaDisplay);
// fragment2.setArguments(bundle);
// fragmentTransaction.replace(R.id.fragment_all, fragment2);
// fragmentTransaction.commit();
// hiddenCard();
// }
//
// public void toggleFragment2() {
// Fragment fragment2 = new Document2();
// // 根据idCode去本地加载图片
// // String idCode = longTermPass.idCode;
// // Bitmap bitmap = ImageUtils.loadBitmapFromPath(Constants.DEFAULT_REGISTER_FACES_DIR + "/" + idCode + ".jpg");
// FragmentManager fragmentManager = getSupportFragmentManager();
// FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
// fragmentTransaction.replace(R.id.fragment_all, fragment2);
// fragmentTransaction.commit();
// hiddenCard();
// }
//
// private void switchFragment3(LongTermPass longTermPass) {
// Fragment fragment3 = new Document3();
// FragmentManager fragmentManager = getSupportFragmentManager();
// FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
// String areaDisplay = new String();
// for (String s : longTermPass.areaDisplayCode) {
// areaDisplay = areaDisplay + s + " ";
// }
// Bundle bundle = new Bundle();
// bundle.putString("leadingPeople", longTermPass.leadingPeople);
// bundle.putString("idCode", longTermPass.idCode);
// bundle.putString("nickname", longTermPass.nickname);
// bundle.putString("areaDisplayCode", areaDisplay);
// bundle.putString("companyName", longTermPass.companyName.toString());
// bundle.putString("startDate", longTermPass.startDate.toString());// 开始时间
// bundle.putString("expiryDate", longTermPass.expiryDate.toString());// 结束时间
// bundle.putString("templateType", String.valueOf(longTermPass.templateType));
// bundle.putString("status", String.valueOf(longTermPass.status));
// fragment3.setArguments(bundle);
// fragmentTransaction.replace(R.id.fragment_all, fragment3);
// fragmentTransaction.commit();
// hiddenCard();
// }
//
// /**
// * 根据hiddenCard判断,如果为true则五分钟后隐藏
// */
// private void hiddenCard() {
// countdownHandler.removeMessages(2);
// countdownHandler.sendEmptyMessageDelayed(2, countdownTime);
//
// // // 开始五分钟计时
// // // 移除之前的任务
// // countdownHandler.removeCallbacks(countdownRunnable);
// //
// // // 创建新的倒计时任务
// // countdownRunnable = new Runnable() {
// // @Override
// // public void run() {
// // // 倒计时结束后的操作
// // runOnUiThread(new Runnable() {
// // @Override
// // public void run() {
// // // 其他需要执行的操作
// // FragmentManager fragmentManager = getSupportFragmentManager();
// // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
// // fragmentTransaction.replace(R.id.fragment_all, fragment1);
// // fragmentTransaction.commit();
// // iv_face.setVisibility(View.INVISIBLE);
// // }
// // });
// // }
// // };
//
// // // 延迟五分钟执行倒计时任务
// // countdownHandler.postDelayed(countdownRunnable, countdownTime);
// }
//
// @Override
// protected void onDestroy() {
// if (irCameraHelper != null) {
// irCameraHelper.release();
// irCameraHelper = null;
// }
//
// if (rgbCameraHelper != null) {
// rgbCameraHelper.release();
// rgbCameraHelper = null;
// }
//
// recognizeViewModel.destroy();
// switch (actionAfterFinish) {
// case NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY:
// navigateToNewPage(RecognizeDebugActivity.class);
// break;
// case NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY:
// navigateToNewPage(RecognizeSettingsActivity.class);
// break;
// default:
// break;
// }
// super.onDestroy();
// }
//
// public String getArea() {
// String areaDetail = infoStorage.getString("deviceAreaDetail", "");
// ALog.i("infoStorage.areaDetail: " + GsonUtils.toJson(areaDetail));
// // 解析 JSON 字符串为 Area 对象
// Area area = GsonUtils.fromJson(areaDetail, Area.class);
// String name = "";
// if (area != null) {
// name = area.getName();
// if (ObjectUtils.isNotEmpty(area.getChildren())) {
// for (Area firstChildren : area.getChildren()) {
// name = firstChildren.getName();
// if (ObjectUtils.isNotEmpty(firstChildren.getChildren())) {
// for (Area secondChildren : firstChildren.getChildren()) {
// name = secondChildren.getName();
// }
// }
// }
// }
// }
// return name;
// }
//
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
//
// /**
// * 调整View的宽高，使2个预览同时显示
// *
// * @param previewView 显示预览数据的view
// * @param faceRectView 画框的view
// * @param previewSize 预览大小
// * @param displayOrientation 相机旋转角度
// * @return 调整后的LayoutParams
// */
// // private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView
// // faceRectView, Camera.Size previewSize, int displayOrientation, float scale) {
// // ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
// // int measuredWidth = previewView.getMeasuredWidth();
// // int measuredHeight = previewView.getMeasuredHeight();
// // float ratio = ((float) previewSize.height) / (float) previewSize.width;
// // if (ratio > 1) {
// // ratio = 1 / ratio;
// // }
// // if (displayOrientation % 180 == 0) {
// // layoutParams.width = measuredWidth;
// // layoutParams.height = (int) (measuredWidth * ratio);
// // } else {
// // layoutParams.height = measuredHeight;
// // layoutParams.width = (int) (measuredHeight * ratio);
// // }
// // if (scale < 1f) {
// // ViewGroup.LayoutParams rgbParam = rgbPreview.getLayoutParams();
// // layoutParams.width = (int) (rgbParam.width * scale);
// // layoutParams.height = (int) (rgbParam.height * scale);
// // } else {
// // layoutParams.width *= scale;
// // layoutParams.height *= scale;
// // }
// //
// // DisplayMetrics metrics = new DisplayMetrics();
// // getWindowManager().getDefaultDisplay().getMetrics(metrics);
// //
// // if (layoutParams.width >= metrics.widthPixels) {
// // float viewRatio = layoutParams.width / ((float) metrics.widthPixels);
// // layoutParams.width /= viewRatio;
// // layoutParams.height /= viewRatio;
// // }
// // if (layoutParams.height >= metrics.heightPixels) {
// // float viewRatio = layoutParams.height / ((float) metrics.heightPixels);
// // layoutParams.width /= viewRatio;
// // layoutParams.height /= viewRatio;
// // }
// //
// // previewView.setLayoutParams(layoutParams);
// // faceRectView.setLayoutParams(layoutParams);
// // return layoutParams;
// // }
// private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView faceRectView,
// Camera.Size previewSize, int displayOrientation, float scale) {
// // 获取当前视图的布局参数
// ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
//
// // 获取屏幕的宽度和高度
// DisplayMetrics metrics = new DisplayMetrics();
// getWindowManager().getDefaultDisplay().getMetrics(metrics);
// int screenWidth = metrics.widthPixels;
// int screenHeight = metrics.heightPixels;
//
// // 将布局参数的宽度和高度设置为屏幕的宽度和高度
// layoutParams.width = screenWidth;
// layoutParams.height = screenHeight;
//
// // 应用调整后的布局参数
// previewView.setLayoutParams(layoutParams);
// faceRectView.setLayoutParams(layoutParams);
//
// return layoutParams;
// }
//
// private void initRgbCamera() {
// CameraListener cameraListener = new CameraListener() {
// @Override
// public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
// runOnUiThread(() -> {
// Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
// ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb,
// binding.dualCameraTexturePreviewRgb, binding.dualCameraFaceRectView, previewSizeRgb,
// displayOrientation, 1.0f);
// rgbFaceRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height,
// layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
// ConfigUtil.isDrawRgbRectHorizontalMirror(RegisterAndRecognizeActivity2.this),
// ConfigUtil.isDrawRgbRectVerticalMirror(RegisterAndRecognizeActivity2.this));
//
// FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewRgb.getParent());
// if (textViewRgb == null) {
// textViewRgb = new TextView(RegisterAndRecognizeActivity2.this, null);
// } else {
// parentView.removeView(textViewRgb);
// }
// textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
// ViewGroup.LayoutParams.WRAP_CONTENT));
// textViewRgb.setText(
// getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
// textViewRgb.setTextColor(Color.WHITE);
// textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
// parentView.addView(textViewRgb);
//
// // // 父View宽度和子View一致，保持居中
// // ViewGroup.LayoutParams parentLayoutParams = parentView.getLayoutParams();
// // parentLayoutParams.width = layoutParams.width;
// // parentView.setLayoutParams(parentLayoutParams);
//
// // 添加recognizeAreaView，在识别区域发生变更时，更新数据给FaceHelper
// if (ConfigUtil.isRecognizeAreaLimited(RegisterAndRecognizeActivity2.this)) {
// if (recognizeAreaView == null) {
// recognizeAreaView = new RecognizeAreaView(RegisterAndRecognizeActivity2.this);
// recognizeAreaView.setLayoutParams(new ViewGroup.LayoutParams(
// ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
// } else {
// parentView.removeView(recognizeAreaView);
// }
// recognizeAreaView.setOnRecognizeAreaChangedListener(
// recognizeArea -> recognizeViewModel.setRecognizeArea(recognizeArea));
// parentView.addView(recognizeAreaView);
// }
//
// recognizeViewModel.onRgbCameraOpened(camera);
// recognizeViewModel.setRgbFaceRectTransformer(rgbFaceRectTransformer);
// });
// }
//
// @Override
// public void onPreview(final byte[] nv21, Camera camera) {
// binding.dualCameraFaceRectView.clearFaceInfo();
// List<FacePreviewInfo> facePreviewInfoList = recognizeViewModel.onPreviewFrame(nv21, true);
// if (facePreviewInfoList != null && rgbFaceRectTransformer != null) {
// drawPreviewInfo(facePreviewInfoList);
// }
// recognizeViewModel.clearLeftFace(facePreviewInfoList);
// }
//
// @Override
// public void onCameraClosed() {
// ALog.i("onCameraClosed: ");
// }
//
// @Override
// public void onCameraError(Exception e) {
// ALog.i("onCameraError: " + e.getMessage());
// e.printStackTrace();
// }
//
// @Override
// public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
// ALog.i("onCameraConfigurationChanged:" + Thread.currentThread().getName());
// if (rgbFaceRectTransformer != null) {
// rgbFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
// }
// ALog.i("onCameraConfigurationChanged: " + cameraID + " " + displayOrientation);
// }
// };
//
// PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
// rgbCameraHelper = new DualCameraHelper.Builder()
// .previewViewSize(new Point(binding.dualCameraTexturePreviewRgb.getMeasuredWidth(),
// binding.dualCameraTexturePreviewRgb.getMeasuredHeight()))
// .rotation(getWindowManager().getDefaultDisplay().getRotation())
// .additionalRotation(previewConfig.getRgbAdditionalDisplayOrientation())
// .previewSize(recognizeViewModel.loadPreviewSize()).specificCameraId(previewConfig.getRgbCameraId())
// .isMirror(ConfigUtil.isDrawRgbPreviewHorizontalMirror(this))
// .previewOn(binding.dualCameraTexturePreviewRgb).cameraListener(cameraListener).build();
// rgbCameraHelper.init();
// rgbCameraHelper.start();
// }
//
// /**
// * 初始化红外相机，若活体检测类型是可见光活体检测或不启用活体，则不需要启用
// */
// private void initIrCamera() {
// if (livenessType == LivenessType.RGB || !enableLivenessDetect) {
// return;
// }
// CameraListener irCameraListener = new CameraListener() {
// @Override
// public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
// Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
// ViewGroup.LayoutParams layoutParams =
// adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb, binding.dualCameraTexturePreviewIr,
// binding.dualCameraFaceRectViewIr, previewSizeIr, displayOrientation, 0.25f);
//
// irFaceRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height,
// layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror,
// ConfigUtil.isDrawIrRectHorizontalMirror(RegisterAndRecognizeActivity2.this),
// ConfigUtil.isDrawIrRectVerticalMirror(RegisterAndRecognizeActivity2.this));
//
// FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewIr.getParent());
// if (textViewIr == null) {
// textViewIr = new TextView(RegisterAndRecognizeActivity2.this, null);
// } else {
// parentView.removeView(textViewIr);
// }
// textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
// ViewGroup.LayoutParams.WRAP_CONTENT));
// textViewIr
// .setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
// textViewIr.setTextColor(Color.WHITE);
// textViewIr.setBackgroundColor(getResources().getColor(R.color.color_bg_notification));
// parentView.addView(textViewIr);
//
// recognizeViewModel.onIrCameraOpened(camera);
// recognizeViewModel.setIrFaceRectTransformer(irFaceRectTransformer);
// }
//
// @Override
// public void onPreview(final byte[] nv21, Camera camera) {
// recognizeViewModel.refreshIrPreviewData(nv21);
// }
//
// @Override
// public void onCameraClosed() {
// ALog.i("onCameraClosed: ");
// }
//
// @Override
// public void onCameraError(Exception e) {
// ALog.i("onCameraError: " + e.getMessage());
// e.printStackTrace();
// }
//
// @Override
// public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
// if (irFaceRectTransformer != null) {
// irFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
// }
// ALog.i("onCameraConfigurationChanged: " + cameraID + " " + displayOrientation);
// }
// };
//
// PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
// irCameraHelper = new DualCameraHelper.Builder()
// .previewViewSize(new Point(binding.dualCameraTexturePreviewIr.getMeasuredWidth(),
// binding.dualCameraTexturePreviewIr.getMeasuredHeight()))
// .rotation(getWindowManager().getDefaultDisplay().getRotation())
// .specificCameraId(previewConfig.getIrCameraId()).previewOn(binding.dualCameraTexturePreviewIr)
// .cameraListener(irCameraListener).isMirror(ConfigUtil.isDrawIrPreviewHorizontalMirror(this))
// .previewSize(recognizeViewModel.loadPreviewSize()) // 相机预览大小设置，RGB与IR需使用相同大小
// .additionalRotation(previewConfig.getIrAdditionalDisplayOrientation()) // 额外旋转角度
// .build();
// irCameraHelper.init();
// try {
// irCameraHelper.start();
// } catch (RuntimeException e) {
// showToast(e.getMessage() + getString(R.string.camera_error_notice));
// }
// }
//
// /**
// * 绘制RGB、IR画面的实时人脸信息
// *
// * @param facePreviewInfoList RGB画面的实时人脸信息
// */
// private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
// if (rgbFaceRectTransformer != null) {
// List<FaceRectView.DrawInfo> rgbDrawInfoList =
// recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.RGB, openRectInfoDraw);
// binding.dualCameraFaceRectView.drawRealtimeFaceInfo(rgbDrawInfoList);
// }
// if (irFaceRectTransformer != null) {
// List<FaceRectView.DrawInfo> irDrawInfoList =
// recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.IR, openRectInfoDraw);
// binding.dualCameraFaceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
// }
// }
//
// @Override
// protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
// if (requestCode == ACTION_REQUEST_PERMISSIONS) {
// if (isAllGranted) {
// recognizeViewModel.init();
// initRgbCamera();
// if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
// initIrCamera();
// }
// } else {
// showToast(getString(R.string.permission_denied));
// }
// }
// }
//
// public void openRectInfoDraw(View view) {
// openRectInfoDraw = !openRectInfoDraw;
// recognizeViewModel.setDrawRectInfoTextValue(openRectInfoDraw);
// }
//
// /**
// * 将准备注册的状态置为待注册
// *
// * @param view 注册按钮
// */
// public void register(View view) {
// recognizeViewModel.prepareRegister();
// }
//
// /**
// * 参数配置
// *
// * @param view
// */
// public void setting(View view) {
// this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY;
// showLongToast(getString(R.string.please_wait));
// finish();
// }
//
// /**
// * 识别分析界面
// *
// * @param view 注册按钮
// */
// public void recognizeDebug(View view) {
// this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY;
// showLongToast(getString(R.string.please_wait));
// finish();
// }
//
// /**
// * 在{@link ActivityRegisterAndRecognizeBinding#dualCameraTexturePreviewRgb}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
// */
// @Override
// public void onGlobalLayout() {
// binding.dualCameraTexturePreviewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
// if (!checkPermissions(NEEDED_PERMISSIONS)) {
// ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
// } else {
// recognizeViewModel.init();
// initRgbCamera();
// if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
// initIrCamera();
// }
// }
// }
//
// @Override
// protected void onResume() {
// super.onResume();
// resumeCamera();
// }
//
// private void resumeCamera() {
// if (rgbCameraHelper != null) {
// rgbCameraHelper.start();
// }
// if (irCameraHelper != null) {
// irCameraHelper.start();
// }
// }
//
// @Override
// protected void onPause() {
// pauseCamera();
// super.onPause();
// }
//
// private void pauseCamera() {
// if (rgbCameraHelper != null) {
// rgbCameraHelper.stop();
// }
// if (irCameraHelper != null) {
// irCameraHelper.stop();
// }
// }
// }
