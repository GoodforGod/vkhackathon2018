package io.hackathon.controller;

import io.hackathon.error.PathCantCalcException;
import io.hackathon.manager.impl.ColorManager;
import io.hackathon.manager.impl.PathManager;
import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

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

    @GetMapping("/path/calc")
    public Path calcPath(@RequestParam("startDeviceId") String startDeviceId,
                         @RequestParam("destZoneId") int destZoneId,
                         @RequestParam("destRoomId") int destRoomId) {
        Set<String> excluded = Collections.emptySet();
        while (true) {
            final Path path = pathManager.findPath(startDeviceId, destZoneId, destRoomId, excluded);
            if (path.isEmpty())
                throw new PathCantCalcException();

            ColorResponse response = colorManager.assign(path);
            if (!response.isNeedRecalculation())
                return path;

            excluded = new HashSet<>(response.getDevices());
        }
    }
}
