package club.fdawei.datawatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Create by david on 2019/04/22
 */

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface WatchInherit {
    int maxGenerations() default 0;
}
