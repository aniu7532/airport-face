package com.arcsoft.arcfacedemo.util;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.db.entity.LongTermPass;
import com.arcsoft.arcfacedemo.entity.ApiResponse;
import com.arcsoft.arcfacedemo.entity.LongPassCard;
import com.arcsoft.arcfacedemo.entity.LongPassCards;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.reflect.TypeToken;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.adapter.Call;
import com.lzy.okgo.convert.StringConvert;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LongPassCardsReInitUtils {

    private static volatile LongPassCardsReInitUtils instance = null;

    private LongPassCardsReInitUtils() {
    }

    public static LongPassCardsReInitUtils getInstance() {
        if (instance == null) {
            synchronized (FaceServer.class) {
                if (instance == null) {
                    instance = new LongPassCardsReInitUtils();
                }
            }
        }
        return instance;
    }


    private boolean doing = false;

    public void start() {
        if (doing) {
            ToastUtils.showShort("正在处理中...");
            return;
        }
        if (LongPassCardsRemedialMeasuresUtils.getInstance().doing) {
            ToastUtils.showShort("正在处理中...");
            return;
        }
        ThreadUtils.executeByFixed(ArcFaceApplication.POOL_SIZE, new SmallTask() {
            @Override
            public String doInBackground() throws Throwable {
                int onlineCount = getOnlineCount();
                int localCount = ArcFaceApplication.getApplication().getDb().longTermPassDao().getAll().size();
                if (localCount >= onlineCount) {
                    ToastUtils.showLong("数据已经是最新");
                    return "";
                }
                update();
                LongPassCardsRemedialMeasuresUtils.getInstance().start();
                return "";
            }
        });
    }

    private int getOnlineCount() {
        GetRequest<String> request =
                OkGo.<String>get(UrlConstants.passCount).tag(UrlConstants.passCount);
        request.headers("tenant-id", "1");
        // 检查是否有 accessToken，如果有则添加 Authorization 头
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        // 同步会阻塞主线程，必须开线程，不传callback即为同步请求
        Call<String> call = request.converter(new StringConvert()).adapt();
        try {
            Response<String> res = call.execute();
            if (res.code() != 200) {
                ALog.d("接口报错", res.code());
                return 0;
            }
            ApiResponse<Integer> response = GsonUtils.fromJson(res.body(), new TypeToken<ApiResponse<Integer>>() {
            }.getType());
            if (response.getCode() != 200) {
                ALog.d("接口报错", response.getCode());
                return 0;
            }
            return response.getData();
        } catch (Exception e) {
            e.printStackTrace();
            ALog.d("接口报错: " + e.getMessage());
        }
        return 0;
    }

    private void update() {
        List<LongPassCard> longPassCards = getLongPassCards();
        if (longPassCards == null) {
            ToastUtils.showLong("请求失败，稍后重试");
            return;
        }
        List<LongTermPass> longTermPassList = new ArrayList<>();
        for (LongPassCard item : longPassCards) {
            LongTermPass longTermPass = Converters.convertToLongTermPass(item);
            longTermPassList.add(longTermPass);
        }
        // 将数据插入到本地数据库
        ArcFaceApplication.getApplication().getDb().longTermPassDao().insertAll(longTermPassList);
    }

    private List<LongPassCard> getLongPassCards() {
        ALog.d("start getLongPassCards");
        doing = true;
        int page = 1;
        List<LongPassCard> longPassCardList = new ArrayList<>();
        Map<String, String> params = new HashMap<>();
        params.put("timestamp", String.valueOf(System.currentTimeMillis()));
        params.put("pageNo", String.valueOf(page));
        params.put("pageSize", String.valueOf(100));
        while (true) {
            GetRequest<String> request =
                    OkGo.<String>get(UrlConstants.URL_GetLongPass).tag(UrlConstants.URL_GetLongPass);
            // 更新或添加 timestamp 参数
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            for (Map.Entry<String, String> entry : params.entrySet()) {
                request.params(entry.getKey(), entry.getValue());
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
                if (res.code() != 200) {
                    ALog.d("接口报错", res.code());
                    return null;
                }
                ApiResponse<LongPassCards> response = GsonUtils.fromJson(res.body(), new TypeToken<ApiResponse<LongPassCards>>() {
                }.getType());
                if (response.getCode() != 200) {
                    ALog.d("接口报错", response.getCode());
                    return null;
                }
                LongPassCards longPassCards = response.getData();
                if (longPassCards != null && longPassCards.getList() != null && !longPassCards.getList().isEmpty()) {
                    longPassCardList.addAll(longPassCards.getList());
                    ALog.d("progress getLongPassCards", longPassCardList.size());
                    page++; // 修改成员变量 page
                    params.put("pageNo", String.valueOf(page));
                    continue;
                }
                break;
            } catch (Exception e) {
                e.printStackTrace();
                ALog.d("接口报错: " + e.getMessage());
                longPassCardList.clear();
                break;
            }
        }
        doing = false;
        ALog.d("end getLongPassCards", longPassCardList.size());
        return longPassCardList;

    }

}
