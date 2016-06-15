package me.chaopeng.grchaos.application

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.grchaos.summer.AbstractSummerModule
import me.chaopeng.grchaos.summer.SummerInspector
import me.chaopeng.grchaos.summer.TestHelper
import me.chaopeng.grchaos.summer.bean.PackageScan
import spock.lang.Specification

/**
 * me.chaopeng.chao4g.application.Chaos4gApplicationTest
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class Chaos4gApplicationTest extends Specification {

    def setup() {
        TestHelper.reloadableClassesSetup()
    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
        new File("application.conf").delete()
    }

    static class TestSummerModule extends AbstractSummerModule {
        @Override
        protected void configure() {
            fromPackage(new PackageScan(packageName: "test"))
        }
    }

    def "from configure: success"() {
        def config = new Chaos4gApplicationConfigure(
                srcPath: 'tmp',
                autoReload: false,
                summerModule: 'me.chaopeng.chao4g.application.Chaos4gApplicationTest$TestSummerModule',
                debug: false
        )

        def application = Chaos4gApplication.fromConfigure(config)

        expect:
        SummerInspector.allBeans(application.summer)*.name.sort() ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"].sort()
    }

    def "from configure: failed - summer module not found"() {
        when:
        def config = new Chaos4gApplicationConfigure(
                srcPath: 'tmp',
                autoReload: false,
                summerModule: 'me.chaopeng.chao4g.application.TestSummerModule',
                debug: false
        )

        Chaos4gApplication.fromConfigure(config)

        then:
        thrown(ClassNotFoundException)
    }

    def "from configure: failed - module field is not summer module"() {
        when:
        def config = new Chaos4gApplicationConfigure(
                srcPath: 'tmp',
                autoReload: false,
                summerModule: 'me.chaopeng.chao4g.application.Chaos4gApplicationTest',
                debug: false
        )

        Chaos4gApplication.fromConfigure(config)

        then:
        thrown(AssertionError)
    }

    def "from string"() {
        def config = '''
srcPath='tmp'
autoReload=false
summerModule='me.chaopeng.chao4g.application.Chaos4gApplicationTest$TestSummerModule'
debug=false
'''
        def application = Chaos4gApplication.fromString(config)

        expect:
        SummerInspector.allBeans(application.summer)*.name.sort() ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"].sort()
    }

    def "from fs file"() {
        def config = '''
srcPath='tmp'
autoReload=false
summerModule='me.chaopeng.chao4g.application.Chaos4gApplicationTest$TestSummerModule'
debug=false
'''
        Files.write(config, new File("application.conf"), Charsets.UTF_8)

        def application = Chaos4gApplication.fromFile("file://application.conf")

        expect:
        SummerInspector.allBeans(application.summer)*.name.sort() ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"].sort()

    }

    def "from classpath file"() {
        def application = Chaos4gApplication.fromFile("classpath://app.conf")

        expect:
        SummerInspector.allBeans(application.summer)*.name.sort() ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"].sort()
    }

    def "auto"() {
        def config = '''
srcPath='tmp'
autoReload=false
summerModule='me.chaopeng.chao4g.application.Chaos4gApplicationTest$TestSummerModule'
debug=false
'''
        Files.write(config, new File("application.conf"), Charsets.UTF_8)

        def application = Chaos4gApplication.auto()
        expect:
        SummerInspector.allBeans(application.summer)*.name.sort() ==
                ["class2",
                 "class3",
                 "class1",
                 "class1Inner",
                 "srcClass1",
                 "srcClass2",
                 "srcClass1Inner"].sort()
    }

}
