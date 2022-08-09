package com.dhyun.portal.voice.common;

import net.minecraft.network.FriendlyByteBuf;

public interface Packet<T extends Packet> {

    T fromBytes(FriendlyByteBuf buf);

    void toBytes(FriendlyByteBuf buf);

    default long getTTL() {
        return 10_000L;
    }

}
