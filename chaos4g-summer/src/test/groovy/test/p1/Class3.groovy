package test.p1

import me.chaopeng.chaos4g.summer.aop.annotations.Aspect
import me.chaopeng.chaos4g.summer.aop.annotations.AspectMe
import me.chaopeng.chaos4g.summer.ioc.annotations.Bean
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import me.chaopeng.chaos4g.summer.ioc.lifecycle.Initialization

/**
 * test.p1.Class3
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
@Bean
@Aspect(handler = "me.chaopeng.chaos4g.summer.aop.Class3Aspecter", type = Aspect.Type.ANNOTATION)
class Class3 implements Initialization {

    def call = -1

    @Inject
    def class1

    @Override
    void initializate() {
        call = 0
    }

    @AspectMe
    def getCall() {
        return call
    }
}
