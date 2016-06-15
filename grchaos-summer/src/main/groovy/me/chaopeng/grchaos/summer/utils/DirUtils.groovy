package me.chaopeng.grchaos.summer.utils

import com.google.common.io.Files
import groovy.io.FileType
import groovy.util.logging.Slf4j

import java.util.regex.Pattern

/**
 * me.chaopeng.grchaos.summer.utils.DirUtils
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Slf4j
class DirUtils {

    /**
     * ls $path | grep $regex
     */
    static List<File> ls(String path, FileType fileType, Pattern regex = ~/.*/) {
        File dir = new File(path)

        List<File> res = []
        dir.eachFile(fileType, { regex.matcher(it.name).find() ? res.add(it) : null })
        res
    }

    /**
     * recursive $path | grep $regex
     */
    static List<File> recursive(String path, FileType fileType, Pattern regex = ~/.*/) {
        File dir = new File(path)

        List<File> res = []
        dir.eachFileRecurse(fileType, { regex.matcher(it.name).find() ? res.add(it) : null })
        res
    }

    /**
     *
     * <pre>
     * $from isDirectory
     * ? cp $from $to
     * : cp -r f$rom $to
     * </pre>
     */
    static boolean cp(String from, String to) {
        try {
            if (from != to) {
                def f = new File(from)

                if (f.isDirectory()) {
                    f.eachFile {
                        def childTo = to + File.separator + it.name
                        if (it.isDirectory()) {
                            mkdir(childTo)
                        }
                        if (!cp(it.path, childTo)) {
                            false
                        }
                    }
                } else {
                    def t = new File(to)
                    if (t.isDirectory()) {
                        t = new File(to + File.separator + f.name)
                    }
                    Files.copy(f, t)
                }
            }
            true
        } catch (error) {
            log.error("cp $from $to failed. ${error.getMessage()}")
            false
        }
    }

    /**
     * rm -rf $path
     */
    static void rm(String path) {
        File file = new File(path);

        if (file.isDirectory()) {
            file.deleteDir()
        } else {
            file.delete()
        }
    }

    /**
     * mkdirs
     */
    static boolean mkdir(String path) {
        File file = new File(path)
        file.mkdirs()
    }


}
