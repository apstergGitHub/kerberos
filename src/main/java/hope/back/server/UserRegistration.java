package hope.back.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.ext.web.Router;

import static java.util.Optional.ofNullable;

public class UserRegistration extends AbstractVerticle {

    private static final String USER_REGISTRATION_URL = "/user";

    @Override
    public void start(Future<Void> startFuture) {
        final Router router = Router.router(vertx);

        router.post(USER_REGISTRATION_URL)
                .handler(request ->
                        vertx.eventBus()
                                .rxSend("user-registration", "user")
                                .map(Message::body)
                                .subscribe(res -> request.response()
                                        .setStatusCode(201)
                                        .end()));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8090,
                        result -> {
                            ofNullable(result)
                                    .filter(AsyncResult::succeeded)
                                    .ifPresentOrElse(
                                            res -> startFuture.complete(),
                                            () -> startFuture.fail("Server failed to start"));
                        });
    }
}
