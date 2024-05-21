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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Tag("unit")
class SilencedGelfTcpAppenderTest {

    private ExecutorService executor;
    private ServerSocket serverSocket;
    private Socket mockConnection;

    @BeforeEach
    void setUp() throws IOException {
        serverSocket = new ServerSocket(0, 1);

        executor = Executors.newSingleThreadExecutor();
    }

    void listenAndAccept() {
        while (!serverSocket.isClosed()) {
            try {
                Socket socket = serverSocket.accept();

                System.out.println("connected");

                if (socket.isConnected()) {
                    socket.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        executor.shutdownNow();

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
}
