package me.chaopeng.chaos4g.summer

import com.google.common.base.Charsets
import com.google.common.io.Files
import me.chaopeng.chaos4g.summer.bean.PackageScan
import me.chaopeng.chaos4g.summer.ioc.annotations.Inject
import me.chaopeng.chaos4g.summer.utils.DirUtils
import spock.lang.Specification

/**
 * me.chaopeng.chaos4g.summer.SummerHotSwapTest
 *
 * @author chao
 * @version 1.0 - 2016-06-11
 */
class SummerHotSwapTest extends Specification {

    Summer summer

    def setup() {
        TestHelper.reloadableClassesSetup()
        summer = new Summer("tmp")
        summer.loadModule(new AbstractSummerModule() {
            @Override
            protected void configure() {
                fromPackage(new PackageScan(packageName: "test"))
            }
        })
    }

    def cleanup() {
        TestHelper.reloadableClassesCleanup()
    }

    static class TestInjectMe {
        @Inject
        def srcClass1
    }

    def "upgrade"() {

        def srcClass1BeforeReload = summer.getBean("srcClass1")
        def class1InnerBeforeReload = summer.getBean("class1Inner")

        def srcClass1 = '''\
package test

import me.chaopeng.chaos4g.summer.ioc.annotations.*

@Bean
class SrcClass1 {

    @Inject
    def class1

    def hello(){
        "hello2"
    }

    @Bean("srcClass1Inner")
    static class SrcClass1Inner {

    }

    @Bean("srcClass1Inner2")
    static class SrcClass1Inner2 {

    }

}
        '''

        def srcClass3 = '''\
package test

import me.chaopeng.chaos4g.summer.ioc.annotations.*

@Bean
class SrcClass3 {

    @Inject
    def srcClass2

}
        '''
        Files.write(srcClass1, new File("tmp/SrcClass1.groovy"), Charsets.UTF_8)
        Files.write(srcClass3, new File("tmp/SrcClass3.groovy"), Charsets.UTF_8)
        DirUtils.rm("tmp/SrcClass2.groovy")

        TestInjectMe testInjectMe = new TestInjectMe()
        summer.injectMe(testInjectMe)

        def testInjectMeSrcClass1 = testInjectMe.srcClass1

        summer.reload()
        def srcClass1AfterReload = summer.getBean("srcClass1")
        def class1InnerAfterReload = summer.getBean("class1Inner")

        expect:

        // update bean
        srcClass1BeforeReload.hello() == "hello"
        srcClass1AfterReload.hello() == "hello2"

        srcClass1BeforeReload != srcClass1AfterReload

        testInjectMeSrcClass1.is(srcClass1BeforeReload)

        // not updated bean
        class1InnerBeforeReload.is(class1InnerAfterReload)

        // new inject
        srcClass1BeforeReload.hasProperty("class1") == null
        srcClass1AfterReload.hasProperty("class1") != null

        srcClass1AfterReload.class1 == summer.getBean("class1")

        // summer upgrade method
        class1InnerAfterReload.srcClass1.is(srcClass1AfterReload)
        class1InnerAfterReload.upgradeCount == 1

        // new bean by adding @Bean
        summer.getBean("srcClass1Inner2") != null

        // brand new bean class
        def srcClass3Bean = summer.getBean("srcClass3")
        srcClass3Bean != null // new bean
        srcClass3Bean.srcClass2 != null // delete will be ignore

        // anonymousBean inject update
        testInjectMe.srcClass1.is(srcClass1AfterReload)
    }
}
