package io.hackathon.storage.impl;

import io.hackathon.model.dao.Device;
import io.hackathon.repository.DeviceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Service
public class DeviceStorage extends BasicCacheStorage<Device, String> {

    @Autowired
    public DeviceStorage(DeviceRepository repository) {
        super(repository);
    }
}
