package me.chaopeng.chaos4g.summer.aop

/**
 * me.chaopeng.chaos4g.summer.aop.AopHelper
 *
 * @author chao
 * @version 1.0 - 2016-06-01
 */
class AopHelper {

    static install(GroovyObject object, IAspectHandler handler) {
        object.metaClass.invokeMethod { name, args ->
            handler.begin(name, args)
            def result = null
            try {
                if (handler.filter(name, args)) {
                    handler.before(name, args)
                    result = delegate.class.metaClass.getMetaMethod(name, args)?.invoke(delegate, args)
                    handler.end(name, args)
                }
            } catch (error) {
                handler.error(name, args, error)
            } finally {
                handler.after(name, args)
            }
            return result
        }
    }
}
