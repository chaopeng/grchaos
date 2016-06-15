package me.chaopeng.grchaos.summer.aop
/**
 * me.chaopeng.grchaos.summer.aop.IAspectHandler
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
trait IAspectHandler {

    def target

    abstract void begin(String name, Object[] args)
    abstract void before(String name, Object[] args)
    boolean filter(String name, Object[] args) {
        return true
    }
    abstract void end(String name, Object[] args)
    void error(String name, Object[] args, Throwable error) {
        throw error
    }
    abstract void after(String name, Object[] args)

}
