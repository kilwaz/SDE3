package application.test.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AssertChange {
    String id() default "";

    String type() default "";

    String attribute() default "";

    String increasedBy() default "";

    String before() default "";

    String after() default "";
}
