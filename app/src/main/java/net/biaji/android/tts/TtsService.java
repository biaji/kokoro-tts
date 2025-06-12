package net.biaji.android.tts;

import android.content.Intent;
import android.media.AudioFormat;
import android.os.IBinder;
import android.speech.tts.SynthesisCallback;
import android.speech.tts.SynthesisRequest;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeechService;
import android.speech.tts.Voice;
import android.util.Log;
import android.util.Pair;

import net.biaji.android.tts.utils.MToken;
import net.biaji.android.tts.utils.VoiceGenerator;
import net.biaji.android.tts.utils.VoiceManager;
import net.biaji.android.tts.utils.ZhFrontend;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TtsService extends TextToSpeechService {

    private final static String TAG = "SERVICE";

    public final static String[] SUPPORTED_LANGUAGES = {"zho-CHN", "eng-AUS", "eng-CAN", "eng-GBR", "eng-USA"};
    private volatile String[] mCurrentLanguage = null;

    private List<String> owrkStringList = new ArrayList<>();

    private VoiceGenerator vg;

    public TtsService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.vg = VoiceGenerator.getInstance((TTSApp) getApplication());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onLoadVoice(String voiceName) {
        TTSApp app = (TTSApp) getApplication();
        VoiceManager vm = app.getVmanager();
        if (vm != null) {
            vm.setCurrentVoice(voiceName);
        }
        return super.onLoadVoice(voiceName);
    }

    @Override
    protected int onIsLanguageAvailable(String lang, String country, String variant) {
        Locale locale = new Locale(lang, country, variant);
        boolean isLanguage = false;
        boolean isCountry = false;
        for (String lan : SUPPORTED_LANGUAGES) {
            String[] temp = lan.split("-");
            Locale locale1 = new Locale(temp[0], temp[1]);
            if (locale.getISO3Language().equals(locale1.getISO3Language())) {
                isLanguage = true;
            }
            if (isLanguage && locale.getISO3Country().equals(locale1.getISO3Country())) {
                isCountry = true;
            }
            if (isCountry && locale.getVariant().equals(locale1.getVariant())) {
                return TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE;
            }

        }
        if (isCountry) {
            return TextToSpeech.LANG_COUNTRY_AVAILABLE;
        }
        if (isLanguage) {
            return TextToSpeech.LANG_AVAILABLE;
        }
        return TextToSpeech.LANG_NOT_SUPPORTED;
    }

    @Override
    protected String[] onGetLanguage() {
        return mCurrentLanguage;
    }

    @Override
    protected int onLoadLanguage(String _lang, String _country, String _variant) {
        String lang = _lang == null ? "" : _lang;
        String country = _country == null ? "" : _country;
        String variant = _variant == null ? "" : _variant;
        int result = onIsLanguageAvailable(lang, country, variant);
        if (result == TextToSpeech.LANG_COUNTRY_AVAILABLE || TextToSpeech.LANG_AVAILABLE == result || result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
            mCurrentLanguage = new String[]{lang, country, variant};
        }

        return result;
    }

    @Override
    protected void onStop() {

    }

    @Override
    public int onIsValidVoiceName(String voiceName) {
        return super.onIsValidVoiceName(voiceName);
    }

    @Override
    public List<Voice> onGetVoices() {
        TTSApp app = (TTSApp) getApplication();
        List<Voice> result = new ArrayList<>();
        for (String voiceName : app.getVoiceList()) {
            result.add(new Voice(voiceName, Locale.CHINESE, Voice.QUALITY_NORMAL, Voice.LATENCY_HIGH, false, null));
        }
        return result;
    }

    @Override
    protected void onSynthesizeText(SynthesisRequest synthesisRequest, SynthesisCallback synthesisCallback) {
        long beginTime = System.currentTimeMillis();
        ZhFrontend zhFrontend = new ZhFrontend();
        String text = synthesisRequest.getCharSequenceText().toString();

        float speed = (float) synthesisRequest.getSpeechRate() / 100;


        // 分隔英文和中文
        String regex = "([A-Za-z \\'-]*[A-Za-z][A-Za-z \\'-]*)|([^A-Za-z]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String[]> stResults = new ArrayList<>();
        List<String> phonemes = new ArrayList<>();

        while (matcher.find()) {
            stResults.add(new String[]{matcher.group(1), matcher.group(2)});
        }

        for (String[] seg : stResults) {
            if (seg[1] == null) { // 当前仅处理中文
                continue;
            }
            Pair<String, List<MToken>> zhR = zhFrontend.process(seg[1], true);
            phonemes.add(zhR.first);
        }
        String phonemeStr = String.join(",", phonemes);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "分词消耗：" + (System.currentTimeMillis() - beginTime) + "毫秒");
        }
        beginTime = System.currentTimeMillis();
        // 音素转大模型生成音频
        float[] audio = VoiceGenerator.getInstance((TTSApp) getApplication()).generateAudio(phonemeStr, speed);
        if (BuildConfig.DEBUG) {
            long timeCost = System.currentTimeMillis() - beginTime;
            Log.d(TAG, String.format("大模型生成 %d 字：%d 毫秒。平均 %.3f 字/秒",
                    text.length(), timeCost, (double) text.length() / (timeCost / 1000.0)));
        }

        //将 Float[] 转为 16-bit PCM 数据
        ByteBuffer byteBuffer = ByteBuffer.allocate(audio.length * 2); // 每个 short 2 字节
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

        for (float sample : audio) {
            short pcmValue = (short) (sample * Short.MAX_VALUE);
            byteBuffer.putShort(pcmValue); // 16bit为short
        }
        byte[] audioBytes = byteBuffer.array();

        // 返回处理结果
        synthesisCallback.start(22050, AudioFormat.ENCODING_PCM_16BIT, 1);

        int chunkSize = 4096; // 每次发送 4096 字节，源码中最大是8192字节限制
        int offset = 0;

        while (offset < audioBytes.length) {
            int length = Math.min(chunkSize, audioBytes.length - offset);
            synthesisCallback.audioAvailable(audioBytes, offset, length);
            offset += length;
        }
        synthesisCallback.done();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}