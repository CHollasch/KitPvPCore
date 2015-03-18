package us.supremeprison.kitpvp.core.module.modifiers;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Connor Hollasch
 * @since 3/11/2015
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Immutable {

    From from() default From.ALL;

    static enum From {
        ALL(3),
        DISABLE(2),
        CHANGE(1);

        private int level;

        private From(int lvl) {
            this.level = lvl;
        }

        public int getImmutableLevel() {
            return level;
        }
    }
}
