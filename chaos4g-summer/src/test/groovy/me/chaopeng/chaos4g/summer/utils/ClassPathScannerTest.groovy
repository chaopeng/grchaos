package me.chaopeng.chaos4g.summer.utils

import spock.lang.Specification

/**
 * me.chaopeng.chaos4g.summer.utils.ClassPathScannerTest
 *
 * @author chao
 * @version 1.0 - 2016-06-03
 */
class ClassPathScannerTest extends Specification {

    def "scan"() {

        expect:
        ClassPathScanner.scan("test", recursive, excludeInner, true).collect { it.simpleName }.sort() == classes.sort()

        where:
        recursive | excludeInner | classes
        true      | false        | ["Class1", "Class1Inner", "Class2", "Class3"]
        true      | true         | ["Class1", "Class2", "Class3"]
        false     | true         | ["Class1", "Class2"]

    }

    def "scan with regex"() {

        expect:
        ClassPathScanner.scan("test", true, false, checkInOrEx, regex).collect {
            it.simpleName
        }.sort() == classes.sort()

        where:
        checkInOrEx | regex | classes
        true        | ~/1/  | ["Class1", "Class1Inner"]
        false       | ~/1/  | ["Class2", "Class3"]

    }
}
