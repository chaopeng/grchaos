package me.chaopeng.chaos4g.summer

import com.google.common.eventbus.EventBus
import groovy.io.FileType
import groovy.util.logging.Slf4j
import me.chaopeng.chaos4g.summer.bean.Changes
import me.chaopeng.chaos4g.summer.bean.PackageScan
import me.chaopeng.chaos4g.summer.event.ClassChanges
import me.chaopeng.chaos4g.summer.excwptions.SummerException
import me.chaopeng.chaos4g.summer.utils.ClassPathScanner
import me.chaopeng.chaos4g.summer.utils.DirUtils
import me.chaopeng.chaos4g.summer.utils.FileWatcher

/**
 * me.chaopeng.chaos4g.summer.SummerClassLoader
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Slf4j
class SummerClassLoader {

    private def gcl = new GroovyClassLoader()
    private String srcRoot;
    private Map<String, List<Class>> fileToClasses = new HashMap<>()
    private FileWatcher fileWatcher
    EventBus eventBus = new EventBus();

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

    synchronized void loadSrc(String srcRoot, boolean autoReload) {
        if (this.srcRoot == null) {

            try {

                if (srcRoot == null) {
                    this.srcRoot = "src/";
                }

                this.srcRoot = srcRoot;

                DirUtils.recursive(srcRoot, FileType.FILES, ~/\.groovy$/).each {
                    parseClass(it)
                }

                // setup file system watch service
                if (autoReload) {
                    fileWatcher = FileWatcher.watchDir(srcRoot, 60, {
                        def changes = reload(it as Changes<File>)
                        eventBus.post(changes)
                    })
                } else {
                    fileWatcher = new FileWatcher(srcRoot)
                }
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

        clazz;
    }


    private List<Class> parseClass(File file) {

        // if reload unload old classes
        unloadClasses(file)

        // compile
        def clazz = gcl.parseClass(new GroovyCodeSource(file, "UTF-8"), false);

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
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        fileToClasses.get(file.absolutePath)?.each { clazz ->
            metaClassRegistry.removeMetaClass(clazz)
        }
    }
}
