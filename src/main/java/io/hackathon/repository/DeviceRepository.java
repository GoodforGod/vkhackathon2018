package io.hackathon.repository;

import io.hackathon.model.dao.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

}
