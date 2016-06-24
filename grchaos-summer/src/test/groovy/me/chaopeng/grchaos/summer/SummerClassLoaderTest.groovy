package me.chaopeng.grchaos.summer

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.exceptions.SummerException
import me.chaopeng.grchaos.summer.utils.DirUtils
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.SummerClassLoaderTest
 *
 * @author chao
 * @version 1.0 - 2016-06-02
 */
class SummerClassLoaderTest extends Specification {

    SummerClassLoader scl

    def setup() {
        TestHelper.setup()
        scl = SummerClassLoader.create("tmp")
    }

    def cleanup() {
        TestHelper.cleanup()
    }

    def "init & find"() {

        expect:
        scl.loadClass(name).name == name
        scl.loadClass(name) == scl.loadClass(name)

        where:
        name                             | _
        "test.Class1"                    | _
        "test.Class1\$Class1Inner"       | _
        "test.Class2"                    | _
        "test.p1.Class3"                 | _
        "test.SrcClass1"                 | _
        "test.SrcClass1\$SrcClass1Inner" | _
        "test.SrcClass2"                 | _

    }

    def "Class4 hello"() {
        expect:
        scl.loadClass("test.SrcClass2").newInstance().hello() == "hello"
    }

    def "reload file change"() {
        sleep(1000)

        def old = scl.loadClass("test.SrcClass2").newInstance()
        def class4 = '''\
package test

class SrcClass2 {

    def hello(){
        "hello2"
    }
}
'''
        Files.write(class4, new File("tmp/SrcClass2.groovy"), Charsets.UTF_8)

        scl.reload()

        expect:
        scl.loadClass("test.SrcClass2").newInstance().hello() == "hello2"
        old.hello() == "hello"

    }

    def "reload new file"() {
        sleep(1000)

        def class5 = '''\
package test

class SrcClass3 {

    def hello(){
        "hello2"
    }
}
'''
        Files.write(class5, new File("tmp/SrcClass3.groovy"), Charsets.UTF_8)
        scl.reload()

        expect:
        scl.loadClass("test.SrcClass3").newInstance().hello() == "hello2"
    }

    def "reload delete file"() {
        sleep(1000)

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
        Files.write(srcClass2, new File("tmp/SrcClass2.groovy"), Charsets.UTF_8)

        DirUtils.rm("tmp/SrcClass1.groovy")
        scl.reload()

        when:
        scl.loadClass("test.SrcClass1")

        then:
        thrown(ClassNotFoundException)
    }

    def "package scan"() {

        expect:
        scl.scanPackage(new PackageScan(packageName: "test", recursive: recursive, excludeInner: excludeInner))
                .collect { it.value.simpleName }.sort() == classes.sort()

        where:
        recursive | excludeInner | classes
        true      | true         | ["Class1", "Class2", "Class3", "SrcClass1", "SrcClass2"]
        true      | false        | ["Class1", "Class1Inner", "Class3", "Class2", "SrcClass1", "SrcClass1Inner", "SrcClass1Inner2", "SrcClass2"]
        false     | false        | ["Class1", "Class1Inner", "Class2", "SrcClass1", "SrcClass1Inner", "SrcClass1Inner2", "SrcClass2"]

    }
}
