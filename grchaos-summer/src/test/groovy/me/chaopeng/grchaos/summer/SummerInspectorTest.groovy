package me.chaopeng.grchaos.summer

import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.ioc.annotations.Inject
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.SummerInspectorTest
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
        SummerInspector.allBeans(summer)*.name.sort() ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"].sort()
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

    static class DepsMissingClass {
        @Inject
        def missing
    }

    def "test deps"() {
        def missing = SummerInspector.testDeps(summer, DepsMissingClass.class.name)

        expect:
        missing.size() == 1
        missing.any {
            it.name == "missing" && it.object.class == DepsMissingClass.class
        }
    }

    def "test all depes: pass"() {
        def missing = SummerInspector.testAllDepes(summer)

        expect:
        missing.isEmpty()
    }

    def "test all depes: missing"() {
        summer = new Summer("tmp")
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                fromPackage(new PackageScan(packageName: "test"))
                bean("m", new DepsMissingClass())
            }
        })

        def missing = SummerInspector.testAllDepes(summer)

        expect:
        missing.get("m").size() == 1
        missing.get("m").any {
            it.name == "missing" && it.object.class == DepsMissingClass.class
        }
    }
}
