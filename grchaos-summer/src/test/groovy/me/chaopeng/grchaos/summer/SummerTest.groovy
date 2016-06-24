package me.chaopeng.grchaos.summer

import me.chaopeng.grchaos.summer.bean.NamedBean
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.bean.SummerAware
import me.chaopeng.grchaos.summer.exceptions.SummerException
import spock.lang.Specification
import test.Class1
import test.Class2
import test.p1.Class3

/**
 * me.chaopeng.grchaos.summer.SummerTest
 *
 * @author chao
 * @version 1.0 - 2016-06-09
 */
class SummerTest extends Specification {

    Summer summer

    def setup() {
        TestHelper.setup()
        summer = new Summer("tmp")

    }

    def cleanup() {
        TestHelper.cleanup()
    }


    def "load bean"() {

        setup:
        Summer summer = new Summer("tmp")
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected List<NamedBean> configure() {
                return [
                        bean(new Class1())
                        , bean("class3", new Class3())
                        , fromClass(Class1.Class1Inner.class)
                        , fromClass(Class2.class.name)
                ]
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
            protected List<NamedBean> configure() {
                return fromPackage(new PackageScan(packageName: "test"))
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

        summer.getBeansByType(GroovyObject.class.name).keySet().sort() ==
                [
                        "class1", "class2", "class3", "class1Inner",
                        "srcClass1", "srcClass1Inner", "srcClass2"
                ].sort()
    }

    def "inject & injectMe"() {
        setup:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected List<NamedBean> configure() {
                return fromPackage(new PackageScan(packageName: "test", recursive: false))
            }
        })
        summer.preStart()

        def class1 = summer.getBean("class1")

        Class3 class3 = new Class3()
        summer.injectMe(class3)

        expect:
        class1.class2.class == Class2.class
        class3.class1.class == Class1.class
        class1 in SummerAware
        class1.summer == summer
    }

    def "injectMe & aspest"() {
        setup:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected List<NamedBean> configure() {
                return fromPackage(new PackageScan(packageName: "test", recursive: false))
            }
        })
        summer.preStart()

        Class3 class3 = new Class3()

        expect:

        class3.getCall() == -1
        class3.getCall() == -1
        class3.getCall() == -1
        summer.injectMe(class3)
        class3.getCall() == -1
        class3.getCall() == 0
        class3.getCall() == 1

    }

    def "initializate & aspest"() {
        setup:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected List<NamedBean> configure() {
                return fromPackage(new PackageScan(packageName: "test", recursive: true))
            }
        })
        summer.preStart()

        Class3 class3 = summer.getBean("class3")

        expect:

        class3.getCall() == 0
        class3.getCall() == 1
        class3.getCall() == 2

    }

    def "bean name conflict"() {

        when:
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected List<NamedBean> configure() {
                List<NamedBean> res = []
                res.addAll(fromPackage(new PackageScan(packageName: "test", recursive: true)))
                res.add(bean("class1", new Object()))
                return res
            }
        })

        then:
        thrown(SummerException)
    }


}
