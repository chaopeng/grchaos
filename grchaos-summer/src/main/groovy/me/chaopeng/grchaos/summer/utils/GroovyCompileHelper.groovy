package me.chaopeng.grchaos.summer.utils

import groovy.io.FileType
import groovyjarjarasm.asm.ClassVisitor
import groovyjarjarasm.asm.ClassWriter
import me.chaopeng.grchaos.summer.exceptions.SummerException
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.*

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
    public Map<String, Class> compile0() throws SummerException {

        def groovyClassLoader = new GroovyClassLoader()

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

        ClassCollector collector = new ClassCollector(groovyClassLoader, unit)
        unit.setClassgenCallback(collector)

        try {
            unit.compile(Phases.CLASS_GENERATION)
        } catch (CompilationFailedException e) {
            throw new SummerException("Exception during script compilation", e)
        }

        collector.loadedClasses
    }

    static class ClassCollector extends CompilationUnit.ClassgenCallback {

        private final GroovyClassLoader cl
        private final CompilationUnit unit
        private final Map<String, Class> loadedClasses

        ClassCollector(GroovyClassLoader gcl, CompilationUnit unit) {
            this.cl = new GroovyClassLoader.InnerLoader(gcl)
            this.unit = unit
            this.loadedClasses = new HashMap<>()
        }

        @Override
        void call(ClassVisitor classVisitor, ClassNode classNode) throws CompilationFailedException {
            BytecodeProcessor bytecodePostprocessor = unit.getConfiguration().getBytecodePostprocessor()
            byte[] code = (classVisitor as ClassWriter).toByteArray()
            if (bytecodePostprocessor != null) {
                code = bytecodePostprocessor.processBytecode(classNode.getName(), code)
            }
            Class clazz = cl.defineClass(classNode.name, code)
            this.loadedClasses.put(classNode.name, clazz)
        }
    }

    static Map<String, Class> compile(List<String> paths) {

        def helper = new GroovyCompileHelper()

        helper.addSourcePaths(paths)

        return helper.compile0()
    }
}