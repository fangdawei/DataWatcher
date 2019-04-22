package club.fdawei.datawatcher.plugin

import club.fdawei.datawatcher.plugin.common.ClassInfoBox
import club.fdawei.datawatcher.plugin.injector.DataWatcherInjector
import club.fdawei.datawatcher.plugin.injector.InjectClassInfo
import club.fdawei.datawatcher.plugin.injector.InjectInfo
import club.fdawei.datawatcher.plugin.log.PluginLogger
import club.fdawei.datawatcher.plugin.util.JarUtils
import com.android.build.api.transform.*
import com.android.ide.common.internal.WaitableExecutor
import com.android.utils.FileUtils
import org.apache.commons.codec.digest.DigestUtils

import java.util.jar.JarFile

class TransformHandler {

    private static final String TAG = 'TransformHandler'

    private final DataWatcherInjector dataWatcherInjector = new DataWatcherInjector()
    private final List<String> classPathList = new LinkedList<>()
    private final List<InjectInfo> injectInfoList = new LinkedList<>()
    private TransformInvocation transformInvocation

    TransformHandler(TransformInvocation transformInvocation) {
        this.transformInvocation = transformInvocation
    }

    private void addClassPath(String path) {
        synchronized (classPathList) {
            classPathList.add(path)
        }
    }

    private void addInjectInfo(InjectInfo injectInfo) {
        synchronized (injectInfoList) {
            injectInfoList.add(injectInfo)
        }
    }

    void transform() {
        PluginLogger.i(TAG, "transform start, incremental=${transformInvocation.incremental}")
        def startTimeMillis = System.currentTimeMillis()
        injectInfoList.clear()
        classPathList.clear()
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
        dataWatcherInjector.addClassPath(classPathList)
        final WaitableExecutor injectExecutor = WaitableExecutor.useGlobalSharedThreadPool()
        injectInfoList.each {
            injectInfo ->
                PluginLogger.i(TAG, "handle ${injectInfo}")
                injectExecutor.execute {
                    handleInject(injectInfo)
                }
        }
        injectExecutor.waitForTasksWithQuickFail(true)
        dataWatcherInjector.clear()
        def endTimeMillis = System.currentTimeMillis()
        PluginLogger.i(TAG, "transform end, cost time %.3fs", (endTimeMillis - startTimeMillis) / 1000f)
    }

    private void handleInject(InjectInfo injectInfo) {
        dataWatcherInjector.inject(injectInfo)
        switch (injectInfo.type) {
            case InjectInfo.Type.DIR:
                FileUtils.copyDirectory(injectInfo.sourceDir, injectInfo.destFile)
                break
            case InjectInfo.Type.JAR:
                JarUtils.zipJarFile(injectInfo.sourceDir.absolutePath, injectInfo.destFile.absolutePath)
                break
            case InjectInfo.Type.FILE_LIST:
                if (injectInfo.classInfoList != null) {
                    injectInfo.classInfoList.each {
                        def sourceFile = new File(injectInfo.sourceDir, it.name)
                        def destFile = new File(injectInfo.destFile, it.name)
                        if (destFile.exists()) {
                            destFile.delete()
                        }
                        FileUtils.copyFile(sourceFile, destFile)
                    }
                }
                break
        }
    }

    private void handleDir(DirectoryInput dirInput) {
        addClassPath(dirInput.file.absolutePath)
        def destDir = transformInvocation.outputProvider.getContentLocation(dirInput.name, dirInput.contentTypes, dirInput.scopes, Format.DIRECTORY)
        if (transformInvocation.incremental) {
            List<InjectClassInfo> classInfoList = new LinkedList<>()
            def changedFileMap = dirInput.getChangedFiles()
            changedFileMap.entrySet().each {
                entry ->
                    def file = entry.key
                    def destFile = new File(file.absolutePath.replace(dirInput.file.absolutePath, destDir.absolutePath))
                    switch (entry.value) {
                        case Status.ADDED:
                        case Status.CHANGED:
                            def injectClassInfo = findClassNeedInjectFromClass(file, dirInput.file)
                            if (injectClassInfo != null) {
                                classInfoList.add(injectClassInfo)
                            } else {
                                FileUtils.copyFile(file, destFile)
                            }
                            break
                        case Status.REMOVED:
                            FileUtils.deletePath(destFile)
                            break
                    }
            }
            if (classInfoList.size() > 0) {
                def injectInfo = new InjectInfo(dirInput.file, destDir, InjectInfo.Type.FILE_LIST)
                injectInfo.setClassInfoList(classInfoList)
                addInjectInfo(injectInfo)
            }
        } else {
            List<InjectClassInfo> classInfoList = findClassNeedInjectFromDir(dirInput.file)
            if (classInfoList != null && classInfoList.size() > 0) {
                def injectInfo = new InjectInfo(dirInput.file, destDir, InjectInfo.Type.DIR)
                injectInfo.setClassInfoList(classInfoList)
                addInjectInfo(injectInfo)
            } else {
                FileUtils.copyDirectory(dirInput, destDir)
            }
        }
    }

    private static InjectClassInfo findClassNeedInjectFromClass(File classFile, File dir) {
        if (ClassInfoBox.DataFields.isDataFields(classFile.name)) {
            return new InjectClassInfo(FileUtils.relativePath(classFile, dir), InjectClassInfo.Type.DATAFIELDS)
        } else if (ClassInfoBox.WatcherProxy.isWatcherProxy(classFile.name)) {
            return new InjectClassInfo(FileUtils.relativePath(classFile, dir), InjectClassInfo.Type.WATCHERPROXY)
        }
        return null
    }

    private static List<InjectClassInfo> findClassNeedInjectFromDir(File dir) {
        final List<InjectClassInfo> classInfoList = new LinkedList<>()
        if (dir.exists()) {
            dir.eachFileRecurse {
                file ->
                    if (file.directory) {
                        return
                    }
                    if (ClassInfoBox.DataFields.isDataFields(file.name)) {
                        def classInfo = new InjectClassInfo(FileUtils.relativePath(file, dir), InjectClassInfo.Type.DATAFIELDS)
                        classInfoList.add(classInfo)
                    } else if (ClassInfoBox.WatcherProxy.isWatcherProxy(file.name)) {
                        def classInfo = new InjectClassInfo(FileUtils.relativePath(file, dir), InjectClassInfo.Type.WATCHERPROXY)
                        classInfoList.add(classInfo)
                    }
            }
        }
        return classInfoList
    }

    private void handleJar(JarInput jarInput) {
        def jarName = jarInput.name
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }
        def jarDestName = jarName + DigestUtils.md2Hex(jarInput.file.absolutePath) + '.jar'
        def dest = transformInvocation.outputProvider.getContentLocation(jarDestName, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        if (transformInvocation.incremental) {
            switch (jarInput.status) {
                case Status.ADDED:
                case Status.CHANGED:
                    handleNeedCheckJar(jarInput, dest)
                    break
                case Status.REMOVED:
                    FileUtils.deleteIfExists(dest)
                    break
                case Status.NOTCHANGED:
                    addClassPath(jarInput.file.absolutePath)
                    break
            }
        } else {
            handleNeedCheckJar(jarInput, dest)
        }
    }

    private void handleNeedCheckJar(JarInput jarInput, File dest) {
        def classInfoList = findClassNeedInjectFromJar(jarInput.file.absolutePath)
        if (classInfoList != null && classInfoList.size() > 0) {
            def tmpDir = new File(transformInvocation.getContext().temporaryDir, DigestUtils.md2Hex(jarInput.file.absolutePath))
            JarUtils.unzipJarFile(jarInput.file.absolutePath, tmpDir.absolutePath)
            addClassPath(tmpDir.absolutePath)
            def injectInfo = new InjectInfo(tmpDir, dest, InjectInfo.Type.JAR)
            injectInfo.setClassInfoList(classInfoList)
            addInjectInfo(injectInfo)
        } else {
            addClassPath(jarInput.file.absolutePath)
            FileUtils.copyFile(jarInput.file, dest)
        }
    }

    private static List<InjectClassInfo> findClassNeedInjectFromJar(String jarPath) {
        def jarFile = new JarFile(jarPath)
        def entries = jarFile.entries()
        final List<InjectClassInfo> classInfoList = new LinkedList<>()
        while (entries.hasMoreElements()) {
            def jarEntry = entries.nextElement()
            if (jarEntry.isDirectory()) {
                continue
            }
            if (ClassInfoBox.DataFields.isDataFields(jarEntry.name)) {
                def classInfo = new InjectClassInfo(jarEntry.name, InjectClassInfo.Type.DATAFIELDS)
                classInfoList.add(classInfo)
            } else if (ClassInfoBox.WatcherProxy.isWatcherProxy(jarEntry.name)) {
                def classInfo = new InjectClassInfo(jarEntry.name, InjectClassInfo.Type.WATCHERPROXY)
                classInfoList.add(classInfo)
            }
        }
        return classInfoList
    }
}