package de.uni_koblenz.jgralab.greql2.funlib;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import de.uni_koblenz.jgralab.greql2.funlib.Function.Category;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD, ElementType.CONSTRUCTOR })
public @interface Description {
	String description() default "";

	Category[] categories() default {};

	String[] params() default {};
}

