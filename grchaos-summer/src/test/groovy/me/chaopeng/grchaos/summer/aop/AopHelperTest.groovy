package me.chaopeng.grchaos.summer.aop

import me.chaopeng.grchaos.summer.aop.annotations.Aspect
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.aop.AopHelperTest
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class AopHelperTest extends Specification {

    class UselessAspectHandler implements IAspectHandler{

        String log = ""

        @Override
        void begin(String name, Object[] args) {
            log+="begin,"
        }

        @Override
        void before(String name, Object[] args) {
            log+="before,"
        }

        @Override
        boolean filter(String name, Object[] args) {
            log+="filter,"
            return true
        }

        @Override
        void end(String name, Object[] args) {
            log+="end,"
        }

        @Override
        void error(String name, Object[] args, Throwable error) {
            log+="${error.getMessage()},"
        }

        @Override
        void after(String name, Object[] args) {
            log+="after"
        }
    }

    @Aspect(handler = "")
    class UselessClass {

        private privateMethod(){

        }

        def nonVoidMethod(){
            return "nonVoidMethod return"
        }

        def voidMethod(){

        }

        def exceptionMethod(){
            throw new Exception("this is an exception")
        }
    }

    def obj = new UselessClass()
    def handler = new UselessAspectHandler()

    void setup() {
        AopHelper.install(obj, handler)
    }

    def "invoke private method"() {
        when:
        obj.privateMethod()
        then:
        handler.log == ""
    }

    def "invoke method with return"() {
        expect:
        obj.nonVoidMethod() == "nonVoidMethod return"
        handler.log == "begin,filter,before,end,after"
    }

    def "invoke void method"() {
        when:
        obj.voidMethod()
        then:
        handler.log == "begin,filter,before,end,after"
    }

    def "invoke method with exception"() {
        when:
        obj.exceptionMethod()
        then:
        handler.log == "begin,filter,before,this is an exception,after"
    }
}
