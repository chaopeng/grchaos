package test

import me.chaopeng.chaos4g.summer.aop.annotations.AspectMe
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject

class Class1 {

    static class Class1Inner {
        @Inject
        private int i1

        @Inject
        private int i2

        @AspectMe
        private def a(){
            return 1
        }

        @AspectMe
        private def b(int i){
            return i
        }

        @AspectMe
        private def b(def i){
            return i
        }
    }

}
