package hope.back.server.web.property.register;

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
public class PropertyRegistrationTest extends AbstractTestCase {

    private static final String USER_ID = randomAlphanumeric(8);
    private static final String PRICE_PER_NIGHT = randomNumeric(8);

    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Before
    public void setUp(TestContext context) {
        super.setUp();

        new Vertx(rule.vertx())
                .deployVerticle(
                        PropertyRegistration.class.getName(),
                        context.asyncAssertSuccess());
    }

    @Test
    public void whenEventResponseIsSuccessfulReturn201(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());
        final WebClient client = WebClient.create(vertx);

        vertx.eventBus().consumer("property-registration", msg -> msg.reply("any"));

        client.post(8091, "localhost", "/property/register")
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(new JsonObject().put("userId", "215155").put("username", "testUsername"))
                .subscribe(resp -> {
                            context.assertEquals(201, resp.statusCode());
                            async.complete();
                        },
                        error -> context.fail(error.getMessage()));
    }

    @Test
    public void whenEventResponseIsFailureReturn400(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());
        final WebClient client = WebClient.create(vertx);

        vertx.eventBus().consumer("property-registration", msg -> msg.fail(400, "failure"));

        client.post(8091, "localhost", "/property/register")
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(new JsonObject().put("userId", "215155").put("username", "testUsername"))
                .subscribe(resp -> {
                            context.assertEquals(400, resp.statusCode());
                            async.complete();
                        },
                        error -> context.fail(error.getMessage()));
    }

    @Test
    public void eventSentBasedOnBody(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());

        final WebClient client = WebClient.create(vertx);
        client.post(8091, "localhost", "/property/register")
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(new JsonObject().put("userId", USER_ID).put("pricePerNight", PRICE_PER_NIGHT))
                .subscribe();
        //http://openmymind.net/Multiple-Collections-Versus-Embedded-Documents/#5
        //https://stackoverflow.com/questions/5373198/mongodb-relationships-embed-or-reference

        vertx.eventBus().<JsonObject>consumer("property-registration", msg -> {
            context.assertEquals(new JsonObject().put("userId", USER_ID).put("pricePerNight", PRICE_PER_NIGHT), msg.body());
            msg.reply("any");
            async.complete();
        });

    }
}