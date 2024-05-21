package ee.bitweb.core.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Context;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Tag("unit")
@Slf4j
class SilencedGelfTcpAppenderTest {

    private ExecutorService executor;
    private ServerSocket serverSocket;
    private Socket mockConnection;

    @BeforeEach
    void setUp() throws IOException {
        executor = Executors.newSingleThreadExecutor();

        serverSocket = new ServerSocket(0, 1);

        log.info("Socket server created and listening at port {}", serverSocket.getLocalPort());
    }

    @AfterEach
    void tearDown() throws IOException {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
        }

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
    void noExceptionIsThrownWhenConnectionIsUnsuccessful() throws IOException {
        // Fill backlog queue by this request so consequent requests will be blocked
        mockConnection = new Socket();
        mockConnection.connect(serverSocket.getLocalSocketAddress());
        log.info("Created mock connection to fill server backlog");

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

    @Test
    @DisplayName("Should not throw any exceptions and continue running when connection to GELF TCP endpoint is successful")
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    void noExceptionIsThrownWhenConnectionIsSuccessful() {
        executor.execute(this::listenAndAccept);

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

    private void listenAndAccept() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();

                log.info("Connection accepted, closing connection");

                if (socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
