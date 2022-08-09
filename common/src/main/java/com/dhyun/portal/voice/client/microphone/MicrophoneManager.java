package com.dhyun.portal.voice.client.microphone;

import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.voice.client.MicrophoneException;
import com.dhyun.portal.voice.client.SoundManager;

import java.util.List;

public class MicrophoneManager {

    private static boolean fallback;

    public static Microphone createMicrophone() throws MicrophoneException {
        Microphone mic;
        if (fallback || TeleportClient.CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            mic = createJavaMicrophone();
        } else {
            try {
                mic = createALMicrophone();
            } catch (MicrophoneException e) {
                Teleport.LOGGER.warn("Failed to use OpenAL microphone implementation: {}", e.getMessage());
                Teleport.LOGGER.warn("Falling back to Java microphone implementation");
                mic = createJavaMicrophone();
                fallback = true;
            }
        }
        return mic;
    }

    private static Microphone createJavaMicrophone() throws MicrophoneException {
        Microphone mic = new JavaxMicrophone(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, TeleportClient.CLIENT_CONFIG.microphone.get());
        mic.open();
        return mic;
    }

    private static Microphone createALMicrophone() throws MicrophoneException {
        Microphone mic = new ALMicrophone(SoundManager.SAMPLE_RATE, SoundManager.FRAME_SIZE, TeleportClient.CLIENT_CONFIG.microphone.get());
        mic.open();
        return mic;
    }

    public static List<String> deviceNames() {
        if (fallback || TeleportClient.CLIENT_CONFIG.javaMicrophoneImplementation.get()) {
            return JavaxMicrophone.getAllMicrophones();
        } else {
            return ALMicrophone.getAllMicrophones();
        }
    }

}
