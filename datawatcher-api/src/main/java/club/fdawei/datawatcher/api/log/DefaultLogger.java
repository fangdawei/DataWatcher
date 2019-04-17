package club.fdawei.datawatcher.api.log;

import android.util.Log;

public class DefaultLogger implements ILogger {

    @Override
    public void i(String tag, String format, Object... args) {
        Log.i(tag, String.format(format, args));
    }

    @Override
    public void e(String tag, String format, Object... args) {
        Log.e(tag, String.format(format, args));
    }

    @Override
    public void d(String tag, String format, Object... args) {
        Log.d(tag, String.format(format, args));
    }
}
