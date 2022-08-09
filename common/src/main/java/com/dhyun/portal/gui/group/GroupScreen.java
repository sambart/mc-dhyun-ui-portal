package com.dhyun.portal.gui.group;

import com.dhyun.portal.gui.tooltips.DisableTooltipSupplier;
import com.dhyun.portal.gui.tooltips.HideGroupHudTooltipSupplier;
import com.dhyun.portal.gui.tooltips.MuteTooltipSupplier;
import com.dhyun.portal.gui.widgets.ImageButton;
import com.dhyun.portal.gui.widgets.ListScreenBase;
import com.dhyun.portal.gui.widgets.ToggleImageButton;
import com.dhyun.portal.net.LeaveGroupPacket;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.voice.client.ClientManager;
import com.dhyun.portal.voice.client.ClientPlayerStateManager;
import com.dhyun.portal.voice.client.MicrophoneActivationType;
import com.dhyun.portal.voice.common.ClientGroup;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.dhyun.portal.Teleport;
import com.dhyun.portal.TeleportClient;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Collections;

public class GroupScreen extends ListScreenBase {

    protected static final ResourceLocation TEXTURE = new ResourceLocation(Teleport.MODID, "textures/gui/gui_group.png");
    protected static final ResourceLocation LEAVE = new ResourceLocation(Teleport.MODID, "textures/icons/leave.png");
    protected static final ResourceLocation MICROPHONE = new ResourceLocation(Teleport.MODID, "textures/icons/microphone_button.png");
    protected static final ResourceLocation SPEAKER = new ResourceLocation(Teleport.MODID, "textures/icons/speaker_button.png");
    protected static final ResourceLocation GROUP_HUD = new ResourceLocation(Teleport.MODID, "textures/icons/group_hud_button.png");
    protected static final Component TITLE = Component.translatable("gui.voicechat.group.title");
    protected static final Component LEAVE_GROUP = Component.translatable("message.voicechat.leave_group");

    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected GroupList groupList;
    protected int units;

    protected final ClientGroup group;
    protected ToggleImageButton mute;
    protected ToggleImageButton disable;
    protected ToggleImageButton showHUD;
    protected ImageButton leave;

    public GroupScreen(ClientGroup group) {
        super(TITLE, 236, 0);
        this.group = group;
    }

    @Override
    protected void init() {
        super.init();
        guiLeft = guiLeft + 2;
        guiTop = 32;
        int minUnits = Mth.ceil((float) (CELL_HEIGHT + 4) / (float) UNIT_SIZE);
        units = Math.max(minUnits, (height - HEADER_SIZE - FOOTER_SIZE - guiTop * 2) / UNIT_SIZE);
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;

        ClientPlayerStateManager stateManager = ClientManager.getPlayerStateManager();

        if (groupList != null) {
            groupList.updateSize(width, height, guiTop + HEADER_SIZE, guiTop + HEADER_SIZE + units * UNIT_SIZE);
        } else {
            groupList = new GroupList(this, width, height, guiTop + HEADER_SIZE, guiTop + HEADER_SIZE + units * UNIT_SIZE, CELL_HEIGHT);
        }
        addWidget(groupList);

        int buttonY = guiTop + ySize - 20 - 7;
        int buttonSize = 20;

        mute = new ToggleImageButton(guiLeft + 7, buttonY, MICROPHONE, stateManager::isMuted, button -> {
            stateManager.setMuted(!stateManager.isMuted());
        }, new MuteTooltipSupplier(this, stateManager));
        addRenderableWidget(mute);

        disable = new ToggleImageButton(guiLeft + 7 + buttonSize + 3, buttonY, SPEAKER, stateManager::isDisabled, button -> {
            stateManager.setDisabled(!stateManager.isDisabled());
        }, new DisableTooltipSupplier(this, stateManager));
        addRenderableWidget(disable);

        showHUD = new ToggleImageButton(guiLeft + 7 + (buttonSize + 3) * 2, buttonY, GROUP_HUD, TeleportClient.CLIENT_CONFIG.showGroupHUD::get, button -> {
            TeleportClient.CLIENT_CONFIG.showGroupHUD.set(!TeleportClient.CLIENT_CONFIG.showGroupHUD.get()).save();
        }, new HideGroupHudTooltipSupplier(this));
        addRenderableWidget(showHUD);

        leave = new ImageButton(guiLeft + xSize - buttonSize - 7, buttonY, LEAVE, button -> {
            NetManager.sendToServer(new LeaveGroupPacket());
            minecraft.setScreen(new JoinGroupScreen());
        }, (button, matrices, mouseX, mouseY) -> {
            renderTooltip(matrices, Collections.singletonList(LEAVE_GROUP.getVisualOrderText()), mouseX, mouseY);
        });
        addRenderableWidget(leave);

        checkButtons();
    }

    @Override
    public void tick() {
        super.tick();
        checkButtons();
        groupList.tick();
    }

    private void checkButtons() {
        mute.active = TeleportClient.CLIENT_CONFIG.microphoneActivationType.get().equals(MicrophoneActivationType.VOICE);
        showHUD.active = !TeleportClient.CLIENT_CONFIG.hideIcons.get();
    }

    @Override
    public void renderBackground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        blit(poseStack, guiLeft, guiTop, 0, 0, xSize, HEADER_SIZE);
        for (int i = 0; i < units; i++) {
            blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * i, 0, HEADER_SIZE, xSize, UNIT_SIZE);
        }
        blit(poseStack, guiLeft, guiTop + HEADER_SIZE + UNIT_SIZE * units, 0, HEADER_SIZE + UNIT_SIZE, xSize, FOOTER_SIZE);
        blit(poseStack, guiLeft + 10, guiTop + HEADER_SIZE + 6 - 2, xSize, 0, 12, 12);
    }

    @Override
    public void renderForeground(PoseStack poseStack, int mouseX, int mouseY, float delta) {
        MutableComponent title = Component.literal(group.getName());
        font.draw(poseStack, title, guiLeft + xSize / 2 - font.width(title) / 2, guiTop + 5, FONT_COLOR);

        groupList.render(poseStack, mouseX, mouseY, delta);
    }

}
