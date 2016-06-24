package me.chaopeng.grchaos.summer.bean

import com.google.common.hash.Hashing
import com.google.common.io.Files
import groovy.io.FileType
import me.chaopeng.grchaos.summer.utils.DirUtils

/**
 * me.chaopeng.grchaos.summer.bean.DirInfo
 *
 * @author chao
 * @version 1.0 - 2016-06-16
 */
class DirInfo {

    private final File dir
    private Map<String, String> md5s = new HashMap<>()
    private long lastModified

    DirInfo(String path) {
        this.dir = new File(path)
        this.lastModified = dir.lastModified()
        DirUtils.recursive(path, FileType.FILES).each { file ->
            md5s.put(file.path, Files.hash(file, Hashing.md5()).toString())
        }
    }

    boolean isModified() {
        if (dir.lastModified() != lastModified) {
            lastModified = dir.lastModified()
            return true
        }
        return false
    }

    void changes(Changes<File> changes) {
        Map<String, String> newMd5s = new HashMap<>()

        DirUtils.recursive(dir.path, FileType.FILES).each { file ->
            def md5 = Files.hash(file, Hashing.md5()).toString()

            // new
            if (!md5s.containsKey(file.path)) {
                changes.adds.add(file)
            }

            // change
            else {
                def oldMd5 = md5s.get(file.path)
                if (oldMd5 != md5) {
                    changes.changes.add(file)
                }
            }

            newMd5s.put(file.path, md5)
        }

        md5s.keySet().each { file ->
            if (!newMd5s.containsKey(file)) {
                changes.deletes.add(new File(file))
            }
        }

        md5s = newMd5s
    }

}
