package cn.xianyijun.planet.common.extension;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The interface Activate.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Activate {
    /**
     * Group string [ ].
     *
     * @return the string [ ]
     */
    String[] group() default {};

    /**
     * Value string [ ].
     *
     * @return the string [ ]
     */
    String[] value() default {};

    /**
     * Before string [ ].
     *
     * @return the string [ ]
     */
    String[] before() default {};

    /**
     * After string [ ].
     *
     * @return the string [ ]
     */
    String[] after() default {};

    /**
     * Order int.
     *
     * @return the int
     */
    int order() default 0;
}