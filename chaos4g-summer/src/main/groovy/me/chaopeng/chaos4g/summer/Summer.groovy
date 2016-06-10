package me.chaopeng.chaos4g.summer

import com.google.common.base.CaseFormat
import com.google.common.eventbus.Subscribe
import groovy.util.logging.Slf4j
import me.chaopeng.chaos4g.summer.aop.AopHelper
import me.chaopeng.chaos4g.summer.aop.IAspectHandler
import me.chaopeng.chaos4g.summer.aop.annotations.Aspect
import me.chaopeng.chaos4g.summer.bean.NamedBean
import me.chaopeng.chaos4g.summer.bean.PackageScan
import me.chaopeng.chaos4g.summer.bean.SummerAware
import me.chaopeng.chaos4g.summer.event.ClassChanges
import me.chaopeng.chaos4g.summer.exceptions.SummerException
import me.chaopeng.chaos4g.summer.ioc.annotations.Bean
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import me.chaopeng.chaos4g.summer.ioc.lifecycle.Destroy
import me.chaopeng.chaos4g.summer.ioc.lifecycle.Initialization
import me.chaopeng.chaos4g.summer.utils.ReflectUtils

import java.lang.ref.WeakReference

/**
 * me.chaopeng.chaos4g.summer.Summer
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
@Slf4j
class Summer {

    private SummerClassLoader classLoader
    private Map<String, Object> namedBeans = new HashMap<>()
    private List<WeakReference<Object>> anonymousBeans = new LinkedList<>()
    private List<PackageScan> watchPackages = new LinkedList<>()
    private Set<String> watchClasses = new HashSet<>()
    private AbstractSummerModule module
    private boolean isInit = false


    Summer(String srcRoot = null, boolean autoReload = false) {
        classLoader = SummerClassLoader.create(srcRoot)
        classLoader.eventBus.register(this)
    }

    ////////////////////////////////////
    // Life Cycle
    ////////////////////////////////////

    synchronized void loadModule(AbstractSummerModule module) {
        if (!isInit) {
            this.module = module
            module.summer = this
            module.configure()
            isInit = true
        }
    }

    synchronized void start() {
        doInject()
        doAddAspect()
        doInitializate()
        module.start()
    }

    @Subscribe
    synchronized void upgrade(ClassChanges changes) {

    }

    synchronized void stop() {
        module.stop()

    }

    /**
     * will do inject, aspect
     * @param object
     */
    void injectMe(Object object) {
        doInject(object)
        doAddAspect(object)
        anonymousBeans.add(new WeakReference<Object>(object))
    }

    private void doInject() {
        namedBeans.values().each { bean ->
            doInject(bean)
        }
    }

    private void doInject(Object object, Map m = namedBeans, boolean isUpgrade = false) {
        def fields = ReflectUtils.getFieldsByAnnotation(object, Inject.class)
        fields.each { field ->
            def inject = field.getAnnotation(Inject.class)
            def name = inject.value().isEmpty() ? field.getName() : inject.value()
            def bean = m.get(name)
            if (bean == null) {
                throw new SummerException("no bean named $name")
            } else {
                ReflectUtils.setField(object, field, bean)
            }
        }

        if (object in SummerAware) {
            ((SummerAware)object).summer = this
        }
    }

    private void doAddAspect() {
        namedBeans.values()
                .each { bean -> doAddAspect(bean) }
    }

    private void doAddAspect(Object object) {
        def aspect = object.class.getAnnotation(Aspect.class)
        if (aspect) {

            if (object in GroovyObject) {
                def handler = classLoader.findClass(aspect.handler())
                AopHelper.install((GroovyObject)object, (IAspectHandler)handler.newInstance())
            } else {
                log.warn("not support java class aop yet")
            }
        }
    }

    private void doInitializate() {
        namedBeans.values().each { bean ->
            doInitializate(bean)
        }
    }

    private void doInitializate(Object object) {
        if (object in Initialization) {
            ((Initialization)object).initializate()
        }
    }

    private void doDestroy() {
        namedBeans.values().each { bean ->
            doDestroy(bean)
        }
    }

    private void doDestroy(Object object) {
        if (object in Destroy) {
            ((Destroy)object).destroy()
        }
    }

    ////////////////////////////////////
    // bean define
    ////////////////////////////////////

    NamedBean bean(String name, Object object, boolean isUpgrade = false) {
        synchronized (this) {

            // check upgrade
            def oldBean = namedBeans.get(name)
            if (oldBean != null) {
                if (!isUpgrade) {
                    throw new SummerException("Bean ${name} is duplicated. ")
                }
            }

            // put into map only if !isUpgrade
            if (!isUpgrade) {
                namedBeans.put(name, object)
            }

            return NamedBean.builder().name(name).object(object).build()
        }
    }

    NamedBean bean(Object object, boolean isUpgrade = false) {
        Bean bean = object.class.getAnnotation(Bean.class)
        if (bean != null) {
            String name
            if (!bean.value().isEmpty()) {
                name = bean.value()
            } else {
                name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, object.class.simpleName)
            }
            watchClasses.add(object.class.name)
            return this.bean(name, object, isUpgrade)
        }
        return null
    }

    NamedBean beanFromClass(Class clazz, boolean isUpgrade = false) {
        def o = clazz.newInstance()
        return bean(o, isUpgrade)
    }

    NamedBean beanFromClassName(String className, boolean isUpgrade = false) {
        return beanFromClass(classLoader.findClass(className), isUpgrade)
    }

    Map<String, Object> beansFromClasses(List<Class> classes, boolean isUpgrade = false) {
        return classes.findResults {
            beanFromClass(it, isUpgrade)
        }.collectEntries {
            [(it.name): it.object]
        }
    }

    Map<String, Object> beansFromPackage(PackageScan packageScan, boolean isUpgrade = false) {
        if (!isUpgrade) {
            watchPackages.add(packageScan)
        }
        return beansFromClasses(classLoader.scanPackage(packageScan).toList(), isUpgrade)
    }

    ////////////////////////////////////
    // get bean(s)
    ////////////////////////////////////

    Object getBean(String name) {
        return namedBeans.get(name)
    }

    public <T> Map<String, T> getBeansByType(Class<T> clazz) {
        Map<String, T> res = [:]

        namedBeans.findAll { _, v -> v in T }.each { k, v -> res.put(k, (T)v) }

        return res
    }

    public <T> Map<String, T> getBeansInPackage(String packageName, Class<T> clazz = Object.class) {
        Map<String, T> res = [:]

        namedBeans.findAll { k, v ->
            v.getClass().name.startsWith(packageName) && v in T
        }.each { k, v ->
            res.put(k, (T)v)
        }

        return res;
    }

}
