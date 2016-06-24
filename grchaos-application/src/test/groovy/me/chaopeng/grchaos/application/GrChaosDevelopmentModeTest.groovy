package me.chaopeng.grchaos.application

import me.chaopeng.grchaos.summer.AbstractSummerModule
import me.chaopeng.grchaos.summer.TestHelper
import me.chaopeng.grchaos.summer.bean.PackageScan
import me.chaopeng.grchaos.summer.ioc.annotations.Bean
import me.chaopeng.grchaos.summer.ioc.annotations.Inject
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.application.GrChaosDevelopmentModeTest
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosDevelopmentModeTest extends Specification {

    GrChaosDevelopmentMode grChaosDevelopmentMode

    def setup() {
        TestHelper.reloadableClassesSetup()
        def application = GrChaosApplication.fromConfigure(config)
        grChaosDevelopmentMode = new GrChaosDevelopmentMode(application)
    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
    }


    def config = new GrChaosApplicationConfigure(
            srcPath: 'tmp',
            autoReload: false,
            summerModule: 'me.chaopeng.grchaos.application.GrChaosDevelopmentModeTest$TestSummerModule',
            developmentMode: false
    )

    static class TestSummerModule extends AbstractSummerModule {

        @Override
        protected void configure() {
            fromPackage(new PackageScan(packageName: "test"))
            bean(new MissingDepsBean())
        }
    }

    @Bean
    static class MissingDepsBean {
        @Inject
        def aaa

        @Inject
        def bbb
    }

    def "test deps"() {

        def error = '''\
me.chaopeng.grchaos.application.GrChaosDevelopmentModeTest$MissingDepsBean doesnot pass deps test, missing deps=[aaa, bbb]
please also add me.chaopeng.grchaos.application.GrChaosDevelopmentModeTest$MissingDepsBean to test case'''

        expect:
        grChaosDevelopmentMode.testDeps(MissingDepsBean.class.name) == error
    }

    def "test all deps"() {

        def error = '''\
Missing:

missingDepsBean:
    aaa
    bbb
'''

        expect:
        grChaosDevelopmentMode.testAllDeps() == error
    }
}
