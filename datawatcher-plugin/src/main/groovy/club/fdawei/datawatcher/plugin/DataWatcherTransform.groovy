package club.fdawei.datawatcher.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager


class DataWatcherTransform extends Transform {

    private static final String TAG = "DataWatcherTransform"

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
    void transform(TransformInvocation transformInvocation)
            throws TransformException, InterruptedException, IOException {
        new TransformHandler(transformInvocation).transform()
    }
}
