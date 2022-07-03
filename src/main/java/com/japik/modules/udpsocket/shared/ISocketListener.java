package com.japik.modules.udpsocket.shared;

import com.japik.utils.datagram.packet.DatagramPacketRecyclable;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISocketListener extends Remote {
    void process(DatagramPacketRecyclable packet) throws RemoteException;
}
