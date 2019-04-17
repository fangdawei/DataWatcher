package club.fdawei.datawatcher.plugin.log

class PluginLogger {

    static void i(String tag, String msg) {
        System.out.println "==> $tag $msg"
    }

    static void e(String tag, String msg) {
        System.err.println "==> $tag $msg"
    }
}
