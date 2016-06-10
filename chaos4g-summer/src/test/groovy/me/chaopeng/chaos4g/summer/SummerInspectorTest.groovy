package me.chaopeng.chaos4g.summer

import me.chaopeng.chaos4g.summer.bean.PackageScan
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import spock.lang.Specification

/**
 * me.chaopeng.chaos4g.summer.SummerInspectorTest
 *
 * @author chao
 * @version 1.0 - 2016-06-10
 */
class SummerInspectorTest extends Specification {

    Summer summer

    def setup() {
        TestHelper.reloadableClassesSetup()
        summer = new Summer("tmp")
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                fromPackage(new PackageScan(packageName: "test"))
            }
        })
    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
    }

    def "all beans"() {
        expect:
        SummerInspector.allBeans(summer)*.name ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"]
    }

    def "all depes"() {
        def map = SummerInspector.allDepes(summer)

        expect:
        map.get(name).size() == number

        where:
        name          | number
        "class1"      | 1
        "class3"      | 1
        "class1Inner" | 2
    }

    static class DepsMissingClass{
        @Inject
        def missing
    }

    def "test deps"() {
        def missing = SummerInspector.testDeps(summer, DepsMissingClass.class.name)

        expect:
        missing.any{
            it.name == "missing" && it.object.class == DepsMissingClass.class
        }
    }

    def "test all depes"() {

    }
}
