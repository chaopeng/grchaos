package me.chaopeng.chaos4g.summer.utils

import com.google.common.hash.Hashing
import groovy.io.FileType
import groovy.util.logging.Slf4j

import java.nio.file.*
import java.util.concurrent.TimeUnit

/**
 * me.chaopeng.chaos4g.summer.utils.FileWatcher
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Slf4j
class FileWatcher {

    private final WatchService watchService
    private final File dir
    private Map<String, String> md5s = new HashMap<>()
    private long lastModified;

    FileWatcher(String filepath) {
        Path path = Paths.get(filepath)

        watchService = FileSystems.getDefault().newWatchService()
        path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)

        dir = new File(filepath)
        lastModified = dir.lastModified()

        DirUtils.recursive(filepath, FileType.FILES).each { file ->
            md5s.put(file.path, com.google.common.io.Files.hash(file, Hashing.md5()).toString())
        }
    }

    /**
     * can only detect the watched path changes
     * @return
     */
    boolean isChange() {
        WatchKey key = watchService.poll()
        def res = (key != null) || (lastModified != dir.lastModified())
        lastModified = dir.lastModified()
        key?.reset()
        res
    }

    static class Changes {
        List<File> adds = [];
        List<File> deletes = [];
        List<File> changes = [];
    }

    /**
     * get the changes files by comparing the MD5
     * @return
     */
    Changes changes() {
        Changes res = new Changes()
        Map<String, String> newMd5s = new HashMap<>()

        DirUtils.recursive(dir.path, FileType.FILES).each { file ->
            def md5 = com.google.common.io.Files.hash(file, Hashing.md5()).toString()

            // new 
            if (!md5s.containsKey(file.path)) {
                res.adds.add(file)
            }

            // change
            else {
                def oldMd5 = md5s.get(file.path)
                if (oldMd5 != md5) {
                    res.changes.add(file)
                }
            }

            newMd5s.put(file.path, md5)
        }

        md5s.keySet().each { file ->
            if (!newMd5s.containsKey(file)) {
                res.deletes.add(new File(file))
            }
        }

        md5s = newMd5s

        res
    }

    /**
     * @param path will watch
     * @param intervalSecond
     * @param closure{Changes -> ...}
     */
    static void watchDir(String path, int intervalSecond, Closure closure) throws IOException {
        FileWatcher fileWatcher = new FileWatcher(path)

        ScheduleUtils.get().scheduleWithFixedDelay({

            // if dir has any changes
            if (fileWatcher.isChange()) {

                try {
                    closure.call();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }

        }, intervalSecond, intervalSecond, TimeUnit.SECONDS);
    }


}
