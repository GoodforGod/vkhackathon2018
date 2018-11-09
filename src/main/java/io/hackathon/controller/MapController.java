package io.hackathon.controller;

import io.hackathon.error.MapLoadException;
import io.hackathon.error.MapPicException;
import io.hackathon.model.dto.MapGraph;
import io.hackathon.storage.impl.DeviceStorage;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@RestController
public class MapController {

    @Autowired
    private DeviceStorage storage;

    @GetMapping(value = "/map/pic")
    public String getMapPic() {
        try {
            final byte[] fileContent = FileUtils.readFileToByteArray(new File("/images/map.jpg"));
            final String encodedImg = Base64.getEncoder().encodeToString(fileContent);
            return encodedImg;
        } catch (IOException e) {
            throw new MapPicException();
        }
    }

    @PostMapping(value = "/map/load")
    public boolean loadMap(@RequestBody MapGraph map) {
        List<String> invalidDevices = storage.loadMap(map);
        if(!invalidDevices.isEmpty())
            throw new MapLoadException(invalidDevices);

        return true;
    }

    @GetMapping(value = "/map/default")
    public boolean loadDefaultMap() {
        List<String> invalidDevices = storage.loadDefaultMap();
        if(!invalidDevices.isEmpty())
            throw new MapLoadException(invalidDevices);

        return true;
    }
}
