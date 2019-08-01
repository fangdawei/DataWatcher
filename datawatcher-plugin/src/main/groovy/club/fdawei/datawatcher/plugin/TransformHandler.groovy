package club.fdawei.datawatcher.plugin

import club.fdawei.datawatcher.plugin.injector.IInjector
import club.fdawei.datawatcher.plugin.injector.InjectEntity
import club.fdawei.datawatcher.plugin.injector.InjectInfo
import club.fdawei.datawatcher.plugin.injector.InjectorImpl
import club.fdawei.datawatcher.plugin.log.PluginLogger
import club.fdawei.datawatcher.plugin.util.InjectUtils
import club.fdawei.datawatcher.plugin.util.JarUtils
import com.android.build.api.transform.*
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils

class TransformHandler {

    private static final String TAG = 'TransformHandler'

    private final IInjector injector = new InjectorImpl()
    private final List<String> classPathList = new LinkedList<>()
    private final List<String> baseClassPathList = new LinkedList<>()
    private final List<InjectInfo> injectInfoList = new LinkedList<>()
    private TransformInvocation transformInvocation

    TransformHandler(TransformInvocation transformInvocation) {
        this.transformInvocation = transformInvocation
    }

    void addBaseClassPath(String path) {
        if (path == null || path.isEmpty()) {
            return
        }
        baseClassPathList.add(path)
    }

    private void syncAddClassPath(String path) {
        synchronized (classPathList) {
            classPathList.add(path)
        }
    }

    private void syncAddInjectInfo(InjectInfo injectInfo) {
        synchronized (injectInfoList) {
            injectInfoList.add(injectInfo)
        }
    }

    void transform() {
        PluginLogger.i(TAG, "transform start, incremental=${transformInvocation.incremental}")
        def startTimeMillis = System.currentTimeMillis()
        injectInfoList.clear()
        classPathList.clear()
        classPathList.addAll(baseClassPathList)
        if (!transformInvocation.incremental) {
            transformInvocation.outputProvider.deleteAll()
        }
        final WaitableExecutor checkExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        transformInvocation.inputs.each {
            input ->
                input.directoryInputs.each {
                    dir ->
                        checkExecutor.execute {
                            handleDir(dir)
                        }
                }
                input.jarInputs.each {
                    jar ->
                        checkExecutor.execute {
                            handleJar(jar)
                        }
                }
        }
        checkExecutor.waitForTasksWithQuickFail(true)
        injector.addClassPath(classPathList)
        final WaitableExecutor injectExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        injectInfoList.each {
            injectInfo ->
                PluginLogger.i(TAG, "handle ${injectInfo}")
                injectExecutor.execute {
                    injector.inject(injectInfo)
                    onAfterInject(injectInfo)
                }
        }
        injectExecutor.waitForTasksWithQuickFail(true)
        injector.clear()
        def endTimeMillis = System.currentTimeMillis()
        PluginLogger.i(TAG, "transform end, cost time %.3fs", (endTimeMillis - startTimeMillis) / 1000f)
    }

    private void handleDir(DirectoryInput dirInput) {
        def destDir = transformInvocation.outputProvider.getContentLocation(dirInput.name,
                dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
        if (transformInvocation.incremental) {
            if (dirInput.file.exists()) {
                syncAddClassPath(dirInput.file.absolutePath)
                List<InjectEntity> injectEntityList = new LinkedList<>()
                def changedFileMap = dirInput.getChangedFiles()
                changedFileMap.entrySet().each {
                    entry ->
                        def destFile = new File(entry.key.absolutePath.replace(dirInput.file.absolutePath,
                                destDir.absolutePath))
                        switch (entry.value) {
                            case Status.ADDED:
                            case Status.CHANGED:
                                def injectEntity = InjectUtils.collectFromClassFile(entry.key, dirInput.file)
                                if (injectEntity != null) {
                                    injectEntityList.add(injectEntity)
                                } else {
                                    FileUtils.copyFile(entry.key, destFile)
                                }
                                break
                            case Status.REMOVED:
                                FileUtils.deleteIfExists(destFile)
                                break
                        }
                }
                if (injectEntityList.size() > 0) {
                    def injectInfo = new InjectInfo(dirInput.file, destDir, InjectInfo.Type.FILE_LIST)
                    injectInfo.setEntities(injectEntityList)
                    syncAddInjectInfo(injectInfo)
                }
            } else {
                FileUtils.deleteRecursivelyIfExists(destDir)
            }
        } else {
            syncAddClassPath(dirInput.file.absolutePath)
            List<InjectEntity> injectEntityList = InjectUtils.collectFromDir(dirInput.file)
            if (injectEntityList != null && injectEntityList.size() > 0) {
                def injectInfo = new InjectInfo(dirInput.file, destDir, InjectInfo.Type.DIR)
                injectInfo.setEntities(injectEntityList)
                syncAddInjectInfo(injectInfo)
            } else {
                FileUtils.copyDirectory(dirInput.file, destDir)
            }
        }
    }

    private void handleJar(JarInput jarInput) {
        def jarDestName = DigestUtils.md2Hex(jarInput.file.absolutePath).concat('.jar')
        def dest = transformInvocation.outputProvider.getContentLocation(jarDestName,
                jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (transformInvocation.incremental) {
            switch (jarInput.status) {
                case Status.ADDED:
                case Status.CHANGED:
                    handleChangedJar(jarInput, dest)
                    break
                case Status.REMOVED:
                    FileUtils.deleteIfExists(dest)
                    break
                case Status.NOTCHANGED:
                    syncAddClassPath(jarInput.file.absolutePath)
                    break
            }
        } else {
            handleChangedJar(jarInput, dest)
        }
    }

    private void handleChangedJar(JarInput jarInput, File dest) {
        def injectEntityList = InjectUtils.collectFromJar(jarInput.file.absolutePath)
        if (injectEntityList != null && injectEntityList.size() > 0) {
            def tmpDir = new File(transformInvocation.getContext().temporaryDir,
                    DigestUtils.md2Hex(jarInput.file.absolutePath))
            JarUtils.unzipJarFile(jarInput.file.absolutePath, tmpDir.absolutePath)
            syncAddClassPath(tmpDir.absolutePath)
            def injectInfo = new InjectInfo(tmpDir, dest, InjectInfo.Type.JAR)
            injectInfo.setEntities(injectEntityList)
            syncAddInjectInfo(injectInfo)
        } else {
            syncAddClassPath(jarInput.file.absolutePath)
            FileUtils.copyFile(jarInput.file, dest)
        }
    }

    private void onAfterInject(InjectInfo injectInfo) {
        switch (injectInfo.type) {
            case InjectInfo.Type.DIR:
                FileUtils.copyDirectory(injectInfo.sourceDir, injectInfo.destFile)
                break
            case InjectInfo.Type.JAR:
                JarUtils.zipJarFile(injectInfo.sourceDir.absolutePath, injectInfo.destFile.absolutePath)
                break
            case InjectInfo.Type.FILE_LIST:
                injectInfo.entities.each {
                    def sourceFile = new File(injectInfo.sourceDir, it.name)
                    def destFile = new File(injectInfo.destFile, it.name)
                    if (destFile.exists()) {
                        destFile.delete()
                    }
                    FileUtils.copyFile(sourceFile, destFile)
                }
                break
        }
    }
}