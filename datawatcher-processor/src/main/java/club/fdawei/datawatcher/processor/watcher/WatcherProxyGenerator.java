package club.fdawei.datawatcher.processor.watcher;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.processor.common.JavaClassGenerator;
import club.fdawei.datawatcher.processor.common.TypeBox;

/**
 * Created by david on 2019/4/16.
 */
public class WatcherProxyGenerator extends JavaClassGenerator {

    private static final String TAG = "ClassGenerator";

    private Map<TypeElement, DataWatchOwnerClassInfo> dataWatchOwnerMap = new HashMap();

    public void addExecutableElement(ExecutableElement executableElement, String pkgName) {
        if (!checkExecutableElementValid(executableElement)) {
            return;
        }
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        DataWatchOwnerClassInfo dataWatchOwner = dataWatchOwnerMap.get(typeElement);
        if (dataWatchOwner == null) {
            dataWatchOwner = new DataWatchOwnerClassInfo(pkgName, typeElement);
            dataWatchOwnerMap.put(typeElement, dataWatchOwner);
        }
        dataWatchOwner.addExecutableElement(executableElement);
    }

    @Override
    public void genJavaFile(Filer filer) {
        for(DataWatchOwnerClassInfo dataWatchOwner : dataWatchOwnerMap.values()) {
            String pkgName = dataWatchOwner.getPkgName();
            String simpleName = dataWatchOwner.getSimpleName();
            JavaFile watcherProxyJavaFile = JavaFile.builder(pkgName, dataWatchOwner.buildTypeSpec())
                    .addFileComment("Generated automatically. Do not modify!")
                    .build();
            try {
                watcherProxyJavaFile.writeTo(filer);
                logi(TAG, "[%s.%s] gen success", pkgName, simpleName);
            } catch (IOException e) {
                loge(TAG, "[%s.%s] gen error", pkgName, simpleName);
                e.printStackTrace();
            }
            String creatorSimpleName = dataWatchOwner.getCreatorSimpleName();
            JavaFile creatorJavaFile = JavaFile.builder(pkgName, dataWatchOwner.buildCreatorTypeSpec())
                    .addFileComment("Generated automatically. Do not modify!")
                    .build();
            try {
                creatorJavaFile.writeTo(filer);
                logi(TAG, "[%s.%s] gen success", pkgName, creatorSimpleName);
            } catch (IOException e) {
                loge(TAG, "[%s.%s] gen error", pkgName, creatorSimpleName);
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
