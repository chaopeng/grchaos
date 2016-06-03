package me.chaopeng.chaos4g.summer

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.chaos4g.summer.bean.Changes
import me.chaopeng.chaos4g.summer.utils.DirUtils
import spock.lang.Specification

/**
 * me.chaopeng.chaos4g.summer.SummerClassLoaderTest
 *
 * @author chao
 * @version 1.0 - 2016-06-02
 */
class SummerClassLoaderTest extends Specification {

    def scl = SummerClassLoader.instance

    def setup() {
        DirUtils.mkdir("tmp")

        def class2 = '''\
package test

class Class2 {

    static class Class2Inner {

    }

}
        '''

        def class3 = '''\
package test

class Class3 {

    def hello(){
        "hello"
    }
}
'''
        Files.write(class2, new File("tmp/Class2.groovy"), Charsets.UTF_8)
        Files.write(class3, new File("tmp/Class3.groovy"), Charsets.UTF_8)

        scl.init("tmp")

    }

    def cleanup() {
        def tmp = new File("tmp")
        tmp.deleteDir();
    }

    def "test init & find"() {

        expect:
        scl.findClass(name).name == name

        where:
        name                       | _
        "test.Class1"              | _
        "test.Class1\$Class1Inner" | _
        "test.Class2"              | _
        "test.Class2\$Class2Inner" | _
        "test.Class3"              | _

    }

    def "test Class3 hello"() {
        expect:
        scl.findClass("test.Class3").newInstance().hello() == "hello"
    }

    def "test reload file change"() {
        def class3 = '''\
package test

class Class3 {

    def hello(){
        "hello2"
    }
}
'''
        Files.write(class3, new File("tmp/Class3.groovy"), Charsets.UTF_8)
        Changes<File> changes = new Changes<>()
        changes.changes.add(new File("tmp/Class3.groovy"))
        scl.reload(changes)

        expect:
        scl.findClass("test.Class3").newInstance().hello() == "hello2"

    }

    def "test reload new file"() {
        def class4 = '''\
package test

class Class4 {

    def hello(){
        "hello2"
    }
}
'''
        Files.write(class4, new File("tmp/Class4.groovy"), Charsets.UTF_8)
        Changes<File> changes = new Changes<>()
        changes.adds.add(new File("tmp/Class4.groovy"))
        scl.reload(changes)

        expect:
        scl.findClass("test.Class4").newInstance().hello() == "hello2"
    }

    def "test reload delete file"() {

        DirUtils.rm("tmp/Class3.groovy")
        Changes<File> changes = new Changes<>()
        changes.deletes.add(new File("tmp/Class3.groovy"))
        scl.reload(changes)

        when:
        scl.findClass("test.Class3")

        then:
        thrown(ClassNotFoundException)
    }
}
