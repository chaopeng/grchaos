package me.chaopeng.grchaos.summer.aop.annotations

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * me.chaopeng.grchaos.summer.aop.annotations.AspectMe
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface AspectMe {
}