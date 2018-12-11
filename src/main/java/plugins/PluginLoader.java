package plugins;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PluginLoader  extends ClassLoader implements JarClassLoader {

    private static final Logger log = Logger.getLogger(PluginLoader.class);

    private Map classesHash = new HashMap();
    private final String[] classPath;

    /**
     *
     * @param classPath define folders with plugins started from "resources"
     */
    public PluginLoader(String[] classPath){
        this.classPath = classPath;
    }

    /**
     *
     * @param className - name of required class
     * @param classPackage - package with the class
     * @return required class with entered name
     * @throws ClassNotFoundException - if class with required name is not found
     * @see JarClassLoader
     */
    public synchronized Class loadClass (String className, String classPackage) throws ClassNotFoundException {
        if (classPackage == null){
            return loadClass(className, true);
        } else{
            return loadClass(classPackage+"."+className, true);
        }
    }

    /**
     *
     * @param name
     *        the name of required class
     * @param resolve
     *        If {@code true} then resolve the class
     * @return class if it was found
     * @throws ClassNotFoundException
     *          Throws, when the class was not found
     */
    @Override
    protected synchronized Class loadClass (String name, boolean resolve) throws ClassNotFoundException {
        Class result = findClass(name);
        if (resolve){
            resolveClass(result);
        }
        return result;
    }

    /**
     *
     * @param name
     *        the name of required class
     * @return class, if it was found
     * @throws ClassNotFoundException
     *         Throws, when class was not found
     */
    @Override
    protected Class findClass(String name) throws ClassNotFoundException {
        Class result = (Class) classesHash.get(name);
        if (result != null){
            return result;
        }

        File file = findFile(name.replace('.','/'), ".class");
        if (file == null){
            return findSystemClass(name);
        }
        try {
            byte[] classBytes = loadFileAsBytes(file);
            result = defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e){
            throw new ClassNotFoundException("Cannot load class "+name+" : "+e);
        } catch (ClassFormatError e){
            throw new ClassNotFoundException("Format of class file incorrect for class "+
                    name+" : "+e);
        }
        classesHash.put(name, result);
        return result;

    }

    /**
     *
     * @param name
     *        Name of the required file
     * @return URL if the resource was found, else null
     */
    protected URL findResource(String name){
        File file = findFile(name, "");
        if (file == null){
            return null;
        }
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    /**
     *
     * @param file
     *        File, from which we will read bytes
     * @return File's content as byte array
     * @throws IOException
     *          if exception occur during reading the file
     */
    private byte[] loadFileAsBytes(File file) throws IOException {
        byte[] result = new byte[(int)file.length()];
        FileInputStream fileStream = new FileInputStream(file);
        try {
            fileStream.read(result, 0, result.length);
        } finally {
            try {
                fileStream.close();
            } catch (IOException e) {
                log.error(e.getMessage()+" : "+ Arrays.toString(e.getStackTrace()));
            }
        }
        return result;
    }

    /**
     *
     * @param name
     *        the name of required file
     * @param extension
     *        extension of required file
     * @return File if it was found, else null
     */
    private File findFile(String name, String extension) {
        for (int i = 0; i < classPath.length; i++){
            File file = new File((new File(classPath[i])).getPath()+
                    File.separatorChar+name.replace('/',File.separatorChar)+extension);
            if (file.exists()){
                return file;
            }
        }
        return null;
    }
}
