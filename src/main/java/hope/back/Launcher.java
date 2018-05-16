package hope.back;

import hope.back.server.UserRegistration;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import io.vertx.rxjava.config.ConfigRetriever;
import io.vertx.rxjava.core.AbstractVerticle;

public class Launcher extends AbstractVerticle {

    @Override
    public void start() {
        ConfigRetriever configRetriever = ConfigRetriever.create(vertx);

        configRetriever.rxGetConfig()
                .subscribe(
                        config -> {
                            vertx.deployVerticle(UserRegistration.class.getName());
                            System.setProperty("vertx.logger-delegate-factory-class-name", config.getString("vertx.logger-delegate-factory-class-name"));
                            InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
                        }
                );
    }
}
