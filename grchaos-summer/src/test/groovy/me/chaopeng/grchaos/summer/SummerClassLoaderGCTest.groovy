package me.chaopeng.grchaos.summer

import groovy.io.FileType
import me.chaopeng.grchaos.summer.bean.Changes
import me.chaopeng.grchaos.summer.utils.DirUtils

/**
 * me.chaopeng.grchaos.summer.SummerClassLoaderGCTest
 *
 * @author chao
 * @version 1.0 - 2016-06-10
 */
class SummerClassLoaderGCTest {
    static void main(String[] args) {
        TestHelper.reloadableClassesSetup()
        SummerClassLoader scl = SummerClassLoader.create("tmp")
        Changes<File> changes = new Changes<>()

        changes.adds.addAll( DirUtils.recursive("tmp", FileType.FILES, ~/\.groocy$/))

        while (true) {
            scl.reload(changes)

            sleep(100)
        }
    }
}
