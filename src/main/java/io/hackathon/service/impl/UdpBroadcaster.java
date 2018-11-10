package io.hackathon.service.impl;

import io.hackathon.model.dao.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${device.udp.port:52313}")
    private int PORT;

    private final Logger logger = LoggerFactory.getLogger(UdpBroadcaster.class);

    public List<String> broadcastToDevices(final List<Device> devices, String message) {
        final List<String> addresses = devices.stream()
                .map(Device::getLastKnownIp)
                .collect(Collectors.toList());

        return broadcast(addresses, message);
    }

    public List<String> broadcast(final List<String> ips, String message) {
        try {
            final DatagramSocket socket = new DatagramSocket();
            socket.setBroadcast(true);

            final List<String> sendedToDevices = ips.stream()
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
        return toAddress(device.getLastKnownIp());
    }

    private InetAddress toAddress(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            logger.warn(e.getLocalizedMessage());
            return null;
        }
    }
}
