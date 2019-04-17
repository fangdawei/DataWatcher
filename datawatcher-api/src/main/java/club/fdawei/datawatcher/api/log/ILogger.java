package club.fdawei.datawatcher.api.log;

public interface ILogger {
    void i(String tag, String format, Object... args);

    void e(String tag, String format, Object... args);

    void d(String tag, String format, Object... args);
}
