package hope.back;

import hope.back.server.UserRegistration;
import hope.back.server.persistence.PropertyPersistence;
import hope.back.server.web.property.register.PropertyRegistration;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.config.ConfigRetriever;
import io.vertx.rxjava.core.AbstractVerticle;

import static java.util.Optional.ofNullable;

public class Launcher extends AbstractVerticle {

    @Override
    public void start() {
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

        configRetriever.rxGetConfig()
                .subscribe(
                        config -> {
                            vertx.deployVerticle(UserRegistration.class.getName(), result -> ofNullable(result)
                                    .filter(AsyncResult::failed)
                                    .ifPresent(res -> {
                                        throw new IllegalStateException(res.cause());
                                    }));
                            vertx.deployVerticle(PropertyRegistration.class.getName(), result -> ofNullable(result)
                                    .filter(AsyncResult::failed)
                                    .ifPresent(res -> {
                                        throw new IllegalStateException(res.cause());
                                    }));
                            vertx.deployVerticle(PropertyPersistence.class.getName(),
                                    new DeploymentOptions()
                                            .setConfig(new JsonObject()
                                                    .put("http.port", config.getString("mongo.http.port"))
                                                    .put("db_name", config.getString("mongo.db.name"))
                                                    .put("connection_string",
                                                            "mongodb://localhost:" + config.getString("mongo.http.port"))),
                                    result -> ofNullable(result)
                                    .filter(AsyncResult::failed)
                                    .ifPresent(res -> {
                                        throw new IllegalStateException(res.cause());
                                    }));
                            System.setProperty("vertx.logger-delegate-factory-class-name", config.getString("vertx.logger-delegate-factory-class-name"));
                            InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
                        }
                );
    }
}
