package me.chaopeng.grchaos.summer.aop.annotations

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

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