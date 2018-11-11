package io.hackathon.manager;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public interface IDeviceManager {

    boolean alive(String deviceId, String ipAddress, int port);

    boolean dead(String deviceId);
}
