package me.chaopeng.grchaos.summer

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import groovy.util.logging.Slf4j
import me.chaopeng.grchaos.summer.aop.AopHelper
import me.chaopeng.grchaos.summer.aop.IAspectHandler
import me.chaopeng.grchaos.summer.aop.annotations.Aspect
import me.chaopeng.grchaos.summer.bean.DependencyBean
import me.chaopeng.grchaos.summer.bean.NamedBean
import me.chaopeng.grchaos.summer.bean.SummerAware
import me.chaopeng.grchaos.summer.exceptions.SummerException
import me.chaopeng.grchaos.summer.ioc.annotations.Inject
import me.chaopeng.grchaos.summer.ioc.lifecycle.Destroy
import me.chaopeng.grchaos.summer.ioc.lifecycle.Initialization
import me.chaopeng.grchaos.summer.ioc.lifecycle.SummerUpgrade
import me.chaopeng.grchaos.summer.utils.ReflectUtils

import java.lang.ref.WeakReference
import java.lang.reflect.Field

/**
 * me.chaopeng.grchaos.summer.Summer
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
@Slf4j
class Summer {

    private final SummerClassLoader classLoader
    private AbstractSummerModule module
    protected Map<String, Object> namedBeans = [:]
    private List<NamedBean> immutableBeans
    private List<WeakReference<Object>> anonymousBeans = new LinkedList<>()
    private int stage = 0 // none(0), init-ed(1), prestart-ed(2), start-ed(3)


    Summer(String srcRoot = null, boolean autoReload = false) {
        classLoader = SummerClassLoader.create(srcRoot, autoReload)
        classLoader.summer = this
    }

    SummerClassLoader getClassLoader() {
        return classLoader
    }

////////////////////////////////////
// Life Cycle
////////////////////////////////////

    synchronized void loadModule(AbstractSummerModule module) {
        if (stage == 0) {
            this.module = module
            module.summer = this

            // load immutable beans
            def beans = module.configure()
            addBeans(beans)
            immutableBeans = beans

            // load mutable beans
            beans = module.reloadableBeansConfigure()
            addBeans(beans)

            stage++

            Runtime.getRuntime().addShutdownHook {
                stop()
            }
        }
    }

    synchronized void preStart() {
        if (stage == 1) {
            // check all dependencies
            def missing = testAllDepes()
            if (!missing.isEmpty()) {
                throw missingDepesException(missing)
            }

            doInject()
            doAddAspect()
            doInitializate()

            stage++
        }
    }

    synchronized void start() {
        if (stage == 2) {
            module.start()
            stage++
        }
    }

    synchronized void reload() {
        classLoader.reload()
    }

    synchronized void upgrade() {

        try {
            Map<String, Object> newNamedBeans = [:]
            addBeans(immutableBeans, newNamedBeans)

            Map<String, Object> upgradedBeans = [:]

            // for updates
            def newBeans = module.reloadableBeansConfigure()
            addBeans(newBeans, newNamedBeans)
            addBeans(newBeans, upgradedBeans)

            // check all dependencies
            def missing = testAllDepes(newNamedBeans, newNamedBeans)
            if (!missing.isEmpty()) {
                throw missingDepesException(missing)
            }

            anonymousBeans.removeAll { it.get() == null }
            missing = testAllAnonymousBeansDepes(anonymousBeans, newNamedBeans)
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

            anonymousBeans.each {
                doInject(it.get(), upgradedBeans, true)
            }

            // removed bean
            def immutableBeansName = immutableBeans*.name.toSet()
            def removedBeans = namedBeans.findAll { k, v -> !immutableBeansName.contains(k) }.values()

            // replace
            namedBeans = newNamedBeans

            // notify all upgrade()
            namedBeans.each { k, v ->
                upgradeNotify(v)
            }

            // do destroy
            removedBeans.each {
                doDestroy(it)
            }


        } catch (Exception e) {
            throw new SummerException(e)
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
        Multimap<String, DependencyBean> res = ArrayListMultimap.create()
        m.each { k, v ->
            ReflectUtils.getFieldsByAnnotation(v, Inject.class).each { field ->
                def name = getBeanNameFromField(field)
                if (!deps.containsKey(name)) {
                    res.put(k as String, new DependencyBean(object: v, field: field, name: name))
                }
            }
        }

        return res
    }

    protected Multimap<String, DependencyBean> testAllAnonymousBeansDepes(List<WeakReference<Object>> list = anonymousBeans, Map<String, Object> deps = namedBeans) {
        Multimap<String, DependencyBean> res = ArrayListMultimap.create()
        list.each { ref ->
            def o = ref.get()
            if (o != null) {
                ReflectUtils.getFieldsByAnnotation(o, Inject.class).each { field ->
                    def name = getBeanNameFromField(field)
                    if (!deps.containsKey(name)) {
                        res.put(o.class.name as String, new DependencyBean(object: o, field: field, name: name))
                    }
                }
            }
        }

        return res
    }

    private SummerException missingDepesException(Multimap<String, DependencyBean> missing) {
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
                def handler = classLoader.loadClass(aspect.handler())
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

    protected synchronized void addBeans(List<NamedBean> beans, Map<String, Object> map = namedBeans) {

        for (NamedBean bean : beans) {
            if (map.containsKey(bean.name)) {
                throw new SummerException("Bean ${bean.name} is duplicated. ")
            }
            map.put(bean.name, bean.object)
        }
    }

////////////////////////////////////
// get bean(s)
////////////////////////////////////

    Object getBean(String name) {
        return namedBeans.get(name)
    }

    public <T> T getBean(String name, Class<T> clazz) {
        return namedBeans.get(name) as T
    }

    public <T> Map<String, T> getBeansByType(String clazzName) {
        return getBeansByType(classLoader.loadClass(clazzName))
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

        return res
    }

}
