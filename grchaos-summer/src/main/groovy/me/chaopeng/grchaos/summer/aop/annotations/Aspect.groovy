package me.chaopeng.grchaos.summer.aop.annotations

import java.lang.annotation.*

/**
 * me.chaopeng.grchaos.summer.aop.annotations.Aspect
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Aspect {
    String handler()

    Type type() default Type.PUBLIC

    /**
     * @see AspectMe
     */
    enum Type {
        ALL, PUBLIC, ANNOTATION
    }
}