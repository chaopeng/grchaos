package me.chaopeng.grchaos.summer.ioc.lifecycle

/**
 * will trigger upgrade() when classloader get any change after Summer upgrade finish, not accept any Exception!!!
 *
 * only for namedBean
 *
 * me.chaopeng.grchaos.summer.ioc.lifecycle.SummerUpgrade
 *
 * @author chao
 * @version 1.0 - 2016-06-08
 */
interface SummerUpgrade {
    void upgrade()
}