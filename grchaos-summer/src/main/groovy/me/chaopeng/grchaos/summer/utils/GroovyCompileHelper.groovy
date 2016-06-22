package me.chaopeng.grchaos.summer.utils

import groovy.io.FileType
import me.chaopeng.grchaos.summer.exceptions.SummerException
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.GroovyClass

/**
 * Helper class for compiling Groovy files into classes. This class takes as it's input a collection
 * of Paths and outputs a {@link GroovyClassLoader} with the classes pre-loaded into it.
 *
 * If a parent {@link ClassLoader} is not provided, the current thread context classloader is used.
 *
 * From Netflix/Nicobar
 */
class GroovyCompileHelper {
    private final List<String> sourcePaths = []
    private final GroovyClassLoader groovyClassLoader

    private GroovyCompileHelper(GroovyClassLoader groovyClassLoader) {
        this.groovyClassLoader = groovyClassLoader
    }

    private GroovyCompileHelper addSourcePaths(List<String> paths) {
        if (paths != null) {
            sourcePaths.addAll(paths)
        }
        return this
    }

    /**
     * Compile the given source and load the resultant classes into a new {@link ClassNotFoundException}
     * @return initialized and laoded classes
     * @throws SummerException
     */
    @SuppressWarnings("unchecked")
    private Set<GroovyClass> compile0() throws SummerException {
        final CompilerConfiguration conf = CompilerConfiguration.DEFAULT
        conf.setTolerance(0)
        conf.setVerbose(true)

        CompilationUnit unit = new CompilationUnit(conf, null, groovyClassLoader)

        try {
            sourcePaths.each { dir ->
                def files = DirUtils.recursive(dir, FileType.FILES, ~/\.groovy/)

                files.each { unit.addSource(it) }
            }
        } catch (Exception e) {
            throw new SummerException("Exception loading source files", e)
        }
        try {
            unit.compile()
        } catch (CompilationFailedException e) {
            throw new SummerException("Exception during script compilation", e)
        }
        return new HashSet<>(unit.getClasses())
    }

    static Set<Class> compile(GroovyClassLoader groovyClassLoader, List<String> paths){

        def helper = new GroovyCompileHelper(groovyClassLoader)

        helper.addSourcePaths(paths)

        def groovyClasses = helper.compile0()

        return groovyClasses.collect { groovyClass ->
            groovyClassLoader.defineClass(groovyClass.name, groovyClass.bytes)
        }.toSet()
    }

    static void unloadClasses(Set<Class> classes) {
        def registry = GroovySystem.getMetaClassRegistry()
        classes.each { clazz ->
            registry.removeMetaClass( clazz )
        }
    }
}