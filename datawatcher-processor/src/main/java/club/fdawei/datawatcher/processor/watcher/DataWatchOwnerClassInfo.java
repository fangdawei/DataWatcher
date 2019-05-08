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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import club.fdawei.datawatcher.annotation.DataWatch;
import club.fdawei.datawatcher.processor.common.AnnoUtils;
import club.fdawei.datawatcher.processor.common.AnnotationWithClassInfo;
import club.fdawei.datawatcher.processor.common.ClassInfoBox;
import club.fdawei.datawatcher.processor.common.FieldDescriptor;
import club.fdawei.datawatcher.processor.common.TypeBox;

/**
 * Created by david on 2019/4/16.
 */
public class DataWatchOwnerClassInfo extends AnnotationWithClassInfo {

    private String pkgName;
    private String simpleName;
    private TypeElement typeElement;
    private Set<ExecutableElement> executableSet = new LinkedHashSet<>();

    public DataWatchOwnerClassInfo(String pkgName, String simpleName, TypeElement typeElement) {
        this.pkgName = pkgName;
        this.simpleName = simpleName;
        this.typeElement = typeElement;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getProxySimpleName() {
        return simpleName + ClassInfoBox.WatcherProxy.NAME_SUFFIX;
    }

    public String getCreatorSimpleName() {
        return simpleName + ClassInfoBox.WatcherProxyCreator.NAME_SUFFIX;
    }

    public void addExecutableElement(ExecutableElement executable) {
        executableSet.add(executable);
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder watcherProxyClassBuilder = TypeSpec.classBuilder(getProxySimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(TypeBox.ABS_WATCHER_PROXY, TypeName.get(typeElement.asType())));

        watcherProxyClassBuilder.addField(TypeName.get(typeElement.asType()),
                ClassInfoBox.WatcherProxy.FIELD_TARGET_NAME, Modifier.PRIVATE);

        MethodSpec watcherProxyConstructorMethod = MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(typeElement.asType()), "target")
                .addStatement("super(target)")
                .build();
        watcherProxyClassBuilder.addMethod(watcherProxyConstructorMethod);

        MethodSpec.Builder initBindKeysMethodBuilder = MethodSpec.methodBuilder("initBindKeys")
                .addModifiers(Modifier.PROTECTED)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class);

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
            FieldSpec methodBindKeyField = FieldSpec.builder(String.class, methodBindKeyFieldName)
                    .addModifiers(Modifier.PRIVATE)
                    .build();
            watcherProxyClassBuilder.addField(methodBindKeyField);

            AnnotationValue dataValue = AnnoUtils.getAnnoValue(executableElement, DataWatch.class, "data");
            if (dataValue != null) {
                TypeMirror dataClassType = (TypeMirror) dataValue.getValue();
                String bindKeyStr = FieldDescriptor.of(dataClassType, dataWatch.field());
                initBindKeysMethodBuilder.addStatement("this.$L = $S", methodBindKeyFieldName, bindKeyStr);
            }

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
        watcherProxyClassBuilder.addMethod(initBindKeysMethodBuilder.build());
        watcherProxyClassBuilder.addMethod(initPublishersMethodBuilder.build());
        return watcherProxyClassBuilder.build();
    }

    public TypeSpec buildCreatorTypeSpec() {
        ClassName proxyClass = ClassName.get(getPkgName(), getProxySimpleName());
        ClassName targetClass = ClassName.get(typeElement);
        MethodSpec createWatcherProxyMethod = MethodSpec.methodBuilder("createWatcherProxy")
                .addModifiers(Modifier.PUBLIC)
                .returns(proxyClass)
                .addParameter(TypeName.OBJECT, "target")
                .beginControlFlow("if (target instanceof $T)", targetClass)
                .addStatement("return new $T(($T) target)", proxyClass, targetClass)
                .nextControlFlow("else")
                .addStatement("return null")
                .endControlFlow()
                .build();
        TypeSpec.Builder creatorClassBuilder = TypeSpec.classBuilder(getCreatorSimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedTypeName.get(TypeBox.I_WATCHER_PROXY_CREATOR, proxyClass))
                .addMethod(createWatcherProxyMethod);
        return creatorClassBuilder.build();
    }
}
