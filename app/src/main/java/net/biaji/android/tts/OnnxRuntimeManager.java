package net.biaji.android.tts;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

public class OnnxRuntimeManager {
    private static String TAG = "OnnxRuntimeManager";
    private static OnnxRuntimeManager instance = null;
    private OrtSession session = null;
    private OrtEnvironment env = null;
    private long maxTokenLength = -1;

    private OnnxRuntimeManager(Context context) {
        this.env = OrtEnvironment.getEnvironment();
        try {
            OrtSession.SessionOptions options = new OrtSession.SessionOptions();
            options.addConfigEntry("nnapi.flags", "USE_FP16");
            options.addConfigEntry("nnapi.use_gpu", "true");
            options.addConfigEntry("nnapi.gpu_precision_loss_allowed", "true");
            InputStream is = context.getAssets().open("model.onnx");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] modelBytes = baos.toByteArray();
            session = env.createSession(modelBytes, options);
            is.close();
        } catch (OrtException | IOException e) {
            Log.e("ONNX", "init failed", e);
        }
    }

    public static OnnxRuntimeManager getInstance(Context context) {
        if (instance == null) {
            instance = new OnnxRuntimeManager(context);
        }

        return instance;
    }

    public OrtSession getSession() {
        return session;
    }

    public long getMaxTokenLength() {
        return maxTokenLength;
    }
}
