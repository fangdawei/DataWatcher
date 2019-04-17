package club.fdawei.datawatcher.processor.log;

/**
 * Created by david on 2019/4/15.
 */
public interface ILogger {

    void logi(String tag, String format, Object... args);

    void logw(String tag, String format, Object... args);

    void loge(String tag, String format, Object... args);

}
