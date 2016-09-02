package retrofit3.annotation.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by allen on 16-8-24.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.CLASS)
public @interface BaseConfig {
  String baseUrl() default "";
  boolean validateEagerly() default false;
}
