package me.chaopeng.chaos4g.summer

import me.chaopeng.chaos4g.summer.bean.PackageScan

/**
 * me.chaopeng.chaos4g.summer.AbstractSummerModule
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
abstract class AbstractSummerModule {


    protected Summer summer

    protected abstract void configure()
    protected void start(){}
    protected void stop(){}

    final void bean(String name, Object obj) {
        summer.bean(name, obj)
    }

    final void bean(Object obj) {
        summer.bean(obj)
    }

    final void fromClass(String className) {
        summer.beanFromClassName(className)
    }

    final void fromClass(Class clazz) {
        summer.beanFromClass(clazz)
    }

    final void fromPackage(PackageScan packageScan) {
        summer.beansFromPackage(packageScan)
    }

}
