package io.hackathon;

import io.hackathon.manager.impl.ColorManager;
import io.hackathon.manager.impl.PathManager;
import io.hackathon.model.ColorResponse;
import io.hackathon.model.Path;
import io.hackathon.storage.impl.DeviceStorage;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.util.*;

/**
 * "default comment"
 *
 * @author GoodforGod
 * @since 09.11.2018
 */
@SpringBootTest
@RunWith(Parameterized.class)
public class MapLoadTest extends Assert {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private DeviceStorage deviceStorage;

    @Autowired
    private PathManager pathManager;

    @Autowired
    private ColorManager colorManager;

    private int i;

    public MapLoadTest(int i) {
        this.i = i;
    }

    @Parameterized.Parameters
    public static Collection data() {
        return Arrays.asList(new Object[][] {
                { 1 },
        });
    }

    @Test
    public void testMapLoad() {
        List<String> strings = deviceStorage.loadDefaultMap();
        assertTrue(strings.isEmpty());
        assertFalse(deviceStorage.findAll().isEmpty());
    }

    @Test
    public void test() {
        Path path = pathManager.findPath("7_224_1", 7, 219);
        assertNotNull(path);
        assertFalse(path.isEmpty());
        ColorResponse assign = colorManager.assign(path);

        Path pathSame = pathManager.findPath("7_224_1", 7, 219);
        assertNotNull(pathSame);
        assertFalse(pathSame.isEmpty());
        ColorResponse assignedSame = colorManager.assign(path);

        assertEquals(assign.getColor(), assignedSame.getColor());
    }

    @Test
    public void testRoomOccupied() {
        Path path = pathManager.findPath("7_224_1", 7, 219);
        assertNotNull(path);
        assertFalse(path.isEmpty());
        ColorResponse assign = colorManager.assign(path);

        Set<String> occupied = new HashSet<>();
        occupied.add("7_220_1");
        occupied.add("7_220_2");
        Path pathDiff = pathManager.findPath("7_224_1", 7, 219, occupied);
        assertNotNull(pathDiff);
        assertFalse(pathDiff.isEmpty());
        ColorResponse assignedDiff = colorManager.assign(path);

        assertNotEquals(path.getDevices().size(), pathDiff.getDevices().size());
        assertNotEquals(path.getDevices().size(), pathDiff.getDevices().size());
        assertNotEquals(assign.getColor(), assignedDiff.getColor());
    }
}
