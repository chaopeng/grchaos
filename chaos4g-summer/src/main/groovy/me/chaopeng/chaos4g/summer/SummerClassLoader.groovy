package me.chaopeng.chaos4g.summer

import com.google.common.hash.Hashing
import com.google.common.io.Files
import groovy.io.FileType
import groovy.util.logging.Slf4j

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
    private def md5s = [:];
    private String srcRoot;

    public synchronized void init(String srcRoot) {
        if (this.srcRoot == null) {

            if (srcRoot == null) {
                srcRoot = "src/";
            }

            this.srcRoot = srcRoot;

            new File(srcRoot).eachFileRecurse(FileType.FILES, {
                if ( it.name.endsWith(".groovy")) {
                    parseClass(it)
                    md5s[it.name] = Files.hash(it, Hashing.md5())
                }
            })

            // setup file system watch service


        }
    }

    private Class parseClass(File file) {
        return gcl.parseClass(new GroovyCodeSource(file, "UTF-8"), false);
    }
}
