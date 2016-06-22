package me.chaopeng.grchaos.summer.utils

import groovy.util.logging.Slf4j
import me.chaopeng.grchaos.summer.bean.PackageScan

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * me.chaopeng.grchaos.summer.utils.ClassPathScanner
 *
 * @author chao
 * @version 1.0 - 2016-06-02
 */
@Slf4j
class ClassPathScanner {

    static boolean filter(String name, PackageScan scanner) {
        String prefix = scanner.packageName + "."

        if (name.startsWith(prefix)) {
            if (!scanner.recursive) {
                return !name.replace(prefix, "").contains(".") ?
                        !scanner.excludeInner || !name.contains("\$")
                        : false
            } else {
                return !scanner.excludeInner || !name.contains("\$")
            }
        } else {
            return false
        }
    }


    static Set<Class> scan(PackageScan packageScan, boolean checkInOrEx = true, Pattern regex = null) {
        return scan(packageScan.packageName, packageScan.recursive, packageScan.excludeInner, checkInOrEx, regex)
    }

    /**
     *
     * @param basePackage
     * @param recursive
     * @param excludeInner
     * @param checkInOrEx if(regex.match==true) checkInOrEx == true -> include checkInOrEx == false -> exclude
     * @param regex
     * @return Set
     */
    public
    static Set<Class> scan(String basePackage, boolean recursive, boolean excludeInner, boolean checkInOrEx = true, Pattern regex = null) {
        Set<Class<?>> classes = new LinkedHashSet<Class>()
        String packageName = basePackage

        if (packageName.endsWith(".")) {
            packageName = packageName
                    .substring(0, packageName.lastIndexOf('.'))
        }
        String package2Path = packageName.replace('.', '/')

        Enumeration<URL> dirs
        try {
            dirs = Thread.currentThread().getContextClassLoader()
                    .getResources(package2Path)
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement()
                String protocol = url.getProtocol()
                if ("file".equals(protocol)) {
                    log.debug("scanning files")
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8")
                    doScanPackageClassesByFile(classes, packageName, filePath,
                            recursive, excludeInner, checkInOrEx, regex)
                } else if ("jar".equals(protocol)) {
                    log.debug("scanning jars")
                    doScanPackageClassesByJar(packageName, url, recursive,
                            classes, excludeInner, checkInOrEx, regex)
                }
            }
        } catch (IOException e) {
            log.error("IOException error:", e)
        }

        return classes
    }

    private static void doScanPackageClassesByJar(String basePackage, URL url,
                                                  final boolean recursive, Set<Class<?>> classes, boolean excludeInner, boolean checkInOrEx, Pattern regex) {
        String packageName = basePackage
        String package2Path = packageName.replace('.', '/')
        JarFile jar
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile()
            Enumeration<JarEntry> entries = jar.entries()
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement()
                String name = entry.getName()
                if (!name.startsWith(package2Path) || entry.isDirectory()) {
                    continue
                }

                // recursive
                if (!recursive
                        && name.lastIndexOf('/') != package2Path.length()) {
                    continue
                }
                // inner class
                if (excludeInner && name.indexOf('$') != -1) {
                    log.debug("exclude inner class with name:" + name)
                    continue
                }
                String classSimpleName = name
                        .substring(name.lastIndexOf('/') + 1)
                // regex
                if (filterClassName(classSimpleName, checkInOrEx, regex)) {
                    String className = name.replace('/', '.')
                    className = className.substring(0, className.length() - 6)
                    try {
                        classes.add(Thread.currentThread()
                                .getContextClassLoader().loadClass(className))
                    } catch (ClassNotFoundException e) {
                        log.error("Class.forName error:", e)
                    }
                }
            }
        } catch (IOException e) {
            log.error("IOException error:", e)
        }
    }

    private static void doScanPackageClassesByFile(Set<Class<?>> classes,
                                                   String packageName, String packagePath, boolean recursive,
                                                   final boolean excludeInner,
                                                   final boolean checkInOrEx, Pattern regex) {
        File dir = new File(packagePath)
        if (!dir.exists() || !dir.isDirectory()) {
            return
        }
        final boolean fileRecursive = recursive
        File[] dirfiles = dir.listFiles(new FileFilter() {
            boolean accept(File file) {
                if (file.isDirectory()) {
                    return fileRecursive
                }
                String filename = file.getName()
                if (excludeInner && filename.indexOf('$') != -1) {
                    log.debug("exclude inner class with name:" + filename)
                    return false
                }
                return filterClassName(filename, checkInOrEx, regex)
            }
        })
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes,
                        packageName + "." + file.getName(),
                        file.getAbsolutePath(), recursive, excludeInner, checkInOrEx, regex)
            } else {
                String className = file.getName().substring(0,
                        file.getName().length() - 6)
                try {
                    classes.add(Thread.currentThread().getContextClassLoader()
                            .loadClass(packageName + '.' + className))

                } catch (ClassNotFoundException e) {
                    log.error("IOException error:", e)
                }
            }
        }
    }

    private static boolean filterClassName(String className, boolean checkInOrEx, Pattern regex) {
        if (!className.endsWith(".class")) {
            return false
        }
        if (regex == null) {
            return true
        }
        String tmpName = className.substring(0, className.length() - 6)
        boolean flag = false
        if (regex.matcher(tmpName).find()) {
            flag = true
        }
        return (checkInOrEx && flag) || (!checkInOrEx && !flag)
    }


}
