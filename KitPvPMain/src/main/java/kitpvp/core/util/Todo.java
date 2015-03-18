package kitpvp.core.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Connor Hollasch
 * @since 3/12/2015
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Todo {
    abstract String[] value();
}
