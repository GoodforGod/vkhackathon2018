package io.hackathon.service.impl;

import io.hackathon.manager.impl.DeviceManager;
import io.hackathon.model.Color;
import io.hackathon.model.UdpBox;
import io.hackathon.model.dao.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class NotifyService {

    private final Logger logger = LoggerFactory.getLogger(NotifyService.class);

    @Value("${server.udp.port:45050}")
    private int serverPort;

    private enum Command {
        ALIVE('A'),
        RETRY('R'),
        HIGH('H'),
        LOW('L');

        private final Character cmd;

        Command(Character cmd) {
            this.cmd = cmd;
        }

        public Character getCmd() {
            return cmd;
        }

        public static String buildHigh(String pathId, Color color) {
            return build(pathId + ";" + color.asRgb(), HIGH);
        }

        public static String buildLow(String pathId) {
            return build(pathId + ";", LOW);
        }

        private static String build(String message, Command command) {
            return command.getCmd() + message + "\r";
        }
    }

    /**
     * Device -> SendColorMsg Timestamp
     */
    private final ConcurrentMap<String, Map<String, UdpBox>> devicePingAwaitMap = new ConcurrentHashMap<>();

    private final Executor pingExecutor = Executors.newSingleThreadExecutor();
    private final Executor listenerExecutor = Executors.newSingleThreadExecutor();

    private final UdpBroadcaster broadcaster = new UdpBroadcaster();

    private final AtomicBoolean isActive = new AtomicBoolean(true);

    @Autowired
    private DeviceManager deviceManager;

    public NotifyService() {
        this.listenerExecutor.execute(createListenerRunner());
    }

    public void notifyWithColor(final List<Device> devices,
                                final String pathId,
                                final Color color) {
        this.pingExecutor.execute(() -> {
            final String msg = Command.buildHigh(pathId, color);
            this.broadcaster.broadcastToDevices(devices, msg)
                    .forEach(d -> {
                        Map<String, UdpBox> map = devicePingAwaitMap.computeIfAbsent(d, (k) -> new HashMap<>());
                        map.put(pathId, new UdpBox(msg));
                        devicePingAwaitMap.put(d, map);
                    });
        });
    }

    public void notifyColorOff(final List<Device> devices,
                               final String pathId) {
        final String msg = Command.buildLow(pathId);
        this.broadcaster.broadcastToDevices(devices, msg)
                .forEach(d -> {
                    Map<String, UdpBox> map = devicePingAwaitMap.computeIfAbsent(d, (k) -> new HashMap<>());
                    map.put(pathId, new UdpBox(msg));
                    devicePingAwaitMap.put(d, map);
                });
    }

    private Runnable createListenerRunner() {
        return () -> {
            try {
                final byte[] buf = new byte[256];
                try (DatagramSocket socket = new DatagramSocket(serverPort)) {
                    while (isActive.get()) {
                        final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        final String received = new String(packet.getData(), 0, packet.getLength());
                        if (received.isEmpty())
                            continue;

                        processReply(received);
                    }
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
        };
    }

    private void processReply(String received) {
        switch (received.charAt(0)) {
            case 'A':
                String[] splitAlive = received.split(";");
                if(splitAlive.length < 3)
                    break;

                deviceManager.alive(splitAlive[2]);
                break;
            case 'R':
                String[] splitRetry = received.split(";");
                if(splitRetry.length < 3)
                    break;
                Map<String, UdpBox> map = this.devicePingAwaitMap.get(splitRetry[2]);
                map.remove(splitRetry[1]);
                break;
            default:
        }
    }

    private boolean noAnswer(LocalDateTime dateTime, LocalDateTime now) {
        return dateTime.until(now, ChronoUnit.SECONDS) > 3;
    }

    @Scheduled(cron = "*/3 * * * * *")
    public void pingRetry() {
        final LocalDateTime now = LocalDateTime.now();
        final Map<String, List<String>> notifyDevices = new HashMap<>();
        for (Map.Entry<String, Map<String, UdpBox>> entry : devicePingAwaitMap.entrySet()) {
            final List<String> msg = entry.getValue().entrySet().stream()
                    .filter(e -> noAnswer(e.getValue().getTimestamp(), now))
                    .map(e -> e.getValue().getMsg())
                    .collect(Collectors.toList());

            notifyDevices.put(entry.getKey(), msg);
        }

        notifyDevices.forEach((k, v) -> {
            final List<String> dev = Collections.singletonList(k);
            v.forEach(msg -> broadcaster.broadcast(dev, msg));
        });
    }
}
