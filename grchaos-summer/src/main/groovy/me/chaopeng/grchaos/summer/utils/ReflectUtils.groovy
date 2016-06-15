package me.chaopeng.grchaos.summer.utils

import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

/**
 * me.chaopeng.grchaos.summer.utils.ReflectUtils
 *
 * @author chao
 * @version 1.0 - 2016-06-05
 */
class ReflectUtils {

    static List<Method> getMethodsByAnnotation(Object o, Class annotation) {
        return o.class.getDeclaredMethods().findAll { it.getAnnotation(annotation) != null }
    }

    static List<Field> getFieldsByAnnotation(Object o, Class annotation) {
        return o.class.getDeclaredFields().findAll { it.getAnnotation(annotation) != null }
    }

    static Object callMethod(Object o, Method method, Object... args) throws IllegalAccessException, InvocationTargetException {
        boolean accessible = method.isAccessible()
        try {
            method.setAccessible(true)
            return method.invoke(o, args)
        } finally {
            method.setAccessible(accessible)
        }
    }

    static void setField(Object o, Field field, Object val) throws IllegalAccessException {
        boolean accessible = field.isAccessible()
        try {
            field.setAccessible(true)
            field.set(o, val)
        } finally {
            field.setAccessible(accessible)
        }
    }

    static Object getField(Object o, Field field) throws IllegalAccessException {
        boolean accessible = field.isAccessible()
        try {
            field.setAccessible(true)
            return field.get(o)
        } finally {
            field.setAccessible(accessible)
        }
    }

}
