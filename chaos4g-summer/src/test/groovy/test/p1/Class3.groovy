package test.p1

import me.chaopeng.chaos4g.summer.aop.annotations.Aspect
import me.chaopeng.chaos4g.summer.ioc.annotations.Bean
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject

/**
 * test.p1.Class3
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
@Bean
@Aspect(handler = "me.chaopeng.chaos4g.summer.aop.Class3Aspecter")
class Class3 {

    def call = 0

    @Inject
    def class1

    def getCall() {
        return call
    }
}
