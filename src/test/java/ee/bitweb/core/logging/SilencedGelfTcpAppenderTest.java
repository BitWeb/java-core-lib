package ee.bitweb.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Tag("unit")
class SilencedGelfTcpAppenderTest {

    private static ServerSocket serverSocket;
    private static Socket mockConnection;

    @BeforeAll
    static void beforeAll() throws IOException {
        // Create server socket with single element backlog queue (1) and dynamically allocated port (0)
        serverSocket = new ServerSocket(0, 1);

        // Fill backlog queue by this request so consequent requests will be blocked
        mockConnection = new Socket();
        mockConnection.connect(serverSocket.getLocalSocketAddress());
    }

    @AfterAll
    static void afterAll() throws IOException {
        if (mockConnection != null && mockConnection.isConnected()) {
            mockConnection.close();
        }

        if (serverSocket != null && !serverSocket.isClosed()) {
            serverSocket.close();
        }
    }

    @Test
    @DisplayName("Should not throw any exceptions and continue running when connection to GELF TCP endpoint cannot be made")
    @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
    void noExceptionIsThrownWhenConnectionIsUnsuccessful() {
        Context context = new LoggerContext();

        SilencedGelfTcpAppender appender = new SilencedGelfTcpAppender();
        appender.setContext(context);
        appender.setGraylogHost("127.0.0.1");
        appender.setGraylogPort(serverSocket.getLocalPort());
        appender.setMaxRetries(0);
        appender.setConnectTimeout(100);
        appender.setSocketTimeout(100);
        appender.start();

        assertDoesNotThrow(() -> appender.doAppend(createLoggingEvent()));
    }

    private ILoggingEvent createLoggingEvent() {
        Logger logger = (Logger) LoggerFactory.getLogger(SilencedGelfTcpAppenderTest.class);

        return new LoggingEvent(SilencedGelfTcpAppender.class.getName(), logger, Level.INFO, "message", null, null);
    }
}
