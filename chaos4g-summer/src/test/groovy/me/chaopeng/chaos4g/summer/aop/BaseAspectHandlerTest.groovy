package me.chaopeng.chaos4g.summer.aop

import groovy.util.logging.Slf4j
import spock.lang.Specification

/**
 * me.chaopeng.chaos4g.summer.aop.BaseAspectHandlerTest
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class BaseAspectHandlerTest extends Specification {

    @Slf4j
    class UselessAspectHandler implements  IAspectHandler{
        @Override
        void begin(name, args) {
            log.info("begin $name")
        }

        @Override
        void before(name, args) {
            log.info("before $name")
        }

        @Override
        boolean filter(name, args) {
            log.info("filter $name")
            return true
        }

        @Override
        void end(name, args) {
            log.info("end $name")
        }

        @Override
        void error(name, args, error) {
            log.error("$name throws error: $error", error)
            throw error
        }

        @Override
        void after(name, args) {
            log.info("after $name")
        }
    }

    class UselessClass {
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

    void setup() {
        AopHelper.install(obj, new UselessAspectHandler())
    }

    def "Invoke method with return"() {
        expect:
        obj.nonVoidMethod() == "nonVoidMethod return"
    }

    def "Invoke void method"() {
        when:
        obj.voidMethod()
        then:
        notThrown(Exception)
    }

    def "Invoke method with exception"() {
        when:
        obj.exceptionMethod()
        then:
        def e = thrown(Exception)
        e.message == "this is an exception"
    }
}
