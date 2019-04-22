package club.fdawei.datawatcher.processor.common;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import club.fdawei.datawatcher.processor.log.ILogger;
import club.fdawei.datawatcher.processor.log.Logger;

/**
 * Created by david on 2019/4/16.
 */
public abstract class JavaClassGenerator implements ILogger {

    private Logger logger;
    private Types typeUtils;
    private Elements elementUtils;

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setTypeUtils(Types typeUtils) {
        this.typeUtils = typeUtils;
    }

    public void setElementUtils(Elements elementUtils) {
        this.elementUtils = elementUtils;
    }

    public Types getTypeUtils() {
        return typeUtils;
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

    public String getPkgName(Element element) {
        return elementUtils.getPackageOf(element).getQualifiedName().toString();
    }

    public abstract void genJavaFile(Filer filer);
}
