package me.chaopeng.grchaos.summer

import com.google.common.eventbus.EventBus
import groovy.util.logging.Slf4j
import me.chaopeng.grchaos.summer.bean.Changes
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.event.ClassChanges
import me.chaopeng.grchaos.summer.exceptions.SummerException
import me.chaopeng.grchaos.summer.utils.ClassPathScanner
import me.chaopeng.grchaos.summer.utils.FileWatcher
import me.chaopeng.grchaos.summer.utils.GroovyCompileHelper

/**
 * me.chaopeng.grchaos.summer.SummerClassLoader
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Slf4j
class SummerClassLoader extends GroovyClassLoader {

    private List<String> srcPaths
    private Map<String, Class> loadedClasses
    private FileWatcher fileWatcher
    protected Summer summer
    private boolean initializated = false

    private SummerClassLoader(String srcPaths = "src/", ClassLoader parent = Thread.currentThread().getContextClassLoader()) {
        super(parent)
        this.srcPaths = srcPaths.split(",")
    }


    static SummerClassLoader create(String srcPaths = "src/", boolean autoReload = false, ClassLoader parent = Thread.currentThread().getContextClassLoader()) {
        SummerClassLoader scl = new SummerClassLoader(srcPaths, parent)
        scl.loadSrc(autoReload)
        return scl
    }

    synchronized void loadSrc(boolean autoReload) {
        if (!this.initializated) {

            try {

                loadedClasses = GroovyCompileHelper.compile(this.srcPaths)

                // setup file system watch service
                if (autoReload) {
                    fileWatcher = FileWatcher.watchDir(srcPaths, 60, {
                        reload(it as Changes<File>)
                    })
                } else {
                    fileWatcher = new FileWatcher(srcPaths)
                }

                initializated = true
            } catch (Exception e) {
                throw new SummerException(e)
            }
        }
    }

    void reload(Changes<File> changes = null) {

        if (changes == null) {
            if (fileWatcher.isChange()) {
                changes = fileWatcher.changes()
                if (changes.isEmpty()) {
                    log.info("no classes file changed, no reload executed")
                    return
                }
            }
        }

        def backup = loadedClasses
        try {

            loadedClasses = GroovyCompileHelper.compile(this.srcPaths)

            summer?.upgrade()

            log.info("Summer reload success")

        } catch (Exception e) {
            log.error("SummerClassLoader reload failed. err={}", e.getMessage(), e)

            loadedClasses = backup
        }


    }

    Map<String, Class> scanPackage(PackageScan packageScan) {
        Set<Class> ret = ClassPathScanner.scan(packageScan)

        Map<String, Class> classMap = ret.collectEntries { [it.getName(), it] }

        loadedClasses.each { name, clazz ->
            if (ClassPathScanner.filter(name, packageScan)) {
                classMap.put(clazz.getName(), clazz)
            }
        }

        classMap
    }


    Class loadClass(String name) {
        def clazz = loadedClasses.get(name)
        if (clazz == null) {
            clazz = super.loadClass(name)
        }

        return clazz
    }

}
