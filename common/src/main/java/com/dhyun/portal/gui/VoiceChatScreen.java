package com.dhyun.portal.gui;

import com.dhyun.portal.gui.group.JoinGroupScreen;
import com.dhyun.portal.gui.tooltips.DisableTooltipSupplier;
import com.dhyun.portal.gui.tooltips.HideTooltipSupplier;
import com.dhyun.portal.gui.tooltips.MuteTooltipSupplier;
import com.dhyun.portal.gui.tooltips.RecordingTooltipSupplier;
import com.dhyun.portal.gui.volume.AdjustVolumesScreen;
import com.dhyun.portal.gui.widgets.ImageButton;
import com.dhyun.portal.gui.widgets.ToggleImageButton;
import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import com.dhyun.portal.gui.group.GroupScreen;
import de.maxhenkel.voicechat.voice.client.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

public class VoiceChatScreen extends VoiceChatScreenBase {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Teleport.MODID, "textures/gui/gui_voicechat.png");
    private static final ResourceLocation MICROPHONE = new ResourceLocation(Teleport.MODID, "textures/icons/microphone_button.png");
    private static final ResourceLocation HIDE = new ResourceLocation(Teleport.MODID, "textures/icons/hide_button.png");
    private static final ResourceLocation VOLUMES = new ResourceLocation(Teleport.MODID, "textures/icons/adjust_volumes.png");
    private static final ResourceLocation SPEAKER = new ResourceLocation(Teleport.MODID, "textures/icons/speaker_button.png");
    private static final ResourceLocation RECORD = new ResourceLocation(Teleport.MODID, "textures/icons/record_button.png");
    private static final Component TITLE = Component.translatable("gui.voicechat.voice_chat.title");
    private static final Component SETTINGS = Component.translatable("message.voicechat.settings");
    private static final Component GROUP = Component.translatable("message.voicechat.group");
    public static final Component ADJUST_PLAYER_VOLUMES = Component.translatable("message.voicechat.adjust_volumes");

    private ToggleImageButton mute;
    private ToggleImageButton disable;
    private HoverArea recordingHoverArea;

    private ClientPlayerStateManager stateManager;

    public VoiceChatScreen() {
        super(TITLE, 195, 76);
        stateManager = ClientManager.getPlayerStateManager();
    }

    @Override
    protected void init() {
        super.init();
        @Nullable ClientVoicechat client = ClientManager.getClient();

        mute = new ToggleImageButton(guiLeft + 6, guiTop + ySize - 6 - 20, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addRenderableWidget(mute);

        disable = new ToggleImageButton(guiLeft + 6 + 20 + 2, guiTop + ySize - 6 - 20, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addRenderableWidget(disable);

        ImageButton volumes = new ImageButton(guiLeft + 6 + 20 + 2 + 20 + 2, guiTop + ySize - 6 - 20, VOLUMES, button -> {
            minecraft.setScreen(new AdjustVolumesScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, ADJUST_PLAYER_VOLUMES, mouseX, mouseY);
        });
        addRenderableWidget(volumes);

        if (client != null) {
            if (client.getRecorder() != null || (client.getConnection() != null && client.getConnection().getData().allowRecording())) {
                ToggleImageButton record = new ToggleImageButton(guiLeft + xSize - 6 - 20 - 2 - 20, guiTop + ySize - 6 - 20, RECORD, () -> ClientManager.getClient() != null && ClientManager.getClient().getRecorder() != null, button -> toggleRecording(), new RecordingTooltipSupplier(this));
                addRenderableWidget(record);
            }
        }

        ToggleImageButton hide = new ToggleImageButton(guiLeft + xSize - 6 - 20, guiTop + ySize - 6 - 20, HIDE, TeleportClient.CLIENT_CONFIG.hideIcons::get, button -> {
            TeleportClient.CLIENT_CONFIG.hideIcons.set(!TeleportClient.CLIENT_CONFIG.hideIcons.get()).save();
        }, new HideTooltipSupplier(this));
        addRenderableWidget(hide);

        Button settings = new Button(guiLeft + 6, guiTop + 6 + 15, 75, 20, SETTINGS, button -> {
            minecraft.setScreen(new VoiceChatSettingsScreen());
        });
        addRenderableWidget(settings);

        Button group = new Button(guiLeft + xSize - 6 - 75 + 1, guiTop + 6 + 15, 75, 20, GROUP, button -> {
            if (stateManager.isInGroup()) {
                minecraft.setScreen(new GroupScreen(stateManager.getGroup()));
            } else {
                minecraft.setScreen(new JoinGroupScreen());
            }
        });
        addRenderableWidget(group);

        group.active = client != null && client.getConnection() != null && client.getConnection().getData().groupsEnabled();
        recordingHoverArea = new HoverArea(6 + 20 + 2 + 20 + 2 + 20 + 2, ySize - 6 - 20, xSize - ((6 + 20 + 2 + 20 + 2) * 2 + 20 + 2), 20);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
    }

    private void checkButtons() {
        mute.active = MuteTooltipSupplier.canMuteMic();
        disable.active = stateManager.canEnable();
    }

    private void toggleRecording() {
        ClientVoicechat c = ClientManager.getClient();
        if (c == null) {
            return;
        }
        c.toggleRecording();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == ClientCompatibilityManager.INSTANCE.getBoundKeyOf(KeyEvents.KEY_VOICE_CHAT).getValue()) {
            minecraft.setScreen(null);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, ySize);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        int titleWidth = font.width(TITLE);
        font.draw(poseStack, TITLE.getVisualOrderText(), (float) (guiLeft + (xSize - titleWidth) / 2), guiTop + 7, FONT_COLOR);

        ClientVoicechat client = ClientManager.getClient();
        if (client != null && client.getRecorder() != null) {
            AudioRecorder recorder = client.getRecorder();
            MutableComponent time = Component.literal(recorder.getDuration());
            font.draw(poseStack, time.withStyle(ChatFormatting.DARK_RED), guiLeft + recordingHoverArea.getPosX() + recordingHoverArea.getWidth() / 2F - font.width(time) / 2F, guiTop + recordingHoverArea.getPosY() + recordingHoverArea.getHeight() / 2F - font.lineHeight / 2F, 0);

            if (recordingHoverArea.isHovered(guiLeft, guiTop, mouseX, mouseY)) {
                renderTooltip(poseStack, Component.translatable("message.voicechat.storage_size", recorder.getStorage()), mouseX, mouseY);
            }
        }
    }

}
