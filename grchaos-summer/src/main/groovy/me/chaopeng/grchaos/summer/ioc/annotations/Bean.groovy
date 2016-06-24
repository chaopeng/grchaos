package me.chaopeng.grchaos.summer.ioc.annotations

import java.lang.annotation.*

/**
 * me.chaopeng.grchaos.summer.ioc.annotations.Bean
 *
 * @author chao
 * @version 1.0 - 2016-05-31
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Bean {
    String value() default ""
}