package me.chaopeng.grchaos.summer

import com.google.common.eventbus.EventBus
import groovy.io.FileType
import groovy.util.logging.Slf4j
import me.chaopeng.grchaos.summer.bean.Changes
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.event.ClassChanges
import me.chaopeng.grchaos.summer.exceptions.SummerException
import me.chaopeng.grchaos.summer.utils.ClassPathScanner
import me.chaopeng.grchaos.summer.utils.DirUtils
import me.chaopeng.grchaos.summer.utils.FileWatcher

/**
 * me.chaopeng.grchaos.summer.SummerClassLoader
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Slf4j
class SummerClassLoader {

    private def gcl = new GroovyClassLoader()
    private Map<String, List<Class>> fileToClasses = new HashMap<>()
    private FileWatcher fileWatcher
    protected EventBus eventBus = new EventBus()
    private boolean initializated = false

    private SummerClassLoader() {
        gcl.class.getDeclaredMethods().each {
            if (it.name == "removeClassCacheEntry") {
                it.setAccessible(true)
            }
        }
    }

    static SummerClassLoader create(String srcRoot, boolean autoReload = false) {
        def scl = new SummerClassLoader()
        if (srcRoot != null) {
            scl.loadSrc(srcRoot, autoReload)
        }
        scl
    }

    synchronized void loadSrc(String srcPaths, boolean autoReload) {
        if (!this.initializated) {

            try {

                if (srcPaths == null) {
                    srcPaths = "src/"
                }

                srcPaths.split(",").each { srcPath ->
                    DirUtils.recursive(srcPath, FileType.FILES, ~/\.groovy$/).each {
                        parseClass(it)
                    }
                }

                // setup file system watch service
                if (autoReload) {
                    fileWatcher = FileWatcher.watchDir(srcPaths, 60, {
                        def changes = reload(it as Changes<File>)
                        eventBus.post(changes)
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

    ClassChanges reload(Changes<File> changes = null) {

        if (changes == null) {
            if (fileWatcher.isChange()) {
                changes = fileWatcher.changes()
                if (changes.isEmpty()) {
                    return null
                }
            }
        }

        ClassChanges ret = new ClassChanges()

        changes.adds.each {
            ret.adds.addAll(parseClass(it))
        }

        changes.changes.each {
            ret.changes.addAll(parseClass(it))
        }

        changes.deletes.each {
            ret.deletes.addAll(deleteFile(it))
        }

        eventBus.post(ret)

        ret
    }

    Set<Class> scanPackage(PackageScan packageScan) {
        Set<Class> ret = ClassPathScanner.scan(packageScan)

        Map<String, Class> classMap = ret.collectEntries { [it.getName(), it] }

        gcl.getLoadedClasses().each { clazz ->
            def name = clazz.getName()
            if (ClassPathScanner.filter(name, packageScan)) {
                classMap.put(clazz.getName(), clazz)
            }
        }

        classMap.values()
    }


    Class findClass(String name) {
        def clazz = gcl.loadedClasses.find { it.name == name }
        if (clazz == null) {
            clazz = Class.forName(name)
        }

        return clazz
    }


    private List<Class> parseClass(File file) {

        // if reload unload old classes
        unloadClasses(file)

        // compile
        def clazz = gcl.parseClass(new GroovyCodeSource(file, "UTF-8"), false)

        // file - classes
        List<Class> loadedClasses = gcl.getLoadedClasses().findAll { cls ->
            cls.name.startsWith(clazz.name)
        }

        fileToClasses.put(file.absolutePath, loadedClasses)

        loadedClasses
    }

    private List<Class> deleteFile(File file) {
        fileToClasses.get(file.absolutePath)?.each { clazz ->
            gcl.removeClassCacheEntry(clazz.name)
        }
        unloadClasses(file)
        fileToClasses.remove(file.absolutePath)
    }

    private void unloadClasses(File file) {
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry()
        fileToClasses.get(file.absolutePath)?.each { clazz ->
            metaClassRegistry.removeMetaClass(clazz)
        }
    }
}
