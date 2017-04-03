package sde.application.test.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(value = TestInputs.class)
public @interface TestInput {
    String name() default "";

    String val() default "";

    String[] list() default "";
}
