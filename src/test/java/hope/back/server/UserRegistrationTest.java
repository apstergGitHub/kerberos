package hope.back.server;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.unit.Async;
import io.vertx.rxjava.ext.unit.TestCompletion;
import io.vertx.rxjava.ext.unit.TestSuite;
import io.vertx.rxjava.ext.web.client.WebClient;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class UserRegistrationTest {

    private Vertx vertx;

    private TestSuite testSuite =
            TestSuite.create("UserRegistrationTest")
                    .beforeEach(context -> {
                        vertx = Vertx.vertx();
                        vertx.deployVerticle(
                                UserRegistration.class.getName(),
                                context.asyncAssertSuccess());
                    })
                    .test("userIsRegistered", context -> {
                        final Async async = context.async();

                        final WebClient client = WebClient.create(vertx);
                        client.post(8090, "localhost", "/user")
                                .putHeader("Content-Type", "application/json")
                                .rxSendJsonObject(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername"))
                                .subscribeOn(RxHelper.scheduler(vertx))
                                .subscribe(
                                        resp -> {
                                            context.assertEquals(201, resp.statusCode());
                                            async.complete();
                                        },
                                        err -> context.fail(err.getMessage())
                                );
                        vertx.eventBus().consumer("user-registration", msg -> {
                            context.assertEquals(new JsonObject().put("email", "random@gmail.com").put("username", "testUsername").encode(), msg.body().toString());
                            msg.reply("any");
                        });
                    })
//                    .test("bodyDispatchedAsEvent", context -> {
//                        final Async async = context.async();
//
//                        final HttpClient client = vertx.createHttpClient();
//                        final HttpClientRequest req = client.post(8090, "localhost", "/user");
//
//                        vertx.eventBus().consumer("user-registration", msg -> {
//                            msg.reply("any");
//                            async.complete();
//                        });
//                    })
                    .afterEach(context -> vertx.close(context.asyncAssertSuccess()));


    @Test
    public void userIsRegistered() {
        TestCompletion testCompletion = testSuite.run();

        testCompletion.awaitSuccess();
    }
}