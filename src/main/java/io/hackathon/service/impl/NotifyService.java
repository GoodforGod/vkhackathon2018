package io.hackathon.service.impl;

import io.hackathon.model.Color;
import io.hackathon.model.dao.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public class NotifyService {

    private final Logger logger = LoggerFactory.getLogger(NotifyService.class);

    /**
     * Device -> SendColorMsg Timestamp
     */
    private final ConcurrentMap<String, LocalDateTime> devicePingAwaitMap = new ConcurrentHashMap<>();

    private final Executor pingExecutor = Executors.newSingleThreadExecutor();
    private final Executor listenerExecutor = Executors.newSingleThreadExecutor();

    private final UdpBroadcaster broadcaster = new UdpBroadcaster();

    private final AtomicBoolean isActive = new AtomicBoolean(true);

    public void notifyWithColor(final List<Device> devices,
                                final String pathId,
                                final Color color) {
        this.pingExecutor.execute(() -> {
            final String msg = "H;" + pathId + ";" + color.asRgb() + "\r";
            this.broadcaster.broadcast(devices, msg)
                    .forEach(d -> devicePingAwaitMap.put(d, LocalDateTime.now()));
        });
    }

    public void notifyColorOff(final List<Device> devices,
                               final String pathId) {
        final String msg = "L;" + pathId + ";" + "\r";
        this.broadcaster.broadcast(devices, msg)
                .forEach(d -> devicePingAwaitMap.put(d, LocalDateTime.now()));
    }

    private Runnable createSocketListener() {
        return () -> {
            try {
                final byte[] buf = new byte[256];
                try (DatagramSocket socket = new DatagramSocket(76767)) {
                    while (isActive.get()) {
                        final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        final String received = new String(packet.getData(), 0, packet.getLength());
                        this.devicePingAwaitMap.remove(received);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
        };
    }

    @Scheduled(cron = "*/1 * * * * *")
    public void rePing() {
        // Add ping re send
    }
}
