package club.fdawei.datawatcher.perfectprocessor.common;

import club.fdawei.datawatcher.perfectprocessor.log.ILogger;
import club.fdawei.datawatcher.perfectprocessor.log.Logger;

/**
 * Created by david on 2019/4/16.
 */
public abstract class JavaClassHandler implements ILogger {

    private Logger logger;
    private UtilProvider utilProvider;

    public void setUtilProvider(UtilProvider utilProvider) {
        this.utilProvider = utilProvider;
    }

    public UtilProvider getUtilProvider() {
        return utilProvider;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void logi(String tag, String format, Object... args) {
        if (logger == null) {
            return;
        }
        logger.logi(tag, format, args);
    }

    @Override
    public void logw(String tag, String format, Object... args) {
        if (logger == null) {
            return;
        }
        logger.logw(tag, format, args);
    }

    @Override
    public void loge(String tag, String format, Object... args) {
        if (logger == null) {
            return;
        }
        logger.loge(tag, format, args);
    }
}
