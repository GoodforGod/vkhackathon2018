package io.hackathon.repository;

import io.hackathon.model.dao.Device;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@Repository
public interface DeviceRepository extends MongoRepository<Device, String> {

    List<Device> findByDetailedIdZoneIdAndDetailedIdRoomId(@Param("zoneId") int zoneId,
                                                           @Param("roomId") int roomId);

    List<Device> findByDetailedIdZoneId(@Param("zoneId") int zoneId);
}
