package ee.bitweb.core.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import de.siegmar.logbackgelf.GelfTcpAppender;
import ee.bitweb.core.exception.CoreException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SilencedGelfTcpAppender extends GelfTcpAppender {

    @Override
    protected void append(final ILoggingEvent event) {
        final byte[] binMessage = getEncoder().encode(event);

        try {
            appendMessage(binMessage);
        } catch (CoreException ignored) {
            log.info("Ignored exception: {}", ignored.getMessage());
//            System.out.println("Ignored exception: %s".formatted(ignored.getMessage()));
            // Catching and ignoring CoreException which will be thrown if application can't connect to Graylog, because we don't want the application
            // to stop.
        }
    }

    @Override
    protected void appendMessage(final byte[] messageToSend) {
        try {
            super.appendMessage(messageToSend);
        } catch (CoreException e) {
            throw new CoreException("Error sending GELF message", e);
        }
    }

    @Override
    public void addError(String msg, Throwable ex) {
        throw new CoreException(msg, ex);
    }
}
