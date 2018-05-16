package hope.back.server;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.core.eventbus.Message;
import io.vertx.rxjava.ext.web.Router;
import io.vertx.rxjava.ext.web.handler.BodyHandler;

import static java.lang.String.format;
import static java.util.Optional.ofNullable;

public class UserRegistration extends AbstractVerticle {

    private static final String USER_REGISTRATION_URL = "/user";

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());


    @Override
    public void start(Future<Void> startFuture) {
        final Router router = Router.router(vertx);

        router.route().handler(BodyHandler.create());
        router.post(USER_REGISTRATION_URL)
                .handler(request ->
                        vertx.eventBus()
                                .rxSend("user-registration", request.getBodyAsJson())
                                .map(Message::body)
                                .subscribe(res -> request.response()
                                        .setStatusCode(201)
                                        .end()));

        vertx.createHttpServer()
                .requestHandler(router::accept)
                .listen(8090,
                        result -> ofNullable(result)
                                .filter(AsyncResult::succeeded)
                                .ifPresentOrElse(
                                        res -> {
                                            startFuture.complete();
                                            logger.info(format("Server started for %s", this.getClass().getName()));
                                        },
                                        () -> startFuture.fail("Server failed to start")));
    }
}
