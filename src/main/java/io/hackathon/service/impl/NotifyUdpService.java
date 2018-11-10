package io.hackathon.service.impl;

import io.hackathon.manager.impl.DeviceManager;
import io.hackathon.manager.impl.NotifyHttpManager;
import io.hackathon.model.Color;
import io.hackathon.model.UdpBox;
import io.hackathon.model.dao.Device;
import io.hackathon.service.INotifyService;
import io.hackathon.storage.impl.DeviceStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
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
public class NotifyUdpService implements INotifyService {

    private final Logger logger = LoggerFactory.getLogger(NotifyUdpService.class);

    @Value("${SERVER_UDP_PORT:45050}")
    private int serverListenerPort;

    public enum Command {
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

        private static String supplyWithZeros(String str, int max) {
            StringBuilder added = new StringBuilder();
            for (int i = 0; i < max - str.length(); i++) {
                added.append("0");
            }

            return added.toString() + str;
        }

        private static String convertPathId(String pathId) {
            String[] split = pathId.split("_");
            StringBuilder builder  = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                if (i == 2)
                    builder.append(supplyWithZeros(split[i], 8));
                else
                    builder.append(supplyWithZeros(split[i], 7)).append("_");
            }
            return builder.toString();
        }

        public static String buildHigh(String pathId, Color color) {
            return build(";" + convertPathId(pathId) + ";" + color.asRgb() + ";", HIGH);
        }

        public static String buildLow(String pathId) {
            return build(";" + convertPathId(pathId), LOW);
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

    @Autowired
    private DeviceStorage deviceStorage;

    @Autowired
    private NotifyHttpManager notifyManager;

    @PostConstruct
    public void post() {
        this.listenerExecutor.execute(createListenerRunner());
    }

    public void changeActiveStatus(boolean status) {
        this.isActive.set(status);
    }

    public void notifyWithColor(final List<Device> devices,
                                final String pathId,
                                final Color color) {
        this.pingExecutor.execute(() -> {
            final String msg = Command.buildHigh(pathId, color);
            this.broadcaster.broadcast(devices, msg)
                    .forEach(d -> {
                        List<String> ips = devices.stream().map(Device::getLastKnownIp).collect(Collectors.toList());
                        logger.warn("RETRY REMEMBER COLOR ON FOR - " + ips + ", WITH MSG - " + msg);
                        Map<String, UdpBox> map = devicePingAwaitMap.computeIfAbsent(d.getId(),
                                (k) -> new ConcurrentHashMap<>());
                        map.put(pathId, new UdpBox(msg));
                        devicePingAwaitMap.put(d.getId(), map);
                    });
        });
    }

    public void notifyColorOff(final List<Device> devices,
                               final String pathId) {
        final String msg = Command.buildLow(pathId);
        this.broadcaster.broadcast(devices, msg)
                .forEach(d -> {
                    List<String> ips = devices.stream().map(Device::getLastKnownIp).collect(Collectors.toList());
                    logger.warn("RETRY REMEMBER COLOR OFF FOR - " + ips + ", WITH MSG - " + msg);
                    Map<String, UdpBox> map = devicePingAwaitMap.computeIfAbsent(d.getId(),
                            (k) -> new ConcurrentHashMap<>());
                    map.put(pathId, new UdpBox(msg));
                    devicePingAwaitMap.put(d.getId(), map);
                });
    }

    private Runnable createListenerRunner() {
        return () -> {
            try {
                final byte[] buf = new byte[256];
                try (DatagramSocket socket = new DatagramSocket(serverListenerPort)) {
                    logger.warn("IS ACTIVE - " + isActive.get() + ", SERVER PORT " + serverListenerPort);
                    while (isActive.get()) {
                        final DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        logger.warn("RECEIVING...");
                        socket.receive(packet);
                        final String received = new String(packet.getData(), 0, packet.getLength());
                        logger.warn("RECEIVED " + received);
                        String[] splitAlive = received.split(";");
                        if (splitAlive.length < 3)
                            continue;

                        final String id = splitAlive[2].trim();

                        final String response = notifyManager.getResponse(id,
                                packet.getAddress().getHostAddress(),
                                packet.getPort());

                        if (response == null || response.isEmpty())
                            continue;

                        logger.warn("RESPONDING WITH " + response);

                        final byte[] bytes = response.getBytes();
                        final DatagramPacket responsePacket = new DatagramPacket(bytes,
                                bytes.length,
                                packet.getSocketAddress());

                        socket.send(responsePacket);
                        if (received.isEmpty())
                            continue;

                        //processReply(received, packet.getAddress().getHostAddress(), packet.getPort());
                    }
                }
            } catch (IOException e) {
                logger.error(e.getLocalizedMessage());
            }
        };
    }

    private void processReply(String received, String ipAddress, int port) {
        switch (received.charAt(0)) {
            case 'A':
                String[] splitAlive = received.split(";");
                if (splitAlive.length < 3)
                    break;

                deviceManager.alive(splitAlive[2].trim(), ipAddress, port);
                break;
            case 'H':
            case 'L':
                String[] splitRetry = received.split(";");
                if (splitRetry.length < 3)
                    break;

                Map<String, UdpBox> map = this.devicePingAwaitMap.get(splitRetry[2]);
                map.remove(splitRetry[1]);
                break;
            default:
        }
    }

    /**
     * Mark as fully dead after 25 sec
     */
    private boolean noAnswer(String deviceId, LocalDateTime dateTime, LocalDateTime now) {
        if (dateTime.until(now, ChronoUnit.SECONDS) > 25)
            return !deviceManager.dead(deviceId);
        return dateTime.until(now, ChronoUnit.SECONDS) > 3;
    }

    @Scheduled(cron = "*/3 * * * * *")
    public void pingRetry() {
        final LocalDateTime now = LocalDateTime.now();
        // Device -> MSG
        final Map<String, List<String>> notifyDevices = new HashMap<>();
        for (Map.Entry<String, Map<String, UdpBox>> entry : devicePingAwaitMap.entrySet()) {
            final List<String> msg = entry.getValue().entrySet().stream()
                    .filter(e -> noAnswer(entry.getKey(), e.getValue().getTimestamp(), now))
                    .map(e -> e.getValue().getMsg())
                    .collect(Collectors.toList());

            devicePingAwaitMap.clear();
            notifyDevices.put(entry.getKey(), msg);
        }

        notifyDevices.forEach((k, v) -> {
            logger.warn("RETRY FOR DEVICE - " + k + ", MSG - " + v);
            deviceStorage.find(k).ifPresent(d -> {
                final List<Device> dev = Collections.singletonList(d);
                v.forEach(msg -> broadcaster.broadcast(dev, msg));
            });
        });
    }
}
