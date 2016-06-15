package me.chaopeng.grchaos.summer.utils

import com.google.common.io.Files
import groovy.io.FileType
import org.omg.CORBA.Any
import spock.lang.Specification

import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent

/**
 * me.chaopeng.grchaos.summer.utils.DirUtilsTest
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class DirUtilsTest extends Specification {

    def setup(){
        DirUtils.mkdir "tmp"
        DirUtils.mkdir "tmp/dir1"
        DirUtils.mkdir "tmp/dir2"
        DirUtils.mkdir "tmp/dir1/dir3"

        def txt1 = new File("tmp/1.txt")
        def txt2 = new File("tmp/2.txt")
        def dir1txt3 = new File("tmp/dir1/3.txt")

        Files.touch(txt1)
        Files.touch(txt2)
        Files.touch(dir1txt3)
    }

    def cleanup(){
        def tmp = new File("tmp")
        tmp.deleteDir();
    }


    def "ls"() {
        expect:
        DirUtils.ls(path, type, regex).collect {it.name}.sort() == files.sort()

        where:
        path       | type                  | regex  | files
        "tmp"      | FileType.ANY          | ~/.*/  | ["1.txt", "2.txt", "dir1", "dir2"]
        "tmp"      | FileType.ANY          | ~/1/   | ["1.txt", "dir1"]
        "tmp"      | FileType.DIRECTORIES  | ~/.*/  | ["dir1", "dir2"]
        "tmp"      | FileType.FILES        | ~/.*/  | ["1.txt", "2.txt"]
        "tmp/dir2" | FileType.ANY          | ~/.*/  | []
    }

    def "recursive"() {
        expect:
        DirUtils.recursive(path, type, regex).collect {it.name}.sort() == files.sort()

        where:
        path  | type                  | regex  | files
        "tmp" | FileType.ANY          | ~/.*/  | ["1.txt", "2.txt", "3.txt", "dir1", "dir2", "dir3"]
        "tmp" | FileType.ANY          | ~/1/   | ["1.txt", "dir1"]
        "tmp" | FileType.DIRECTORIES  | ~/.*/  | ["dir1", "dir2", "dir3"]
        "tmp" | FileType.FILES        | ~/.*/  | ["1.txt", "2.txt", "3.txt"]
    }

    def "cp file"() {
        when:
        DirUtils.cp("tmp/1.txt", "tmp/dir2")

        then:
        DirUtils.ls("tmp/dir2", FileType.ANY).collect {it.name} == ["1.txt"]
    }

    def "cp dir"() {
        when:
        DirUtils.cp("tmp/dir1", "tmp/dir2")

        then:
        DirUtils.ls("tmp/dir2", FileType.ANY).collect {it.name}.sort() == ["3.txt", "dir3"].sort()
    }

    def "rm file"() {
        when:
        DirUtils.rm("tmp/1.txt")

        then:
        DirUtils.ls("tmp", FileType.FILES).collect {it.name} == ["2.txt"]
    }

    def "rm dir"() {
        when:
        DirUtils.rm("tmp/dir1")

        then:
        DirUtils.ls("tmp", FileType.DIRECTORIES).collect {it.name} == ["dir2"]
    }

    def "mkdir"() {
        when:
        DirUtils.mkdir("tmp/dir4")

        then:
        DirUtils.ls("tmp", FileType.DIRECTORIES).collect {it.name}.contains("dir4")
    }
}
