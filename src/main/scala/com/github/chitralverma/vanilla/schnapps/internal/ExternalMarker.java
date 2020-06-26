package com.github.chitralverma.vanilla.schnapps.internal;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExternalMarker {
    String tpe() default "";
}