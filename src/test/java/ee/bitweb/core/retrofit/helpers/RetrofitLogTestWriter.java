package ee.bitweb.core.retrofit.helpers;

import ee.bitweb.core.retrofit.logging.writers.RetrofitLogWriteAdapter;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Profile("retrofit-log-test-writer")
public class RetrofitLogTestWriter implements RetrofitLogWriteAdapter {

    @Getter
    private Map<String, String> container = null;

    @Override
    public void write(Map<String, String> container) {
        this.container = container;
    }

    public void clear() {
        container = null;
    }
}
