package me.chaopeng.chaos4g.summer.aop
/**
 * me.chaopeng.chaos4g.summer.aop.IAspectHandler
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
interface IAspectHandler {

    void begin(String name, Object[] args);
    void before(String name, Object[] args);
    boolean filter(String name, Object[] args);
    void end(String name, Object[] args);
    void error(String name, Object[] args, Throwable error);
    void after(String name, Object[] args);

}
