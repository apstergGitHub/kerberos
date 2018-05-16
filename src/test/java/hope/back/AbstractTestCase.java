package hope.back;

import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Log4J2LoggerFactory;
import org.junit.Before;

public class AbstractTestCase {

    @Before
    public void setUp() {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory");
        InternalLoggerFactory.setDefaultFactory(Log4J2LoggerFactory.INSTANCE);
    }
}
