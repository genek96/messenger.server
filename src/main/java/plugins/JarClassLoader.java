package plugins;

public interface JarClassLoader {
    Class loadClass(String className, String classPackage) throws ClassNotFoundException;
}
