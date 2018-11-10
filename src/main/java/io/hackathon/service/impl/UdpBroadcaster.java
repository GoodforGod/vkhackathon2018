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
import java.util.stream.Collectors;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class UdpBroadcaster {

    @Value("${DEVICE_UDP_PORT:52313}")
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

            logger.warn("POTENTIAL MSG TO SEND - " + message);
            final List<String> sendedToDevices = ips.stream()
                    .map(this::toAddress)
                    .filter(a -> a != null && !a.getHostAddress().isEmpty())
                    .peek(addr -> send(socket, message, addr))
                    .map(InetAddress::getHostAddress)
                    .collect(Collectors.toList());

            if(sendedToDevices.isEmpty()) {
                logger.warn("NO DEVICES WITH KNOWN IPs");
                return sendedToDevices;
            }

            logger.warn("BROADCASTING TO IPs - " + sendedToDevices.toString());
            socket.close();
            return sendedToDevices;
        } catch (SocketException e) {
            logger.error("SOCKET ERROR - " + e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    private void send(final DatagramSocket socket,
                      final String broadcastMessage,
                      final InetAddress address) {
        try {
            if (address == null || address.getHostAddress().isEmpty())
                return;

            byte[] buffer = broadcastMessage.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, PORT);
            socket.send(packet);
        } catch (IOException e) {
            logger.error("SEND ERROR WITH " + e.getLocalizedMessage());
            logger.error("SEND ERROR ID - " + broadcastMessage + " FOR IP " + address);
        }
    }

    private InetAddress toAddress(Device device) {
        return toAddress(device.getLastKnownIp());
    }

    private InetAddress toAddress(String ip) {
        try {
            return (ip == null || ip.isEmpty())
                    ? null
                    : InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            logger.warn("INVALID ADDRESS - " + e.getLocalizedMessage());
            return null;
        }
    }
}
