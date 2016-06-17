package me.chaopeng.grchaos.summer

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.grchaos.summer.utils.DirUtils
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.GroovyClassLoaderOrderedTest
 *
 * @author chao
 * @version 1.0 - 2016-06-16
 */
class GroovyClassLoaderOrderedTest extends Specification {

    def gcl = new GroovyClassLoader()

    def setup() {

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
import test.SrcClass1

@Bean
class SrcClass2 {

    SrcClass1 srcClass1

    def hello(){
        "hello"
    }
}
'''
        Files.write(srcClass1, new File("tmp/SrcClass1.groovy"), Charsets.UTF_8)
        Files.write(srcClass2, new File("tmp/SrcClass2.groovy"), Charsets.UTF_8)

    }

    def cleanup() {
        def tmp = new File("tmp")
        tmp.deleteDir()
    }

    def "load in order"(){

        def class1 = parseClass("tmp/SrcClass1.groovy")
        def class2 = parseClass("tmp/SrcClass2.groovy")

        def object1 = class1.newInstance()
        def object2 = class2.newInstance()
        object2.srcClass1 = object1

        expect:
        object2.srcClass1.hello() == "hello"
    }

    def "reload in order"(){

        def class1 = parseClass("tmp/SrcClass1.groovy")
        def class2 = parseClass("tmp/SrcClass2.groovy")

        def object1 = class1.newInstance()
        def object2 = class2.newInstance()
        object2.srcClass1 = object1

        def srcClass1 = '''\
package test

import me.chaopeng.grchaos.summer.ioc.annotations.Bean

@Bean
class SrcClass1 {

    def hello(){
        "hello1"
    }

    def hello2(){
        "hello2"
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
import test.SrcClass1

@Bean
class SrcClass2 {

    SrcClass1 srcClass1

    def hello(){
        "hello"
    }
}
'''
        Files.write(srcClass1, new File("tmp/SrcClass1.groovy"), Charsets.UTF_8)
        Files.write(srcClass2, new File("tmp/SrcClass2.groovy"), Charsets.UTF_8)

        def rClass1 = parseClass("tmp/SrcClass1.groovy")
        def rClass2 = parseClass("tmp/SrcClass2.groovy")

        def rObject1 = rClass1.newInstance()
        def rObject2 = rClass2.newInstance()
        rObject2.srcClass1 = rObject1

        expect:
        rObject2.srcClass1.hello() == "hello1"
    }

    def parseClass(String path) {
        def clazz = gcl.parseClass(new GroovyCodeSource(new File(path), "UTF-8"), false)
        return clazz
    }

}
