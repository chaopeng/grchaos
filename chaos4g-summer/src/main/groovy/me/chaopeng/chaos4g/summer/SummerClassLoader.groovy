package me.chaopeng.chaos4g.summer

import com.google.common.eventbus.EventBus
import groovy.io.FileType
import groovy.util.logging.Slf4j
import me.chaopeng.chaos4g.summer.bean.Changes
import me.chaopeng.chaos4g.summer.utils.ClassPathScanner
import me.chaopeng.chaos4g.summer.utils.FileWatcher

/**
 * me.chaopeng.chaos4g.summer.SummerClassLoader
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Singleton
@Slf4j
class SummerClassLoader extends Observable {

    private def gcl = new GroovyClassLoader()
    private String srcRoot;
    private Map<String, List<Class>> fileToClasses = new HashMap<>()
    private EventBus eventBus;

    public synchronized void init(String srcRoot) {
        if (this.srcRoot == null) {

            if (srcRoot == null) {
                srcRoot = "src/";
            }

            this.srcRoot = srcRoot;

            new File(srcRoot).eachFileRecurse(FileType.FILES, {
                if ( it.name.endsWith(".groovy")) {
                    parseClass(it)
                }
            })

            // setup file system watch service
            FileWatcher.watchDir(srcRoot, 60, {
                it ->
                    def changes = reload(it as Changes<File>)
                    eventBus.post(changes)
            })

        }
    }

    public Changes<Class> reload(Changes<File> changes) {
        Changes<Class> ret = new Changes<>();

        changes.adds.each {
            ret.adds.addAll(parseClass(it))
        }

        changes.changes.each {
            ret.changes.addAll(parseClass(it))
        }

        changes.deletes.each {
            ret.deletes.addAll(deleteFile(it))
        }

        return ret;
    }

    public Set<Class> scanPackage(String basePackage, boolean recursive, boolean includeInner = false) {
        Set<Class> ret = ClassPathScanner.scan(basePackage, recursive, !includeInner, true, null);

        Map<String, Class> classMap = ret.collectEntries { [it.getName(), it] }

        def prefix = basePackage + "."

        gcl.getLoadedClasses().each { clazz ->
            def name = clazz.getName()
            if (name.startsWith(prefix)) {
                if (!recursive) {
                    if (!name.replace(prefix, "").contains(".")) {
                        if (includeInner || !name.contains("\$")) {
                            classMap.put(clazz.getName(), clazz)
                        }
                    }
                } else {
                    if (includeInner || !name.contains("\$")) {
                        classMap.put(clazz.getName(), clazz)
                    }
                }
            }
        }

        classMap.values()
    }


    public Class findClass(String name) {
        def clazz = gcl.loadClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }

        clazz;
    }


    private List<Class> parseClass(File file) {

        // if reload unload old classes
        unloadClasses(file)

        // compile
        def clazz = gcl.parseClass(new GroovyCodeSource(file, "UTF-8"), false);

        // file - classes
        List<Class> loadedClasses = gcl.getLoadedClasses().findAll {cls ->
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

    private void unloadClasses(File file){
        MetaClassRegistry metaClassRegistry = GroovySystem.getMetaClassRegistry();
        fileToClasses.get(file.absolutePath)?.each { clazz ->
            metaClassRegistry.removeMetaClass(clazz)
        }
    }
}
