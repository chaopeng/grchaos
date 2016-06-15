package me.chaopeng.grchaos.application

/**
 * me.chaopeng.chao4g.application.Chaos4gApplicationConfigure
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class Chaos4gApplicationConfigure {

    private String srcPath
    private boolean autoReload
    private String summerModule
    private boolean debug

    String getSrcPath() {
        return srcPath
    }

    boolean getAutoReload() {
        return autoReload
    }

    String getSummerModule() {
        return summerModule
    }

    boolean getDebug() {
        return debug
    }
}
