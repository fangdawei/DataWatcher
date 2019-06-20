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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.annotation.WatchInherit;
import club.fdawei.datawatcher.processor.common.AnnoUtils;
import club.fdawei.datawatcher.processor.common.ClassInfoBox;
import club.fdawei.datawatcher.processor.common.CommonTag;
import club.fdawei.datawatcher.processor.common.JavaClassGenerator;
import club.fdawei.datawatcher.processor.common.TypeBox;

/**
 * Created by david on 2019/4/16.
 */
public class WatcherProxyGenerator extends JavaClassGenerator {

    private static final String TAG = CommonTag.TAG;

    private Map<TypeElement, WatchDataOwnerClassInfo> dataWatchOwnerMap = new LinkedHashMap<>();

    public void clear() {
        dataWatchOwnerMap.clear();
    }

    public void addExecutableWithDataWatch(ExecutableElement executableElement) {
        if (!checkExecutableWithDataWatchValid(executableElement)) {
            return;
        }
        TypeElement typeElement = (TypeElement) executableElement.getEnclosingElement();
        addExecutableReal(typeElement, executableElement);
    }

    public void addTypeWithWatchInherit(TypeElement typeElement) {
        WatchInherit watchInherit = typeElement.getAnnotation(WatchInherit.class);
        if (watchInherit == null) {
            return;
        }
        final int maxGenerations = watchInherit.maxGenerations() > 0 ? watchInherit.maxGenerations() : Integer.MAX_VALUE;
        final Map<String, ExecutableElement> executableMap = new LinkedHashMap<>();
        //查找被@DataWatch注解的方法
        for (Element element : typeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.METHOD) {
                continue;
            }
            ExecutableElement executableElement = (ExecutableElement) element;
            if (!checkExecutableWithDataWatchValid(executableElement)) {
                continue;
            }
            executableMap.put(executableElement.getSimpleName().toString(), executableElement);
        }

        //查找父类被@DataWatch注解的方法
        TypeMirror superTypeMirror = typeElement.getSuperclass();
        int currGeneration = 1;
        while (currGeneration <= maxGenerations && superTypeMirror != null
                && superTypeMirror.getKind() != TypeKind.NONE
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
                if (!checkExecutableWithDataWatchValid(executableElement)) {
                    continue;
                }
                String methodName = executableElement.getSimpleName().toString();
                if (executableMap.containsKey(methodName)) {
                    //子类已经覆盖注解，忽略
                    continue;
                }
                executableMap.put(methodName, executableElement);
            }
            superTypeMirror = superTypeElement.getSuperclass();
            currGeneration++;
        }

        for (ExecutableElement executableElement : executableMap.values()) {
            addExecutableReal(typeElement, executableElement);
        }
    }

    private void addExecutableReal(TypeElement typeElement, ExecutableElement executableElement) {
        WatchDataOwnerClassInfo watchDataOwner = dataWatchOwnerMap.get(typeElement);
        if (watchDataOwner == null) {
            String pkgName = getUtilProvider().getElementsUtils().
                    getPackageOf(typeElement).getQualifiedName().toString();
            String binaryName = getUtilProvider().getElementsUtils().getBinaryName(typeElement).toString();
            int index = binaryName.lastIndexOf('.');
            String simpleName;
            if (index >= 0 && index < binaryName.length() - 1) {
                simpleName = binaryName.substring(index + 1);
            } else {
                simpleName = binaryName;
            }
            String proxySimpleName = simpleName.replace('$', '_') +
                    ClassInfoBox.WatcherProxy.NAME_SUFFIX;
            watchDataOwner = new WatchDataOwnerClassInfo(pkgName, proxySimpleName, typeElement);
            dataWatchOwnerMap.put(typeElement, watchDataOwner);
        }
        watchDataOwner.addExecutableElement(executableElement);
    }

    @Override
    public void genJavaFile(Filer filer) {
        for(WatchDataOwnerClassInfo dataWatchOwner : dataWatchOwnerMap.values()) {
            String pkgName = dataWatchOwner.getPkgName();
            String simpleName = dataWatchOwner.getProxySimpleName();
            JavaFile watcherProxyJavaFile = JavaFile.builder(pkgName, dataWatchOwner.buildTypeSpec())
                    .addFileComment("Generated automatically by DataWatcher. Do not modify!")
                    .build();
            try {
                watcherProxyJavaFile.writeTo(filer);
                logi(TAG, "gen %s.%s", pkgName, simpleName);
            } catch (IOException e) {
                loge(TAG, "gen error %s.%s, %s", pkgName, simpleName, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private boolean checkExecutableWithDataWatchValid(ExecutableElement executableElement) {
        if (executableElement == null) {
            return false;
        }
        String methodDesc = String.format("Method %s(in %s)", executableElement.getSimpleName().toString(),
                executableElement.getEnclosingElement().getSimpleName().toString());
        //check WatchData
        WatchData watchData = executableElement.getAnnotation(WatchData.class);
        if (watchData == null) {
            logw(TAG, "%s without @WatchData", methodDesc);
            return false;
        }
        AnnotationValue annoDataValue = AnnoUtils.getAnnoValue(executableElement, WatchData.class, "data");
        if (annoDataValue == null) {
            logw(TAG, "%s with @WatchData but data's value is null", methodDesc);
            return false;
        }
        Element dataElement = ((DeclaredType) annoDataValue.getValue()).asElement();
        if (dataElement.getKind() != ElementKind.CLASS) {
            logw(TAG, "%s with @WatchData but data's value is not a class", methodDesc);
            return false;
        }
        //检查@WatchData中参数field是否是data的字段
        TypeElement dataTypeElement = (TypeElement) dataElement;
        List<? extends Element> enclosedElements = dataTypeElement.getEnclosedElements();
        boolean hasField = false;
        for(Element element : enclosedElements) {
            if (element.getKind() == ElementKind.FIELD) {
                VariableElement variableElement = (VariableElement) element;
                if (variableElement.getSimpleName().toString().equals(watchData.field())) {
                    hasField = true;
                    break;
                }
            }
        }
        if (!hasField) {
            logw(TAG, "%s with @WatchData but field %s not found in data source", methodDesc, watchData.field());
            return false;
        }
        //check method
        if (executableElement.getModifiers().contains(Modifier.PRIVATE)) {
            logw(TAG, "%s with @WatchData can not be private", methodDesc);
            return false;
        }
        //check method parameter
        List<? extends VariableElement> parameters = executableElement.getParameters();
        if (parameters.size() != 1) {
            logw(TAG, "%s with @WatchData but parameter illegal", methodDesc);
            return false;
        }
        TypeName typeName = TypeName.get(parameters.get(0).asType());
        if (typeName instanceof ParameterizedTypeName) {
            ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) typeName;
            if (!parameterizedTypeName.rawType.equals(TypeBox.CHANGE_EVENT)) {
                logw(TAG, "%s with @WatchData but parameter illegal", methodDesc);
                return false;
            }
        } else if (typeName instanceof ClassName) {
            ClassName className = (ClassName) typeName;
            if (!className.equals(TypeBox.CHANGE_EVENT)) {
                logw(TAG, "%s with @WatchData but parameter illegal", methodDesc);
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
