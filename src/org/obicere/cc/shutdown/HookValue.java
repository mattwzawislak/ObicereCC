package org.obicere.cc.shutdown;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Obicere
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HookValue {

    public String value() default "";

}
