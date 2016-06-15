package me.chaopeng.grchaos.application

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.common.io.Resources
import me.chaopeng.grchaos.summer.AbstractSummerModule
import me.chaopeng.grchaos.summer.Summer

/**
 * me.chaopeng.chao4g.application.GrChaosApplication
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosApplication {

    public final Summer summer
    public final GrChaosApplicationConfigure configure

    GrChaosApplication(Summer summer, GrChaosApplicationConfigure configure) {
        this.summer = summer
        this.configure = configure
    }

    public void start() {
        summer.preStart()
        summer.start()
    }

    public void reload() {
        summer.reload()
    }

    static GrChaosApplication fromConfigure(GrChaosApplicationConfigure configure) {

        Summer s = new Summer(configure.srcPath, configure.autoReload)

        def clazz = s.getClassLoader().findClass(configure.summerModule)

        def moduleIns = clazz.newInstance()

        assert moduleIns in AbstractSummerModule: "${clazz.name} is not a summer module"

        def module = clazz.newInstance() as AbstractSummerModule

        s.loadModule(module)

        GrChaosApplication application = new GrChaosApplication(s, configure)

        return application
    }

    static GrChaosApplication fromString(String s) {
        def configure = new ConfigSlurper().parse(s)

        return fromConfigure(configure as GrChaosApplicationConfigure)
    }

    static GrChaosApplication fromFile(String filePath) {
        if (filePath.startsWith("file://")) {
            return fromFileSystemFile(filePath.replace("file://", ""))
        } else if (filePath.startsWith("classpath://")) {
            return fromClassPathFile(filePath.replace("classpath://", ""))
        }
        return null
    }

    static GrChaosApplication fromFileSystemFile(String filePath) {
        def s = Files.toString(new File(filePath), Charsets.UTF_8)
        return fromString(s)
    }

    static GrChaosApplication fromClassPathFile(String filePath) {
        URL url = Resources.getResource(filePath)
        def s = Resources.toString(url, Charsets.UTF_8)
        return fromString(s)
    }

    /**
     * auto check below by order:
     * <ol>
     * <li>classpath://application.conf</li>
     * <li>file://$current/application.conf</li>
     * </ol>
     * @return
     */
    static GrChaosApplication auto() {

        try {
            return fromClassPathFile("application.conf")
        } catch (IllegalArgumentException e) {
            // ignore
        }

        return fromFileSystemFile("application.conf")
    }


}
