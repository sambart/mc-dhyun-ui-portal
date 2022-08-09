package com.dhyun.portal.net;

import com.dhyun.portal.voice.common.PlayerState;
import com.dhyun.portal.Teleport;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class PlayerStatePacket implements Packet<PlayerStatePacket> {

    public static final ResourceLocation PLAYER_STATE = new ResourceLocation(Teleport.MODID, "player_state");

    private PlayerState playerState;

    public PlayerStatePacket() {

    }

    public PlayerStatePacket(PlayerState playerState) {
        this.playerState = playerState;
    }

    public PlayerState getPlayerState() {
        return playerState;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return PLAYER_STATE;
    }

    @Override
    public PlayerStatePacket fromBytes(FriendlyByteBuf buf) {
        playerState = PlayerState.fromBytes(buf);
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        playerState.toBytes(buf);
    }

}
