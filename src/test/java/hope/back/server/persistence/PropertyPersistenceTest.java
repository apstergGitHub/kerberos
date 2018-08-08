package hope.back.server.persistence;

import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import hope.back.AbstractTestCase;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.mongo.MongoClient;
import org.junit.*;
import org.junit.runner.RunWith;

import java.io.IOException;

import static de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;
import static de.flapdoodle.embed.process.runtime.Network.localhostIsIPv6;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RunWith(VertxUnitRunner.class)
public class PropertyPersistenceTest extends AbstractTestCase {

    private static final String USER_ID = randomAlphanumeric(8);
    private static final String PRICE_PER_NIGHT = randomNumeric(8);
    private static final String PROPERTIES_COLLECTION = "properties";
    private static int MONGO_PORT = 12345;
    private static MongodProcess MONGO;

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp(TestContext context) {
        super.setUp();

        new Vertx(rule.vertx())
                .deployVerticle(
                        PropertyPersistence.class.getName(),
                        new DeploymentOptions()
                                .setConfig(new JsonObject()
                                        .put("http.port", MONGO_PORT)
                                        .put("db_name", "properties-test")
                                        .put("connection_string",
                                                "mongodb://localhost:" + MONGO_PORT)),
                        context.asyncAssertSuccess());
    }

    @Test
    public void whenEventIsReceivedPersistIt(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());

        final MongoClient mongoClient = MongoClient.createNonShared(vertx, new JsonObject()
                .put("http.port", MONGO_PORT)
                .put("db_name", "properties-test")
                .put("connection_string",
                        "mongodb://localhost:" + MONGO_PORT));
        //https://vertx.io/blog/    combine-vert-x-and-mongo-to-build-a-giant/

        vertx.eventBus()
                .send("property-registration", new JsonObject().put("userId", USER_ID).put("pricePerNight", PRICE_PER_NIGHT),
                        event -> mongoClient.count(PROPERTIES_COLLECTION, new JsonObject(), count -> {
                            if (count.succeeded() && count.result() == 1) {
                                mongoClient.find(PROPERTIES_COLLECTION, new JsonObject().put("userId", USER_ID), res -> {
                                    if (res.succeeded()) {
                                        context.assertEquals(res.result().get(0).getString("pricePerNight"), PRICE_PER_NIGHT);
                                    } else {
                                        context.fail();
                                    }
                                });
                                async.complete();
                            } else {
                                context.fail();
                            }
                        }));
    }

    @BeforeClass
    public static void initialize() throws IOException {
        MONGO = MongodStarter.getDefaultInstance()
                .prepare(new MongodConfigBuilder()
                        .version(PRODUCTION)
                        .net(new Net(MONGO_PORT, localhostIsIPv6()))
                        .build())
                .start();
    }

    @AfterClass
    public static void shutdown() {
        MONGO.stop();
    }
}