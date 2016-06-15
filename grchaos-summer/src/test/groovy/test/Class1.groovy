package test

import me.chaopeng.grchaos.summer.aop.annotations.AspectMe
import me.chaopeng.grchaos.summer.bean.SummerAware
import me.chaopeng.grchaos.summer.ioc.annotations.Bean
import me.chaopeng.grchaos.summer.ioc.annotations.Inject
import me.chaopeng.grchaos.summer.ioc.lifecycle.SummerUpgrade

@Bean
class Class1 implements SummerAware {

    @Inject
    def class2

    @Bean
    static class Class1Inner implements SummerUpgrade {

        int upgradeCount = 0

        @Inject
        private def class2

        @Inject
        private def srcClass1

        @AspectMe
        private def a(){
            return 1
        }

        @AspectMe
        private def b(int i){
            return i
        }

        @AspectMe
        private def c(def i){
            return i
        }

        @Override
        void upgrade() {
            upgradeCount++
        }
    }

}
