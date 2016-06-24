package me.chaopeng.grchaos.summer.utils

import me.chaopeng.grchaos.summer.TestClassWithDepend
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.utils.GroovyCompileHelperTest
 *
 * @author chao
 * @version 1.0 - 2016-06-22
 */
class GroovyCompileHelperTest extends Specification {

    def gcl = new GroovyClassLoader()

    def setup(){
        TestClassWithDepend.setup()
    }

    def cleanup(){
        TestClassWithDepend.cleanup()
    }

    def "compile"(){
        def classes = GroovyCompileHelper.compile(["tmp"])

        expect:
        classes.size() == 4
        classes.find{it.name == "test.SrcClass1"}.newInstance().srcClass2.class == classes.find{it.name == "test.SrcClass2"}
    }

}
