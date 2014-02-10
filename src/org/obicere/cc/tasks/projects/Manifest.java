package org.obicere.cc.tasks.projects;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author Obicere
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Manifest {

    public String author();

    public double version();

    public int difficulty();

    public String description() default "";

}
