package com.dhyun.portal.net;

import com.dhyun.portal.voice.common.PlayerState;
import com.dhyun.portal.Teleport;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerStatesPacket implements Packet<PlayerStatesPacket> {

    private Map<UUID, PlayerState> playerStates;

    public static final ResourceLocation PLAYER_STATES = new ResourceLocation(Teleport.MODID, "player_states");

    public PlayerStatesPacket() {

    }

    public PlayerStatesPacket(Map<UUID, PlayerState> playerStates) {
        this.playerStates = playerStates;
    }

    public Map<UUID, PlayerState> getPlayerStates() {
        return playerStates;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return PLAYER_STATES;
    }

    @Override
    public PlayerStatesPacket fromBytes(FriendlyByteBuf buf) {
        playerStates = new HashMap<>();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            PlayerState playerState = PlayerState.fromBytes(buf);
            playerStates.put(playerState.getUuid(), playerState);
        }

        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(playerStates.size());
        for (Map.Entry<UUID, PlayerState> entry : playerStates.entrySet()) {
            entry.getValue().toBytes(buf);
        }
    }

}
