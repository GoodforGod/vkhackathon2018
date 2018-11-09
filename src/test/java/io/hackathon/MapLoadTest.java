package io.hackathon;

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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
    public void test() {
        List<String> strings = deviceStorage.loadDefaultMap();
        assertTrue(strings.isEmpty());
    }
}
