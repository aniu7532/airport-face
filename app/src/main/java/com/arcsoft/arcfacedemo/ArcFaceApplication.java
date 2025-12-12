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
import com.arcsoft.arcfacedemo.util.ImageDownloader;
import com.arcsoft.arcfacedemo.util.ImageUploader;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.arcsoft.arcfacedemo.util.LogUploadUtils;
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

public class ArcFaceApplication extends Application {
    // public class RemoteLeakReporter implements OnHeapAnalyzedListener {
    // @Override
    // public void onHeapAnalyzed(@NonNull HeapAnalysis heapAnalysis) {
    // String leakTrace = heapAnalysis.toString();
    // // 上报至服务器
    // ALog.e(leakTrace);
    // LogUploadUtils.upload(getApplication(), leakTrace);
    // }
    // }

    private static ArcFaceApplication application;
    public static final String TAG = "YCJC";
    private String wlyCacheDir;
    public static boolean TEST = false;
    public static int READ_TIME = 1000;

    public String getWlyCacheDir() {
        return wlyCacheDir;
    }

    // public static ArcFaceApplication getInstance() {
    // return application;
    // }

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
        ALog.getConfig().setLogSwitch(BuildConfig.DEBUG)// 设置 log 总开关，包括输出到控制台和文件，默认开
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
        // initParameters.setLogDir(DebugInfoDumper.CRASH_LOG_DIR);
        // XCrash.init(application, initParameters);
    }

    @Override
    public void onTerminate() {
        application = null;
        super.onTerminate();
    }

    public static ArcFaceApplication getApplication() {
        return application;
    }

    public static int UPLOAD_LOG_TIME = 10 * 60 * 1000;
    public static int UPDATE_DELAY_TIME = 5;
    public static int PING_DELAY_TIME = 10 * 1000;
    public static int POOL_SIZE = 15;
    // private SmallTask task = new SmallTask() {
    // @Override
    // public String doInBackground() throws Throwable {
    // if (DateUtil.getHour(TimeUtils.getNowDate()) == 2 && DateUtil.getMinute(TimeUtils.getNowDate()) == 0) {
    // // ALog.e("AppUtils.relaunchApp(true)");
    // // // AppUtils.relaunchApp(true);
    // // Intent intent = IntentUtils.getLaunchAppIntent(Utils.getApp().getPackageName());
    // // if (intent == null) {
    // // Log.e("AppUtils", "Didn't exist launcher activity.");
    // // return null;
    // // }
    // // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
    // // | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    // // intent.putExtra("auto", true);
    // // Utils.getApp().startActivity(intent);
    // // android.os.Process.killProcess(android.os.Process.myPid());
    // // System.exit(0);
    // int screenSize = ScreenUtils.getScreenWidth();
    // int typeDevice = screenSize > 800 ? 1 : 2;
    // ALog.d("获取屏幕尺寸宽度:" + screenSize);
    // if (typeDevice == 1) {
    // // 声明manager对象
    // @SuppressLint("WrongConstant")
    // ZysjSystemManager manager = (ZysjSystemManager) getSystemService("zysj");
    // int result = manager.zYRebootSys();
    // ALog.e("zYRebootSys result:" + result);
    // // ALog.e("IntentUtils.getLaunchAppIntent(Utils.getApp().getPackageName())");
    // // Intent intent = IntentUtils.getLaunchAppIntent(Utils.getApp().getPackageName());
    // // if (intent == null) {
    // // ALog.e("Didn't exist launcher activity.");
    // // return null;
    // // }
    // // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP
    // // | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    // // intent.putExtra("auto", true);
    // // Utils.getApp().startActivity(intent);
    // // android.os.Process.killProcess(android.os.Process.myPid());
    // // System.exit(0);
    //
    // } else {
    // // 安装 test.apk
    // MyManager manager = MyManager.getInstance(getInstance());
    // manager.reboot();
    // }
    // return null;
    // }
    // // 执行任务逻辑
    // ALog.d("更新通行证任务执行中...");
    //
    // // new Thread(() -> {
    // // List<LongTermPass> all = db.longTermPassDao().getAll();
    // // ALog.d("查询本地数据库数据: "+gson.toJson(all));
    // // }).start();
    // // updateNext = true;
    //
    // // ALog.d("Ping 开始");
    // // boolean result = NetworkUtils.isAvailableByPing();
    // // if (result) {
    // // isOffLine = false;
    // // ALog.d("Ping 成功");
    // // } else {
    // // isOffLine = true;
    // // ALog.d("Ping 失败");
    // // }
    // updatePage = 1;
    // getLongPassCardsUpdate();
    //
    // return null;
    // }
    // };
    private ImageUploader imageUploader;
    private SmallTask task;

    public void startUpDataToServer() {
        // if (ArcFaceApplication.TEST) {
        // return;
        // }
        task = new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                ALog.d("SimpleTask startGetDataFromServer ");
                // List<LongTermPass> list1 = db.longTermPassDao().getAll();
                int count = db.longTermPassDao().getCount();
                ALog.e("通行证数量 count:" + count);

                if (faceRepository != null) {
                    ALog.e("faceRepository.getTotalFaceCount():" + faceRepository.getTotalFaceCount());
                }

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
                //
                // if (ObjectUtils.isEmpty(card3)) {
                // ALog.i("本地数据库未查询到 getByApplyId: 1909876155616845826");
                // } else {
                // ALog.e(card3.toString());
                // }

                // if (ObjectUtils.isNotEmpty(list1)) {
                // ALog.e("长期证件 list1.size():" + list1.size());
                //// for (LongTermPass item : list1) {
                //// ALog.e(item.toString());
                //// }
                // // new ImageUploader()
                // // .uploadBitmap2(ImageDownloader.loadAndDecryptImage(list1.get(0).idCode, getInstance()));
                // }

                List<LongTermRecords> list2 = db.longTermRecordsDao().getAll();
                if (ObjectUtils.isNotEmpty(list2)) {
                    ALog.e("list2.size():" + list2.size());
                    for (LongTermRecords item : list2) {
                        ALog.e(item.toString());
                        // {id='664573625730469888', passid='1872521146050961409', cardId='EF61ACF2', idCode='A40203',
                        // applyId='null', direction='-1', nickname='齐修平',
                        // photo='null', deviceId='1896459165704093697', deviceName='航站楼2楼员工通道出口设备02',
                        // checkUserId='1872521165688688642',
                        // checkUserName='李海明', companyName='安检护卫部', expiryDate='2027-12-31', templateType=1,
                        // areaDisplayCode=[A, C, F],
                        // area='1840686989613682689', areaName='A国内候机隔离区A1国内', status='true', reason='null',
                        // parentld='null',
                        // sitePhoto='/storage/emulated/0/Android/data/com.arcsoft.arcfacedemo/files/records/664573625730469888.jpg',
                        // checkTime='null', faceSimilar='0.7142504', leadingPeopleld='null'}
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
                        request.headers("tenant-id", "1");
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
                        request.headers("tenant-id", "1");
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
            }
        };
        ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task, UPLOAD_LOG_TIME, TimeUnit.MILLISECONDS);
    }

    public void reset() {
        if (task != null) {
            ThreadUtils.cancel(task);
            task.cancel();
        }
        startUpDataToServer();
    }

    public void resetAll() {
        if (task != null) {
            ThreadUtils.cancel(task);
            task.cancel();
        }

    }

    // 在 Activity 中定时监测 CPU
    private CpuMonitor cpuMonitor = new CpuMonitor();
    private static int UPDATE_PAGE_SIZE = 20;
    private static int updatePage = 1;
    private static boolean updateNext = true;
    InfoStorage infoStorage;

    public YinchuanAirportDB getDb() {
        return db;
    }

    public void setDb(YinchuanAirportDB db) {
        this.db = db;
    }

    boolean isOffLine;
    private YinchuanAirportDB db;
    private FaceRepository faceRepository;
    private static final int PAGE_SIZE = 20;
    private FaceDao faceDao;
    private MutableLiveData<List<FaceEntity>> faceEntityList = new MutableLiveData<>();

    // private MutableLiveData<Boolean> initFinished = new MutableLiveData<>();
    // 总数
    private int faceCount = -1;

    private MutableLiveData<Integer> totalFaceCount = new MutableLiveData<>();

    public boolean isOffLine() {
        return isOffLine;
    }

    //
    // public MutableLiveData<Boolean> getInitFinished() {
    // return initFinished;
    // }
    //
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
                    // 2点重启，并设置重启标致为false，防止继续重启
                    boolean flag = SPUtils.getInstance().getBoolean("upload_log", true);
                    if (flag) {
                        LogUploadUtils.upload(getApplication());
                        SPUtils.getInstance().put("upload_log", false);
                    }
                } else {
                    // 2点过后，设置重启标致为true，到2点时自动重启
                    SPUtils.getInstance().put("upload_log", true);
                }

                // 执行任务逻辑
                ALog.d("更新通行证任务执行中...");
                updatePage = 1;

                GetRequest<String> request = OkGo.<String> get(UrlConstants.heartbeat).tag(UrlConstants.heartbeat);
                request.headers("tenant-id", "1");
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

        // SmallTask task2 = new SmallTask() {
        // @Override
        // public String doInBackground() throws Throwable {
        // LongTermPass card1 = getDb().longTermPassDao().getByNickname("小芳");
        // if (ObjectUtils.isEmpty(card1)) {
        // ALog.i("本地数据库未查询到 getByNickname: 小芳");
        // } else {
        // ALog.e(card1.toString());
        // LongPassCard card = Converters.convertToLongPassCard(card1);
        // List<LongPassCard> longPassCardList = new ArrayList<>();
        // longPassCardList.add(card);
        // handleUpdateComplete(longPassCardList);
        // }
        // return null;
        // }
        // };
        // ThreadUtils.executeByFixedAtFixRate(POOL_SIZE, task2, 30 * 1000, TimeUnit.MILLISECONDS);

    }

    /**
     * 获取更新通行证数据
     */
    private void getLongPassCardsUpdate() {
        List<LongPassCard> longPassCardList = new ArrayList<>();
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
        updateNext = true;
        fetchNextPage(params, longPassCardList);
    }

    private synchronized void fetchNextPage(Map<String, String> params, List<LongPassCard> longPassCardList) {
        if (!updateNext) {
            return;
        }

        GetRequest<String> request = OkGo.<String> get(UrlConstants.URL_GetLongPass).tag(UrlConstants.URL_GetLongPass);
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
                            for (LongPassCard longPassCard : longPassCards.list) {
                                ALog.e("正在下载：" + longPassCard.nickname + "，第" + updatePage + "页");
                                File directory1 = new File(getApplication().getExternalFilesDir(null), "register");// 应用的私有目录
                                if (!directory1.exists()) {
                                    directory1.mkdirs();
                                }
                                boolean result = ImageDownloader.downloadImage(directory1, longPassCard.checkPhoto,
                                        longPassCard.id, longPassCard.nickname, false);
                                if (!result) {
                                    ALog.e("下载失敗 checkPhoto：" + longPassCard.nickname + "，第" + updatePage + "页");
                                    return;
                                }
                                File directory2 = new File(getApplication().getExternalFilesDir(null), "photo");// 应用的私有目录
                                if (!directory2.exists()) {
                                    directory2.mkdirs();
                                }
                                result = ImageDownloader.downloadImage(directory2, longPassCard.photo, longPassCard.id,
                                        longPassCard.nickname, true);
                                if (!result) {
                                    ALog.e("下载失敗 photo：" + longPassCard.nickname + "，第" + updatePage + "页");
                                    return;
                                }
                            }

                            // 下载图片到本地
                            // ImageDownloader.downloadImages(longPassCards.list, getApplication());
                        }
                        longPassCardList.addAll(longPassCards.getList());
                        // updatePage++;
                        // params.put("pageNo", String.valueOf(updatePage));
                        // ALog.i("更新通行证有数据，当前页码: " + updatePage);
                        // fetchNextPage(params, longPassCardList); // 递归调用
                    } else {
                        ALog.i("更新通行证数据为空，当前页码: " + updatePage);
                    }
                } else if (resResponse.getCode() == 401) {
                    ALog.d("更新通行证线程接口401: " + resResponse.getMsg());
                    ALog.e("Intent intent = new Intent(getInstance(), LoginActivity.class)");
                    Intent intent = new Intent(getApplication(), LoginActivity.class);
                    intent.putExtra("auto", true);
                    ActivityUtils.startActivity(intent);
                    // ActivityUtils.getTopActivity().finish();
                    return;
                } else {
                    ALog.d("更新通行证线程接口非200: " + resResponse.getMsg());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ALog.e("更新通信证接口数据失败", e);
        }
        updateNext = false;
        if (!ArcFaceApplication.TEST) {
            handleUpdateComplete(longPassCardList);
        }
    }

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
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                // // faceRepository = null;
                // if (!ArcFaceApplication.TEST) {
                // // faceDao = FaceDatabase.getInstance(getApplication()).faceDao();
                // // if (faceRepository == null)
                // {
                // FaceServer instance = FaceServer.getInstance();
                // instance.init(getApplication(), new FaceServer.OnInitFinishedCallback() {
                // @Override
                // public void onFinished(int faceCount) {
                // ALog.e("faceCount:" + faceCount);
                // // initFinished.postValue(true);
                // }
                // });
                // faceRepository = new FaceRepository(PAGE_SIZE, faceDao, instance);
                // }
                // }

                // FaceServer instance = FaceServer.getInstance();
                // if (FaceServer.getFaceEngine() == null) {
                // ALog.e("FaceServer.getFaceEngine() == null");
                // instance.init(getApplication(), new FaceServer.OnInitFinishedCallback() {
                // @Override
                // public void onFinished(int faceCount) {
                // ALog.e("faceCount:" + faceCount);
                // // initFinished.postValue(true);
                // }
                // });
                // }
                // if (faceRepository == null) {
                // ALog.e("faceRepository == null");
                // faceRepository = new FaceRepository(PAGE_SIZE, faceDao, instance);
                // }

                List<FaceEntity> faceEntityList = FaceDatabase.getInstance(getApplication()).faceDao().getAllFaces();
                for (LongPassCard longPassCard : longPassCardList) {
                    for (FaceEntity faceEntity : faceEntityList) {
                        if (faceEntity.getUserName().equals(longPassCard.id)) {
                            if (FaceServer.getInstance().getFaceEngine() == null
                                    && FaceServer.getInstance().getFrEngine() == null) {
                                ALog.e("FaceServer.getInstance().getFaceEngine() == null");
                                continue;
                            }

                            ALog.e(longPassCard.nickname + ", " + faceEntity.toString2());
                            if (ActivityUtils.getTopActivity() instanceof RegisterAndRecognizeActivity) {
                                int flag1 = FaceServer.getInstance().getFrEngine()
                                        .removeFaceFeature((int) faceEntity.getFaceId());
                                ALog.e("getFrEngine deleteFace removeFaceFeature：" + flag1);
                            } else {
                                int flag1 = FaceServer.getInstance().getFaceEngine()
                                        .removeFaceFeature((int) faceEntity.getFaceId());
                                ALog.e("getFaceEngine deleteFace removeFaceFeature：" + flag1);
                            }

                            boolean flag = FaceServer.getInstance().removeOneFace(faceEntity);

                            ALog.e("deleteFace removeOneFace：" + flag);
                            // 删除人脸
                            int result = FaceDatabase.getInstance(getApplication()).faceDao().deleteFace(faceEntity);
                            ALog.e("deleteFace result：" + result);
                            ALog.e("FaceDatabase.getInstance(getApplication()).faceDao().deleteFace(faceEntity)："
                                    + faceEntity.toString2());

                        }
                    }

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
                // registerFace(bitmap, (facePreviewInfo, success) -> {
                // // showLongSnackBar(binding.fabAdd, getString(success ? R.string.register_success :
                // // R.string.register_failed));
                // ALog.i("单个注册人脸: " + success);
                // });
                return null;
            }
        });
    }

    public void registerFace(Bitmap bitmap, OnRegisterFinishedCallback callback, String applyId) {
        Bitmap alignedBitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        // if (bitmap != null && !bitmap.isRecycled()) {
        // bitmap.recycle();
        // bitmap = null;
        // }
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
        // return DateUtil.getHour(TimeUtils.getNowDate()) >= 1 && DateUtil.getHour(TimeUtils.getNowDate()) <= 3;
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
