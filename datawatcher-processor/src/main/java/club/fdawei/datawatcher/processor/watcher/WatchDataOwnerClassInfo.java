package club.fdawei.datawatcher.processor.watcher;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import club.fdawei.datawatcher.annotation.WatchData;
import club.fdawei.datawatcher.annotation.WatcherProxy;
import club.fdawei.datawatcher.processor.common.AnnoUtils;
import club.fdawei.datawatcher.processor.common.AnnoWithClassInfo;
import club.fdawei.datawatcher.processor.common.ClassInfoBox;
import club.fdawei.datawatcher.processor.common.CommonTag;
import club.fdawei.datawatcher.processor.common.TypeBox;

/**
 * Created by david on 2019/4/16.
 */
public class WatchDataOwnerClassInfo extends AnnoWithClassInfo {

    private static final String TAG = CommonTag.TAG;

    private String pkgName;
    private TypeElement typeElement;
    private Set<ExecutableElement> executableSet = new LinkedHashSet<>();
    private String proxySimpleName;

    public WatchDataOwnerClassInfo(String pkgName, String simpleName, TypeElement typeElement) {
        this.pkgName = pkgName;
        this.typeElement = typeElement;
        this.proxySimpleName = simpleName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getProxySimpleName() {
        return proxySimpleName;
    }

    public void addExecutableElement(ExecutableElement executable) {
        executableSet.add(executable);
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder watcherProxyClassBuilder = TypeSpec.classBuilder(getProxySimpleName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(TypeBox.ABS_WATCHER_PROXY, TypeName.get(typeElement.asType())))
                .addAnnotation(WatcherProxy.class);

        watcherProxyClassBuilder.addField(TypeName.get(typeElement.asType()),
                ClassInfoBox.WatcherProxy.FIELD_TARGET_NAME, Modifier.PRIVATE);

        MethodSpec watcherProxyConstructorMethod = MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(typeElement.asType()), "target")
                .addStatement("super(target)")
                .build();
        watcherProxyClassBuilder.addMethod(watcherProxyConstructorMethod);

        MethodSpec.Builder initPublishersMethodBuilder = MethodSpec.methodBuilder("initPublishers")
                .addModifiers(Modifier.PROTECTED)
                .returns(TypeName.VOID)
                .addAnnotation(Override.class);
        for (ExecutableElement executableElement : executableSet) {
            WatchData watchData = executableElement.getAnnotation(WatchData.class);
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

            AnnotationValue dataValue = AnnoUtils.getAnnoValue(executableElement, WatchData.class, "data");
            if (dataValue == null) {
                continue;
            }
            TypeMirror dataClassType = (TypeMirror) dataValue.getValue();

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
            initPublishersMethodBuilder.addStatement("$L.setThread($L)", publisherName, watchData.thread());
            initPublishersMethodBuilder.addStatement("$L.setNeedNotifyWhenBind($L)", publisherName, watchData.notifyWhenBind());
            initPublishersMethodBuilder.addStatement("registerPublisher($T.class, $S, $L)",
                    dataClassType, watchData.field(), publisherName);
        }
        watcherProxyClassBuilder.addMethod(initPublishersMethodBuilder.build());
        return watcherProxyClassBuilder.build();
    }
}
