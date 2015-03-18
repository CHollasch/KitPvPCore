package us.supremeprison.kitpvp.core.module.modifiers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Connor Hollasch
 * @since 3/11/2015
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ModuleDependency {
    String[] depends_on() default {"modulemanager"};
}
