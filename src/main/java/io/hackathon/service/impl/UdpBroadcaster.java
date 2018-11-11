package io.hackathon.service.impl;

import io.hackathon.model.dao.Device;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
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

    private final Logger logger = LoggerFactory.getLogger(UdpBroadcaster.class);

    public List<Device> broadcast(final List<Device> devices, String message) {
        try {
            logger.warn("POTENTIAL MSG TO SEND - " + message);
            final List<Device> sendedToDevices = devices.stream()
                    .peek(dev -> send(message, dev.getLastKnownIp(), dev.getLastKnownPort()))
                    .collect(Collectors.toList());

            if (sendedToDevices.isEmpty()) {
                logger.warn("NO DEVICES WITH KNOWN IPs");
                return sendedToDevices;
            }

            logger.warn("BROADCAST TO IPs - " + sendedToDevices.toString());
            return sendedToDevices;
        } catch (Exception e) {
            logger.warn("SOCKET ERROR - " + e.getLocalizedMessage());
            return Collections.emptyList();
        }
    }

    private void send(final String broadcastMessage,
                      final String ipAddress,
                      final int port) {
        try {
            final DatagramSocket socket = new DatagramSocket();
            if(ipAddress == null || ipAddress.isEmpty())
                return;

            final InetAddress inetAddress = toAddress(ipAddress);
            socket.setBroadcast(true);
            final byte[] buffer = broadcastMessage.getBytes();
            final DatagramPacket packet = new DatagramPacket(buffer, buffer.length, inetAddress, port);
            logger.warn(packet.getSocketAddress().toString());
            socket.send(packet);
            socket.close();
        } catch (IOException e) {
            logger.warn("SEND ERROR WITH " + e.getLocalizedMessage());
            logger.warn("SEND ERROR ID - " + broadcastMessage
                    + " FOR IP " + ipAddress
                    + ", port " + port);
        }
    }

    private InetAddress toAddress(String ip) {
        try {
            InetAddress address = (ip == null || ip.isEmpty())
                    ? null
                    : InetAddress.getByName(ip);

            if (address != null)
                logger.warn("DEVICE ADDRESS BUILD " + address.getHostAddress());

            return address;
        } catch (UnknownHostException e) {
            logger.warn("INVALID ADDRESS - " + e.getLocalizedMessage());
            return null;
        }
    }
}
