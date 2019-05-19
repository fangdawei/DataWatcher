package club.fdawei.datawatcher.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class DataWatcherPlugin implements Plugin<Project> {

    private static final String TAG = "DataWatcherPlugin"

    void apply(Project project) {
        def android = project.extensions.findByType(AppExtension)
        if (android != null) {
            android.registerTransform(new DataWatcherTransform())
        }
    }
}