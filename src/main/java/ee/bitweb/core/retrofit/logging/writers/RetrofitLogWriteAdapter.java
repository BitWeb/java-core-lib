package ee.bitweb.core.retrofit.logging.writers;

import java.util.Map;

public interface RetrofitLogWriteAdapter {

    void write(Map<String, String> container);
}
