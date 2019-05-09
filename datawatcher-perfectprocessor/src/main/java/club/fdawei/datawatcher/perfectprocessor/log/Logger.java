package club.fdawei.datawatcher.perfectprocessor.log;

import javax.annotation.processing.Messager;
import javax.tools.Diagnostic;

/**
 * Created by david on 2019/4/15.
 */
public class Logger implements ILogger {

    private Messager messager;

    public Logger(Messager messager) {
        this.messager = messager;
    }

    @Override
    public void logi(String tag, String format, Object... args) {
        if (messager == null) {
            return;
        }
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("%s %s", tag, String.format(format, args)));
    }

    @Override
    public void logw(String tag, String format, Object... args) {
        if (messager == null) {
            return;
        }
        messager.printMessage(Diagnostic.Kind.WARNING, String.format("%s %s", tag, String.format(format, args)));
    }

    @Override
    public void loge(String tag, String format, Object... args) {
        if (messager == null) {
            return;
        }
        messager.printMessage(Diagnostic.Kind.ERROR, String.format("%s %s", tag, String.format(format, args)));
    }
}
