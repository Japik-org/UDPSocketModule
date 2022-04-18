package com.pro100kryto.server.modules.udpsocket;

import com.pro100kryto.server.module.AModuleConnection;
import com.pro100kryto.server.module.ModuleConnectionParams;
import com.pro100kryto.server.modules.udpsocket.shared.ISocketListener;
import com.pro100kryto.server.modules.udpsocket.shared.IUDPSocketModuleConnection;
import com.pro100kryto.server.utils.datagram.packet.DatagramPacketRecyclable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public class UDPSocketModuleConnection
        extends AModuleConnection<UDPSocketModule, IUDPSocketModuleConnection>
        implements IUDPSocketModuleConnection {

    public UDPSocketModuleConnection(@NotNull UDPSocketModule module, ModuleConnectionParams params) {
        super(module, params);
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
    public DatagramPacketRecyclable getNextPacket() {
        return getModule().getPacketBuffer().poll();
    }

    @Override
    public void recycleAllPackets() {
        while (hasNextPacket()){
            getNextPacket().recycle();
        }
    }

    @Override
    public void send(DatagramPacketRecyclable packet) throws IOException {
        getModule().getSocket().send(packet.getDatagramPacket());
    }

    @Override
    public void sendAndRecycle(DatagramPacketRecyclable packet) throws IOException {
        getModule().getSocket().send(packet.getDatagramPacket());
        packet.recycle();
    }
}
