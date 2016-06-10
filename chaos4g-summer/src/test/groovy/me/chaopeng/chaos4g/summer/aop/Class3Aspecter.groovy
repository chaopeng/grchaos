package me.chaopeng.chaos4g.summer.aop

/**
 * me.chaopeng.chaos4g.summer.aop.Class3Aspecter
 *
 * @author chao
 * @version 1.0 - 2016-06-09
 */
class Class3Aspecter implements IAspectHandler {
    @Override
    void begin(String name, Object[] args) {
    }

    @Override
    void before(String name, Object[] args) {

    }

    @Override
    void end(String name, Object[] args) {

    }

    @Override
    void after(String name, Object[] args) {
        target.call++
    }
}
