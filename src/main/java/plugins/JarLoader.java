package plugins;

import java.io.File;
import java.net.*;
import java.nio.file.Paths;

public class JarLoader implements JarClassLoader{
    private final String classPath;

    public JarLoader(String classPath) {
        this.classPath = classPath;
    }

    public synchronized Class loadClass(String className, String classPackage) throws ClassNotFoundException {
        URL url = getUrlToResource(className);
        if (url == null){
            throw new ClassNotFoundException("Class "+className+" not found");
        }
        URL[] urls = new URL[] {url};
        ClassLoader loader = new URLClassLoader(urls);
        classPackage = (classPackage == null) ? "" : classPackage;
        return loader.loadClass(classPackage+className);
    }

    private File findFile(String fileName) throws Throwable {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource("Factorial.jar");

//        File file = new File(classPath + fileName+".jar");
        File file = new File(url.getPath());
        System.out.println(file.getAbsolutePath());
        if (file.exists()){
            return file;
        } else {
            return null;
        }
    }

    private URL getUrlToResource(String fileName) {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(classPath+fileName+".jar");
        return url;
    }
}
