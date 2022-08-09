package de.maxhenkel.voicechat.net;

import com.dhyun.portal.Teleport;
import com.dhyun.portal.net.Channel;
import com.dhyun.portal.net.NetManager;
import com.dhyun.portal.net.Packet;
import com.dhyun.portal.net.RequestSecretPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

public class ForgeNetManager extends NetManager {

    @Override
    public <T extends Packet<T>> Channel<T> registerReceiver(Class<T> packetType, boolean toClient, boolean toServer) {
        Channel<T> c = new Channel<>();
        try {
            T dummyPacket = packetType.getDeclaredConstructor().newInstance();
            EventNetworkChannel channel = NetworkRegistry.newEventChannel(
                    dummyPacket.getIdentifier(),
                    () -> NetworkRegistry.ACCEPTVANILLA,
                    NetworkRegistry.ACCEPTVANILLA::equals,
                    NetworkRegistry.ACCEPTVANILLA::equals
            );
            channel.addListener(event -> {
                if (event.getPayload() == null) {
                    return;
                }
                NetworkEvent.Context context = event.getSource().get();
                if (toServer && context.getDirection().equals(NetworkDirection.PLAY_TO_SERVER)) {
                    try {
                        if (!Teleport.SERVER.isCompatible(context.getSender()) && !packetType.equals(RequestSecretPacket.class)) {
                            return;
                        }
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        c.onServerPacket(context.getSender().server, context.getSender(), context.getSender().connection, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        T packet = packetType.getDeclaredConstructor().newInstance();
                        packet.fromBytes(event.getPayload());
                        onClientPacket(c, packet);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
        return c;
    }

    @OnlyIn(Dist.CLIENT)
    private <T extends Packet<T>> void onClientPacket(Channel<T> channel, T packet) {
        channel.onClientPacket(Minecraft.getInstance(), Minecraft.getInstance().getConnection(), packet);
    }

}
