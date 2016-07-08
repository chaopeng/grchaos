package me.chaopeng.grchaos.summer

import com.google.common.base.CaseFormat
import me.chaopeng.grchaos.summer.bean.NamedBean
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.ioc.annotations.Bean

/**
 * me.chaopeng.grchaos.summer.AbstractSummerModule
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
abstract class AbstractSummerModule {


    protected Summer summer

    protected abstract List<NamedBean> configure()

    protected List<NamedBean> reloadableBeansConfigure() {
        return []
    }

    protected void start() {}

    protected void stop() {}

    final NamedBean bean(String name, Object obj) {
        return NamedBean.builder().name(name).object(obj).build()
    }

    final NamedBean bean(Object obj) {
        Bean bean = obj.class.getAnnotation(Bean.class)
        if (bean != null) {
            String name
            if (!bean.value().isEmpty()) {
                name = bean.value()
            } else {
                name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, obj.class.simpleName)
            }
            return NamedBean.builder().name(name).object(obj).build()
        }
        return null
    }

    final NamedBean fromClass(String className) {
        return fromClass(summer.classLoader.loadClass(className))
    }

    final NamedBean fromClass(Class clazz) {
        def o = clazz.newInstance()
        return bean(o)
    }

    final List<NamedBean> fromPackage(PackageScan packageScan) {
        return summer.classLoader.scanPackage(packageScan).values()
                .findResults { fromClass(it) }
                .collect()

    }

}
