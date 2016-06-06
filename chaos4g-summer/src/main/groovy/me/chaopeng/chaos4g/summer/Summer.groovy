package me.chaopeng.chaos4g.summer

import com.google.common.base.CaseFormat
import com.google.common.eventbus.EventBus
import groovy.util.logging.Slf4j
import me.chaopeng.chaos4g.summer.excwptions.SummerException
import me.chaopeng.chaos4g.summer.ioc.annotations.Bean

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
    private EventBus eventBus
    private Map<String, Object> namedBeans = new HashMap<>()
    private List<WeakReference<Object>> anonymousBeans = new LinkedList<>()
    private List<String> watchPackages = new LinkedList<>()
    private AbstractSummerModule module
    private boolean isInit = false


    Summer(String srcRoot = null, boolean autoReload = false) {
        classLoader = SummerClassLoader.create(srcRoot)
        eventBus = classLoader.eventBus
        eventBus.register(this)
    }

    synchronized void loadModule(AbstractSummerModule module){
        if (!isInit) {
            this.module = module
            module.summer = this
            module.configure()
            isInit = true
        }
    }

    synchronized void start(){
        module.start()
    }

    synchronized void stop(){
        module.stop()
    }

    public void bean(String name, Object object, boolean isUpgrade=false) {
        synchronized (this) {

            // do upgrade
            def oldBean = namedBeans.get(name)
            if (oldBean!=null) {
                if (!isUpgrade) {
                    throw new SummerException("Bean ${name} is duplicated. ")
                }
            }

            if (!isUpgrade) {
                namedBeans.put(name, object)
            }
        }
    }

    public String bean(Object object, boolean isUpgrade=false) {
        Bean bean = object.getClass().getAnnotation(Bean.class);
        if (bean != null) {
            String name
            if (!bean.value().isEmpty()) {
                name = bean.value()
            } else {
                name = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, object.class.simpleName)
            }
            this.bean(name, object, isUpgrade);
            name;
        }
        log.warn("Cannot find @Bean in ${object.getClass()}. ignore it. ");
        null;
    }

    public String bean(Class clazz, boolean isUpgrade=false) {
        def o = clazz.newInstance();
        return bean(o, isUpgrade);
    }

    public void beans(List list) {
        list.each {
            if(it instanceof Map){
                it.each { k, v -> bean(k as String, v); }
            } else if (it instanceof PackageScan) {
                loadPackage(it);
            } else if (it instanceof Collection<Class<?>>) {
                loadClasses(it);
            } else if (it instanceof ClassFor) {
                loadClass(it.name);
            }
            else {
                addBean(it);
            }
        }
    }
}
