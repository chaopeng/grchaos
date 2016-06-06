package me.chaopeng.chaos4g.summer

import com.google.common.eventbus.EventBus

import java.lang.ref.WeakReference

/**
 * me.chaopeng.chaos4g.summer.Summer
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
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

}
