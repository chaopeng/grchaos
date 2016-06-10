package me.chaopeng.chaos4g.summer

import groovy.io.FileType
import me.chaopeng.chaos4g.summer.bean.Changes
import me.chaopeng.chaos4g.summer.utils.DirUtils

/**
 * me.chaopeng.chaos4g.summer.SummerClassLoaderGCTest
 *
 * @author chao
 * @version 1.0 - 2016-06-10
 */
class SummerClassLoaderGCTest {
    public static void main(String[] args) {
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
