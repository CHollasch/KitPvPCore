package kitpvp.core.util.config;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Connor Hollasch
 * @since 2/28/2015
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface ConfigOption {

    String configuration_section();

}
