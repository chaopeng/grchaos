package me.chaopeng.grchaos.application

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.grchaos.summer.AbstractSummerModule
import me.chaopeng.grchaos.summer.SummerInspector
import me.chaopeng.grchaos.summer.TestHelper
import me.chaopeng.grchaos.summer.bean.PackageScan
import spock.lang.Specification

/**
 * me.chaopeng.chao4g.application.GrChaosApplicationTest
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosApplicationTest extends Specification {

    def setup() {
        TestHelper.reloadableClassesSetup()
    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
        new File("application.conf").delete()
    }

    def config = new GrChaosApplicationConfigure(
            srcPath: 'tmp',
            autoReload: false,
            summerModule: 'me.chaopeng.grchaos.application.GrChaosApplicationTest$TestSummerModule',
            developmentMode: false
    )

    def configStr = '''
srcPath='tmp'
autoReload=false
summerModule='me.chaopeng.grchaos.application.GrChaosApplicationTest$TestSummerModule'
developmentMode=false
'''

    static class TestSummerModule extends AbstractSummerModule {
        @Override
        protected void configure() {
            fromPackage(new PackageScan(packageName: "test"))
        }
    }

    def "from configure: success"() {

        def application = GrChaosApplication.fromConfigure(config)

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
        def config = new GrChaosApplicationConfigure(
                srcPath: 'tmp',
                autoReload: false,
                summerModule: 'me.chaopeng.chao4g.application.TestSummerModule',
                developmentMode: false
        )

        GrChaosApplication.fromConfigure(config)

        then:
        thrown(ClassNotFoundException)
    }

    def "from configure: failed - module field is not summer module"() {
        when:
        def config = new GrChaosApplicationConfigure(
                srcPath: 'tmp',
                autoReload: false,
                summerModule: 'me.chaopeng.grchaos.application.GrChaosApplicationTest',
                developmentMode: false
        )

        GrChaosApplication.fromConfigure(config)

        then:
        thrown(AssertionError)
    }

    def "from string"() {
        def application = GrChaosApplication.fromString(configStr)

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
        Files.write(configStr, new File("application.conf"), Charsets.UTF_8)

        def application = GrChaosApplication.fromFile("file://application.conf")

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
        def application = GrChaosApplication.fromFile("classpath://app.conf")

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
        Files.write(configStr, new File("application.conf"), Charsets.UTF_8)

        def application = GrChaosApplication.auto()
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
