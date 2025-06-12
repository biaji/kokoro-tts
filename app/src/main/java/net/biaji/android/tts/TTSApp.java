package net.biaji.android.tts;

import android.app.Application;

import net.biaji.android.tts.utils.VoiceLoadCallback;
import net.biaji.android.tts.utils.VoiceManager;

import java.util.ArrayList;
import java.util.List;

public class TTSApp extends Application {

    private static VoiceManager vmanager;

    private static List<String> voiceList = new ArrayList<>();

    private VoiceLoadCallback callback = null;

    @Override
    public void onCreate() {
        super.onCreate();
        vmanager = new VoiceManager(this, voices -> {
            voiceList = voices;
            if (callback != null) {
                callback.onLoaded(voices);
            }
        });
    }

    public void setVoiceLoadCallback(VoiceLoadCallback callback) {
        this.callback = callback;
    }

    public List<String> getVoiceList() {
        return voiceList;
    }

    public VoiceManager getVmanager() {
        return vmanager;
    }
}
