package club.fdawei.datawatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by david on 2019/05/24.
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface DataFields {
}
