package net.biaji.android.tts.utils;

import android.content.Context;
import android.util.Log;

import net.biaji.android.tts.OnnxRuntimeManager;
import net.biaji.android.tts.TTSApp;

import java.util.Map;
import java.util.stream.LongStream;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtSession;

/**
 * 用于生成音频的单例
 */
public class VoiceGenerator {
    private static final String TAG = "VoiceGenerator";
    private OrtSession session;

    private static VoiceManager vmanager;
    private static VoiceGenerator instance;

    private VoiceGenerator(Context context) {
        this.session = OnnxRuntimeManager.getInstance(context).getSession();
    }

    public static VoiceGenerator getInstance(TTSApp context) {
        if (instance == null) {
            instance = new VoiceGenerator(context);
            VoiceGenerator.vmanager = context.getVmanager();
        }
        return instance;
    }

    /**
     * 生成用于播放的wav格式音频
     *
     * @param phonemeStr 传入的音素字符串
     * @param speed      语速
     * @return 音频数据
     */
    public float[] generateAudio(String phonemeStr, float speed) {
        float[] audio;
        long[] tokens = Tokenizer.tokenize(phonemeStr);
        long[][] targeTokens = new long[][]{LongStream.concat(LongStream.of(0L), LongStream.concat(LongStream.of(tokens), LongStream.of(0L))).toArray()};
        float[][] voice = vmanager.loadVoice();
        if (voice == null) {
            return null;
        }
        try {
            OnnxTensor tokenTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), targeTokens);
            OnnxTensor styleTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), voice);
            OnnxTensor speedTensor = OnnxTensor.createTensor(OrtEnvironment.getEnvironment(), new float[]{speed});

            Map<String, OnnxTensor> inputs = Map.of("tokens", tokenTensor, "style", styleTensor, "speed", speedTensor);

            // 生成22050的音频数据
            OrtSession.Result results = session.run(inputs);

            audio = (float[]) results.get(0).getValue();
            results.close();
        } catch (Exception e) {
            Log.e(TAG, "genAudioErr:", e);
            return null;
        }
        return audio;
    }

}
