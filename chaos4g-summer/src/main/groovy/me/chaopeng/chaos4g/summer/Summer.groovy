package me.chaopeng.chaos4g.summer

import com.google.common.base.CaseFormat
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import com.google.common.eventbus.Subscribe
import groovy.util.logging.Slf4j
import me.chaopeng.chaos4g.summer.aop.AopHelper
import me.chaopeng.chaos4g.summer.aop.IAspectHandler
import me.chaopeng.chaos4g.summer.aop.annotations.Aspect
import me.chaopeng.chaos4g.summer.bean.DependencyBean
import me.chaopeng.chaos4g.summer.bean.NamedBean
import me.chaopeng.chaos4g.summer.bean.PackageScan
import me.chaopeng.chaos4g.summer.bean.SummerAware
import me.chaopeng.chaos4g.summer.event.ClassChanges
import me.chaopeng.chaos4g.summer.exceptions.SummerException
import me.chaopeng.chaos4g.summer.ioc.annotations.Bean
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import me.chaopeng.chaos4g.summer.ioc.lifecycle.Destroy
import me.chaopeng.chaos4g.summer.ioc.lifecycle.Initialization
import me.chaopeng.chaos4g.summer.ioc.lifecycle.SummerUpgrade
import me.chaopeng.chaos4g.summer.utils.ClassPathScanner
import me.chaopeng.chaos4g.summer.utils.ReflectUtils

import java.lang.ref.WeakReference
import java.lang.reflect.Field

/**
 * me.chaopeng.chaos4g.summer.Summer
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
@Slf4j
class Summer {

    private final SummerClassLoader classLoader
    private AbstractSummerModule module
    protected Map<String, Object> namedBeans = [:]
    private List<WeakReference<Object>> anonymousBeans = new LinkedList<>()
    private List<PackageScan> watchPackages = new LinkedList<>()
    private Set<String> watchClasses = new HashSet<>()
    private boolean isInit = false

    Summer(String srcRoot = null, boolean autoReload = false) {
        classLoader = SummerClassLoader.create(srcRoot)
        classLoader.eventBus.register(this)
    }

    protected SummerClassLoader getClassLoader() {
        return classLoader
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

            Runtime.getRuntime().addShutdownHook({
                stop()
            });
        }
    }

    synchronized void start() {
        // check all dependencies
        def missing = testAllDepes()
        if (!missing.isEmpty()) {
            throw missingDepesException(missing)
        }

        doInject()
        doAddAspect()
        doInitializate()
        module.start()
    }

    @Subscribe
    synchronized void upgrade(ClassChanges changes) {

        if (!changes.isEmpty()) {
            try {
                Map<String, Object> newNamedBeans = [:]
                newNamedBeans.putAll(namedBeans)

                Map<String, Object> upgradedBeans = [:]

                // for updates
                changes.changes.each {
                    // if it already a bean, replace or remove
                    if (watchClasses.contains(it.name)) {
                        def b = bean(it.newInstance(), true)
                        if (b != null) {
                            // update bean
                            newNamedBeans.put(b.name, b.object)
                            upgradedBeans.put(b.name, b.object)
                        } else {
                            // class still here just without @Bean, but we ignore it
                        }
                    }

                    // else check is it in watchPackage
                    else if (watchPackages.any { p -> ClassPathScanner.filter(it.name, p) }) {
                        def b = bean(it.newInstance(), true)
                        if (b != null) {
                            // add bean
                            newNamedBeans.put(b.name, b.object)
                            upgradedBeans.put(b.name, b.object)
                        }
                    }
                }

                // for add
                changes.adds.each {
                    if (watchPackages.any { p -> ClassPathScanner.filter(it.name, p) }) {
                        def b = bean(it.newInstance(), true)
                        if (b != null) {
                            // add bean
                            newNamedBeans.put(b.name, b.object)
                        }
                    }
                }

                // for delete
                changes.deletes.each {
                    // we ignore it
                }

                // check all dependencies
                def missing = testAllDepes(newNamedBeans, newNamedBeans)
                if (!missing.isEmpty()) {
                    throw missingDepesException(missing)
                }

                // inject & init new obj
                doInject(upgradedBeans, newNamedBeans)
                doInitializate(upgradedBeans)

                // update inject in exists beans
                newNamedBeans.findAll { k, v ->
                    !upgradedBeans.containsKey(k)
                }.each { k, v ->
                    doInject(v, upgradedBeans, true)
                }

                anonymousBeans.removeAll { it.get() == null }
                anonymousBeans.each {
                    doInject(it, upgradedBeans, true)
                }

                // replace
                namedBeans = newNamedBeans

                // notify all upgrade()
                namedBeans.findAll { k, v ->
                    !upgradedBeans.containsKey(k)
                }.each { k, v ->
                    upgradeNotify(v)
                }

            } catch (Exception e) {
                log.error("upgrade failed. ${e.message}", e)
            }
        }

    }

    protected synchronized void stop() {
        module.stop()
        doDestroy()
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

    private void doInject(Map m = namedBeans, Map deps = namedBeans) {
        m.values().each { bean ->
            doInject(bean, deps)
        }
    }

    protected Multimap<String, DependencyBean> testAllDepes(Map<String, Object> m = namedBeans, Map<String, Object> deps = namedBeans) {
        Multimap<String, DependencyBean> res = ArrayListMultimap.create();
        m.namedBeans.each { k, v ->
            ReflectUtils.getFieldsByAnnotation(v, Inject.class).each { field ->
                def name = getBeanNameFromField(field)
                if (!deps.containsKey(name)) {
                    res.put(k as String, new DependencyBean(object: v, field: field, name: name))
                }
            }
        }

        return res
    }

    private SummerException missingDepesException(Multimap<String, DependencyBean> missing){
        def errorMessage = missing.values().collect {
            "inject ${it.object.class.name} failed: no bean named ${it.name}."
        }.join("\n")

        return new SummerException(errorMessage)
    }

    protected static String getBeanNameFromField(Field field) {
        def inject = field.getAnnotation(Inject.class)
        return inject.value().isEmpty() ? field.getName() : inject.value()
    }

    private void doInject(Object object, Map m = namedBeans, isUpgrade = false) {
        def fields = ReflectUtils.getFieldsByAnnotation(object, Inject.class)
        fields.each { field ->
            def name = getBeanNameFromField(field)
            def bean = m.get(name)
            if (bean == null && !isUpgrade) {
                throw new SummerException("inject ${object.class.name} failed: no bean named $name.")
            } else {
                ReflectUtils.setField(object, field, bean)
            }
        }

        if (object in SummerAware) {
            ((SummerAware) object).summer = this
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
                AopHelper.install((GroovyObject) object, (IAspectHandler) handler.newInstance())
            } else {
                log.warn("not support java class aop yet")
            }
        }
    }

    private void doInitializate(Map m = namedBeans) {
        m.values().each { bean ->
            doInitializate(bean)
        }
    }

    private void doInitializate(Object object) {
        if (object in Initialization) {
            ((Initialization) object).initializate()
        }
    }

    private void doDestroy() {
        namedBeans.values().each { bean ->
            doDestroy(bean)
        }
    }

    private void doDestroy(Object object) {
        if (object in Destroy) {
            ((Destroy) object).destroy()
        }
    }

    private void upgradeNotify(Object object) {
        if (object in SummerUpgrade) {
            ((SummerUpgrade) object).upgrade()
        }
    }

////////////////////////////////////
// bean define
////////////////////////////////////

    protected NamedBean bean(String name, Object object, boolean isUpgrade = false) {
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

    protected NamedBean bean(Object object, boolean isUpgrade = false) {
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

    protected NamedBean beanFromClass(Class clazz, boolean isUpgrade = false) {
        def o = clazz.newInstance()
        return bean(o, isUpgrade)
    }

    protected NamedBean beanFromClassName(String className, boolean isUpgrade = false) {
        return beanFromClass(classLoader.findClass(className), isUpgrade)
    }

    protected Map<String, Object> beansFromClasses(List<Class> classes, boolean isUpgrade = false) {
        return classes.findResults {
            beanFromClass(it, isUpgrade)
        }.collectEntries {
            [(it.name): it.object]
        }
    }

    protected Map<String, Object> beansFromPackage(PackageScan packageScan, boolean isUpgrade = false) {
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

    public <T> Map<String, T> getBeansByType(String clazzName) {
        Map<String, T> res = [:]

        namedBeans.findAll { _, v -> v.class.name == clazzName }.each { k, v -> res.put(k, (T) v) }

        return res
    }

    public <T> Map<String, T> getBeansByType(Class<T> clazz) {
        Map<String, T> res = [:]

        namedBeans.findAll { _, v -> v in T }.each { k, v -> res.put(k, (T) v) }

        return res
    }

    public <T> Map<String, T> getBeansInPackage(String packageName, Class<T> clazz = Object.class) {
        Map<String, T> res = [:]

        namedBeans.findAll { k, v ->
            v.getClass().name.startsWith(packageName) && v in T
        }.each { k, v ->
            res.put(k, (T) v)
        }

        return res;
    }

}
