package club.fdawei.datawatcher.processor.watcher;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.processor.common.GenClassInfoDef;
import club.fdawei.datawatcher.processor.common.TypeBox;

/**
 * Created by david on 2019/4/16.
 */
public class DataWatchOwnerClassInfo {

    private String pkgName;
    private String simpleName;
    private TypeElement typeElement;
    private Set<ExecutableElement> executableSet = new LinkedHashSet<>();

    public DataWatchOwnerClassInfo(String pkgName, TypeElement typeElement) {
        this.pkgName = pkgName;
        this.typeElement = typeElement;
        String qualifiedName = typeElement.getQualifiedName().toString();
        String nameWithoutPkg = qualifiedName.replace(pkgName + ".", "");
        simpleName = nameWithoutPkg.replace(".", "$");
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getProxySimpleName() {
        return simpleName + GenClassInfoDef.WatcherProxy.NAME_SUFFIX;
    }

    public String getCreatorSimpleName() {
        return simpleName + GenClassInfoDef.WatcherProxyCreator.NAME_SUFFIX;
    }

    public void addExecutableElement(ExecutableElement executable) {
        executableSet.add(executable);
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder watcherProxyClassBuilder = TypeSpec.classBuilder(getProxySimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(TypeBox.ABS_WATCHER_PROXY, TypeName.get(typeElement.asType())));

        watcherProxyClassBuilder.addField(TypeName.get(typeElement.asType()), GenClassInfoDef.WatcherProxy.FIELD_TARGET_NAME, Modifier.PRIVATE);

        MethodSpec watcherProxyConstructorMethod = MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(typeElement.asType()), "target")
                .addStatement("super(target)")
                .build();
        watcherProxyClassBuilder.addMethod(watcherProxyConstructorMethod);

        MethodSpec initBindKeysMethod = MethodSpec.methodBuilder("initBindKeys")
                .addModifiers(Modifier.PROTECTED)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class)
                .addComment("here will insert code")
                .build();
        watcherProxyClassBuilder.addMethod(initBindKeysMethod);

        MethodSpec.Builder initPublishersMethodBuilder = MethodSpec.methodBuilder("initPublishers")
                .addModifiers(Modifier.PROTECTED)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class);
        for (ExecutableElement executableElement : executableSet) {
            DataWatch dataWatch = executableElement.getAnnotation(DataWatch.class);
            TypeName paramTypeName = TypeName.get(executableElement.getParameters().get(0).asType());
            TypeName sourceType;
            TypeName fieldType;
            if (paramTypeName instanceof ClassName) {
                sourceType = TypeName.OBJECT;
                fieldType = TypeName.OBJECT;
            } else if (paramTypeName instanceof ParameterizedTypeName) {
                ParameterizedTypeName parameterizedTypeName = (ParameterizedTypeName) paramTypeName;
                List<TypeName> argTypeNameList = parameterizedTypeName.typeArguments;
                sourceType = argTypeNameList.size() > 0 ? argTypeNameList.get(0) : TypeName.OBJECT;
                fieldType = argTypeNameList.size() > 1 ? argTypeNameList.get(1) : TypeName.OBJECT;
            } else {
                continue;
            }

            String methodName = executableElement.getSimpleName().toString();
            String methodBindKeyFieldName = methodName + "_BindKey";
            FieldSpec methodKeyField = FieldSpec.builder(String.class, methodBindKeyFieldName)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            watcherProxyClassBuilder.addField(methodKeyField);

            String publisherName = executableElement.getSimpleName().toString() + "_Publisher";
            MethodSpec publishMethod = MethodSpec.methodBuilder("publish")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PROTECTED)
                    .returns(TypeName.VOID)
                    .addParameter(Object.class, "source")
                    .addParameter(Object.class, "oldValue")
                    .addParameter(Object.class, "newValue")
                    .beginControlFlow("if (getTarget() == null)")
                    .addStatement("return")
                    .endControlFlow()
                    .beginControlFlow("if (!(source instanceof $T) " +
                            "|| (oldValue != null && !(oldValue instanceof $T)) " +
                            "|| (newValue != null && !(newValue instanceof $T)))",
                            sourceType, fieldType, fieldType)
                    .addStatement("return")
                    .endControlFlow()
                    .addStatement("$T event = $T.obtainChangeEvent(source, oldValue, newValue)", paramTypeName, TypeBox.CHANGE_EVENT)
                    .addStatement("getTarget().$L(event)", executableElement.getSimpleName().toString())
                    .build();
            TypeSpec publisher = TypeSpec.anonymousClassBuilder("")
                    .superclass(TypeBox.NOTIFY_PUBLISHER)
                    .addMethod(publishMethod)
                    .build();
            initPublishersMethodBuilder.addComment("register $L publisher", executableElement.getSimpleName().toString());
            initPublishersMethodBuilder.addStatement("$T $L = $L", TypeBox.NOTIFY_PUBLISHER, publisherName, publisher);
            initPublishersMethodBuilder.addStatement("$L.setThread($L)", publisherName, dataWatch.thread());
            initPublishersMethodBuilder.addStatement("$L.setNeedNotifyWhenBind($L)", publisherName, dataWatch.notifyWhenBind());
            initPublishersMethodBuilder.addStatement("registerPublisher($N, $L)", methodBindKeyFieldName, publisherName);
        }
        watcherProxyClassBuilder.addMethod(initPublishersMethodBuilder.build());
        return watcherProxyClassBuilder.build();
    }

    public TypeSpec buildCreatorTypeSpec() {
        ClassName proxyClass = ClassName.get(pkgName, getProxySimpleName());
        ClassName targetClass = ClassName.get(typeElement);
        MethodSpec createWatcherProxyMethod = MethodSpec.methodBuilder("createWatcherProxy")
                .addModifiers(Modifier.PUBLIC)
                .returns(proxyClass)
                .addParameter(targetClass, "target")
                .addStatement("return new $T(target)", proxyClass)
                .build();
        TypeSpec.Builder creatorClassBuilder = TypeSpec.classBuilder(getCreatorSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(TypeBox.I_WATCHER_PROXY_CREATOR, proxyClass, targetClass))
                .addMethod(createWatcherProxyMethod);
        return creatorClassBuilder.build();
    }
}
