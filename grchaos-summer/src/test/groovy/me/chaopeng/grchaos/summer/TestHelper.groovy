package me.chaopeng.grchaos.summer

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.grchaos.summer.utils.DirUtils

/**
 * me.chaopeng.grchaos.summer.TestHelper
 *
 * @author chao
 * @version 1.0 - 2016-06-09
 */
class TestHelper {

    static def reloadableClassesSetup() {

        DirUtils.mkdir("tmp")

        def srcClass1 = '''\
package test

import me.chaopeng.grchaos.summer.ioc.annotations.Bean

@Bean
class SrcClass1 {

    def hello(){
        "hello"
    }

    @Bean("srcClass1Inner")
    static class SrcClass1Inner {

    }

    static class SrcClass1Inner2 {

    }

}
        '''

        def srcClass2 = '''\
package test

import me.chaopeng.grchaos.summer.ioc.annotations.Bean

@Bean
class SrcClass2 {

    def hello(){
        "hello"
    }
}
'''
        Files.write(srcClass1, new File("tmp/SrcClass1.groovy"), Charsets.UTF_8)
        Files.write(srcClass2, new File("tmp/SrcClass2.groovy"), Charsets.UTF_8)

    }

    static def reloadableClassesCleanup() {
        def tmp = new File("tmp")
        tmp.deleteDir()
    }

}
