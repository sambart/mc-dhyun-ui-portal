package com.dhyun.portal.net;

import com.dhyun.portal.Teleport;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public class RequestSecretPacket implements Packet<RequestSecretPacket> {

    public static final ResourceLocation REQUEST_SECRET = new ResourceLocation(Teleport.MODID, "request_secret");

    private int compatibilityVersion;

    public RequestSecretPacket() {

    }

    public RequestSecretPacket(int compatibilityVersion) {
        this.compatibilityVersion = compatibilityVersion;
    }

    public int getCompatibilityVersion() {
        return compatibilityVersion;
    }

    @Override
    public ResourceLocation getIdentifier() {
        return REQUEST_SECRET;
    }

    @Override
    public RequestSecretPacket fromBytes(FriendlyByteBuf buf) {
        compatibilityVersion = buf.readInt();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(compatibilityVersion);
    }

}
