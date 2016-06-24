package me.chaopeng.grchaos.summer.utils

import groovy.util.logging.Slf4j
import me.chaopeng.grchaos.summer.bean.Changes
import me.chaopeng.grchaos.summer.bean.DirInfo
import rx.Observable
import rx.schedulers.Schedulers

import java.nio.file.*
import java.util.concurrent.TimeUnit

/**
 * me.chaopeng.grchaos.summer.utils.FileWatcher
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
@Slf4j
class FileWatcher {

    private final WatchService watchService
    private final List<DirInfo> dirs = []

    FileWatcher(List<String> filepaths) {
        watchService = FileSystems.getDefault().newWatchService()

        filepaths.each { filepath ->
            Path path = Paths.get(filepath)

            path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_DELETE)

            def dirInfo = new DirInfo(filepath)

            dirs << dirInfo
        }
    }

    /**
     * can only detect the watched path changes
     * @return
     */
    boolean isChange() {

        def isChange = false

        WatchKey key = watchService.poll()

        while (key != null) {
            key = watchService.poll()
            key?.reset()
            isChange = true
        }

        if (!isChange) {
            isChange = dirs*.isModified().any { it }
        }
        return isChange
    }

    /**
     * get the changes files by comparing the MD5
     * @return
     */
    Changes<File> changes() {
        Changes<File> res = new Changes<>()

        dirs*.changes(res)

        return res
    }

    /**
     * @param paths will watch
     * @param intervalSecond
     * @param closure {Changes -> ...}
     */
    static FileWatcher watchDir(List<String> paths, int intervalSecond, Closure closure) throws IOException {
        FileWatcher fileWatcher = new FileWatcher(paths)

        Observable.interval(intervalSecond, intervalSecond, TimeUnit.SECONDS).observeOn(Schedulers.newThread()).subscribe {

            // if dir has any changes
            if (fileWatcher.isChange()) {

                def changes = fileWatcher.changes()
                if (!changes.isEmpty()) {
                    try {
                        closure.call(changes)
                    } catch (Exception e) {
                        log.error(e.getMessage(), e)
                    }
                }
            }
        }

        fileWatcher
    }


}
