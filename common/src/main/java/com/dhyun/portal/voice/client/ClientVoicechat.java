package com.dhyun.portal.voice.client;

import com.dhyun.portal.voice.client.speaker.SpeakerException;
import com.dhyun.portal.voice.common.SoundPacket;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.debug.CooldownTimer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientVoicechat {

    @Nullable
    private SoundManager soundManager;
    private final Map<UUID, AudioChannel> audioChannels;
    private final TalkCache talkCache;
    @Nullable
    private MicThread micThread;
    @Nullable
    private ClientVoicechatConnection connection;
    @Nullable
    private AudioRecorder recorder;

    public ClientVoicechat() {
        this.talkCache = new TalkCache();
        try {
            reloadSoundManager();
        } catch (SpeakerException e) {
            Teleport.LOGGER.error("Failed to start sound manager: {}", e.getMessage());
            ClientManager.sendPlayerError("message.voicechat.speaker_unavailable", e);
        }
        this.audioChannels = new HashMap<>();
    }

    public void onVoiceChatConnected(ClientVoicechatConnection connection) {
        startMicThread(connection);
    }

    public void onVoiceChatDisconnected() {
        closeMicThread();
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public void connect(InitializationData data) throws Exception {
        Teleport.LOGGER.info("Connecting to server: '" + data.getServerIP() + ":" + data.getServerPort() + "'");
        connection = new ClientVoicechatConnection(this, data);
        connection.start();
    }

    public void processSoundPacket(SoundPacket packet) {
        if (connection == null) {
            return;
        }
        synchronized (audioChannels) {
            if (!ClientManager.getPlayerStateManager().isDisabled()) {
                AudioChannel sendTo = audioChannels.get(packet.getSender());
                if (sendTo == null) {
                    try {
                        AudioChannel ch = new AudioChannel(this, connection.getData(), packet.getSender());
                        ch.addToQueue(packet);
                        ch.start();
                        audioChannels.put(packet.getSender(), ch);
                    } catch (NativeDependencyException e) {
                        CooldownTimer.run("decoder_unavailable", () -> {
                            Teleport.LOGGER.error("Failed to create audio channel: {}", e.getMessage());
                            ClientManager.sendPlayerError("message.voicechat.playback_unavailable", e);
                        });
                    }
                } else {
                    sendTo.addToQueue(packet);
                }
            }

            audioChannels.values().stream().filter(AudioChannel::canKill).forEach(AudioChannel::closeAndKill);
            audioChannels.entrySet().removeIf(entry -> entry.getValue().isClosed());
        }
    }

    public void reloadSoundManager() throws SpeakerException {
        if (soundManager != null) {
            soundManager.close();
        }
        soundManager = new SoundManager(TeleportClient.CLIENT_CONFIG.speaker.get());
    }

    public void reloadAudio() {
        Teleport.LOGGER.info("Reloading audio");

        closeMicThread();

        synchronized (audioChannels) {
            Teleport.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
            try {
                Teleport.LOGGER.info("Restarting sound manager");
                reloadSoundManager();
            } catch (SpeakerException e) {
                e.printStackTrace();
            }
        }

        Teleport.LOGGER.info("Starting microphone thread");
        if (connection != null) {
            startMicThread(connection);
        }
    }

    private void startMicThread(ClientVoicechatConnection connection) {
        if (micThread != null) {
            micThread.close();
        }
        try {
            micThread = new MicThread(this, connection);
            micThread.start();
        } catch (Exception e) {
            Teleport.LOGGER.error("Failed to start microphone thread: {}", e.getMessage());
            ClientManager.sendPlayerError("message.voicechat.microphone_unavailable", e);
        }
    }

    public void closeMicThread() {
        if (micThread != null) {
            Teleport.LOGGER.info("Stopping microphone thread");
            micThread.close();
            micThread = null;
        }
    }

    public void toggleRecording() {
        setRecording(recorder == null);
    }

    public void setRecording(boolean recording) {
        if (recording == (recorder != null)) {
            return;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (recording) {
            if (connection == null || !connection.getData().allowRecording()) {
                if (player != null) {
                    player.displayClientMessage(Component.translatable("message.voicechat.recording_disabled"), true);
                }
                return;
            }
            recorder = AudioRecorder.create();
            if (player != null) {
                player.displayClientMessage(Component.translatable("message.voicechat.recording_started").withStyle(ChatFormatting.DARK_RED), true);
            }
            return;
        }

        AudioRecorder rec = recorder;
        recorder = null;
        if (player != null) {
            player.displayClientMessage(Component.translatable("message.voicechat.recording_stopped").withStyle(ChatFormatting.DARK_RED), true);
        }
        rec.saveAndClose();
    }

    @Nullable
    public MicThread getMicThread() {
        return micThread;
    }

    @Nullable
    public ClientVoicechatConnection getConnection() {
        return connection;
    }

    @Nullable
    public SoundManager getSoundManager() {
        return soundManager;
    }

    public TalkCache getTalkCache() {
        return talkCache;
    }

    @Nullable
    public AudioRecorder getRecorder() {
        return recorder;
    }

    public void close() {
        synchronized (audioChannels) {
            Teleport.LOGGER.info("Clearing audio channels");
            audioChannels.forEach((uuid, audioChannel) -> audioChannel.closeAndKill());
            audioChannels.clear();
        }

        if (soundManager != null) {
            soundManager.close();
        }

        closeMicThread();

        if (connection != null) {
            connection.close();
        }

        if (recorder != null) {
            AudioRecorder rec = recorder;
            recorder = null;
            rec.saveAndClose();
        }
    }

}
