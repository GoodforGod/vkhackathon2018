package io.hackathon.manager;

import io.hackathon.service.INotifyService;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 10.11.2018
 */
public interface INotifyManager extends INotifyService {

    String getResponse(String deviceId, String ip, int port);
}
