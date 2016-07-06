package me.chaopeng.grchaos.application

/**
 * me.chaopeng.chao4g.application.GrChaosApplicationConfigure
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosApplicationConfigure {

    String srcPath
    boolean autoReload
    String summerModule
    boolean developmentMode

    @Override
    String toString() {
        return "srcPath='" + srcPath + '\'' +
                "\nautoReload=" + autoReload +
                "\nsummerModule='" + summerModule + '\'' +
                "\ndevelopmentMode=" + developmentMode
    }
}
