package me.chaopeng.chaos4g.summer

/**
 * me.chaopeng.chaos4g.summer.AbstractSummerModule
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
abstract class AbstractSummerModule {


    protected Summer summer

    abstract void configure()
    abstract void start()
    abstract void stop()

}
