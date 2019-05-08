package club.fdawei.datawatcher.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target(ElementType.METHOD)
public @interface DataWatch {

    Class<?> data();

    String field();

    int thread() default Thread.CURRENT;

    boolean notifyWhenBind() default true;

    final class Thread {
        public static final int CURRENT = 0;
        public static final int MAIN = 1;
        public static final int WORK_THREAD = 2;
    }
}
