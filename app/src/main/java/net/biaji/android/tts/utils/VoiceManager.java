package net.biaji.android.tts.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import net.biaji.android.tts.R;
import net.biaji.android.tts.TTSApp;

import org.jetbrains.bio.npy.NpyArray;
import org.jetbrains.bio.npy.NpzEntry;
import org.jetbrains.bio.npy.NpzFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 *
 */
public class VoiceManager {

    private final ExecutorService loadService = Executors.newSingleThreadExecutor();
    private final Application context;
    private boolean loaded = false;
    private List<String> voiceList;
    private final VoiceLoadCallback callback;

    private String currentVoice = Constants.DEFAULT_VOICE;


    /**
     * 语音文件为npz格式的数据文件
     */
    private final File cacheVoiceFile;

    public VoiceManager(TTSApp context, VoiceLoadCallback callback) {
        this.context = context;
        this.callback = callback;
        cacheVoiceFile = new File(context.getCacheDir(), "voices.bin");
        init();
    }

    /**
     * 由资源加载音色
     */
    private void init() {
        loadService.execute(() -> {
            if (!cacheVoiceFile.exists()) { // TODO： 完整性校验
                try {
                    InputStream is = context.getAssets().open("voices.bin");
                    FileOutputStream fos = new FileOutputStream(cacheVoiceFile);
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    Log.w("VoiceManager", "", e);
                }
            }

            NpzFile.Reader reader = NpzFile.read(cacheVoiceFile.toPath());
            List<NpzEntry> entryList = reader.introspect();
            voiceList = entryList.stream().map(NpzEntry::getName).collect(Collectors.toList());
            reader.close();
            loadCurrentVoice();
            loaded = true;
            callback.onLoaded(voiceList);
        });
    }

    public List<String> getVoiceList() {
        return this.voiceList;
    }

    public float[][] loadVoice() {
        return loadVoice(this.currentVoice);
    }

    public float[][] loadVoice(String name) {
        if (!loaded) {
            return null;
        }

        if (!voiceList.contains(name)) {
            return null;
        }

        NpzFile.Reader reader = NpzFile.read(cacheVoiceFile.toPath());
        NpyArray data = reader.get(name, 262144); // 262144为NumPy的默认值(1<<18)。此处为java模拟

        reader.close();
        float[][] result = new float[1][256];
        float[] content = data.asFloatArray();
        System.arraycopy(content, 0, result[0], 0, 256);
        return result;
    }

    public void setCurrentVoice(String voice) {
        this.currentVoice = voice;
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.default_preference), Context.MODE_PRIVATE);
        pref.edit().putString(Constants.PERF_KEY_VOICE, voice).apply();
    }

    public String getCurrentVoice() {
        return currentVoice;
    }

    private void loadCurrentVoice() {
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.default_preference), Context.MODE_PRIVATE);
        currentVoice = pref.getString(Constants.PERF_KEY_VOICE, Constants.DEFAULT_VOICE);
    }
}

