package com.hawkfranklin.aura;

import android.content.Context;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.google.mediapipe.tasks.genai.llminference.LlmInference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@CapacitorPlugin(name = "GenAI")
public class GenAIPlugin extends Plugin {

    private LlmInference llmInference;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private static final String TAG = "GenAIPlugin";

    @PluginMethod
    public void downloadModel(PluginCall call) {
        String urlString = call.getString("url");
        String fileName = call.getString("fileName");

        if (urlString == null || fileName == null) {
            call.reject("URL and fileName are required");
            return;
        }

        executorService.execute(() -> {
            try {
                File modelFile = new File(getContext().getFilesDir(), fileName);
                if (modelFile.exists()) {
                    JSObject ret = new JSObject();
                    ret.put("path", modelFile.getAbsolutePath());
                    ret.put("status", "exists");
                    call.resolve(ret);
                    return;
                }

                URL url = new URL(urlString);
                try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(modelFile)) {
                    byte dataBuffer[] = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                }

                JSObject ret = new JSObject();
                ret.put("path", modelFile.getAbsolutePath());
                ret.put("status", "downloaded");
                call.resolve(ret);

            } catch (IOException e) {
                Log.e(TAG, "Download failed", e);
                call.reject("Download failed: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void checkModel(PluginCall call) {
        String fileName = call.getString("fileName");
        if (fileName == null) {
            call.reject("fileName is required");
            return;
        }
        File modelFile = new File(getContext().getFilesDir(), fileName);
        JSObject ret = new JSObject();
        ret.put("exists", modelFile.exists());
        if (modelFile.exists()) {
            ret.put("path", modelFile.getAbsolutePath());
        }
        call.resolve(ret);
    }

    @PluginMethod
    public void initModel(PluginCall call) {
        String modelPath = call.getString("path");
        if (modelPath == null) {
            call.reject("Model path is required");
            return;
        }

        executorService.execute(() -> {
            try {
                LlmInference.LlmInferenceOptions options = LlmInference.LlmInferenceOptions.builder()
                        .setModelPath(modelPath)
                        .setMaxTokens(1024)
                        .setTopK(40)
                        .setTemperature(0.8f)
                        .setRandomSeed(101)
                        .build();

                llmInference = LlmInference.createFromOptions(getContext(), options);
                call.resolve();
            } catch (Exception e) {
                Log.e(TAG, "Initialization failed", e);
                call.reject("Initialization failed: " + e.getMessage());
            }
        });
    }

    @PluginMethod
    public void generateResponse(PluginCall call) {
        String prompt = call.getString("prompt");
        if (prompt == null) {
            call.reject("Prompt is required");
            return;
        }

        if (llmInference == null) {
            call.reject("Model not initialized. Call initModel first.");
            return;
        }

        executorService.execute(() -> {
            try {
                String response = llmInference.generateResponse(prompt);
                JSObject ret = new JSObject();
                ret.put("response", response);
                call.resolve(ret);
            } catch (Exception e) {
                Log.e(TAG, "Inference failed", e);
                call.reject("Inference failed: " + e.getMessage());
            }
        });
    }
}
