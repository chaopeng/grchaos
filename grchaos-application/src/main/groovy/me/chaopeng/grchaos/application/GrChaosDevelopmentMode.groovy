package me.chaopeng.grchaos.application

import com.google.common.base.Throwables

/**
 * me.chaopeng.grchaos.application.GrChaosDevelopmentMode
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosDevelopmentMode {

    final GrChaosApplication application

    GrChaosDevelopmentMode(GrChaosApplication application) {
        this.application = application
    }

    String handle(String cmd, String[] args) {
        try {
            if (cmd != "handler" && this.metaClass.methods.any{it.name == cmd}) {
                return this.invokeMethod(cmd, args)
            } else {
                return "command not found"
            }
        } catch (Exception e) {
            return "${e.getMessage()}:\n${Throwables.getStackTraceAsString(e)}"
        }
    }

    String exit(){
        System.exit(0)
        return "exit"
    }

    String start(){
        application.summer.start()
        return "started"
    }


}
