package me.chaopeng.chaos4g.summer.aop
/**
 * me.chaopeng.chaos4g.summer.aop.IAspectHandler
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
trait IAspectHandler {

    def target

    abstract void begin(String name, Object[] args)
    abstract void before(String name, Object[] args)
    abstract boolean filter(String name, Object[] args)
    abstract void end(String name, Object[] args)
    abstract void error(String name, Object[] args, Throwable error)
    abstract void after(String name, Object[] args)

}
