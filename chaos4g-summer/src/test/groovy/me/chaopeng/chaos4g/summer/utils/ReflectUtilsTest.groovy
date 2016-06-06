package me.chaopeng.chaos4g.summer.utils

import me.chaopeng.chaos4g.summer.aop.annotations.AspectMe
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import spock.lang.Specification
import test.Class1

/**
 * me.chaopeng.chaos4g.summer.utils.ReflectUtilsTest
 *
 * @author chao
 * @version 1.0 - 2016-06-05
 */
class ReflectUtilsTest extends Specification {

    def "get methods by annotation"() {
        expect:
        ReflectUtils.getMethodsByAnnotation(new Class1.Class1Inner(), AspectMe.class).collect {it.name}.sort() == ["a", "b", "b"].sort()
    }

    def "GetFieldsByAnnotation"() {
        expect:
        ReflectUtils.getFieldsByAnnotation(new Class1.Class1Inner(), Inject.class).collect {it.name}.sort() == ["i1", "i2"].sort()
    }

    def "call mathod"() {
        def obj = new Class1.Class1Inner()

        expect:
        ReflectUtils.callMethod(obj, obj.class.declaredMethods.find{it.name == "a"}, null) == 1
        ReflectUtils.callMethod(obj, obj.class.declaredMethods.find{it.name == "b"}, 1) == 1
        ReflectUtils.callMethod(obj, obj.class.declaredMethods.find{it.name == "b"}, "ss") == "ss"
    }

    def "get set field"() {
        def obj = new Class1.Class1Inner()

        expect:
        ReflectUtils.setField(obj, obj.class.declaredFields.find {it.name == "i1"}, 1)
        ReflectUtils.getField(obj, obj.class.declaredFields.find {it.name == "i1"}) == 1

    }
}
