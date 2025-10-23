package com.arcsoft.arcfacedemo.widget.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.data.http.JsonCallback;
import com.arcsoft.arcfacedemo.entity.Base;
import com.arcsoft.arcfacedemo.entity.CardRecords;
import com.arcsoft.arcfacedemo.network.ApiUtils;
import com.arcsoft.arcfacedemo.network.UrlConstants;
import com.arcsoft.arcfacedemo.ui.adapter.RecordsListAdapter;
import com.arcsoft.arcfacedemo.util.InfoStorage;
import com.arcsoft.arcfacedemo.util.log.ALog;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.core.BottomPopupView;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;
import com.yuyh.easyadapter.recyclerview.EasyRVAdapter;

import java.util.ArrayList;
import java.util.List;

public class RecordsPopDialog extends BottomPopupView {
    List<CardRecords.ListDTO> list = new ArrayList<>();
    InfoStorage infoStorage;
    RecordsListAdapter mListAdapter;
    int direction;

    public RecordsPopDialog(@NonNull Context context, int direction) {
        super(context);
        infoStorage = new InfoStorage(context);
        this.direction = direction;
    }

    public RecordsPopDialog(Context context, List<CardRecords.ListDTO> list) {
        super(context);
        this.list = list;
        infoStorage = new InfoStorage(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_records;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        View imgClose = findViewById(R.id.imgClose);
        imgClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        RecyclerView rvList = findViewById(R.id.rvList);
        mListAdapter = new RecordsListAdapter(getContext(), list, R.layout.list_records_item);
        rvList.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
        rvList.setAdapter(mListAdapter);

        mListAdapter.setOnItemClickListener(new EasyRVAdapter.OnItemClickListener<CardRecords.ListDTO>() {
            @Override
            public void onItemClick(View view, int position, CardRecords.ListDTO item) {
                new XPopup.Builder(getContext()).dismissOnTouchOutside(true)
                        .asCustom(new ImagePopDialog(getContext(), item)).show();
            }
        });

        getAllRecords();
    }

    // 自适应， 最大高度为Window的0.85
    @Override
    protected int getMaxHeight() {
        return 0;
    }

    @Override
    protected int getMaxWidth() {
        return 0;
    }

    // 上传临时证件日志
    public void getAllRecords() {
        GetRequest<Base<CardRecords>> request =
                OkGo.<Base<CardRecords>> get(UrlConstants.URL_GET_RESORD_PAGE).tag(UrlConstants.URL_GET_RESORD_PAGE);
        request.headers("tenant-id", "1");
        if (ApiUtils.accessToken != null) {
            request.headers("Authorization", "Bearer " + ApiUtils.accessToken);
        }
        request.params("deviceId", infoStorage.getString("deviceId", "")).params("pageNo", 1).params("pageSize", 50)
                .params("direction", direction).execute(new JsonCallback<Base<CardRecords>>() {
                    @Override
                    public void onSuccess(Response<Base<CardRecords>> response) {
                        if (ObjectUtils.isEmpty(response.body())) {
                            ToastUtils.showLong("getAllRecords失败");
                            return;
                        }
                        Base<CardRecords> res = response.body();
                        if (res.getCode() == 200) {
                            if (ObjectUtils.isNotEmpty(res.getData())
                                    && ObjectUtils.isNotEmpty(res.getData().getList())) {
                                mListAdapter.clear();
                                mListAdapter.addAll(res.getData().getList());
                            }
                        } else {
                            ToastUtils.showLong(res.getMsg());
                        }
                    }

                    @Override
                    public void onError(Response<Base<CardRecords>> response) {
                        response.getException().printStackTrace();
                        ALog.e("uploadTemporaryRecords," + response.getException().getMessage());
                    }
                });
    }
}
