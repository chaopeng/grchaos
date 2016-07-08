package me.chaopeng.grchaos.starter

import com.google.common.base.Charsets
import com.google.common.io.Files
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

        File dir = new File(projectName)

        if (dir.exists()) {
            System.err.println("$projectName is already exists. ")
            System.exit(1)
        }

        initPath(dir, projectName)

        println "done. "
    }

    public static void initPath(File dir, String projectName) {

        def bind = [
                projectName: projectName,
        ]

        dir.mkdir()

        def root = dir.absolutePath

        def engine = new SimpleTemplateEngine()

        // /build.gradle
        write(Templates.rootBuild, root + "/build.gradle")

        // /setting.gradle
        write(Templates.settings, bind, root + "/setting.gradle")

        // /libs.gradle
        write(Templates.libs, root + "/libs.gradle")

        def lib = root + "/${projectName}-lib"
        new File(lib).mkdir()

        new File(lib + "/src/main/groovy").mkdirs()
        new File(lib + "/src/main/resources").mkdirs()

        // /lib/build.gradle
        write(Templates.libBuild, lib + "/build.gradle")

        // /lib/src/main/resources/application.conf
        write(Templates.conf, bind, lib + "/src/main/resources/application.conf")


        def app = root + "/${projectName}-app"
        new File(app).mkdir()
        new File(app + "/src/main/groovy").mkdir()
        new File(app + "/src/main/resources").mkdir()

        // /app/build.gradle
        write(Templates.appBuild, bind, app + "/build.gradle")

    }

    private static void write(String template, Map bind, String filepath) {
        def engine = new SimpleTemplateEngine()

        def s = engine.createTemplate(template)
                .make(bind).toString()

        write(s, filepath)
    }

    private static void write(String s, String filepath) {

        println "generating $filepath"

        Files.write(s, new File(filepath), Charsets.UTF_8)
    }
}
