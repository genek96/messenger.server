package plugins;

public interface JarClassLoader {
    /**
     *
     * @param className - name of required class
     * @param classPackage - package with the class
     * @return required class with entered name
     * @throws ClassNotFoundException - if class with required name is not found
     * @see JarClassLoader
     */
    Class loadClass(String className, String classPackage) throws ClassNotFoundException;
}
