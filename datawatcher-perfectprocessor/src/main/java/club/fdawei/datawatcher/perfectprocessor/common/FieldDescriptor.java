package club.fdawei.datawatcher.perfectprocessor.common;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Create by david on 2019/04/27.
 */
public class FieldDescriptor {

    public static final String SEPARATOR = ".";

    public static String of(Class clz, String fieldName) {
        return clz.getCanonicalName() + SEPARATOR + fieldName;
    }

    public static String of(TypeElement typeElement, String fieldName) {
        return typeElement.getQualifiedName() + SEPARATOR + fieldName;
    }

    public static String of(TypeMirror typeMirror, String fieldName) {
        return typeMirror.toString() + SEPARATOR + fieldName;
    }
}
