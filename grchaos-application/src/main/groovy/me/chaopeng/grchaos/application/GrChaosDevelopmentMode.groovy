package me.chaopeng.grchaos.application

import com.google.common.base.Throwables
import me.chaopeng.grchaos.summer.SummerInspector

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
            if (cmd != "handler" && this.metaClass.methods.any { it.name == cmd }) {
                return this.invokeMethod(cmd, args)
            } else {
                return "command not found"
            }
        } catch (Exception e) {
            return "${e.getMessage()}:\n${Throwables.getStackTraceAsString(e)}"
        }
    }

    String help() {
        return '''\
help
exit
start - execute application.start()
testDeps className - test deps of a class
testAllDeps - test deps of all classes in SummerModule
'''
    }

    String exit() {
        System.exit(0)
        return "exit"
    }

    String start() {
        application.start()
        return "started"
    }

    String testDeps(String className) {
        def missing = SummerInspector.testDeps(application.summer, className)
        String res = missing.isEmpty() ?
                "$className pass deps test" :
                "$className doesnot pass deps test, missing deps=${missing*.name}"
        res += "\nplease also add $className to test case"
        return res
    }

    String testAllDeps(){
        def missing = SummerInspector.testAllDepes(application.summer)

        if (missing.isEmpty()) {
            return "PASS"
        } else {
            def res = new StringBuilder("Missing:\n")

            missing.keySet().each { k ->
                res << "\n" << k << ":\n"

                missing.get(k).each {dep ->
                    res << "    " << dep.name << "\n"
                }
            }

            return res.toString()
        }
    }


}
