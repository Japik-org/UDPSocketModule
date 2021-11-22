package com.pro100kryto.server.modules.udpsocket;

import com.pro100kryto.server.logger.ILogger;
import com.pro100kryto.server.module.AModuleConnection;
import com.pro100kryto.server.modules.udpsocket.connection.ISocketListener;
import com.pro100kryto.server.modules.udpsocket.connection.IUDPSocketModuleConnection;
import com.pro100kryto.server.utils.datagram.packet.Packet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class UDPSocketModuleConnection
        extends AModuleConnection<UDPSocketModule, IUDPSocketModuleConnection>
        implements IUDPSocketModuleConnection {

    public UDPSocketModuleConnection(@NotNull UDPSocketModule module, ILogger logger) {
        super(module, logger);
    }


    @Override
    public void setPacketListener(ISocketListener listener) {
        getModule().setListener(listener);
    }

    @Override
    public void removePacketListener() {
        getModule().setListener(null);
    }

    @Override
    public boolean hasNextPacket() {
        return !getModule().getPacketBuffer().isEmpty();
    }

    @Override
    @Nullable
    public Packet getNextPacket() {
        return getModule().getPacketBuffer().poll();
    }

    @Override
    public void recycleAllPackets() {
        while (hasNextPacket()){
            getNextPacket().recycle();
        }
    }

    @Override
    public void send(Packet packet) throws IOException {
        getModule().getSocket().send(packet.asDatagramPacket());
    }

    @Override
    public void sendAndRecycle(Packet packet) throws IOException {
        getModule().getSocket().send(packet.asDatagramPacket());
        packet.recycle();
    }
}
