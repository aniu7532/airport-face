package com.arcsoft.arcfacedemo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.arcsoft.arcfacedemo.data.FaceRepository;
import com.arcsoft.arcfacedemo.data.http.HttpInitUtils;
import com.arcsoft.arcfacedemo.db.YinchuanAirportDB;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;
import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;
import com.arcsoft.arcfacedemo.entity.ApiResponse;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.entity.LongPassCards;
import com.arcsoft.arcfacedemo.facedb.FaceDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.ui.activity.LoginActivity;
import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
import com.arcsoft.arcfacedemo.ui.callback.OnRegisterFinishedCallback;
import com.arcsoft.arcfacedemo.util.Converters;
import com.arcsoft.arcfacedemo.util.DateUtil;
import com.arcsoft.arcfacedemo.util.DeviceUtils;
import com.arcsoft.arcfacedemo.util.DuplicateFaceCleanupUtils;
import com.arcsoft.arcfacedemo.util.ImageDeleter;
import com.arcsoft.arcfacedemo.util.ImageDownloader;
import com.arcsoft.arcfacedemo.util.ImageUploader;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.arcsoft.arcfacedemo.util.LogUploadUtils;
import com.arcsoft.arcfacedemo.util.LongPassCardsReInitUtils;
import com.arcsoft.arcfacedemo.util.LongPassCardsRemedialMeasuresUtils;
import com.arcsoft.arcfacedemo.util.SmallTask;
import com.arcsoft.arcfacedemo.util.debug.DebugInfoDumper;
import com.arcsoft.arcfacedemo.util.face.model.FacePreviewInfo;
import com.arcsoft.arcfacedemo.util.glide.AESUtils;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.lzy.okgo.request.PostRequest;
import com.tencent.bugly.crashreport.CrashReport;
import com.xuexiang.xupdate.XUpdate;
import com.ys.rkapi.MyManager;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Application;
import android.app.ZysjSystemManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Debug;

import androidx.lifecycle.MutableLiveData;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 应用全局入口类，负责初始化数据库、网络、人脸引擎、定时任务及日志上传等核心组件。
 */
public class ArcFaceApplication extends Application {

    private static ArcFaceApplication application;
    public static final String TAG = "YCJC";
    /** ALog 日志目录绝对路径 */
    private String wlyCacheDir;
    /** 测试模式开关，为 true 时缩短周期任务间隔 */
    public static boolean TEST = false;
    /** 读卡相关超时/间隔（毫秒），查验页引用 */
    public static int READ_TIME = 1000;

    /** 获取 ALog 日志目录 */
    public String getWlyCacheDir() {
        return wlyCacheDir;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;

        // // 配置 LeakCanary
        // LeakCanary.Config config =
        // LeakCanary.getConfig().newBuilder().onHeapAnalyzedListener(new RemoteLeakReporter()).build();
        // LeakCanary.setConfig(config);

        File logFile = this.getExternalFilesDir("log");
        if (!logFile.exists()) {
            logFile.mkdir();
        }
        wlyCacheDir = logFile.getAbsolutePath();
        XUpdate.get().init(this);

        initCrashDumper();
        Utils.init(this);
        // Toasty.Config.getInstance().allowQueue(false).setTextSize(24).apply();

        Toasty.Config.getInstance().tintIcon(true).setTextSize(24).allowQueue(false).apply();

        CrashReport.initCrashReport(this, "7db9a3ce0b", BuildConfig.DEBUG);
        HttpInitUtils.init(this);
        ALog.getConfig().setLogSwitch(true)// 设置 log 总开关，包括输出到控制台和文件，默认开
                .setConsoleSwitch(BuildConfig.DEBUG)// 设置是否输出到控制台开关，默认开
                .setGlobalTag(TAG) // 设置 log 全局标签，默认为空，当全局标签不为空时，我们输出的 log 全部为该 tag， 为空时，如果传入的 tag
                // 为空那就显示类名，否则显示 tag
                .setLogHeadSwitch(true) // 设置 log 头信息开关，默认为开
                .setLog2FileSwitch(true) // 打印 log 时是否存到文件的开关，默认关
                .setFilePrefix(TAG) // 当文件前缀为空时，默认为 "alog"，即写入文件为 "alog-MM-dd.txt"
                .setDir(getWlyCacheDir())// 当自定义路径为空时，写入应用的 /cache/log/ 目录中
                .setBorderSwitch(false) // 输出日志是否带边框开关，默认开
                .setSingleTagSwitch(true) // 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
                .setConsoleFilter(ALog.V) // log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
                .setFileFilter(ALog.D).setSaveDays(2); // log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
        // SharedPreferences工具类
        infoStorage = new InfoStorage(this);
        imageUploader = new ImageUploader();// 图片上传工具类

        infoStorage.remove("linshiID");

        // 保存当前时间到本地
        // infoStorage.saveString("startDate", "2025-04-07 00:00:00");

        // 本地仓库初始化
        File file = this.getExternalFilesDir("db");
        if (!file.exists()) {
            file.mkdir();
        }
        File customDbPath = new File(file.getAbsolutePath(), "airportDb.db");
        db = Room.databaseBuilder(getApplicationContext(), YinchuanAirportDB.class, customDbPath.getAbsolutePath())
                .setJournalMode(RoomDatabase.JournalMode.AUTOMATIC).fallbackToDestructiveMigration().build();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ALog.e("onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        ALog.e("onTrimMemory");
    }

    private void initCrashDumper() {
        // XCrash.InitParameters initParameters = new XCrash.InitParameters();
        File dir = new File(DebugInfoDumper.CRASH_LOG_DIR);
        if (dir.isFile()) {
            dir.delete();
        }
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void onTerminate() {
        application = null;
        super.onTerminate();
    }

    /**
     * 获取 Application 单例实例。
     */
    public static ArcFaceApplication getApplication() {
        return application;
    }

//    public static int UPLOAD_LOG_TIME = 10 * 60 * 1000;
    /** 通行记录批量上传定时间隔（毫秒），默认 30 秒 */
    public static int UPLOAD_LOG_TIME = 30 * 1000;
    /** 增量同步默认定时间隔（分钟），可被 InfoStorage.interval 覆盖 */
    public static int UPDATE_DELAY_TIME = 5;
    /** 网络 Ping 定时间隔（毫秒） */
    public static int PING_DELAY_TIME = 10 * 1000;
    /** ThreadUtils 固定线程池大小 */
    public static int POOL_SIZE = 15;
    private ImageUploader imageUploader;
    private SmallTask task;

    /** 防止上传任务重入，同一时刻仅允许一个上传批次 */
    private final AtomicBoolean isUploadingRecord = new AtomicBoolean(false);


    /**
     * 启动定时上传任务，周期性将本地通行记录、日志等数据同步至服务端。
     */
    public void startUpDataToServer() {
        // if (ArcFaceApplication.TEST) {
        // return;
        // }
        task = new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {

                if (!isUploadingRecord.compareAndSet(false, true)) {
                    return "";
                }

                try {
                    ALog.d("SimpleTask startGetDataFromServer ");
                    // List<LongTermPass> list1 = db.longTermPassDao().getAll();
                    int count = db.longTermPassDao().getCount();
                    ALog.e("通行证数量 count:" + count);

                    if (faceRepository != null) {
                        ALog.e("faceRepository.getTotalFaceCount():" + faceRepository.getTotalFaceCount());
                    }

                    List<LongTermRecords> list2 = db.longTermRecordsDao().getAll();
                    if (ObjectUtils.isNotEmpty(list2)) {
                        ALog.e("list2.size():" + list2.size());
                        for (LongTermRecords item : list2) {
                            ALog.e(item.toString());
                            if (ObjectUtils.isNotEmpty(item.sitePhoto)
                                    && (item.sitePhoto.startsWith("/") || item.sitePhoto.startsWith("storage/"))) {
                                // Bitmap bitmap = ImageUtils.getBitmap(item.sitePhoto);
                                Bitmap bitmap = AESUtils.decryptFileToBitmap(item.sitePhoto);
                                if (bitmap == null) {
                                    ALog.d("bitmap == null");
                                    item.sitePhoto = "";
                                } else {
                                    // 上传通行图片到服务器
                                    String imgUrl = imageUploader.uploadBitmap2(bitmap);
                                    ALog.i("上传图片路径: " + imgUrl);
                                    if (ObjectUtils.isEmpty(imgUrl)) {
                                        continue;
                                    }
                                    FileUtils.delete(item.sitePhoto);
                                    item.sitePhoto = imgUrl;
                                }
                            }

                            // String oldId = item.id;
                            // item.id = SnowflakeIdUtil.getInstance().nextId() + "";
                            PostRequest<String> request = OkGo.<String> post(UrlConstants.URL_CREATE_LONG_RECORD)
                                    .tag(UrlConstants.URL_CREATE_LONG_RECORD);
                            request.headers("tenant-id", UrlConstants.TENANT_ID);
                            // 检查是否有 accessToken，如果有则添加 Authorization 头
                            if (ApiUtils.accessToken != null) {
                                request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
                            }
                            request.upJson(GsonUtils.toJson(item));
                            // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
                            Call<String> call = request.converter(new StringConvert()).adapt();
                            try {
                                Response<String> res = call.execute();
                                if (res.code() == 200) {
                                    ALog.d("上传长期证件成功返回");
                                    // item.id = oldId;
                                    db.longTermRecordsDao().delete(item);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ALog.e("上传长期证件日志失败返回: " + e.getMessage());
                            }
                        }
                    }

                    List<TemporaryCardRecords> list3 = db.temporaryCardRecordsDao().getAll();
                    if (ObjectUtils.isNotEmpty(list3)) {
                        ALog.e("list3.size():" + list3.size());
                        for (TemporaryCardRecords item : list3) {
                            ALog.e(item.toString());
                            if (ObjectUtils.isNotEmpty(item.sitePhoto)
                                    && (item.sitePhoto.startsWith("/") || item.sitePhoto.startsWith("storage/"))) {
                                // Bitmap bitmap = ImageUtils.getBitmap(item.sitePhoto);
                                Bitmap bitmap = AESUtils.decryptFileToBitmap(item.sitePhoto);
                                if (bitmap == null) {
                                    ALog.d("bitmap == null");
                                    item.sitePhoto = "";
                                } else {
                                    // 上传通行图片到服务器
                                    String imgUrl = imageUploader.uploadBitmap2(bitmap);
                                    ALog.i("上传图片路径: " + imgUrl);
                                    if (ObjectUtils.isEmpty(imgUrl)) {
                                        continue;
                                    }
                                    FileUtils.delete(item.sitePhoto);
                                    item.sitePhoto = imgUrl;
                                }

                            }

                            PostRequest<String> request = OkGo.<String> post(UrlConstants.URL_CREATE_TEMP_RECORD)
                                    .tag(UrlConstants.URL_CREATE_TEMP_RECORD);
                            request.headers("tenant-id", UrlConstants.TENANT_ID);
                            // 检查是否有 accessToken，如果有则添加 Authorization 头
                            if (ApiUtils.accessToken != null) {
                                request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
                            }
                            request.upJson(GsonUtils.toJson(item));
                            // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
                            Call<String> call = request.converter(new StringConvert()).adapt();
                            try {
                                Response<String> res = call.execute();
                                if (res.code() == 200) {
                                    ALog.d("上传临时证件日志成功返回");
                                    db.temporaryCardRecordsDao().delete(item);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ALog.e("上传临时证件日志失败返回: " + e.getMessage());
                            }
                        }
                    }

                    // final SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd_HH", Locale.getDefault());
                    try {
                        long max = 3 * 86400000L;
                        long cur = TimeUtils.getNowMills();
                        File directory = new File(getExternalFilesDir(null), "records");
                        if (!directory.exists()) {
                            directory.mkdirs();
                        }
                        List<File> list = FileUtils.listFilesInDir(directory.getAbsolutePath());
                        if (ObjectUtils.isNotEmpty(list)) {
                            for (int i = 0; i < list.size(); i++) {
                                long temp = cur - list.get(i).lastModified();
                                ALog.e("temp:" + temp);
                                if (temp > max) {
                                    boolean delete = list.get(i).delete();
                                    ALog.e(list.get(i).getAbsolutePath() + "，delete:" + delete);
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return "";
                } finally {
                    isUploadingRecord.set(false);
                }
            }
        };
        ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task, UPLOAD_LOG_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 重启定时上传任务（先取消当前任务再重新启动）。
     */
    public void reset() {
        if (task != null) {
            ThreadUtils.cancel(task);
            task.cancel();
        }
        startUpDataToServer();
    }

    /**
     * 停止所有定时上传任务。
     */
    public void resetAll() {
        if (task != null) {
            ThreadUtils.cancel(task);
            task.cancel();
        }

    }

    // 在 Activity 中定时监测 CPU
    private CpuMonitor cpuMonitor = new CpuMonitor();
    /** 增量同步分页大小 */
    private static int UPDATE_PAGE_SIZE = 20;
    private static int updatePage = 1;
    /** 增量同步是否允许发起下一页请求 */
    private static boolean updateNext = true;
    /** 设备/登录配置持久化 */
    InfoStorage infoStorage;

    /** 获取业务 Room 数据库（通行证、通行记录） */
    public YinchuanAirportDB getDb() {
        return db;
    }

    /** 替换业务库实例（测试或重建场景） */
    public void setDb(YinchuanAirportDB db) {
        this.db = db;
    }

    /** 是否离线（Ping 失败时为 true） */
    boolean isOffLine;
    private YinchuanAirportDB db;
    private FaceRepository faceRepository;
    private static final int PAGE_SIZE = 20;
    private FaceDao faceDao;
    private MutableLiveData<List<FaceEntity>> faceEntityList = new MutableLiveData<>();

    /** 人脸库总数缓存 */
    private int faceCount = -1;

    private MutableLiveData<Integer> totalFaceCount = new MutableLiveData<>();

    /** @return 当前是否处于离线状态 */
    public boolean isOffLine() {
        return isOffLine;
    }

    public MutableLiveData<List<FaceEntity>> getFaceEntityList() {
        return faceEntityList;
    }

    public MutableLiveData<Integer> getTotalFaceCount() {
        return totalFaceCount;
    }

    /**
     * 每隔一分钟获取通行证，更新到本地数据库.
     */
    public void startPeriodicTask() {
        if (!ArcFaceApplication.TEST) {
            faceDao = FaceDatabase.getInstance(getApplication()).faceDao();
            FaceServer instance = FaceServer.getInstance();
            if (instance.getFaceEngine() == null) {
                ALog.e("FaceServer.getFaceEngine() == null");
                instance.init(getApplication(), new FaceServer.OnInitFinishedCallback() {
                    @Override
                    public void onFinished(int faceCount) {
                        ALog.e("faceCount:" + faceCount);
                        // initFinished.postValue(true);
                    }
                });
            }
            if (faceRepository == null) {
                faceRepository = new FaceRepository(PAGE_SIZE, faceDao, instance);
            }
        }
        int interval = infoStorage.getInt("interval", UPDATE_DELAY_TIME);
        SmallTask task = new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                if (DateUtil.getHour(TimeUtils.getNowDate()) == 2) {
                    // 2点重启，并设置重启标致为false，防止继续重启
                    boolean flag = SPUtils.getInstance().getBoolean("reboot", true);
                    if (flag) {
                        int screenSize = ScreenUtils.getScreenWidth();
                        int typeDevice = screenSize > 800 ? 1 : 2;
                        ALog.d("获取屏幕尺寸宽度:" + screenSize);
                        SPUtils.getInstance().put("reboot", false);
                        if (typeDevice == 1) {
                            // 声明manager对象
                            @SuppressLint("WrongConstant")
                            ZysjSystemManager manager = (ZysjSystemManager) getSystemService("zysj");
                            int result = manager.zYRebootSys();
                            ALog.e("zYRebootSys result:" + result);
                        } else {
                            MyManager manager = MyManager.getInstance(getApplication());
                            manager.reboot();
                        }
                        return null;
                    }
                } else {
                    // 2点过后，设置重启标致为true，到2点时自动重启
                    SPUtils.getInstance().put("reboot", true);
                }

                if (DateUtil.getHour(TimeUtils.getNowDate()) == 10) {
                    // 10点上传日志，并设置上传标致为false，防止继续上传
                    boolean flag = SPUtils.getInstance().getBoolean("upload_log", true);
                    if (flag) {
                        LogUploadUtils.upload(getApplication());
                        SPUtils.getInstance().put("upload_log", false);
                    }
                } else {
                    // 10点过后，设置上传标致为true，到10点时自动上传
                    SPUtils.getInstance().put("upload_log", true);
                }

                // 每天凌晨1点执行数据完整性检查
                if (DateUtil.getHour(TimeUtils.getNowDate()) == 1) {
                    boolean flag = SPUtils.getInstance().getBoolean("reinit_check", true);
                    if (flag) {
                        ALog.d("凌晨1点，开始执行数据完整性检查");
                        LongPassCardsReInitUtils.getInstance().start();
                        SPUtils.getInstance().put("reinit_check", false);
                    }
                } else {
                    // 1点过后，设置检查标致为true，到1点时自动检查
                    SPUtils.getInstance().put("reinit_check", true);
                }

                // 执行任务逻辑
                ALog.d("更新通行证任务执行中...");
                updatePage = 1;

                GetRequest<String> request = OkGo.<String> get(UrlConstants.heartbeat).tag(UrlConstants.heartbeat);
                request.headers("tenant-id", UrlConstants.TENANT_ID);
                // 检查是否有 accessToken，如果有则添加 Authorization 头
                if (ApiUtils.accessToken != null) {
                    request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
                }
                request.params("mac", DeviceUtils.getDeviceId(ArcFaceApplication.getApplication()))
                        .params("interval", interval).execute(new StringCallback() {
                            @Override
                            public void onSuccess(Response<String> response) {
                                if (response.code() == 200) {
                                    ALog.d("heartbeat成功返回");
                                }
                            }

                            @Override
                            public void onError(Response<String> response) {
                                super.onError(response);
                                ALog.e("heartbeat失败返回: " + response.getException().getMessage());
                            }
                        });

                getLongPassCardsUpdate();
                getJavaHeapUsage();
                getMemoryUsage();
                double cpuUsage = cpuMonitor.getCpuUsage();
                ALog.i("App CPU Usage: " + String.format("%.1f%%", cpuUsage));

                return null;
            }
        };

        if (TEST) {
            ThreadUtils.executeByCachedWithDelay(task, 30 * 1000, TimeUnit.MILLISECONDS);
        } else {
            ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task, interval * 60 * 1000, TimeUnit.MILLISECONDS);
        }

        SmallTask task1 = new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                ALog.d("Ping  开始");
                boolean result = NetworkUtils.isAvailableByPing();
                if (result) {
                    isOffLine = false;
                    ALog.d("Ping  成功");
                } else {
                    isOffLine = true;
                    ALog.d("Ping  失败");
                }
                return null;
            }
        };
        ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task1, PING_DELAY_TIME, TimeUnit.MILLISECONDS);
    }

    /**
     * 分页拉取增量通行证（startPeriodicTask 内调用）。
     * 以本地 maxUpdateTime 为 startDate，逐页 GET {@link UrlConstants#URL_GetLongPass}。
     */
    private void getLongPassCardsUpdate() {
		if (!updateNext) {
			return;
		}
		updateNext = false;
        // 获取开始时间，默认是2025年1月24日
        // String startDate = infoStorage.getString("startDate", "2025-01-24 17:20:00");

        String startDate = db.longTermPassDao().getMaxUpdateTime();
        ALog.e("通行证数量 getMaxUpdateTime:" + startDate);

        if (ObjectUtils.isEmpty(startDate)) {
            startDate = "2025-06-11 10:56:00";
        }
        if (TEST) {
            startDate = "2025-06-11 10:56:00";
        }

        Map<String, String> params = new HashMap<>();
        params.put("pageNo", String.valueOf(updatePage));
        params.put("pageSize", String.valueOf(UPDATE_PAGE_SIZE));
        params.put("startDate", startDate);
        params.put("endDate", DeviceUtils.getCurrentTime());
        fetchNextPage(params);
    }

    /** 同步请求单页增量通行证，下载/删除图片后合并入库并注册人脸 */
    private synchronized void fetchNextPage(Map<String, String> params) {
        List<LongPassCard> longPassCardList = new ArrayList<>();
        GetRequest<String> request = OkGo.<String> get(UrlConstants.URL_GetLongPass).tag(UrlConstants.URL_GetLongPass);
        if (params != null) {
            // 更新或添加 timestamp 参数
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
            }
        }
        request.headers("tenant-id", UrlConstants.TENANT_ID);
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }

        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        Call<String> call = request.converter(new StringConvert()).adapt();

		boolean needFetchNext = false;

        try {
            Response<String> res = call.execute();
            if (res.code() == 200) {
                ApiResponse<LongPassCards> resResponse =
                        GsonUtils.fromJson(res.body(), new TypeToken<ApiResponse<LongPassCards>>() {
                        }.getType());
                ALog.i("入参: " + GsonUtils.toJson(params));
                ALog.d("更新通信证接口数据response: " + GsonUtils.toJson(resResponse));
                if (resResponse.getCode() == 200) {
                    LongPassCards longPassCards = resResponse.getData();
                    if (longPassCards != null && longPassCards.getList() != null
                            && !longPassCards.getList().isEmpty()) {
                        if (!ArcFaceApplication.TEST) {
                            List<Map<String, String>> downloadFailures = new ArrayList<>();
                            for (LongPassCard longPassCard : longPassCards.list) {
								// 如果是注销的 需要删除之前的缓存
								if (longPassCard.status == 2) {
									ALog.e("正在删除：" + longPassCard.nickname + "，第" + updatePage + "页");
									File directory1 = new File(getApplication().getExternalFilesDir(null), "register");// 应用的私有目录
									boolean result = ImageDeleter.deleteImage(directory1, longPassCard.checkPhoto,
										longPassCard.id, longPassCard.nickname);
									if (!result) {
										ALog.e("删除失敗 checkPhoto：" + longPassCard.nickname + "，第" + updatePage + "页");
									}
									File directory2 = new File(getApplication().getExternalFilesDir(null), "photo");// 应用的私有目录
									result = ImageDeleter.deleteImage(directory2, longPassCard.photo, longPassCard.id,
										longPassCard.nickname);
									if (!result) {
										ALog.e("删除失敗 photo：" + longPassCard.nickname + "，第" + updatePage + "页");
									}
								} else {
									ALog.e("正在下载：" + longPassCard.nickname + "，第" + updatePage + "页");
									File directory1 = new File(getApplication().getExternalFilesDir(null), "register");// 应用的私有目录
									if (!directory1.exists()) {
										directory1.mkdirs();
									}
									boolean result = ImageDownloader.downloadImage(directory1, longPassCard.checkPhoto,
										longPassCard.id, longPassCard.nickname, false);
									if (!result) {
										ALog.e("下载失敗 checkPhoto：" + longPassCard.nickname + "，第" + updatePage + "页");
                                        downloadFailures.add(LongPassCardsRemedialMeasuresUtils
                                                .buildFailedContent(longPassCard.id, "checkPhoto下载失败"));
									}
									File directory2 = new File(getApplication().getExternalFilesDir(null), "photo");// 应用的私有目录
									if (!directory2.exists()) {
										directory2.mkdirs();
									}
									result = ImageDownloader.downloadImage(directory2, longPassCard.photo, longPassCard.id,
										longPassCard.nickname, true);
									if (!result) {
										ALog.e("下载失敗 photo：" + longPassCard.nickname + "，第" + updatePage + "页");
                                        downloadFailures.add(LongPassCardsRemedialMeasuresUtils
                                                .buildFailedContent(longPassCard.id, "photo下载失败"));
									}
								}
                            }
                            LongPassCardsRemedialMeasuresUtils.reportCheckAbnormal(downloadFailures);
                        }
						needFetchNext = true;
                        longPassCardList.addAll(longPassCards.getList());
                    } else {
                        ALog.i("更新通行证数据为空，当前页码: " + updatePage);
                    }
                } else if (resResponse.getCode() == 401) {
                    ALog.d("更新通行证线程接口401: " + resResponse.getMsg());
                    ALog.e("Intent intent = new Intent(getInstance(), LoginActivity.class)");
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    intent.putExtra("auto", true);
                    ActivityUtils.startActivity(intent);
					updateNext = true;
                    return;
                } else {
                    ALog.d("更新通行证线程接口非200: " + resResponse.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("更新通信证接口数据失败", e);
        }
        if (!ArcFaceApplication.TEST) {
            handleUpdateComplete(longPassCardList);
        }
		if (needFetchNext) {
			// 等待5分钟后再获取下一页
			ALog.d("等待5分钟后获取下一页数据...");
            int interval = infoStorage.getInt("interval", UPDATE_DELAY_TIME);
			ThreadUtils.executeByCachedWithDelay(new SmallTask() {
				@Override
				public String doInBackground() throws Throwable {
					String pageNo = params.get("pageNo");
					int pageNumInt = Integer.parseInt(pageNo);
					params.put("pageNo", String.valueOf(pageNumInt + 1));
					fetchNextPage(params);
					return null;
				}
			}, interval * 60 * 1000, TimeUnit.MILLISECONDS); // 延迟5分钟执行
		} else {
			updateNext = true;
		}
    }

    /** 单页增量同步完成后：入库并触发人脸更新 */
    private void handleUpdateComplete(List<LongPassCard> longPassCardList) {
        if (longPassCardList.size() > 0) {
            updateLocalDatabase(longPassCardList);
            ALog.i("更新到数据库的数据: " + GsonUtils.toJson(longPassCardList));
        }
        updatePage = 1; // 只在成功更新数据库后重置
    }

    /**
     * 更新本地数据库
     *
     * @param longPassCardList
     */
    private void updateLocalDatabase(List<LongPassCard> longPassCardList) {
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                List<LongTermPass> longTermPassList = new ArrayList<>();
                for (LongPassCard item : longPassCardList) {
                    // ALog.d("接口数据longPassCard: "+gson.toJson(longPassCard));
                    LongTermPass longTermPass = Converters.convertToLongTermPass(item);
                    longTermPassList.add(longTermPass);
                }
                db.longTermPassDao().insertOrUpdateUsers(longTermPassList);

                // // 保存当前时间到本地
                // infoStorage.saveString("startDate", DeviceUtils.getCurrentTime());
                ALog.i("更新本地数据库成功");
                updateFace(longPassCardList);
                return null;
            }
        });
    }

    /**
     * 更新人脸数据库,先删除，再注册
     */
    public void updateFace(List<LongPassCard> longPassCardList) {

        if (longPassCardList.isEmpty()) { return; }

        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {

                for (LongPassCard longPassCard : longPassCardList) {
                    DuplicateFaceCleanupUtils.getInstance().prepareRegisterFace(longPassCard.id);
                    Bitmap bitmap = AESUtils.decryptRegisterFileToBitmap(longPassCard.id);
                    // 获取图片
                    // Bitmap bitmap = ImageDownloader.loadAndDecryptImage(longPassCard.id, getInstance());
                    // 注册人脸
                    registerFaceByBitmap(bitmap, longPassCard.id);
                }

                return null;
            }
        });
    }

    /**
     * 单个注册人脸
     */
    public void registerFaceByBitmap(Bitmap bitmap, String applyId) {
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {

                registerFace(bitmap, new OnRegisterFinishedCallback() {
                    @Override
                    public void onRegisterFinished(FacePreviewInfo facePreviewInfo, boolean success) {
                        ALog.i("单个注册人脸: " + success);
                    }
                }, applyId);
                return null;
            }
        });
    }

    /**
     * 将 Bitmap 注册到人脸库。
     */
    public void registerFace(Bitmap bitmap, OnRegisterFinishedCallback callback, String applyId) {
        Bitmap alignedBitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        ALog.e("alignedBitmap.getWidth():" + alignedBitmap.getWidth() + ",alignedBitmap.getHeight():"
                + alignedBitmap.getHeight());
        if (FaceServer.getInstance().getFaceEngine() == null) {
            ALog.e("FaceServer.getInstance().getFaceEngine() == null");
            return;
        }
        Observable.create(new ObservableOnSubscribe<byte[]>() {
            @Override
            public void subscribe(ObservableEmitter<byte[]> emitter) throws Exception {
                byte[] bgr24Data = ArcSoftImageUtil.createImageData(alignedBitmap.getWidth(), alignedBitmap.getHeight(),
                        ArcSoftImageFormat.BGR24);
                int transformCode =
                        ArcSoftImageUtil.bitmapToImageData(alignedBitmap, bgr24Data, ArcSoftImageFormat.BGR24);
                if (transformCode == ArcSoftImageUtilError.CODE_SUCCESS) {
                    emitter.onNext(bgr24Data);
                } else {
                    emitter.onError(new Exception("transform failed, code is " + transformCode));
                }
            }
        }).flatMap(new Function<byte[], ObservableSource<FaceEntity>>() {

            @Override
            public ObservableSource<FaceEntity> apply(byte[] bgr24Data) throws Exception {
                Observable<FaceEntity> faceEntityObservable;
                if (ActivityUtils.getTopActivity() instanceof RegisterAndRecognizeActivity) {
                    ALog.e("ActivityUtils.getTopActivity() instanceof RegisterAndRecognizeActivity");
                    faceEntityObservable =
                            Observable.just(faceRepository.registerBgr24(ArcFaceApplication.getApplication(), bgr24Data,
                                    alignedBitmap.getWidth(), alignedBitmap.getHeight(), applyId, true));

                } else {
                    faceEntityObservable =
                            Observable.just(faceRepository.registerBgr24(ArcFaceApplication.getApplication(), bgr24Data,
                                    alignedBitmap.getWidth(), alignedBitmap.getHeight(), applyId));
                }

                loadData(true);
                // 注册成功时，数据也同步更新下
                return faceEntityObservable;
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<FaceEntity>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(FaceEntity faceEntity) {
                if (faceEntity != null) {
                    callback.onRegisterFinished(null, true);
                } else {
                    callback.onRegisterFinished(null, false);
                }
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                callback.onRegisterFinished(null, false);
            }

            @Override
            public void onComplete() {

            }
        });
    }

    /**
     * 加载数据
     *
     * @param reload true：重新加载 ， false：分页加载
     */
    public synchronized void loadData(boolean reload) {
        if (faceCount == -1 || reload) {
            faceCount = faceRepository.getTotalFaceCount();
            totalFaceCount.postValue(faceCount);
        }
        List<FaceEntity> faceEntityList = getFaceEntityList().getValue();
        if (faceEntityList == null) {
            faceEntityList = new LinkedList<>();
        }
        List<FaceEntity> faceEntities = reload ? faceRepository.reload() : faceRepository.loadMore();
        if (reload) {
            faceEntityList.clear();
        }
        faceEntityList.addAll(faceEntities);
        getFaceEntityList().postValue(faceEntityList);
    }

    public boolean isValid() {
        return true;
    }

    // 获取当前进程的内存信息
    private void getMemoryUsage() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);

        // 系统总内存（单位：字节）
        long totalMemory = memoryInfo.totalMem;
        // 系统可用内存
        long availableMemory = memoryInfo.availMem;

        // 获取当前进程的内存占用（单位：KB）
        int pid = android.os.Process.myPid();
        Debug.MemoryInfo[] memoryInfoArray = activityManager.getProcessMemoryInfo(new int[] { pid });
        Debug.MemoryInfo processMemoryInfo = memoryInfoArray[0];
        long usedMemory = processMemoryInfo.getTotalPss() * 1024; // 转换为字节

        ALog.i("Total Memory: " + formatSize(totalMemory));
        ALog.i("Available Memory: " + formatSize(availableMemory));
        ALog.i("App Used Memory: " + formatSize(usedMemory));
    }

    // 格式化内存大小（字节转 MB）
    private String formatSize(long size) {
        return String.format("%.2f MB", size / (1024.0 * 1024.0));
    }

    // 获取当前应用的 Java 堆内存信息
    private void getJavaHeapUsage() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory(); // 堆最大可分配内存
        long totalMemory = runtime.totalMemory(); // 当前已分配的堆内存
        long freeMemory = runtime.freeMemory(); // 剩余可用堆内存
        long usedMemory = totalMemory - freeMemory; // 实际已用堆内存

        ALog.i("Java Heap Max: " + formatSize(maxMemory));
        ALog.i("Java Heap Used: " + formatSize(usedMemory));
    }

    // 工具类：获取 CPU 使用率
    public class CpuMonitor {
        private long lastSystemTime = 0;
        private long lastProcessTime = 0;

        public double getCpuUsage() {
            if (lastSystemTime == 0) {
                // 首次调用，初始化数据
                lastSystemTime = System.currentTimeMillis();
                lastProcessTime = getProcessCpuTime();
                return 0;
            }

            long currentSystemTime = System.currentTimeMillis();
            long currentProcessTime = getProcessCpuTime();

            long systemDelta = currentSystemTime - lastSystemTime;
            long processDelta = currentProcessTime - lastProcessTime;

            if (systemDelta == 0)
                return 0;

            // 计算 CPU 使用率（百分比）
            double cpuUsage = (double) processDelta / systemDelta * 100;
            cpuUsage = Math.min(100, cpuUsage); // 确保不超过 100%

            lastSystemTime = currentSystemTime;
            lastProcessTime = currentProcessTime;

            return cpuUsage;
        }

        // 获取进程的 CPU 时间（单位：毫秒）
        private long getProcessCpuTime() {
            try {
                BufferedReader reader =
                        new BufferedReader(new FileReader("/proc/" + android.os.Process.myPid() + "/stat"));
                String line = reader.readLine();
                String[] tokens = line.split(" ");
                long utime = Long.parseLong(tokens[13]); // 用户态时间
                long stime = Long.parseLong(tokens[14]); // 内核态时间
                return utime + stime;
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
        }
    }
}
