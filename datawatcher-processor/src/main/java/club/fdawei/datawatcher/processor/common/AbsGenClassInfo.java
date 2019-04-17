package club.fdawei.datawatcher.processor.common;

import javax.annotation.processing.Filer;

import club.fdawei.datawatcher.processor.log.ILogger;
import club.fdawei.datawatcher.processor.log.Logger;

/**
 * Created by david on 2019/4/5.
 */
public abstract class AbsGenClassInfo implements ILogger {

    private Logger logger;

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

    public abstract void genJavaFile(Filer filer);
}
