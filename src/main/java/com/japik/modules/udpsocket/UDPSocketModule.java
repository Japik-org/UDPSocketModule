package com.japik.modules.udpsocket;

import com.japik.livecycle.AShortLiveCycleImpl;
import com.japik.livecycle.controller.ILiveCycleImplId;
import com.japik.livecycle.controller.LiveCycleController;
import com.japik.module.AModule;
import com.japik.module.IModuleConnectionSafe;
import com.japik.module.ModuleConnectionParams;
import com.japik.module.ModuleParams;
import com.japik.modules.packetpool.shared.IPacketPoolModuleConnection;
import com.japik.modules.udpsocket.shared.ISocketListener;
import com.japik.modules.udpsocket.shared.IUDPSocketModuleConnection;
import com.japik.settings.IntegerSettingListener;
import com.japik.settings.SettingListenerContainer;
import com.japik.settings.SettingListenerEventMask;
import com.japik.tick.AModuleTickRunnable;
import com.japik.tick.ITick;
import com.japik.tick.ITickGroup;
import com.japik.tick.Ticks;
import com.japik.utils.datagram.packet.DatagramPacketRecyclable;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class UDPSocketModule extends AModule<IUDPSocketModuleConnection> {
    private IModuleConnectionSafe<IPacketPoolModuleConnection> packetPoolModuleConnection;

    private BlockingQueue<DatagramPacketRecyclable> packetBuffer;

    private DatagramSocket socket;
    private ITickGroup tickGroup;
    private ITick tick;

    private ISocketListener listener;

    public UDPSocketModule(ModuleParams moduleParams) {
        super(moduleParams);
    }

    @Override
    public @NotNull IUDPSocketModuleConnection createModuleConnection(ModuleConnectionParams params) {
        return new UDPSocketModuleConnection(this, params);
    }

    @Override
    protected void initLiveCycleController(LiveCycleController liveCycleController) {
        super.initLiveCycleController(liveCycleController);
        liveCycleController.putImplAll(new UDPSocketModuleLiveCycleImpl(this));
    }

    public void setListener(ISocketListener listener) {
        this.listener = listener;
    }

    public BlockingQueue<DatagramPacketRecyclable> getPacketBuffer() {
        return packetBuffer;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    private final class UDPSocketModuleLiveCycleImpl extends AShortLiveCycleImpl implements ILiveCycleImplId {
        @Getter
        private final String name = "UDPSocketModuleLiveCycleImpl";
        @Getter @Setter
        private int priority = LiveCycleController.PRIORITY_NORMAL;

        private final UDPSocketModule module;

        private UDPSocketModuleLiveCycleImpl(UDPSocketModule module) {
            this.module = module;
        }

        @Override
        public void init() throws Throwable {
            packetPoolModuleConnection = setupModuleConnectionSafe(
                    settings.getOrDefault("connection-PacketPoolModule", "packetPool")
            );

            socket = new DatagramSocket(
                    settings.getIntOrDefault("socket-port", 0)
            );
            logger.info("Socket port is "+socket.getLocalPort());

            packetBuffer = new ArrayBlockingQueue<>(
                    settings.getIntOrDefault("buffer-size", 64)
            );

            settingsManager.setListener(new SettingListenerContainer(
                    "socket-timeout",
                    new IntegerSettingListener() {
                        @Override
                        public void apply2(String key, Integer val, SettingListenerEventMask eventMask) throws Throwable {
                             socket.setSoTimeout(val);
                        }
                    },
                    5000
            ));

            tickGroup = Ticks.newTickGroupPreMod(settings);
            tickGroup.getLiveCycle().init();

            tick = tickGroup.createTick(new AModuleTickRunnable<UDPSocketModule>(module, logger) {
                @Override
                public void tick(long dtms) throws Throwable {
                    final DatagramPacketRecyclable packet = packetPoolModuleConnection.getModuleConnection().getNextPacket();

                    try {
                        final DatagramPacket datagramPacket = packet.receive(socket);

                        if (listener != null) {
                            listener.process(packet);
                        } else {
                            if (!packetBuffer.offer(packet)) {
                                logger.warn("packetBuffer is full");
                            }
                        }

                    } catch (SocketTimeoutException socketTimeoutException) {
                        logger.info("SocketTimeoutException : " + socketTimeoutException.getMessage());
                        packet.recycle();

                    } catch (Exception e) {
                        packet.recycle();
                        throw e;
                    }
                }
            });
            tick.init();
        }

        @Override
        public void start() throws Throwable {
            if (socket.isClosed()){
                throw new IllegalStateException("Socket is closed");
            }

            tick.activate();
            tickGroup.getLiveCycle().start();
        }

        @Override
        public void stopForce() {
            tickGroup.getLiveCycle().stopForce();
            tick.inactivate();
        }

        @Override
        public void destroy() {
            socket.close();
            socket = null;

            packetBuffer = null;

            tick.destroy();
            tickGroup.getLiveCycle().destroy();

            packetPoolModuleConnection.close();
        }
    }
}
