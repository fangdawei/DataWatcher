package club.fdawei.datawatcher.plugin.log

class PluginLogger {

    static void i(String tag, String format, Object... args) {
        System.out.println(String.format("%s %s", tag, String.format(format, args)))
    }

    static void e(String tag, String format, Object... args) {
        System.err.println(String.format("%s %s", tag, String.format(format, args)))
    }
}
