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
            String replace = str.replace("-", "0");
            for (int i = 0; i < max - replace.length(); i++) {
                added.append("0");
            }

            return added.toString() + replace;
        }

        private static String convertPathId(String pathId) {
            String[] split = pathId.split("_");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < split.length; i++) {
                if (i == 2)
                    builder.append(supplyWithZeros(split[i], 11));
                else
                    builder.append(supplyWithZeros(split[i], 7)).append("_");
            }
            return builder.toString();
        }

        public static String buildHigh(String pathId, Color color) {
            return build(";" + convertPathId(pathId) + ";" + color.asRgb() + "\n", HIGH);
        }

        public static String buildLow(String pathId) {
            return build(";" + convertPathId(pathId), LOW);
        }

        private static String build(String message, Command command) {
            return command.getCmd() + message + "\r";
        }
    }

    /**
     * Device -> (Path Id -> SendColorMsg Timestamp)
     */
    private final ConcurrentMap<String, Map<String, UdpBox>> devicePingAwaitMap = new ConcurrentHashMap<>();

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

    public void notifyWithColor(final List<Device> devices,
                                final String pathId,
                                final Color color) {
        final String msg = Command.buildHigh(pathId, color);
        notify(devices, pathId, msg);
    }

    public void notifyColorOff(final List<Device> devices,
                               final String pathId) {
        final String msg = Command.buildLow(pathId);
        notify(devices, pathId, msg);
    }

    private void notify(final List<Device> devices,
                        final String pathId,
                        final String msg) {
        this.broadcaster.broadcast(devices, msg)
                .forEach(d -> {
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
                        socket.receive(packet);
                        final String received = new String(packet.getData(), 0, packet.getLength());
                        if (received.isEmpty())
                            continue;

                        logger.warn("RECEIVED " + received);
                        final DatagramPacket response = processRequest(received, packet);
                        if (response != null)
                            socket.send(response);
                    }
                }
            } catch (IOException e) {
                logger.warn(e.getLocalizedMessage());
            }
        };
    }

    private DatagramPacket processRequest(String received, DatagramPacket packet) {
        final String[] splitRetry = received.split(";");
        if (splitRetry.length < 3)
            return null;

        final String pathId = splitRetry[1];
        final String deviceId = splitRetry[2];
        final String response = notifyManager.getResponse(deviceId,
                packet.getAddress().getHostAddress(),
                packet.getPort());

        final boolean haveResponse = (response != null && !response.isEmpty());
        final Map<String, UdpBox> retryPolicyMap = this.devicePingAwaitMap.computeIfAbsent(deviceId,
                (k) -> new ConcurrentHashMap<>());

        switch (received.charAt(0)) {
            case 'H':
            case 'L':
                retryPolicyMap.remove(pathId);
                this.devicePingAwaitMap.put(deviceId, retryPolicyMap);
        }

        if (!haveResponse)
            return null;

        logger.warn("RESPONDING : " + response);
        retryPolicyMap.put(pathId, new UdpBox(response));
        this.devicePingAwaitMap.put(deviceId, retryPolicyMap);
        final byte[] bytes = response.getBytes();
        return new DatagramPacket(bytes, bytes.length, packet.getSocketAddress());
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
    public void retryPolicySchedule() {
        final LocalDateTime now = LocalDateTime.now();
        // Device -> MSGs
        final Map<String, List<String>> notifyDevices = new HashMap<>();
        for (Map.Entry<String, Map<String, UdpBox>> entry : devicePingAwaitMap.entrySet()) {
            final List<String> msg = entry.getValue().entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .filter(e -> noAnswer(entry.getKey(), e.getValue().getTimestamp(), now))
                    .map(e -> e.getValue().getMsg())
                    .collect(Collectors.toList());

            devicePingAwaitMap.clear();
            if (!msg.isEmpty())
                notifyDevices.put(entry.getKey(), msg);
        }

        notifyDevices.forEach((k, messages) -> {
            logger.warn("RETRY FOR DEVICE - " + k + ", MSG - " + messages.toString());
            deviceStorage.find(k).ifPresent(d -> {
                final List<Device> dev = Collections.singletonList(d);
                messages.forEach(msg -> broadcaster.broadcast(dev, msg));
            });
        });
    }
}
