package me.chaopeng.grchaos.application

/**
 * me.chaopeng.chao4g.application.GrChaosApplicationConfigure
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosApplicationConfigure {

    private String srcPath
    private boolean autoReload
    private String summerModule
    private boolean developmentMode

    String getSrcPath() {
        return srcPath
    }

    boolean getAutoReload() {
        return autoReload
    }

    String getSummerModule() {
        return summerModule
    }

    boolean getDevelopmentMode() {
        return developmentMode
    }


    @Override
    String toString() {
        return "srcPath='" + srcPath + '\'' +
                "\nautoReload=" + autoReload +
                "\nsummerModule='" + summerModule + '\'' +
                "\ndevelopmentMode=" + developmentMode
    }
}
