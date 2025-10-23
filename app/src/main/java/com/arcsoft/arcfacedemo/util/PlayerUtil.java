package com.arcsoft.arcfacedemo.util;

import java.io.IOException;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;

public class PlayerUtil implements OnCompletionListener, OnPreparedListener {
    private static PlayerUtil instance;
    int mAudioModeBackup;
    private Context mContext;
    private MediaPlayer mediaPlayer;
    private OnCompletionListener listener;
    private OnPreparedListener listener1;

    public static PlayerUtil getInstance() {
        if (instance == null) {
            instance = new PlayerUtil();
        }
        return instance;
    }

    // volumax now Voulume backup and volume Max
    public static void setVolumeMax(Context context) {
        AudioManager mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) != mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC)) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), AudioManager.FLAG_PLAY_SOUND);
        }
    }

    public void back2Default(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.isWiredHeadsetOn()) {
                CloseSpeaker(context);
            } else {
                if (!audioManager.isSpeakerphoneOn()) {
                    OpenSpeaker(context);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 关闭扬声器
    public void CloseSpeaker(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            if (audioManager.isBluetoothA2dpOn()) {
                Log.e("PlayerUtil", "CloseSpeaker   isBluetoothA2dpOn");
                // Adjust output for Bluetooth. 蓝牙设备
            }
            if (audioManager.isSpeakerphoneOn()) {
                Log.e("PlayerUtil", "CloseSpeaker   isSpeakerphoneOn");
                // Adjust output for Speakerphone. 内置扬声器(免提)
            }
            if (audioManager.isWiredHeadsetOn()) {
                Log.e("PlayerUtil", "CloseSpeaker   isWiredHeadsetOn");
                // Adjust output for headsets 有线耳机
            }
            if (audioManager.isWiredHeadsetOn() && audioManager.isSpeakerphoneOn()) {
                audioManager.setMode(mAudioModeBackup);
                audioManager.setSpeakerphoneOn(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 打开扬声器
    public void OpenSpeaker(Context context) {
        try {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            if (audioManager.isBluetoothA2dpOn()) {
                Log.e("PlayerUtil", "OpenSpeaker   isBluetoothA2dpOn");
                // Adjust output for Bluetooth. 蓝牙设备
            }
            if (audioManager.isSpeakerphoneOn()) {
                Log.e("PlayerUtil", "OpenSpeaker   isSpeakerphoneOn");
                // Adjust output for Speakerphone. 内置扬声器(免提)
            }
            if (audioManager.isWiredHeadsetOn()) {
                Log.e("PlayerUtil", "OpenSpeaker   isWiredHeadsetOn");
                // Adjust output for headsets 有线耳机
            }
            if (audioManager.isWiredHeadsetOn() && !audioManager.isSpeakerphoneOn()) {
                Log.e("PlayerUtil", "OpenSpeaker   init OpenSpeaker");
                sendMediaButton(context, KeyEvent.KEYCODE_MEDIA_STOP);
                mAudioModeBackup = audioManager.getMode();
                // audioManager.setMode(AudioManager.MODE_IN_CALL);
                audioManager.setMode(AudioManager.STREAM_MUSIC);
                audioManager.setSpeakerphoneOn(true);// 使用扬声器外放，即使已经插入耳机
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // /**
    // * 关闭背景音乐
    // *
    // * @param context
    // * @param keyCode
    // */
    private static void sendMediaButton(Context context, int keyCode) {
        KeyEvent keyEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
        Intent intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);

        keyEvent = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
        intent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        intent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        context.sendOrderedBroadcast(intent, null);
    }

    public void initPlayer(Context context, int i) {
        mContext = context;
        AssetFileDescriptor fd = context.getResources().openRawResourceFd(i);
        stopPlay();
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.reset();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setDataSource(fd.getFileDescriptor(), fd.getStartOffset(), fd.getLength());
            mediaPlayer.prepare();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener = null;
    }

    public void stopPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            listener = null;
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        if (listener != null) {
            listener.onCompletion(mp);
        }
    }

    public void setOnCompletionListener(OnCompletionListener listener) {
        this.listener = listener;
    }

    public void setOnPreparedListener(OnPreparedListener listener) {
        this.listener1 = listener;
    }

    public void startPlay(long delayTime) {
        // ALog.e("startPlay");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    mediaPlayer.start();
                }
            }
        }, delayTime);
    }

    public void startPlay() {
        if (mediaPlayer != null) {
            mediaPlayer.start();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        if (listener1 != null) {
            listener1.onPrepared(mp);
        }
    }
}
