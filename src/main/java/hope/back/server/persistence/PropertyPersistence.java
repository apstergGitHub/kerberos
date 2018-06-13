package hope.back.server.persistence;

import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.AbstractVerticle;
import io.vertx.rxjava.ext.mongo.MongoClient;

public class PropertyPersistence extends AbstractVerticle {

    @Override
    public void start() {
        final MongoClient mongoClient = MongoClient.createNonShared(vertx, config());
        mongoClient.createCollection("properties", event -> {
        });

        vertx.eventBus()
                .<JsonObject>consumer("property-registration")
                .toObservable()
                .subscribe(msg ->
                        mongoClient.insert("properties", msg.body(),
                                res -> {
                                    if (res.succeeded()) {
                                        msg.reply("Persisted: " + msg.body());
                                    }
                                }));
    }
}
