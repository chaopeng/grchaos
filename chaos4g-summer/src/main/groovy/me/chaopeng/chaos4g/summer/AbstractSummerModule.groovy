package me.chaopeng.chaos4g.summer

/**
 * me.chaopeng.chaos4g.summer.AbstractSummerModule
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
abstract class AbstractSummerModule {


    protected Summer summer

    protected abstract void configure()
    protected abstract void start()
    protected abstract void stop()



}
