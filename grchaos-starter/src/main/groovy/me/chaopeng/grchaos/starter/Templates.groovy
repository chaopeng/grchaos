package me.chaopeng.grchaos.starter

/**
 * me.chaopeng.grchaos.starter.Templates
 *
 * @author chao
 * @version 1.0 - 2016-07-07
 */
class Templates {

    static def rootBuild = '''\
plugins {
    id "com.github.ben-manes.versions" version "0.12.0"
}


apply plugin: 'groovy'
apply plugin: 'idea'

version = '1.0.0'

task cleanExport << {
    delete file("export")
}
clean.dependsOn(cleanExport)

subprojects {

    apply plugin: 'groovy'
    apply plugin: 'com.github.ben-manes.versions'
    apply from: rootDir.path + '/libs.gradle'

    compileJava.options.encoding = 'UTF-8'
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8

    ext.isProject = new File(projectDir.path + '/build.gradle').exists()
    ext.exportPath = rootDir.path + '/export'

    project.version = rootProject.version

    repositories {
        mavenCentral()
    }

}
'''

    static def libs = '''\
ext.libs = [
    grchaos : [
        'org.codehaus.groovy:groovy-all:2.4.6',
        'me.chaopeng.grchaos:grchaos-summer:1.0.0-alpha-2',
        'me.chaopeng.grchaos:grchaos-application:1.0.0-alpha-2'
    ],
]
'''

    static def settings = '''\
include '${projectName}-lib', '${projectName}-app'
'''

    static def libBuild = '''\
apply plugin: 'application'

mainClassName = 'me.chaopeng.grchaos.application.GrChaosApplicationMain'

dependencies {
    compile(
            libs.grchaos,
    )
}

jar {
    exclude('**/application.conf')
}

distZip {
    archiveName = project.name + '.zip'
    destinationDir = file(exportPath)
}
'''

    static def conf = '''\
srcPath='${projectName}-app/src/main/groovy'
autoReload=false
summerModule=''
developmentMode=true
'''

    static def appBuild = '''\
dependencies {
    compile project(':${projectName}-lib')
}

task distZip(type: Zip) {
    def exportName = project.name + '.zip'

    from 'src/main/groovy'
    into project.name
    archiveName = exportName
    destinationDir = file(exportPath)
}
'''
}
