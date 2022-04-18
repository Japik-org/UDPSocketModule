package com.pro100kryto.server.modules.udpsocket.shared;

import com.pro100kryto.server.utils.datagram.packet.DatagramPacketRecyclable;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISocketListener extends Remote {
    void process(DatagramPacketRecyclable packet) throws RemoteException;
}
