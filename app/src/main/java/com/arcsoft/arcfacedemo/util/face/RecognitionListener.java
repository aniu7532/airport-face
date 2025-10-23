package com.arcsoft.arcfacedemo.util.face;

import com.arcsoft.arcfacedemo.ui.model.CompareResult;

public interface RecognitionListener {
    void onRecognitionSuccess(CompareResult compareResult);
    void onRecognitionFailure(String message);
}

