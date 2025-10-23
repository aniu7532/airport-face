//package com.arcsoft.arcfacedemo.business;
//
//import java.util.List;
//
//import com.arcsoft.arcfacedemo.ArcFaceApplication;
//import com.arcsoft.arcfacedemo.R;
//import com.arcsoft.arcfacedemo.db.dao.LongTermPassDao;
//import com.arcsoft.arcfacedemo.db.dao.LongTermRecordsDao;
//import com.arcsoft.arcfacedemo.db.dao.TemporaryCardRecordsDao;
//import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
//import com.arcsoft.arcfacedemo.db.entity.LongTermRecords;
//import com.arcsoft.arcfacedemo.db.entity.TemporaryCardRecords;
//import com.arcsoft.arcfacedemo.entity.Area;
//import com.arcsoft.arcfacedemo.manager.ToastDialogManager;
//import com.arcsoft.arcfacedemo.network.ApiUtils;
//import com.arcsoft.arcfacedemo.network.UrlConstants;
//import com.arcsoft.arcfacedemo.ui.activity.RegisterAndRecognizeActivity;
//import com.arcsoft.arcfacedemo.util.DeviceUtils;
//import com.arcsoft.arcfacedemo.util.ImageUploader;
//import com.arcsoft.arcfacedemo.util.InfoStorage;
//import com.arcsoft.arcfacedemo.util.SmallTask;
//import com.arcsoft.arcfacedemo.util.SnowFlake;
//import com.arcsoft.arcfacedemo.util.log.ALog;
//import com.blankj.utilcode.util.ActivityUtils;
//import com.blankj.utilcode.util.NetworkUtils;
//import com.blankj.utilcode.util.SPUtils;
//import com.blankj.utilcode.util.ThreadUtils;
//import com.google.gson.Gson;
//
//import android.app.Activity;
//import android.graphics.Bitmap;
//import android.media.MediaPlayer;
//
//public class GoOffWork {
//    private static final String TAG = "goOffWork";
//    // private YinchuanAirportDB db;
//    private LongTermPassDao cardDao;
//    private Activity activity; // 假设你需要一个 Activity 来调用 runOnUiThread
//    private MediaPlayer mediaGet;
//    private MediaPlayer mediaPass;
//    private MediaPlayer mediaReject;
//    private MediaPlayer mediaFailed;
//    private ImageUploader imageUploader;
//    private InfoStorage infoStorage;
//    private Gson gson;
//
//    public GoOffWork() {
//        activity = ActivityUtils.getTopActivity();
//        gson = new Gson();
//        // // 本地仓库初始化
//        // db = Room.databaseBuilder(getApplicationContext(), YinchuanAirportDB.class, "yinchuan-airport-database")
//        // .fallbackToDestructiveMigration().build();
//        cardDao = ArcFaceApplication.getApplication().getDb().longTermPassDao();
//        imageUploader = new ImageUploader();// 图片上传工具类
//        infoStorage = new InfoStorage(activity);
//        initSound();
//    }
//
//    private void initSound() {
//        // 初始化MediaPlayer实例
//        mediaGet = MediaPlayer.create(activity, R.raw.get);
//        mediaPass = MediaPlayer.create(activity, R.raw.verification_successful);
//        mediaReject = MediaPlayer.create(activity, R.raw.validation_failed);
//        mediaFailed = MediaPlayer.create(activity, R.raw.shibie_failed);
//    }
//
//    private void releaseSound() {
//        if (mediaGet != null) {
//            mediaGet.release();
//            mediaGet = null;
//        }
//        if (mediaPass != null) {
//            mediaPass.release();
//            mediaPass = null;
//        }
//        if (mediaReject != null) {
//            mediaReject.release();
//            mediaReject = null;
//        }
//    }
//
//    private void stopAllAudio() {
//        stopAudio(mediaGet);
//        stopAudio(mediaPass);
//        stopAudio(mediaReject);
//    }
//
//    private void stopAudio(MediaPlayer mediaPlayer) {
//        if (mediaPlayer != null) {
//            if (mediaPlayer.isPlaying()) {
//                mediaPlayer.stop();
//            }
//        }
//    }
//
//    private void playAudio(MediaPlayer mediaPlayer) {
//        if (mediaPlayer != null) {
//            if (!mediaPlayer.isPlaying()) {
//                mediaPlayer.start();
//            } else if (mediaPlayer.isPlaying()) {
//                // mediaPlayer.stop();
//                stopAllAudio();
//                mediaPlayer.start();
//            }
//        }
//    }
//
//    public void start(Bitmap bitmap, String userName) {
//        ALog.i("start.userName:" + userName);
//        queryPassCard(userName, bitmap);
//    }
//
//    public void start(Bitmap bitmap) {
//        ALog.i("start");
//        // 陌生人脸的情况
//        playAudio(mediaFailed);
//
//        // if (activity instanceof RegisterAndRecognizeActivity) {
//        // ALog.e("activity instanceof RegisterAndRecognizeActivity");
//        // RegisterAndRecognizeActivity registerAndRecognizeActivity = (RegisterAndRecognizeActivity) activity;
//        // registerAndRecognizeActivity.toggleFragment2();
//        // }
//    }
//
//    /**
//     * 判断证件是否正常
//     *
//     * @param status
//     * @return
//     */
//    public String theCardIsExpired(int status) {
//        String sta = "正常";
//        if (status == 1) {
//            sta = "正常";
//        }
//        if (status == 2) {
//            sta = "注销";
//        }
//        if (status == 3) {
//            sta = "过期";
//        }
//        if (status == 4) {
//            sta = "挂失";
//        }
//        return sta;
//    }
//
//    /**
//     * 根据名字查询通行证
//     */
//    public void queryPassCard(String name, Bitmap bitmap) {
//        if (null == cardDao) {
//            ALog.i("dao初始化失败 ");
//            return;
//        }
//        ThreadUtils.executeByCached(new SmallTask() {
//            @Override
//            public String doInBackground() throws Throwable {
//                String imgUrl = imageUploader.uploadBitmap(bitmap);
//                List<LongTermPass> cards = cardDao.getAllByIdCode(name);
//                // 判断证件是否存在
//                if (cards == null || cards.size() == 0) {
//                    ALog.i("本地数据库未查询到: " + name);
//                    playAudio(mediaReject);
//                    activity.runOnUiThread(() -> ToastDialogManager.showCustomDialog(activity, 2, "证件不存在"));
//                    return null;
//                }
//                LongTermPass card = cards.get(0);
//                // 判断证件是否过期
//                boolean currentTimeInRange = DeviceUtils.isCurrentTimeInRange(card.startDate, card.expiryDate);
//                if (!currentTimeInRange) {
//                    card.status = 3;
//                    playAudio(mediaReject);
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastDialogManager.showCustomDialog(activity, 2, "证件过期");
//                        }
//                    });
//                    savePassRecord(false, card, imgUrl);
//                    return null;
//                }
//                // 判断证件是否正常
//                if (card.status != 1) {
//                    playAudio(mediaReject);
//                    activity.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            ToastDialogManager.showCustomDialog(activity, 2, "证件已" + theCardIsExpired(card.status));
//                        }
//                    });
//                    savePassRecord(false, card, imgUrl);
//                    return null;
//                }
//                // 正常,提示成功,保存到数据库
//                playAudio(mediaPass);
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        ToastDialogManager.showSuccess(activity);
//                    }
//                });
//                if (activity instanceof RegisterAndRecognizeActivity) {
//                    ALog.e("activity instanceof RegisterAndRecognizeActivity");
//                    RegisterAndRecognizeActivity registerAndRecognizeActivity = (RegisterAndRecognizeActivity) activity;
//                    registerAndRecognizeActivity.toggleFragment(card); // 更新fragment
//                }
//                // uploadPassRecord(true);
//                savePassRecord(true, card, imgUrl);
//                return null;
//            }
//        });
//
//    }
//
//    /**
//     * 保存通行记录到本地数据库
//     */
//    public void savePassRecord(Boolean isPass, LongTermPass card, String imgUrl) {
//        // uploadPassRecord();
//        boolean availableByPing = NetworkUtils.isAvailableByPing();
//        ALog.i("用 ping 判断网络是否可用：" + availableByPing);
//        int type = card.type;
//        if (type == 0) {// 长期卡
//            saveLongRecord(isPass, card, imgUrl);
//        } else if (type == 1) {
//            saveShortRecord(isPass, card, imgUrl);
//        }
//    }
//
//    /**
//     * 保存长期通行记录到本地
//     */
//    public void saveLongRecord(Boolean isPass, LongTermPass longTermPass, String imgUrl) {
//        try {
//            LongTermRecords longTermRecords = new LongTermRecords();
//            // longTermRecords.id = UUID.randomUUID().toString();
////            longTermRecords.id = SnowflakeIdUtil.getInstance().nextId() + "";
//            SnowFlake worker = new SnowFlake(1, 1, 1);
//            longTermRecords.id = worker.nextId() + "";
//
//            longTermRecords.status = String.valueOf(isPass);
//            longTermRecords.cardId = longTermPass.cardId;
//            longTermRecords.sitePhoto = imgUrl;// 识别图片路径
//            // String deviceDirection = infoStorage.getString("deviceDirection", "-1");
//            longTermRecords.direction = SPUtils.getInstance().getInt("direction", 1) + "";
//            ;// 通行方向（1：进，-1出，2：核验）
//            String deviceId = infoStorage.getString("deviceId", "");
//            longTermRecords.deviceId = deviceId;
//            String deviceName = infoStorage.getString("deviceName", "立式查验终端");
//            longTermRecords.deviceName = deviceName;
//            String userId = ApiUtils.userId;
//            longTermRecords.checkUserId = userId;
//            String loginName = infoStorage.getString("loginName", "");
//            longTermRecords.checkUserName = loginName;
//            String areaDetail = infoStorage.getString("deviceAreaDetail", "");
//            ALog.i("infoStorage.areaDetail: " + gson.toJson(areaDetail));
//            // 解析 JSON 字符串为 Area 对象
//            Area area = gson.fromJson(areaDetail, Area.class);
//            if (area != null) {
//                longTermRecords.area = area.getId();
//                longTermRecords.areaName = area.getCode() + area.getName();
//            }
//            if (area.getChildren() != null && !area.getChildren().isEmpty()) {
//                Area childArea = area.getChildren().get(0);
//                if (childArea != null) {
//                    longTermRecords.areaName += childArea.getCode() + childArea.getName();// "通行区域名称(通行区域编码+名称)";
//                }
//                Area childArea1 = area.getChildren().get(1);
//                if (childArea1 != null) {
//                    longTermRecords.areaName += childArea1.getCode() + childArea1.getName();
//                }
//            }
//            ThreadUtils.executeByCached(new SmallTask() {
//                @Override
//                public String doInBackground() throws Throwable {
//                    LongTermRecordsDao dao = ArcFaceApplication.getApplication().getDb().longTermRecordsDao();
//                    dao.insert(longTermRecords);
//                    return null;
//                }
//            });
//
//            uploadLongRecords(longTermRecords);
//        } catch (Exception e) {
//            ALog.e("保存长期记录失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 保存临时通信记录到本地
//     */
//    public void saveShortRecord(Boolean isPass, LongTermPass longTermPass, String imgUrl) {
//        try {
//            TemporaryCardRecords temporaryCardRecords = new TemporaryCardRecords();
////            temporaryCardRecords.id = UUID.randomUUID().toString();
////            temporaryCardRecords.id =  SnowflakeIdUtil.getInstance().nextId() + "";
//            SnowFlake worker = new SnowFlake(1, 1, 1);
//            temporaryCardRecords.id = worker.nextId() + "";
//            temporaryCardRecords.sitePhoto = imgUrl;
//            temporaryCardRecords.status = String.valueOf(isPass);
//            temporaryCardRecords.applyId = longTermPass.applyId;
//            String deviceDirection = infoStorage.getString("deviceDirection", "-1");
//            temporaryCardRecords.direction = deviceDirection;
//            String deviceId = infoStorage.getString("deviceId", "");
//            temporaryCardRecords.deviceId = deviceId;
//            String deviceName = infoStorage.getString("deviceName", "立式查验终端");
//            temporaryCardRecords.deviceName = deviceName;
//            String userId = ApiUtils.userId;
//            temporaryCardRecords.checkUserId = userId;
//            String loginName = infoStorage.getString("loginName", "");
//            ALog.i("saveTemporaryLocalSQL查验人名字: " + loginName);
//            temporaryCardRecords.checkUserName = loginName;
//            String areaDetail = infoStorage.getString("deviceAreaDetail", "");
//            Area area = gson.fromJson(areaDetail, Area.class);
//            if (area != null) {
//                temporaryCardRecords.area = area.getId();
//                // temporaryCardRecords.areaName = "通行区域名称(通行区域编码+名称)";
//                temporaryCardRecords.areaName += area.getCode() + area.getName();
//            }
//            if (area.getChildren() != null && !area.getChildren().isEmpty()) {
//                Area childArea = area.getChildren().get(0);
//                if (childArea != null) {
//                    temporaryCardRecords.areaName += childArea.getCode() + childArea.getName();// "通行区域名称(通行区域编码+名称)";
//                }
//                Area childArea1 = area.getChildren().get(1);
//                if (childArea1 != null) {
//                    temporaryCardRecords.areaName += childArea1.getCode() + childArea1.getName();
//                }
//            }
//            ThreadUtils.executeByCached(new SmallTask() {
//                @Override
//                public String doInBackground() throws Throwable {
//                    TemporaryCardRecordsDao dao = ArcFaceApplication.getApplication().getDb().temporaryCardRecordsDao();
//                    dao.insert(temporaryCardRecords);
//                    uploadTemporaryRecords(temporaryCardRecords);
//                    return null;
//                }
//            });
//
//        } catch (Exception e) {
//            ALog.e("保存临时记录失败: " + e.getMessage());
//        }
//    }
//
//    /**
//     * 上传长期证件通行记录到后端服务器
//     */
//    public void uploadLongRecords(LongTermRecords longTermRecords) {
//        ALog.i("本地长期记录: " + gson.toJson(longTermRecords));
//        ThreadUtils.executeByCached(new SmallTask() {
//            @Override
//            public String doInBackground() throws Throwable {
//                ApiUtils.post(UrlConstants.URL_CREATE_LONG_RECORD, gson.toJson(longTermRecords),
//                        new ApiUtils.ApiCallback() {
//                            @Override
//                            public void onSuccess(String response) {
//                                ALog.d("上传长期证件成功返回: " + response);
//                            }
//
//                            @Override
//                            public void onFailure(Throwable e) {
//                                ALog.e("上传长期证件日志失败返回: " + e.getMessage());
//                            }
//                        });
//                return null;
//            }
//        });
//    }
//
//    /**
//     * 上传临时证件通行记录到后端服务器
//     */
//    public void uploadTemporaryRecords(TemporaryCardRecords temporaryCardRecords) {
//        ALog.i("本地临时记录: " + gson.toJson(temporaryCardRecords));
//        ThreadUtils.executeByCached(new SmallTask() {
//            @Override
//            public String doInBackground() throws Throwable {
//                ApiUtils.post(UrlConstants.URL_CREATE_TEMP_RECORD, gson.toJson(temporaryCardRecords),
//                        new ApiUtils.ApiCallback() {
//                            @Override
//                            public void onSuccess(String response) {
//                                ALog.d("上传临时证件成功返回: " + response);
//                            }
//
//                            @Override
//                            public void onFailure(Throwable e) {
//                                ALog.e("Error: " + e.getMessage());
//                            }
//                        });
//                return null;
//            }
//        });
//    }
//    /**
//     * 切换Fragment
//     */
//    // public void turnFragment(int fragment) {
//    // Fragment fragment1 = null;
//    // if (fragment == 1){
//    // fragment1 = new Document1();
//    // }else {
//    // fragment1 = new Document2();
//    // }
//    // FragmentManager fragmentManager = getSupportFragmentManager();
//    // FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//    // fragmentTransaction.replace(R.id.fragment_all, fragment1);
//    // fragmentTransaction.commit();
//    // }
//}
