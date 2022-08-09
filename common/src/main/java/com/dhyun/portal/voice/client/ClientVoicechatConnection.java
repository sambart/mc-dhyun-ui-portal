package com.dhyun.portal.voice.client;

import com.dhyun.portal.intercompatibility.ClientCompatibilityManager;
import com.dhyun.portal.plugins.PluginManager;
import com.dhyun.portal.voice.common.*;
import com.dhyun.portal.Teleport;
import de.maxhenkel.voicechat.api.ClientVoicechatSocket;
import de.maxhenkel.voicechat.voice.common.*;

import java.net.InetAddress;
import java.net.InetSocketAddress;

public class ClientVoicechatConnection extends Thread {

    private ClientVoicechat client;
    private final InitializationData data;
    private final ClientVoicechatSocket socket;
    private final InetAddress address;
    private boolean running;
    private boolean authenticated;
    private final AuthThread authThread;
    private long lastKeepAlive;

    public ClientVoicechatConnection(ClientVoicechat client, InitializationData data) throws Exception {
        this.client = client;
        this.data = data;
        this.address = InetAddress.getByName(data.getServerIP());
        this.socket = PluginManager.instance().getClientSocketImplementation();
        this.lastKeepAlive = -1;
        this.running = true;
        this.authThread = new AuthThread();
        this.authThread.start();
        setDaemon(true);
        setName("VoiceChatConnectionThread");
        this.socket.open();
    }

    public InitializationData getData() {
        return data;
    }

    public InetAddress getAddress() {
        return address;
    }

    public ClientVoicechatSocket getSocket() {
        return socket;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void run() {
        try {
            while (running) {
                NetworkMessage in = NetworkMessage.readPacketClient(socket.read(), this);
                if (in == null) {
                    continue;
                } else if (in.getPacket() instanceof AuthenticateAckPacket) {
                    if (!authenticated) {
                        Teleport.LOGGER.info("Server acknowledged authentication");
                        authenticated = true;
                        ClientCompatibilityManager.INSTANCE.emitVoiceChatConnectedEvent(this);
                        lastKeepAlive = System.currentTimeMillis();
                    }
                } else if (in.getPacket() instanceof SoundPacket packet) {
                    client.processSoundPacket(packet);
                } else if (in.getPacket() instanceof PingPacket packet) {
                    Teleport.LOGGER.info("Received ping {}, sending pong...", packet.getId());
                    sendToServer(new NetworkMessage(packet));
                } else if (in.getPacket() instanceof KeepAlivePacket) {
                    lastKeepAlive = System.currentTimeMillis();
                    sendToServer(new NetworkMessage(new KeepAlivePacket()));
                }
            }
        } catch (InterruptedException ignored) {
        } catch (Exception e) {
            if (running) {
                Teleport.LOGGER.error("Failed to process packet from server: {}", e.getMessage(), e);
            }
        }
    }

    public void close() {
        Teleport.LOGGER.info("Disconnecting voicechat");
        running = false;

        socket.close();
        authThread.close();
    }

    public boolean isConnected() {
        return running && !socket.isClosed();
    }

    public void sendToServer(NetworkMessage message) throws Exception {
        if (!isConnected()) {
            return; // Ignore sending packets when connection is closed
        }
        socket.send(message.writeClient(this), new InetSocketAddress(address, data.getServerPort()));
    }

    public void checkTimeout() {
        if (lastKeepAlive >= 0 && System.currentTimeMillis() - lastKeepAlive > data.getKeepAlive() * 10L) {
            Teleport.LOGGER.info("Connection timeout");
            disconnect();
        }
    }

    public void disconnect() {
        ClientCompatibilityManager.INSTANCE.emitVoiceChatDisconnectedEvent();
    }

    private class AuthThread extends Thread {
        private boolean running;

        public AuthThread() {
            this.running = true;
            setDaemon(true);
            setName("VoiceChatAuthenticationThread");
        }

        @Override
        public void run() {
            while (running && !authenticated) {
                try {
                    Teleport.LOGGER.info("Trying to authenticate voice connection");
                    sendToServer(new NetworkMessage(new AuthenticatePacket(data.getPlayerUUID(), data.getSecret())));
                } catch (Exception e) {
                    if (!socket.isClosed()) {
                        Teleport.LOGGER.error("Failed to authenticate voice connection: {}", e.getMessage());
                    }
                }
                Utils.sleep(1000);
            }
        }

        public void close() {
            running = false;
        }
    }

}