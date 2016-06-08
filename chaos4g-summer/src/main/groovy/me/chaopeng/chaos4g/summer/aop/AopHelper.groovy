package me.chaopeng.chaos4g.summer.aop

import me.chaopeng.chaos4g.summer.aop.annotations.Aspect
import me.chaopeng.chaos4g.summer.aop.annotations.AspectMe
import rx.Observable

/**
 * me.chaopeng.chaos4g.summer.aop.AopHelper
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class AopHelper {

    static void install(GroovyObject object, IAspectHandler handler) {

        Aspect aspect = object.getClass().getAnnotation(Aspect);
        if (aspect != null) {

            object.metaClass.invokeMethod { name, args ->

                def methodName = name as String
                def arguments = args as Object[]
                def method = delegate.class.metaClass.getMetaMethod(methodName, arguments)

                Class[] argClasses = Observable.from(arguments).map{it.class}.toBlocking().toIterable().toList()
                def m = delegate.class.getDeclaredMethod(name, argClasses)

                boolean needAspect = aspect.type() == Aspect.Type.ALL ||
                        (Aspect.Type.PUBLIC && method.isPublic()) ||
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
                    result
                } else {
                    method?.invoke(delegate, arguments)
                }
            }
        }

    }
}
