package hope.back.server;

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

@RunWith(VertxUnitRunner.class)
public class UserRegistrationTest extends AbstractTestCase {


    @Rule
    public RunTestOnContext rule = new RunTestOnContext();

    @Test
    public void whenEventResponseIsSuccessfulReturn201(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());
        final WebClient client = WebClient.create(vertx);

        vertx.eventBus().consumer("user-registration", msg -> msg.reply("any"));

        client.post(8090, "localhost", "/user")
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername"))
                .subscribe(resp -> {
                            context.assertEquals(201, resp.statusCode());
                            async.complete();
                        },
                        error -> context.fail(error.getMessage()));
    }

    @Test
    public void eventSentBasedOnBody(TestContext context) {
        final Async async = context.async();
        final Vertx vertx = new Vertx(rule.vertx());

        final WebClient client = WebClient.create(vertx);
        client.post(8090, "localhost", "/user")
                .putHeader("Content-Type", "application/json")
                .rxSendJsonObject(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername"))
                .subscribe();

        vertx.eventBus().<JsonObject>consumer("user-registration", msg -> {
            context.assertEquals(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername"), msg.body());
            msg.reply("any");
            async.complete();
        });

    }


//    private TestSuite testSuite =
//            TestSuite.create("UserRegistrationTest")
//                    .beforeEach(context -> {
//                        vertx = Vertx.vertx();
//                        vertx.deployVerticle(
//                                UserRegistration.class.getName(),
//                                context.asyncAssertSuccess());
//                    })
//                    .test("userIsRegistered", context -> {
//                        final Async async = context.async();
//
//                        final WebClient client = WebClient.create(vertx);
//                        client.post(8090, "localhost", "/user")
//                                .putHeader("Content-Type", "application/json")
//                                .rxSendJsonObject(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername"))
//                                .subscribeOn(RxHelper.scheduler(vertx))
//                                .subscribe(
//                                        resp -> {
//                                            context.assertEquals(201, resp.statusCode());
//                                            async.complete();
//                                        },
//                                        err -> context.fail(err.getMessage())
//                                );
//                        vertx.eventBus().consumer("user-registration", msg -> {
//                            context.assertEquals(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername").encode(), msg.body().toString());
//                            msg.reply("any");
//                        });
//                    })
////                    .test("bodyDispatchedAsEvent", context -> {
////                        final Async async = context.async();
////
////                        final HttpClient client = vertx.createHttpClient();
////                        final HttpClientRequest req = client.post(8090, "localhost", "/user");
////
////                        vertx.eventBus().consumer("user-registration", msg -> {
////                            msg.reply("any");
////                            async.complete();
////                        });
////                    })
//                    .afterEach(context -> vertx.close(context.asyncAssertSuccess()));

    @Before
    public void setUp(TestContext context) {
        super.setUp();

        new Vertx(rule.vertx())
                .deployVerticle(
                        UserRegistration.class.getName(),
                        context.asyncAssertSuccess());
    }
}