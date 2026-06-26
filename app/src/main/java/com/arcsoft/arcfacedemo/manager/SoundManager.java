package com.arcsoft.arcfacedemo.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.util.HashMap;
import java.util.Map;


/**
 * 音效管理器，基于 SoundPool 封装提示音的加载、播放与释放。
 */
public class SoundManager {
    private SoundPool soundPool;
    private Map<Integer, Integer> soundMap;
    private Context context;


    public SoundManager(Context context) {
        this.context = context;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new SoundPool(10, android.media.AudioManager.STREAM_MUSIC, 0);
        }


        soundMap = new HashMap<>();
    }


    /**
     * 预加载指定资源 ID 的音效文件。
     */
    public void loadSound(int soundResId) {
        int soundId = soundPool.load(context, soundResId, 1);
        soundMap.put(soundResId, soundId);
    }


    /**
     * 播放已加载的音效。
     */
    public void playSound(int soundResId, float volumeLeft, float volumeRight, int loop, float rate) {
        Integer soundId = soundMap.get(soundResId);
        if (soundId!= null) {
//            soundPool.play(soundId, volumeLeft, volumeRight, 1, loop, rate);
            soundPool.play(soundId, 1.0f, 1.0f, 1, loop, rate);
        }
    }


    public void pauseSound(int soundResId) {
        Integer soundId = soundMap.get(soundResId);
        if (soundId!= null) {
            soundPool.pause(soundId);
        }
    }


    public void stopSound(int soundResId) {
        Integer soundId = soundMap.get(soundResId);
        if (soundId!= null) {
            soundPool.stop(soundId);
        }
    }


    /**
     * 释放 SoundPool 资源。
     */
    public void release() {
        soundPool.release();
        soundPool = null;
    }


    public boolean isSoundLoaded(int soundResId) {
        return soundMap.containsKey(soundResId);
    }


    /**
     * 批量预加载多个音效资源。
     */
    public void preloadSounds(int[] soundResIds) {
        for (int soundResId : soundResIds) {
            if (!isSoundLoaded(soundResId)) {
                loadSound(soundResId);
            }
        }
    }


    public void unloadSound(int soundResId) {
        Integer soundId = soundMap.get(soundResId);
        if (soundId!= null) {
            soundPool.unload(soundId);
            soundMap.remove(soundResId);
        }
    }
}
