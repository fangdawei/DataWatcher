package club.fdawei.datawatcher.plugin

import club.fdawei.datawatcher.plugin.injector.DataWatcherInjector
import club.fdawei.datawatcher.plugin.injector.InjectInfo
import club.fdawei.datawatcher.plugin.log.PluginLogger
import club.fdawei.datawatcher.plugin.util.JarUtils
import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils

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
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        PluginLogger.i(TAG, "transform start")
        def dataWatcherInjector = new DataWatcherInjector()
        List<InjectInfo> injectInfoList = new LinkedList<>()
        transformInvocation.inputs.each {
            input ->
                input.directoryInputs.each {
                    dir ->
                        PluginLogger.i(TAG, "transform ${dir.scopes} ${dir.file.absolutePath}")
                        dataWatcherInjector.addClassPath(dir.file.absolutePath)
                        def dest = transformInvocation.outputProvider.getContentLocation(dir.name, dir.contentTypes, dir.scopes, Format.DIRECTORY)
                        injectInfoList.add(new InjectInfo(dir.file, dest, Format.DIRECTORY))
                }
                input.jarInputs.each {
                    jar ->
                        def jarName = jar.name
                        if (jarName.endsWith(".jar")) {
                            jarName = jarName.substring(0, jarName.length() - 4)
                        }
                        def jarDestName = jarName + DigestUtils.md2Hex(jar.file.absolutePath)
                        def dest = transformInvocation.outputProvider.getContentLocation(jarDestName, jar.contentTypes, jar.scopes, Format.JAR)
                        if (dataWatcherInjector.checkJarNeedInject(jar.file.absolutePath)) {
                            PluginLogger.i(TAG, "transform ${jar.scopes} inject=true ${jar.file.absolutePath}")
                            def tmpDir = new File(transformInvocation.getContext().temporaryDir, DigestUtils.md2Hex(jar.file.absolutePath))
                            if (tmpDir.exists()) {
                                tmpDir.delete()
                            }
                            JarUtils.unzipJarFile(jar.file.absolutePath, tmpDir.absolutePath)
                            dataWatcherInjector.addClassPath(tmpDir.absolutePath)
                            injectInfoList.add(new InjectInfo(tmpDir, dest, Format.JAR))
                        } else {
                            PluginLogger.i(TAG, "transform ${jar.scopes} inject=false ${jar.file.absolutePath}")
                            dataWatcherInjector.addClassPath(jar.file.absolutePath)
                            FileUtils.copyFile(jar.file, dest)
                        }
                }
        }
        injectInfoList.each {
            injectInfo ->
                dataWatcherInjector.inject(injectInfo)
                if (injectInfo.format == Format.DIRECTORY) {
                    FileUtils.copyDirectory(injectInfo.sourceDir, injectInfo.destFile)
                } else {
                    JarUtils.zipJarFile(injectInfo.sourceDir.absolutePath, injectInfo.destFile.absolutePath)
                }
        }
        dataWatcherInjector.clear()
        PluginLogger.i(TAG, "transform end")
    }
}
