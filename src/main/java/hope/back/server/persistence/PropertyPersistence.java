package hope.back.server.persistence;

import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.mongo.MongoClient;

public class PropertyPersistence extends AbstractVerticle {
    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    @Override
    public void start() {
        final MongoClient mongoClient = MongoClient.createNonShared(vertx, config());

        vertx.eventBus()
                .<JsonObject>consumer("property-registration")
                .toObservable()
                .subscribe(msg ->
                        mongoClient.insert("properties", msg.body(),
                                res -> {
                                    if (res.succeeded()) {
                                        final String message = "Persisted: " + msg.body();
                                        logger.debug(message);
                                        msg.reply(message);
                                    }
                                    logger.fatal("Error persisting: " + msg.body());
                                }));
    }
}
