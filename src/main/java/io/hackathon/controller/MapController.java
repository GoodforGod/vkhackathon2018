package io.hackathon.controller;

import io.hackathon.model.RestResponse;
import io.hackathon.model.dto.MapGraph;
import io.hackathon.storage.impl.DeviceStorage;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

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

    @Value("${IMG_PATH:C:\\Users\\GoodforGod\\IdeaProjects\\hermitage\\src\\resources\\images\\map.jpg}")
    private String imgPath;

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Map image loaded successfully", response = RestResponse.class),
            @ApiResponse(code = 400, message = "Could not load map image", response = RestResponse.class),
    })
    @GetMapping(value = "/map/pic")
    public ResponseEntity<RestResponse<String>> getMapPic() {
        try {
            final byte[] fileContent = FileUtils.readFileToByteArray(new File(imgPath));
            final String encodedImg = Base64.getEncoder().encodeToString(fileContent);
            return RestResponse.validEntity(encodedImg);
        } catch (IOException e) {
            return RestResponse.errorEntity("Can't load map, contact administration",
                    HttpStatus.BAD_REQUEST);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Map loaded successfully", response = RestResponse.class),
            @ApiResponse(code = 400, message = "Could not load map", response = RestResponse.class),
    })
    @PostMapping(value = "/map/load")
    public ResponseEntity<RestResponse<Boolean>> loadMap(@RequestBody MapGraph map) {
        final List<String> invalidDevices = storage.loadMap(map);
        if(!invalidDevices.isEmpty()) {
            return RestResponse.errorEntity("Invalid nodes found, such as:" + invalidDevices.toString(),
                    HttpStatus.BAD_REQUEST);
        }

        return RestResponse.validEntity(null);
    }

    @ApiIgnore
    @GetMapping(value = "/map/default")
    public ResponseEntity<RestResponse<Boolean>> loadDefaultMap() {
        final List<String> invalidDevices = storage.loadDefaultMap();
        if(!invalidDevices.isEmpty()) {
            return RestResponse.errorEntity("Invalid nodes found, such as:" + invalidDevices.toString(),
                    HttpStatus.BAD_REQUEST);
        }

        return RestResponse.validEntity(null);
    }
}
