package me.chaopeng.grchaos.summer.aop

import me.chaopeng.grchaos.summer.aop.annotations.Aspect
import me.chaopeng.grchaos.summer.aop.annotations.AspectMe

/**
 * me.chaopeng.grchaos.summer.aop.AopHelper
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class AopHelper {

    static void install(GroovyObject object, IAspectHandler handler) {

        Aspect aspect = object.getClass().getAnnotation(Aspect)
        if (aspect != null) {

            handler.target = object

            object.metaClass.invokeMethod { name, args ->

                def methodName = name as String
                def arguments = args as Object[]
                def method = delegate.class.metaClass.getMetaMethod(methodName, arguments)

                Class[] argClasses = arguments.collect { it.class }
                def m = delegate.class.getDeclaredMethod(name, argClasses)

                boolean needAspect = (aspect.type() == Aspect.Type.ALL) ||
                        (aspect.type() == Aspect.Type.PUBLIC && method.isPublic()) ||
                        m.getAnnotation(AspectMe.class) != null

                if (needAspect) {
                    handler.begin(methodName, arguments)
                    def result = null
                    try {
                        if (handler.filter(methodName, arguments)) {
                            handler.before(methodName, arguments)
                            result = method?.invoke(delegate, arguments)
                            handler.end(methodName, arguments)
                        }
                    } catch (error) {
                        handler.error(methodName, arguments, error)
                    } finally {
                        handler.after(methodName, arguments)
                    }
                    return result
                } else {
                    return method?.invoke(delegate, arguments)
                }
            }
        }

    }
}
