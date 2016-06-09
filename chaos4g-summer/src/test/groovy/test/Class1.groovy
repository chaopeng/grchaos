package test

import me.chaopeng.chaos4g.summer.aop.annotations.AspectMe
import me.chaopeng.chaos4g.summer.ioc.annotations.Bean
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject

@Bean
class Class1 {

    @Inject
    def class2

    @Bean
    static class Class1Inner {
        @Inject
        private def class2

        @Inject
        private def class1

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
    }

}
