package me.chaopeng.grchaos.summer.utils

import com.google.common.base.Charsets
import com.google.common.io.Files
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.utils.FileWatcherTest
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class FileWatcherTest extends Specification {

    FileWatcher watcher

    def setup() {
        DirUtils.mkdir "tmp"
        DirUtils.mkdir "tmp/dir1"
        DirUtils.mkdir "tmp/dir2"
        DirUtils.mkdir "tmp/dir1/dir3"

        def txt1 = new File("tmp/1.txt")
        def txt2 = new File("tmp/2.txt")
        def dir1txt3 = new File("tmp/dir1/3.txt")

        Files.write("1", txt1, Charsets.UTF_8)
        Files.write("2", txt2, Charsets.UTF_8)
        Files.write("3", dir1txt3, Charsets.UTF_8)

        watcher = new FileWatcher(["tmp"])
    }

    def cleanup() {
        def tmp = new File("tmp")
        tmp.deleteDir()
    }

    def "change when create"() {
        when:
        sleep(1000)
        Files.touch(new File("tmp/4.txt"))

        then:
        watcher.isChange()
    }

    def "change when touch"() {
        when:
        sleep(1000)
        Files.touch(new File("tmp"))

        then:
        watcher.isChange()
    }

    def "get changes - no change"() {
        when:
        sleep(1000)
        Files.touch(new File("tmp"))

        then:
        watcher.isChange()
        def changes = watcher.changes()
        changes.adds.isEmpty()
        changes.deletes.isEmpty()
        changes.changes.isEmpty()
    }

    def "get changes"() {
        when:
        Files.write("4", new File("tmp/4.txt"), Charsets.UTF_8) // new 4
        DirUtils.rm("tmp/1.txt")
        Files.write("5", new File("tmp/2.txt"), Charsets.UTF_8)

        then:
        watcher.isChange()
        def changes = watcher.changes()
        changes.adds.collect { it.name } == ["4.txt"]
        changes.deletes.collect { it.name } == ["1.txt"]
        changes.changes.collect { it.name } == ["2.txt"]
    }
}
