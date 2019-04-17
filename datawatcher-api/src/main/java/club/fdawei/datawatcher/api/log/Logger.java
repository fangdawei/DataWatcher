package club.fdawei.datawatcher.api.log;

public class Logger {

    private static ILogger logger;

    public static void setLogger(ILogger logger) {
        Logger.logger = logger;
    }

    private static ILogger getLogger() {
        if (logger == null) {
            synchronized (Logger.class) {
                if (logger == null) {
                    logger = new DefaultLogger();
                }
            }
        }
        return logger;
    }

    public static void i(String tag, String format, Object... args) {
        getLogger().i(tag, format, args);
    }

    public static void e(String tag, String format, Object... args) {
        getLogger().e(tag, format, args);
    }

    public static void d(String tag, String format, Object... args) {
        getLogger().d(tag, format, args);
    }
}
