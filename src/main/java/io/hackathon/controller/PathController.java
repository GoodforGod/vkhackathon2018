package io.hackathon.controller;

import io.hackathon.error.PathException;
import io.hackathon.manager.impl.ColorManager;
import io.hackathon.manager.impl.NotifyHttpManager;
import io.hackathon.manager.impl.PathManager;
import io.hackathon.model.Color;
import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;
import io.hackathon.model.RestResponse;
import io.hackathon.model.dao.Device;
import io.hackathon.model.dto.PathTO;
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
import java.util.stream.Collectors;

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
    private DeviceStorage deviceStorage;

    @Autowired
    private NotifyHttpManager notifyManager;

    private final Logger logger = LoggerFactory.getLogger(PathController.class);
    private final ForkJoinPool pool = ForkJoinPool.commonPool();

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful path", response = PathTO.class),
            @ApiResponse(code = 400, message = "Start or Destination invalid path", response = RestResponse.class),
            @ApiResponse(code = 404, message = "Path not found", response = RestResponse.class),
            @ApiResponse(code = 409, message = "Path collision detected", response = RestResponse.class)
    })
    @GetMapping("/path/calc")
    public DeferredResult<ResponseEntity<RestResponse<PathTO>>> calcPath(
            @RequestParam("startDeviceId") String startDeviceId,
            @RequestParam("destZoneId") int destZoneId,
            @RequestParam("destRoomId") int destRoomId) {
        logger.warn("CALC PATH FOR START - " + startDeviceId + ", DEST - " + destZoneId + "_" + destRoomId);
        final DeferredResult<ResponseEntity<RestResponse<PathTO>>> response = new DeferredResult<>(1500L);
        pool.submit(() -> {
            Set<String> excluded = Collections.emptySet();
            while (true) {
                try {
                    final Path path = pathManager.findPath(startDeviceId, destZoneId, destRoomId, excluded);
                    if (path.isEmpty() && excluded.isEmpty()) {
                        logger.warn("PATH NOT FOUND");
                        response.setErrorResult(RestResponse.errorEntity("Path was not found", HttpStatus.NOT_FOUND));
                    } else if (path.isEmpty() && !excluded.isEmpty()) {
                        final String collisionMsg = "Path conflict detected, can not calculate path tight now";
                        logger.warn(collisionMsg + ", " + excluded.toString());
                        response.setErrorResult(RestResponse.errorEntity(collisionMsg, HttpStatus.CONFLICT));
                    }

                    final ColorResponse colorResponse = colorManager.assign(path);
                    if (!colorResponse.isNeedRecalculation()) {
                        PathTO pathResponse = new PathTO(path.isOptimal(),
                                colorResponse.getColor(),
                                path.getPathId(),
                                path.getLength(),
                                path.getDevices(),
                                path.getDestDevice());

                        final List<Device> devices = deviceStorage.findByIds(path.getDevices());

                        notifyManager.notifyWithColor(devices, path.getPathId(), colorResponse.getColor());

                        logger.warn("PATH FOUND - " + pathResponse.getPathId() + ", DEVICES " + path.getDevices().toString());
                        response.setResult(RestResponse.validEntity(pathResponse));
                        break;
                    }

                    excluded = new HashSet<>(colorResponse.getDevices());
                } catch (PathException e) {
                    response.setErrorResult(RestResponse.errorEntity(e.getMessage(), HttpStatus.BAD_REQUEST));
                }
            }
        });

        response.onTimeout(() ->
                response.setErrorResult(
                        ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT)
                                .body(RestResponse.errorEntity("Request timeout occurred.",
                                        HttpStatus.GATEWAY_TIMEOUT))));

        return response;
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful path", response = PathTO.class),
            @ApiResponse(code = 404, message = "Path not found", response = RestResponse.class),
    })
    @GetMapping("/path/remembered")
    public ResponseEntity<RestResponse<PathTO>> memorizedPath(@RequestParam("pathId") String pathId) {
        final Path path = pathManager.memorized(pathId);
        if (path == null)
            return RestResponse.errorEntity("Path was not found", HttpStatus.NOT_FOUND);

        final Color color = colorManager.memorized(path);

        final PathTO pathTO = new PathTO(path.isOptimal(),
                color,
                path.getPathId(),
                path.getLength(),
                path.getDevices(),
                path.getDestDevice());

        return RestResponse.validEntity(pathTO);
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Some paths found", response = PathTO.class),
            @ApiResponse(code = 404, message = "None path was found", response = RestResponse.class),
    })
    @GetMapping("/path/remembered/all")
    public List<PathTO> allMemorizedPaths() {
        final List<Path> paths = pathManager.memorized();
        return paths.stream()
                .map(p -> new PathTO(p.isOptimal(),
                        colorManager.memorized(p),
                        p.getPathId(),
                        p.getLength(),
                        p.getDevices(),
                        p.getDestDevice()))
                .collect(Collectors.toList());
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful path", response = RestResponse.class),
            @ApiResponse(code = 404, message = "Path not found", response = RestResponse.class),
    })
    @GetMapping("/path/reset")
    public ResponseEntity<RestResponse> resetPath(@RequestParam("pathId") String pathId) {
        final Path path = pathManager.memorized(pathId);
        if (path == null) {
            logger.warn("CAN NOT RESET, PATH NOT FOUND " + pathId);
            return new ResponseEntity<>(RestResponse.error("Path was not found"), HttpStatus.NOT_FOUND);
        }

        final List<Device> devices = deviceStorage.findByIds(path.getDevices());
        colorManager.reset(path.getPathId(), new HashSet<>(path.getDevices()));
        notifyManager.notifyColorOff(devices, path.getPathId());
        logger.warn("DEVICES RESET SUCCESS : " + devices.toString());
        return ResponseEntity.ok(RestResponse.valid(null));
    }
}
