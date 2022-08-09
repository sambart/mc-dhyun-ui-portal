package com.dhyun.portal.net;

import com.dhyun.portal.Teleport;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class LeaveGroupPacket implements Packet<LeaveGroupPacket> {

    public static final ResourceLocation LEAVE_GROUP = new ResourceLocation(Teleport.MODID, "leave_group");

    public LeaveGroupPacket() {

    }

    @Override
    public ResourceLocation getIdentifier() {
        return LEAVE_GROUP;
    }

    @Override
    public LeaveGroupPacket fromBytes(FriendlyByteBuf buf) {
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {

    }

}
