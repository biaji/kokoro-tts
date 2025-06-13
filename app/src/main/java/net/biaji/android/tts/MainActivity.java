package net.biaji.android.tts;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import net.biaji.android.tts.utils.MToken;
import net.biaji.android.tts.utils.VoiceGenerator;
import net.biaji.android.tts.utils.VoiceManager;
import net.biaji.android.tts.utils.ZhFrontend;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

    private final static String TAG = "MAIN";
    private Context context;
    private Spinner voiceSelector;
    private VoiceManager vmanager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        voiceSelector = findViewById(R.id.voice_selector);
        this.context = this;
        loadVoice();
        voiceSelector.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String voice = ((TextView) view).getText().toString().trim();
                vmanager.setCurrentVoice(voice);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void loadVoice() {
        TTSApp app = (TTSApp) getApplication();
        List<String> voices = app.getVoiceList();
        this.vmanager = app.getVmanager();
        if (voices.isEmpty()) {
            app.setVoiceLoadCallback(voicesList -> runOnUiThread(() -> {
                voiceSelector.setAdapter(new ArrayAdapter<>(context, R.layout.voice_item, voicesList));
                String currentVoice = vmanager.getCurrentVoice();
                voiceSelector.setSelection(voicesList.indexOf(currentVoice));
            }));
        } else {
            voiceSelector.setAdapter(new ArrayAdapter<>(context, R.layout.voice_item, voices));
        }

    }


    public void generate(View view) {

        new Thread(() -> {
            long beginTime = System.currentTimeMillis();
            ZhFrontend zhFrontend = new ZhFrontend();
            String text = ((EditText) findViewById(R.id.text_source)).getText().toString().trim();

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
            Log.d(TAG, "分词消耗：" + (System.currentTimeMillis() - beginTime) / 1000 + "秒");
            beginTime = System.currentTimeMillis();
            // 音素转大模型生成音频

            float[] audio = VoiceGenerator.getInstance((TTSApp) ((MainActivity) this.context).getApplication()).generateAudio(phonemeStr, 1.0f);
            if (BuildConfig.DEBUG) {
                long timeCost = System.currentTimeMillis() - beginTime;
                Log.d(TAG, String.format("大模型生成 %d 字：%d 毫秒。平均 %.3f 字/秒",
                        text.length(), timeCost, (double) text.length() / (timeCost / 1000.0)));
            }
            int sampleRate = 22050;  //实际音频为 RIFF (little-endian) data, WAVE audio, Microsoft PCM, 16 bit, mono 24000 Hz
            int channelConfig = AudioFormat.CHANNEL_OUT_MONO;
            int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
            int minBufSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat);

            AudioTrack audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                            .build())
                    .setAudioFormat(new AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build())
                    .setBufferSizeInBytes(minBufSize)
                    .build();

            //将 Float[] 转为 16-bit PCM 数据
            ByteBuffer byteBuffer = ByteBuffer.allocate(audio.length * 2); // 每个 short 2 字节
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            for (float sample : audio) {
                short pcmValue = (short) (sample * Short.MAX_VALUE);
                byteBuffer.putShort(pcmValue); // 16bit为short
            }

            audioTrack.play();
            audioTrack.write(byteBuffer.array(), 0, byteBuffer.position());
            audioTrack.stop();
            audioTrack.release();
        }).start();
    }
}