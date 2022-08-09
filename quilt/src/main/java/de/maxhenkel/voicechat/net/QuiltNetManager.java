package de.maxhenkel.voicechat.net;

import com.dhyun.portal.Teleport;
import com.dhyun.portal.intercompatibility.CommonCompatibilityManager;
import com.dhyun.portal.net.Channel;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.Packet;
import com.dhyun.portal.net.RequestSecretPacket;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

public class QuiltNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            if (toServer) {
                ServerPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (server, player, handler, buf, responseSender) -> {
                    try {
                        if (!Teleport.SERVER.isCompatible(player) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        c.onServerPacket(server, player, handler, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (toClient && !CommonCompatibilityManager.INSTANCE.isDedicatedServer()) {
                ClientPlayNetworking.registerGlobalReceiver(dummyPacket.getIdentifier(), (client, handler, buf, responseSender) -> {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(buf);
                        client.execute(() -> c.onClientPacket(client, handler, packet));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

}
