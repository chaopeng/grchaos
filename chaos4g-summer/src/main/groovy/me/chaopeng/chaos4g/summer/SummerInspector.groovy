package me.chaopeng.chaos4g.summer

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.chaopeng.chaos4g.summer.bean.DependencyBean
import me.chaopeng.chaos4g.summer.bean.NamedBean
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import me.chaopeng.chaos4g.summer.utils.ReflectUtils

/**
 * me.chaopeng.chaos4g.summer.SummerInspector
 *
 * @author chao
 * @version 1.0 - 2016-06-10
 */
class SummerInspector {

    static List<NamedBean> allBeans(Summer summer) {
        return summer.namedBeans.collect { NamedBean.builder().name(it.key).object(it.value).build() }
    }

    static Multimap<String, String> allDepes(Summer summer) {
        Multimap<String, String> res = ArrayListMultimap.create();
        summer.namedBeans.each { k, v ->
            ReflectUtils.getFieldsByAnnotation(v, Inject.class).each { field ->
                def name = Summer.getBeanNameFromField(field)
                res.put(k, name)
            }
        }

        return res
    }

    /**
     * @return missing deps
     */
    static List<DependencyBean> testDeps(Summer summer, String className) {
        return testDeps(summer, summer.classLoader.findClass(className))
    }

    /**
     * @return missing deps
     */
    static List<DependencyBean> testDeps(Summer summer, Class clazz) {

        def obj = clazz.newInstance()
        def result = []

        ReflectUtils.getFieldsByAnnotation(obj, Inject.class).each { field ->
            def name = Summer.getBeanNameFromField(field)
            if (summer.getBean(name) == null) {
                result << new DependencyBean(object: obj, field: field, name: name)
            }
        }

        return result
    }

    /**
     * @return all missing deps
     */
    static Multimap<String, DependencyBean> testAllDepes(Summer summer) {
        return summer.testAllDepes()
    }


}
