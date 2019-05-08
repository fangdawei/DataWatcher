package club.fdawei.datawatcher.processor.common;

import com.squareup.javapoet.TypeName;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * Created by david on 2019/05/06.
 */
public class AnnoUtils {

    public static AnnotationMirror getAnnoMirror(Element element, Class<? extends Annotation> annoClz) {
        List<? extends AnnotationMirror> annoMirrors = element.getAnnotationMirrors();
        for(AnnotationMirror annoMirror : annoMirrors) {
            if (TypeName.get(annoMirror.getAnnotationType()).equals(TypeName.get(annoClz))) {
                return annoMirror;
            }
        }
        return null;
    }

    public static AnnotationValue getAnnoValue(AnnotationMirror mirror, String key) {
        if (mirror != null) {
            Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues = mirror.getElementValues();
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
                if (entry.getKey().getSimpleName().toString().equals(key)) {
                    return entry.getValue();
                }
            }
        }
        return null;
    }

    public static AnnotationValue getAnnoValue(Element element, Class<? extends Annotation> annoClz, String key) {
        return getAnnoValue(getAnnoMirror(element, annoClz), key);
    }
}
