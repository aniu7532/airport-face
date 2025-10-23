package com.arcsoft.arcfacedemo.manager;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;
import java.util.HashMap;
import java.util.Map;


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


    public void loadSound(int soundResId) {
        int soundId = soundPool.load(context, soundResId, 1);
        soundMap.put(soundResId, soundId);
    }


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


    public void release() {
        soundPool.release();
        soundPool = null;
    }


    public boolean isSoundLoaded(int soundResId) {
        return soundMap.containsKey(soundResId);
    }


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
