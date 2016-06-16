package me.chaopeng.grchaos.application

import com.google.common.base.Strings

/**
 * me.chaopeng.grchaos.application.GrChaosApplicationMain
 *
 * @author chao
 * @version 1.0 - 2016-06-15
 */
class GrChaosApplicationMain {

    static String ASCII_ART = '''\
  _______ .______        ______  __    __       ___       ______        _______.
 /  _____||   _  \\      /      ||  |  |  |     /   \\     /  __  \\      /       |
|  |  __  |  |_)  |    |  ,----'|  |__|  |    /  ^  \\   |  |  |  |    |   (----`
|  | |_ | |      /     |  |     |   __   |   /  /_\\  \\  |  |  |  |     \\   \\
|  |__| | |  |\\  \\----.|  `----.|  |  |  |  /  _____  \\ |  `--'  | .----)   |
 \\______| | _| `._____| \\______||__|  |__| /__/     \\__\\ \\______/  |_______/
'''

    public static void main(String[] args) {

        def GrChaosApplication application

        switch (args.length) {
            case 0:
                application = GrChaosApplication.auto()
                break
            case 1:
                application = GrChaosApplication.fromFile(args[0])
                break
            default:
                throw new IllegalArgumentException("Only accept 0 or 1 argument, if you need more fancy start argument, write your main class. ")
        }

        println ASCII_ART
        println "GrChaos Application Start AT ${new Date().format("yyyy-MM-dd HH:mm:ss z")}..."
        println "----------------------------------------"
        println application.configure
        println "----------------------------------------"

        // not developmentMode, start automatically
        if (!application.configure.developmentMode){
            application.start()
        }

        // else start a command line interface
        else {
            GrChaosDevelopmentMode developmentMode = new GrChaosDevelopmentMode(application)
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in))
            while (true) {

                print "GrChaos > "

                String str = br.readLine().trim()

                if (Strings.isNullOrEmpty(str)) {
                    continue
                }

                String[] ss = str.split(" ")
                String cmd = ss[0]

                String[] params = null
                if (ss.length > 1) {
                    params = Arrays.copyOfRange(ss, 1, ss.length)
                }

                println developmentMode.handle(cmd, params)
            }
        }

    }

}
