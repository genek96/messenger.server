package plugins;

import java.io.File;
import java.net.*;
import java.nio.file.Paths;

public class JarLoader implements JarClassLoader{
    private final String classPath;

    /**
     *
     * @param classPath define folder with plugins started from "resources"
     */
    public JarLoader(String classPath) {
        this.classPath = classPath;
    }

    /**
     *
     * @param className - name of required class
     * @param classPackage - package with the class
     * @return required class with entered name
     * @throws ClassNotFoundException - if class with required name is not found
     * @see JarLoader
     */
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

    /**
     *
     * @param fileName name of required file
     * @return File, if it was found or null if it is not
     */
    private File findFile(String fileName)  {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource("Factorial.jar");
        if (url == null){
            return null;
        }
        File file = new File(url.getPath());
        System.out.println(file.getAbsolutePath());
        if (file.exists()){
            return file;
        } else {
            return null;
        }
    }

    /**
     *
     * @param fileName name of the required file
     * @return url, if resource was found, else null
     */
    private URL getUrlToResource(String fileName) {
        ClassLoader loader = getClass().getClassLoader();
        URL url = loader.getResource(classPath+fileName+".jar");
        return url;
    }
}
