package club.fdawei.datawatcher.perfectprocessor.source;

import java.util.LinkedList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import club.fdawei.datawatcher.perfectprocessor.common.AnnoWithClassInfo;

/**
 * Created by david on 2019/4/16.
 */
public class DataSourceClassInfo extends AnnoWithClassInfo {

    private String pkgName;
    private TypeElement typeElement;
    private List<VariableElement> fieldList;

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

    public List<VariableElement> getFieldList() {
        if (fieldList == null) {
            fieldList = new LinkedList<>();
            List<? extends Element> enclosedElements = typeElement.getEnclosedElements();
            for(Element element : enclosedElements) {
                if (element.getKind() != ElementKind.FIELD) {
                    continue;
                }
                VariableElement variableElement = (VariableElement) element;
                fieldList.add(variableElement);
            }
        }
        return fieldList;
    }
}
