package club.fdawei.datawatcher.processor.watcher;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.processor.common.CommonTag;
import club.fdawei.datawatcher.processor.common.JavaClassGenerator;
import club.fdawei.datawatcher.processor.common.TypeBox;

/**
 * Created by david on 2019/4/16.
 */
public class WatcherProxyGenerator extends JavaClassGenerator {

    private static final String TAG = CommonTag.TAG;

    private Map<TypeElement, DataWatchOwnerClassInfo> dataWatchOwnerMap = new LinkedHashMap<>();

    public void clear() {
        dataWatchOwnerMap.clear();
    }

    public void addExecutableWithDataWatch(ExecutableElement executableElement) {
        if (!checkExecutableElementValid(executableElement)) {
            return;
        }
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        addExecutableReal(typeElement, executableElement);
    }

    public void addTypeWithDataWatcher(TypeElement typeElement) {
        final Map<String, ExecutableElement> executableMap = new LinkedHashMap<>();
        //查找被@DataWatch注解的方法
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            if (!checkExecutableElementValid(executableElement)) {
                continue;
            }
            executableMap.put(executableElement.getSimpleName().toString(), executableElement);
        }

        //查找父类被@DataWatch注解的方法
        TypeMirror superTypeMirror = typeElement.getSuperclass();
        while (superTypeMirror != null && superTypeMirror.getKind() != TypeKind.NONE
                && !TypeName.get(superTypeMirror).equals(TypeName.OBJECT)) {
            if (superTypeMirror.getKind() != TypeKind.DECLARED) {
                break;
            }
            DeclaredType superDeclaredType = (DeclaredType) superTypeMirror;
            Element superElement = superDeclaredType.asElement();
            if (superElement.getKind() != ElementKind.CLASS) {
                break;
            }
            TypeElement superTypeElement = (TypeElement) superElement;
            for (Element element : superTypeElement.getEnclosedElements()) {
                if (element.getKind() != ElementKind.METHOD) {
                    continue;
                }
                ExecutableElement executableElement = (ExecutableElement) element;
                if (!checkExecutableElementValid(executableElement)) {
                    continue;
                }
                String methodName = executableElement.getSimpleName().toString();
                if (executableMap.containsKey(methodName)) {
                    //子类已经覆盖，忽略
                    continue;
                }
                executableMap.put(methodName, executableElement);
            }
            superTypeMirror = superTypeElement.getSuperclass();
        }

        for (ExecutableElement executableElement : executableMap.values()) {
            addExecutableReal(typeElement, executableElement);
        }
    }

    public void addExecutableReal(TypeElement typeElement, ExecutableElement executableElement) {
        DataWatchOwnerClassInfo dataWatchOwner = dataWatchOwnerMap.get(typeElement);
        if (dataWatchOwner == null) {
            dataWatchOwner = new DataWatchOwnerClassInfo(getPkgName(typeElement), typeElement);
            dataWatchOwnerMap.put(typeElement, dataWatchOwner);
        }
        dataWatchOwner.addExecutableElement(executableElement);
    }

    @Override
    public void genJavaFile(Filer filer) {
        for(DataWatchOwnerClassInfo dataWatchOwner : dataWatchOwnerMap.values()) {
            String pkgName = dataWatchOwner.getPkgName();
            String simpleName = dataWatchOwner.getProxySimpleName();
            JavaFile watcherProxyJavaFile = JavaFile.builder(pkgName, dataWatchOwner.buildTypeSpec())
                    .addFileComment("Generated automatically. Do not modify!")
                    .build();
            try {
                watcherProxyJavaFile.writeTo(filer);
                logi(TAG, "gen success %s.%s", pkgName, simpleName);
            } catch (IOException e) {
                loge(TAG, "gen error %s.%s, %s", pkgName, simpleName, e.getMessage());
                e.printStackTrace();
            }
            String creatorSimpleName = dataWatchOwner.getCreatorSimpleName();
            JavaFile creatorJavaFile = JavaFile.builder(pkgName, dataWatchOwner.buildCreatorTypeSpec())
                    .addFileComment("Generated automatically. Do not modify!")
                    .build();
            try {
                creatorJavaFile.writeTo(filer);
                logi(TAG, "gen success %s.%s", pkgName, creatorSimpleName);
            } catch (IOException e) {
                loge(TAG, "gen error %s.%s, %s", pkgName, creatorSimpleName, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean checkExecutableElementValid(ExecutableElement executableElement) {
        if (executableElement == null) {
            return false;
        }
        String methodName = executableElement.getSimpleName().toString();
        //check method with @DataWatch
        if (executableElement.getAnnotation(DataWatch.class) == null) {
            logw(TAG, "Method %s without @DataWatch", methodName);
            return false;
        }
        //check method
        if (!executableElement.getModifiers().contains(Modifier.PUBLIC)) {
            logw(TAG, "Method %s with @DataWatch should be public", methodName);
            return false;
        }
        //check method return
        if (!TypeName.get(executableElement.getReturnType()).equals(TypeName.VOID)) {
            logw(TAG, "Method %s with @DataWatch return should be void", methodName);
            return false;
        }
        //check method parameter
        List<? extends VariableElement> parameters = executableElement.getParameters();
        if (parameters == null || parameters.size() != 1) {
            logw(TAG, "Method %s with @DataWatch but parameter illegal", methodName);
            return false;
        }
        TypeName typeName = TypeName.get(parameters.get(0).asType());
        if (typeName instanceof ParameterizedTypeName) {
            ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
            if (!parameterizedTypeName.rawType.equals(TypeBox.CHANGE_EVENT)) {
                logw(TAG, "Method %s with @DataWatch but parameter illegal", methodName);
                return false;
            }
        } else if (typeName instanceof ClassName) {
            ClassName className = (ClassName) typeName;
            if (!className.equals(TypeBox.CHANGE_EVENT)) {
                logw(TAG, "Method %s with @DataWatch but parameter illegal", methodName);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
