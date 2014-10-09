package org.obicere.cc.shutdown;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * org.obicere.cc.shutdown
 * Created: 4/7/14 11:53 AM
 *
 * @author Obicere
 * @version 1.0
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface HookValue {

    public String value() default "";

}
