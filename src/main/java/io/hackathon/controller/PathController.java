package io.hackathon.controller;

import io.hackathon.error.PathCantCalcException;
import io.hackathon.manager.impl.ColorManager;
import io.hackathon.manager.impl.PathManager;
import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;
import io.hackathon.model.dao.Device;
import io.hackathon.model.dto.PathTO;
import io.hackathon.service.impl.NotifyService;
import io.hackathon.storage.impl.DeviceStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@RestController
public class PathController {

    @Autowired
    private PathManager pathManager;

    @Autowired
    private ColorManager colorManager;

    @Autowired
    private NotifyService notifyService;

    @Autowired
    private DeviceStorage deviceStorage;

    private final ForkJoinPool pool = ForkJoinPool.commonPool();

    @GetMapping("/path/calc")
    public DeferredResult<ResponseEntity<PathTO>> calcPath(
            @RequestParam("startDeviceId") String startDeviceId,
            @RequestParam("destZoneId") int destZoneId,
            @RequestParam("destRoomId") int destRoomId) {
        final DeferredResult<ResponseEntity<PathTO>> response = new DeferredResult<>();
        pool.submit(() -> {
            Set<String> excluded = Collections.emptySet();
            while (true) {
                final Path path = pathManager.findPath(startDeviceId, destZoneId, destRoomId, excluded);
                if (path.isEmpty())
                    throw new PathCantCalcException();

                final ColorResponse colorResponse = colorManager.assign(path);
                if (!colorResponse.isNeedRecalculation()) {
                    PathTO pathResponse = new PathTO(path.isOptimal(),
                            colorResponse.getColor(),
                            path.getPathId(),
                            path.getLength(),
                            path.getDevices(),
                            path.getDestDevice());

                    final List<Device> devices = deviceStorage.findByIds(path.getDevices());
                    notifyService.notifyWithColor(devices, path.getPathId(), colorResponse.getColor());
                    response.setResult(ResponseEntity.ok(pathResponse));
                    break;
                }

                excluded = new HashSet<>(colorResponse.getDevices());
            }
        });

        return response;
    }

    @GetMapping("/path/reset")
    public boolean resetPath(@RequestParam("pathId") String pathId) {
        final Path path = pathManager.memorized(pathId);
        if (path == null)
            return false;

        final List<Device> devices = deviceStorage.findByIds(path.getDevices());
        colorManager.reset(path.getPathId(), new HashSet<>(path.getDevices()));
        notifyService.notifyColorOff(devices, path.getPathId());
        return true;
    }
}
