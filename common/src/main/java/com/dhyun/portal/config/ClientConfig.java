package com.dhyun.portal.config;

import de.maxhenkel.configbuilder.ConfigEntry;
import com.dhyun.portal.voice.client.GroupPlayerIconOrientation;
import com.dhyun.portal.voice.client.MicrophoneActivationType;
import com.dhyun.portal.voice.client.speaker.AudioType;

public abstract class ClientConfig {

    public ConfigEntry<Double> voiceChatVolume;
    public ConfigEntry<Double> voiceActivationThreshold;
    public ConfigEntry<Double> microphoneAmplification;
    public ConfigEntry<MicrophoneActivationType> microphoneActivationType;
    public ConfigEntry<Integer> outputBufferSize;
    public ConfigEntry<Integer> audioPacketThreshold;
    public ConfigEntry<Integer> deactivationDelay;
    public ConfigEntry<String> microphone;
    public ConfigEntry<String> speaker;
    public ConfigEntry<Boolean> muted;
    public ConfigEntry<Boolean> disabled;
    public ConfigEntry<Boolean> hideIcons;
    public ConfigEntry<Boolean> showGroupHUD;
    public ConfigEntry<Boolean> showOwnGroupIcon;
    public ConfigEntry<Double> groupHudIconScale;
    public ConfigEntry<GroupPlayerIconOrientation> groupPlayerIconOrientation;
    public ConfigEntry<Integer> groupPlayerIconPosX;
    public ConfigEntry<Integer> groupPlayerIconPosY;
    public ConfigEntry<Integer> hudIconPosX;
    public ConfigEntry<Integer> hudIconPosY;
    public ConfigEntry<Double> hudIconScale;
    public ConfigEntry<String> recordingDestination;
    public ConfigEntry<Boolean> denoiser;
    public ConfigEntry<Boolean> runLocalServer;
    public ConfigEntry<Boolean> javaMicrophoneImplementation;
    public ConfigEntry<Boolean> macosMicrophoneWorkaround;
    public ConfigEntry<Boolean> showFakePlayersDisconnected;
    public ConfigEntry<Boolean> offlinePlayerVolumeAdjustment;
    public ConfigEntry<AudioType> audioType;
    public ConfigEntry<Boolean> useNatives;

}
