package me.chaopeng.grchaos.summer

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.utils.DirUtils
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.SummerClassLoaderTest
 *
 * @author chao
 * @version 1.0 - 2016-06-02
 */
class SummerClassLoaderTest extends Specification {

    def scl

    def setup() {
        TestHelper.reloadableClassesSetup()
        scl = SummerClassLoader.create("tmp")
    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
    }

    def "init & find"() {

        expect:
        scl.findClass(name).name == name
        scl.findClass(name) == scl.findClass(name)

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
        scl.findClass("test.SrcClass2").newInstance().hello() == "hello"
    }

    def "reload file change"() {
        sleep(1000)

        def old = scl.findClass("test.SrcClass2").newInstance()
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
        scl.findClass("test.SrcClass2").newInstance().hello() == "hello2"
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
        scl.findClass("test.SrcClass3").newInstance().hello() == "hello2"
    }

    def "reload delete file"() {
        sleep(1000)

        File srcClass2 = new File("tmp/SrcClass2.groovy")
        DirUtils.rm("tmp/SrcClass2.groovy")
        scl.reload()

        scl.class.getDeclaredField("fileToClasses").accessible = true

        when:
        scl.findClass("test.SrcClass2")

        then:
        thrown(ClassNotFoundException)
        !scl.fileToClasses.containsKey(srcClass2.absolutePath)
    }

    def "package scan"() {

        expect:
        scl.scanPackage(new PackageScan(packageName:"test", recursive:recursive, excludeInner:excludeInner))
                .collect { it.simpleName }.sort() == classes.sort()

        where:
        recursive | excludeInner | classes
        true      | true         | ["Class1", "Class2", "Class3", "SrcClass1", "SrcClass2"]
        true      | false        | ["Class1", "Class1Inner", "Class3", "Class2", "SrcClass1", "SrcClass1Inner", "SrcClass1Inner2", "SrcClass2"]
        false     | false        | ["Class1", "Class1Inner", "Class2", "SrcClass1", "SrcClass1Inner", "SrcClass1Inner2", "SrcClass2"]

    }
}