package me.chaopeng.chaos4g.summer.aop
/**
 * me.chaopeng.chaos4g.summer.aop.IAspectHandler
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
interface IAspectHandler {

    void begin(name, args);
    void before(name, args);
    boolean filter(name, args);
    void end(name, args);
    void error(name, args, error);
    void after(name, args);

}
