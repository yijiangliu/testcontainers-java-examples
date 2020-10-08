import com.mycompany.cache.Cache;
import com.mycompany.cache.RedisBackedCache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;
import redis.clients.jedis.Jedis;

import java.util.Optional;

import static org.rnorth.visibleassertions.VisibleAssertions.assertEquals;
import static org.rnorth.visibleassertions.VisibleAssertions.assertFalse;
import static org.rnorth.visibleassertions.VisibleAssertions.assertTrue;

/**
 * Integration test for Redis-backed cache implementation.
 */
public class RedisBackedCacheTest {

    final static DockerImageName  imageName = DockerImageName.parse("redis:3.0.6");
    final static Integer port = 6379;

    @Rule
    public GenericContainer redis = new GenericContainer(imageName).withExposedPorts(port);
        //new GenericContainer("redis:3.0.6").withExposedPorts(6379);
    private Cache cache;

    @Before
    public void setUp() {
        Jedis jedis = new Jedis(redis.getContainerIpAddress(), redis.getMappedPort(port));

        cache = new RedisBackedCache(jedis, "test");
    }

    @Test
    public void testFindingAnInsertedValue() {
        cache.put("foo", "bar");
        Optional<String> foundObject = cache.get("foo", String.class);

        assertTrue("When an object in the cache is retrieved, it can be found",
                        foundObject.isPresent());
        assertEquals("When we put a String in to the cache and retrieve it, the value is the same",
                        "bar",
                        foundObject.get());

        cache.put("FOO", "BAR");
        foundObject = cache.get("FOO", String.class);

        assertTrue("When an object in the cache is retrieved, it can be found",
            foundObject.isPresent());
        assertEquals("When we put a String in to the cache and retrieve it, the value is the same",
            "BAR",
            foundObject.get());
    }

    @Test
    public void testNotFindingAValueThatWasNotInserted() {
        Optional<String> foundObject = cache.get("bar", String.class);

        assertFalse("When an object that's not in the cache is retrieved, nothing is found",
                foundObject.isPresent());
    }
}
