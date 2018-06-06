package hope.back.server.persistence;

import hope.back.AbstractTestCase;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.RunTestOnContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@RunWith(VertxUnitRunner.class)
public class PropertyPersistenceTest extends AbstractTestCase {

    private static final String USER_ID = randomAlphanumeric(8);
    private static final String PRICE_PER_NIGHT = randomNumeric(8);

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp(TestContext context) {
        super.setUp();

        new Vertx(rule.vertx())
                .deployVerticle(
                        PropertyPersistence.class.getName(),
                        context.asyncAssertSuccess());
    }

    @Test
    public void whenEventIsReceivedPersistIt(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());
        final WebClient client = WebClient.create(vertx);

        vertx.eventBus().send("property-registration", new JsonObject().put("userId", USER_ID).put("pricePerNight", PRICE_PER_NIGHT));
        //https://vertx.io/blog/combine-vert-x-and-mongo-to-build-a-giant/

        client.post(8091, "localhost", "/property/register")
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(new JsonObject().put("userId", "215155").put("username", "testUsername"))
                .subscribe(resp -> {
                            context.assertEquals(201, resp.statusCode());
                            async.complete();
                        },
                        error -> context.fail(error.getMessage()));
    }
}