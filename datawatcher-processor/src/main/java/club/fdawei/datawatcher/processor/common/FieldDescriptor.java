package club.fdawei.datawatcher.processor.common;

import javax.lang.model.element.TypeElement;

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
}
