package me.chaopeng.grchaos.summer

import me.chaopeng.grchaos.summer.utils.GroovyCompileHelper

/**
 * me.chaopeng.grchaos.summer.SummerClassLoaderGCTest
 *
 * @author chao
 * @version 1.0 - 2016-06-10
 */
class SummerClassLoaderGCTest {
    static void main(String[] args) {
        TestClassWithDepend.setup()

        while (true) {
            def classes = GroovyCompileHelper.compile(["tmp"])

            sleep(1)

        }
    }
}
