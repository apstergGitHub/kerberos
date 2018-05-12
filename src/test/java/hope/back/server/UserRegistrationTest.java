package hope.back.server;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import io.vertx.rxjava.ext.unit.Async;
import io.vertx.rxjava.ext.unit.TestCompletion;
import io.vertx.rxjava.ext.unit.TestSuite;
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

                        final HttpClient client = vertx.createHttpClient();
                        final HttpClientRequest req = client.post(8090, "localhost", "/user");
                        vertx.eventBus().consumer("user-registration", msg -> {
                            context.assertEquals("user", msg.body().toString());
                            msg.reply("any");
                        });
                        req.exceptionHandler(err -> context.fail(err.getMessage()));
                        req.handler(resp -> {
                            context.assertEquals(201, resp.statusCode());
                            async.complete();
                        });
                        req.end();

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