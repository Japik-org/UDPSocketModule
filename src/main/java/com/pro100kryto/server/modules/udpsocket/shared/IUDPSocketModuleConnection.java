package com.pro100kryto.server.modules.udpsocket.shared;

import com.pro100kryto.server.module.IModuleConnection;
import com.pro100kryto.server.utils.datagram.packet.DatagramPacketRecyclable;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.rmi.RemoteException;

public interface IUDPSocketModuleConnection extends IModuleConnection {
    void setPacketListener(ISocketListener listener) throws RemoteException;
    void removePacketListener() throws RemoteException;

    boolean hasNextPacket() throws RemoteException;
    @Nullable
    DatagramPacketRecyclable getNextPacket() throws RemoteException;
    void recycleAllPackets() throws RemoteException;

    void send(DatagramPacketRecyclable packet) throws RemoteException, IOException;
    void sendAndRecycle(DatagramPacketRecyclable packet) throws RemoteException, IOException;
}
