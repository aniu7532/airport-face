package com.arcsoft.arcfacedemo.ui.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectFaceOrientPriority;


/**
 * 首页 ViewModel，提供虹软 SDK 激活状态查询能力。
 */
public class HomeViewModel extends ViewModel {
    private MutableLiveData<Boolean> activated = new MutableLiveData<>();
    private MutableLiveData<Integer> activeCode = new MutableLiveData<>();

    public MutableLiveData<Boolean> getActivated() {
        return activated;
    }

    public MutableLiveData<Integer> getActiveCode() {
        return activeCode;
    }

    /** 检查本地激活文件是否有效。 */
    public boolean isActivated(Context context) {
        return FaceEngine.getActiveFileInfo(context, new ActiveFileInfo()) == ErrorInfo.MOK;
    }
}
