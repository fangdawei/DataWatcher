package club.fdawei.datawatcher.processor.source;

import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import club.fdawei.datawatcher.annotation.FieldIgnore;
import club.fdawei.datawatcher.processor.common.GenClassInfoDef;


/**
 * Created by david on 2019/4/16.
 */
public class DataSourceClassInfo {

    private String pkgName;
    private String simpleName;
    private TypeElement typeElement;
    private Map<String, DataSourceClassInfo> innerClassMap = new LinkedHashMap<>();
    private boolean isTopClass = true;

    public DataSourceClassInfo(String pkgName, String simpleName) {
        this.pkgName = pkgName;
        this.simpleName = simpleName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public String getSimpleName() {
        return simpleName;
    }

    public void setTypeElement(TypeElement typeElement) {
        this.typeElement = typeElement;
    }

    public void setTopClass(boolean topClass) {
        this.isTopClass = topClass;
    }

    public DataSourceClassInfo getInnerClass(String simpleName) {
        return innerClassMap.get(simpleName);
    }

    public void addInnerClass(DataSourceClassInfo innerClass) {
        innerClassMap.put(innerClass.getSimpleName(), innerClass);
    }

    public TypeSpec buildTypeSpec() {
        TypeSpec.Builder classBuilder;
        if (isTopClass) {
            classBuilder = TypeSpec.classBuilder(GenClassInfoDef.DataFields.NAME_PREFIX + simpleName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        } else {
            classBuilder = TypeSpec.classBuilder(simpleName)
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        }
        if (typeElement != null) {
            classBuilder.addField(TypeName.get(typeElement.asType()), GenClassInfoDef.DataFields.FIELD_SOURCE_NAME, Modifier.PRIVATE);
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            if (enclosedElements != null) {
                for (Element element : enclosedElements) {
                    if (!(element instanceof VariableElement)) {
                        continue;
                    }
                    VariableElement variableElement = (VariableElement) element;
                    if (variableElement.getAnnotation(FieldIgnore.class) != null) {
                        continue;
                    }
                    String filedName = variableElement.getSimpleName().toString();
                    String filedValue = typeElement.getQualifiedName().toString() + "." + filedName;
                    FieldSpec fieldSpec = FieldSpec.builder(String.class, filedName)
                            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                            .initializer("$S", filedValue)
                            .build();
                    classBuilder.addField(fieldSpec);
                }
            }
        }
        for(DataSourceClassInfo innerClass : innerClassMap.values()) {
            classBuilder.addType(innerClass.buildTypeSpec());
        }
        return classBuilder.build();
    }
}
