package ee.bitweb.core.amqp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;

@Slf4j
@RequiredArgsConstructor
public class CoreExceptionStrategy extends ConditionalRejectingErrorHandler.DefaultExceptionStrategy {

    // todo: mis ja miks vajalik? kas keegi mäletab?
    @Override
    public boolean isUserCauseFatal(Throwable t) {
        return true;
    }
}
