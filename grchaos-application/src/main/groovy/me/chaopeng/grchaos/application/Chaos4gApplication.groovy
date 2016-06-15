package me.chaopeng.grchaos.application

import com.google.common.base.Charsets
import com.google.common.io.Files
import com.google.common.io.Resources
import me.chaopeng.grchaos.summer.AbstractSummerModule
import me.chaopeng.grchaos.summer.Summer

/**
 * me.chaopeng.chao4g.application.Chaos4gApplication
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class Chaos4gApplication {

    public final Summer summer
    public final Chaos4gApplicationConfigure configure

    Chaos4gApplication(Summer summer, Chaos4gApplicationConfigure configure) {
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

    static Chaos4gApplication fromConfigure(Chaos4gApplicationConfigure configure) {

        Summer s = new Summer(configure.srcPath, configure.autoReload)

        def clazz = s.getClassLoader().findClass(configure.summerModule)

        def moduleIns = clazz.newInstance()

        assert moduleIns in AbstractSummerModule: "${clazz.name} is not a summer module"

        def module = clazz.newInstance() as AbstractSummerModule

        s.loadModule(module)

        Chaos4gApplication application = new Chaos4gApplication(s, configure)

        return application
    }

    static Chaos4gApplication fromString(String s) {
        def configure = new ConfigSlurper().parse(s)

        return fromConfigure(configure as Chaos4gApplicationConfigure)
    }

    static Chaos4gApplication fromFile(String filePath) {
        if (filePath.startsWith("file://")) {
            return fromFileSystemFile(filePath.replace("file://", ""))
        } else if (filePath.startsWith("classpath://")) {
            return fromClassPathFile(filePath.replace("classpath://", ""))
        }
        return null
    }

    static Chaos4gApplication fromFileSystemFile(String filePath) {
        def s = Files.toString(new File(filePath), Charsets.UTF_8)
        return fromString(s)
    }

    static Chaos4gApplication fromClassPathFile(String filePath) {
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
    static Chaos4gApplication auto() {

        try {
            return fromClassPathFile("application.conf")
        } catch (IllegalArgumentException e) {
            // ignore
        }

        return fromFileSystemFile("application.conf")
    }


}
