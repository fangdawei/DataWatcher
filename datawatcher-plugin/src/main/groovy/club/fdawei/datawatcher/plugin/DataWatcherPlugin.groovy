package club.fdawei.datawatcher.plugin

import club.fdawei.datawatcher.plugin.log.PluginLogger
import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DataWatcherPlugin implements Plugin<Project> {

    private static final String TAG = "DataWatcherPlugin"

    void apply(Project project) {
        PluginLogger.i(TAG, "apply DataWatcherTransform")
        def android = project.extensions.findByType(AppExtension)
        android.registerTransform(new DataWatcherTransform())
    }
}