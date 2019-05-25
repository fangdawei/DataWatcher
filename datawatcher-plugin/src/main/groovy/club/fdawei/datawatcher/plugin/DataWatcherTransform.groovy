package club.fdawei.datawatcher.plugin


import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Project

class DataWatcherTransform extends Transform {

    private static final String TAG = "DataWatcherTransform"

    private Project project

    DataWatcherTransform(Project project) {
        this.project = project
    }

    @Override
    String getName() {
        return "DataWatcherTransform"
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws
            TransformException, InterruptedException, IOException {
        def handler = new TransformHandler(transformInvocation)
        handler.addBaseClassPath(getAndroidJarPath())
        handler.transform()
    }

    private String getAndroidJarPath() {
        if (project == null) {
            return null
        }
        def baseExtension = project.extensions.getByType(BaseExtension)
        def sdkDir = baseExtension.sdkDirectory
        def sdkVersion = baseExtension.getCompileSdkVersion()
        def path = "platforms${File.separator}${sdkVersion}${File.separator}android.jar"
        return new File(sdkDir, path).absolutePath
    }
}
