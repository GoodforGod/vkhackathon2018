package io.hackathon.controller;

import io.hackathon.error.PathCantCalcException;
import io.hackathon.manager.impl.ColorManager;
import io.hackathon.manager.impl.PathManager;
import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;
import io.hackathon.model.dao.Device;
import io.hackathon.model.dto.PathTO;
import io.hackathon.service.impl.NotifyUdpService;
import io.hackathon.storage.impl.DeviceStorage;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    private NotifyUdpService notifyUdpService;

    @Autowired
    private DeviceStorage deviceStorage;

    private final Logger logger = LoggerFactory.getLogger(PathController.class);

    private final ForkJoinPool pool = ForkJoinPool.commonPool();

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful path", response = PathTO.class)
    })
    @GetMapping("/path/calc")
    public DeferredResult<ResponseEntity<PathTO>> calcPath(
            @RequestParam("startDeviceId") String startDeviceId,
            @RequestParam("destZoneId") int destZoneId,
            @RequestParam("destRoomId") int destRoomId) {
        final DeferredResult<ResponseEntity<PathTO>> response = new DeferredResult<>(1500L);
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

                    notifyUdpService.notifyWithColor(devices, path.getPathId(), colorResponse.getColor());

                    logger.warn("PATH FOUND - " + pathResponse.getPathId());
                    response.setResult(ResponseEntity.ok(pathResponse));
                    break;
                }

                excluded = new HashSet<>(colorResponse.getDevices());
            }
        });

        response.onTimeout(() ->
                response.setErrorResult(
                        ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                                .body("Request timeout occurred.")));

        return response;
    }

    @GetMapping("/path/reset")
    public boolean resetPath(@RequestParam("pathId") String pathId) {
        final Path path = pathManager.memorized(pathId);
        if (path == null)
            return false;

        final List<Device> devices = deviceStorage.findByIds(path.getDevices());
        colorManager.reset(path.getPathId(), new HashSet<>(path.getDevices()));
        notifyUdpService.notifyColorOff(devices, path.getPathId());
        return true;
    }
}
