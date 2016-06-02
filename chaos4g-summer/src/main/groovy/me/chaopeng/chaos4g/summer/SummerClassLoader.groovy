package me.chaopeng.chaos4g.summer

import com.google.common.eventbus.EventBus
import com.google.common.hash.Hashing
import com.google.common.io.Files
import groovy.io.FileType
import groovy.util.logging.Slf4j
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
                changes ->
                    reload(changes as FileWatcher.Changes)

            })

        }
    }

    public Set<Class> reload(FileWatcher.Changes changes) {
        Set<Class> ret = new HashSet<>();

        changes.each {
            def md5 = FileUtils.fileMD5(it);
            if (md5 != md5s.get(it.name)) {

                logger.info("found file modify or new, do reload file=${it.name}");

                Class clazz = parseClass(it);
                md5s.put(clazz.name, md5);
                ret.add(clazz);
            }
        }

        return ret;
    }

    public Class findClass(String name) {
        def clazz = gcl.loadClass(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }

        clazz;
    }


    private Class parseClass(File file) {
        gcl.parseClass(new GroovyCodeSource(file, "UTF-8"), false);
    }
}
