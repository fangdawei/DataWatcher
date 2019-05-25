package club.fdawei.datawatcher.plugin.util


import com.android.utils.FileUtils
import javassist.CtClass
import javassist.bytecode.AnnotationsAttribute

/**
 * Created by david on 2019/05/22.
 */
final class ClassUtils {

    /**
     * 根据类文件获取类名
     * @param classFile 类文件
     * @param dir 类文件所在目录
     * @return 类名
     */
    static String getClassNameFromFile(File classFile, File dir) {
        def relativePath = FileUtils.relativePath(classFile, dir)
        def className = relativePath
                .replace(File.separator, '.')
                .replace('.class', '')
        return className
    }

    /**
     * 判断CtClass是否被注解
     * @param ctClass
     * @param type
     * @param runtimeVisible
     * @return boolean
     */
    static boolean isCtClassWithAnno(CtClass ctClass, String type, boolean runtimeVisible) {
        if (ctClass == null) {
            return false
        }
        def annoAttr = ctClass.classFile.getAttribute(runtimeVisible ?
                AnnotationsAttribute.visibleTag : AnnotationsAttribute.invisibleTag)
        if (annoAttr == null) {
            return false
        }
        def anno = (annoAttr as AnnotationsAttribute).getAnnotation(type)
        if (anno == null) {
            return false
        }
        return true
    }
}
