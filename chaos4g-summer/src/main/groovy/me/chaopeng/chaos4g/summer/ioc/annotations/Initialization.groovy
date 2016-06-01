package me.chaopeng.chaos4g.summer.ioc.annotations

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * me.chaopeng.chaos4g.summer.ioc.annotations.Initialization
 *
 * Caution!!!
 * <ul>
 *   <li>@Initialization method will invoke after Construtor and @Inject... Bean is already in Context</li>
 *   <li>Unsupport method with argument</li>
 * </ul>
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@interface Initialization {
}