package club.fdawei.datawatcher.perfectprocessor.source;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import club.fdawei.datawatcher.annotation.DataSource;
import club.fdawei.datawatcher.annotation.FieldIgnore;
import club.fdawei.datawatcher.annotation.FieldSetter;
import club.fdawei.datawatcher.perfectprocessor.common.AnnoWithClassInfo;

/**
 * Created by david on 2019/4/16.
 */
public class DataSourceClassInfo extends AnnoWithClassInfo {

    private String pkgName;
    private TypeElement typeElement;
    private List<VariableElement> dataFieldList;
    private List<ExecutableElement> possibleSetterMethodList;

    public DataSourceClassInfo(String pkgName, TypeElement typeElement) {
        this.pkgName = pkgName;
        this.typeElement = typeElement;
    }

    public String getPkgName() {
        return pkgName;
    }

    public TypeElement getTypeElement() {
        return typeElement;
    }

    private void checkFieldsAndMethods() {
        dataFieldList = new LinkedList<>();
        possibleSetterMethodList = new LinkedList<>();
        DataSource annoDataSource = typeElement.getAnnotation(DataSource.class);
        boolean autoFindSetter = annoDataSource.setterAutoFind();
        List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
        for (Element element : enclosedElements) {
            switch (element.getKind()) {
                case FIELD:
                    VariableElement varElement = (VariableElement) element;
                    if (varElement.getAnnotation(FieldIgnore.class) == null) {
                        dataFieldList.add(varElement);
                    }
                    break;
                case METHOD:
                    ExecutableElement execElement = (ExecutableElement) element;
                    if (autoFindSetter) {
                        possibleSetterMethodList.add(execElement);
                    } else {
                        if (execElement.getAnnotation(FieldSetter.class) != null) {
                            possibleSetterMethodList.add(execElement);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public List<VariableElement> getDataFields() {
        if (dataFieldList != null) {
            return dataFieldList;
        }
        checkFieldsAndMethods();
        return dataFieldList;
    }

    public List<ExecutableElement> getPossibleSetterMethods() {
        if (possibleSetterMethodList != null) {
            return possibleSetterMethodList;
        }
        checkFieldsAndMethods();
        return possibleSetterMethodList;
    }
}
