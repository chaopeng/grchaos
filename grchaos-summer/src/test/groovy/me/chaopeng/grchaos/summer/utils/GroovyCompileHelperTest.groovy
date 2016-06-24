package me.chaopeng.grchaos.summer.utils

import me.chaopeng.grchaos.summer.TestHelper
import spock.lang.Specification

/**
 * me.chaopeng.grchaos.summer.utils.GroovyCompileHelperTest
 *
 * @author chao
 * @version 1.0 - 2016-06-22
 */
class GroovyCompileHelperTest extends Specification {

    def gcl = new GroovyClassLoader()

    def setup() {
        TestHelper.setup()
    }

    def cleanup() {
        TestHelper.cleanup()
    }

    def "compile"() {
        def classes = GroovyCompileHelper.compile(["tmp"])

        expect:
        classes.size() == 4
        classes.values().find { it.name == "test.SrcClass1" }.newInstance().srcClass2.class == classes.values().find {
            it.name == "test.SrcClass2"
        }
    }

}
