package io.hackathon.service.impl;

import io.hackathon.model.dao.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class UdpBroadcaster {

    private static final int PORT = 52313;

    private final Logger logger = LoggerFactory.getLogger(UdpBroadcaster.class);

    public List<String> broadcast(final List<Device> devices, String message) {
        try {
            final DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            final List<String> sendedToDevices = devices.stream()
                    .map(this::toAddress)
                    .filter(Objects::nonNull)
                    .peek(addr -> send(socket, message, addr))
                    .map(InetAddress::getHostAddress)
                    .collect(Collectors.toList());

            socket.close();
            return sendedToDevices;
        } catch (SocketException e) {
            logger.error(e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    private void send(final DatagramSocket socket,
                      final String broadcastMessage,
                      final InetAddress address) {
        try {
            byte[] buffer = broadcastMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
            socket.send(packet);
        } catch (IOException e) {
            logger.error(e.getLocalizedMessage());
        }
    }

    private InetAddress toAddress(Device device) {
        try {
            return InetAddress.getByName(device.getLastKnownIp());
        } catch (UnknownHostException e) {
            logger.warn(e.getLocalizedMessage());
            return null;
        }
    }
}
