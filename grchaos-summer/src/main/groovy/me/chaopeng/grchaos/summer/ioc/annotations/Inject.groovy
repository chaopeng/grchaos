package me.chaopeng.grchaos.summer.ioc.annotations

import java.lang.annotation.*

/**
 * me.chaopeng.grchaos.summer.ioc.annotations.Inject
 *
 * @author chao
 * @version 1.0 - 2016-05-31
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Inject {
    String value() default ""
}