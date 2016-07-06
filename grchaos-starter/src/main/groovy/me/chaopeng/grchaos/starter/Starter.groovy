package me.chaopeng.grchaos.starter

import groovy.text.SimpleTemplateEngine

/**
 * me.chaopeng.grchaos.starter.Starter
 *
 * @author chao
 * @version 1.0 - 2016-07-06
 */
class Starter {

    public static void main(String[] args) {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

        printf "project name: "
        String projectName = br.readLine()

        printf "module package name: "
        String modulePackageName = br.readLine()

        printf "module class name: "
        String moduleClassName = br.readLine()

        def bind = [
                projectName: projectName,
                modulePackageName: modulePackageName,
                moduleClassName: moduleClassName
        ]

        def engine = new SimpleTemplateEngine()

    }
}
