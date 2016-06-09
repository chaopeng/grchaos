package me.chaopeng.chaos4g.summer

import me.chaopeng.chaos4g.summer.bean.PackageScan
import spock.lang.Specification
import test.Class1
import test.Class2
import test.p1.Class3

/**
 * me.chaopeng.chaos4g.summer.SummerTest
 *
 * @author chao
 * @version 1.0 - 2016-06-09
 */
class SummerTest extends Specification {

    Summer summer

    def setup() {
        TestHelper.reloadableClassesSetup()
        summer = new Summer("tmp")

    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
    }


    def "load bean"() {

        setup:
        Summer summer = new Summer("tmp")
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                bean(new Class1())
                bean("class3", new Class3())
                fromClass(Class1.Class1Inner.class)
                fromClass(Class2.class.name)
            }

            @Override
            protected void start() {

            }

            @Override
            protected void stop() {

            }
        })

        expect:
        summer.getBean(name).class == clazz

        where:

        name          | clazz
        "class1"      | Class1.class
        "class2"      | Class2.class
        "class3"      | Class3.class
        "class1Inner" | Class1.Class1Inner.class

    }

    def "load bean by package"() {

        setup:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                fromPackage(new PackageScan(packageName: "test"))
            }

            @Override
            protected void start() {

            }

            @Override
            protected void stop() {

            }
        })

        expect:
        summer.getBeansInPackage("test").keySet().sort() ==
                [
                        "class1", "class2", "class3", "class1Inner",
                        "srcClass1", "srcClass1Inner", "srcClass2"
                ].sort()

        summer.getBeansByType(GroovyObject.class).keySet().sort() ==
                [
                        "class1", "class2", "class3", "class1Inner",
                        "srcClass1", "srcClass1Inner", "srcClass2"
                ].sort()
    }

    def "inject & injectMe"(){
        setup:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                fromPackage(new PackageScan(packageName: "test", recursive: false))
            }

            @Override
            protected void start() {

            }

            @Override
            protected void stop() {

            }
        })
        summer.start()

        def class1 = summer.getBean("class1")

        Class3 class3 = new Class3()
        summer.injectMe(class3)

        expect:
        class1.class2.class == Class2.class
        class3.class1.class == Class1.class
    }

    def "injectMe & aspest"(){
        setup:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                fromPackage(new PackageScan(packageName: "test", recursive: false))
            }

            @Override
            protected void start() {

            }

            @Override
            protected void stop() {

            }
        })
        summer.start()

        Class3 class3 = new Class3()

        expect:

        class3.call == 0
        class3.call == 0
        class3.call == 0
        summer.injectMe(class3)
        class3.call == 0
        class3.call == 1
        class3.call == 2

    }

}
