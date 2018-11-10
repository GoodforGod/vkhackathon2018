package io.hackathon.service;

import io.hackathon.model.Color;
import io.hackathon.model.dao.Device;

import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
public interface INotifyService {

    void notifyWithColor(final List<Device> devices,
                         final String pathId,
                         final Color color);

    void notifyColorOff(final List<Device> devices,
                        final String pathId);
}
