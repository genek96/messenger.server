package plugins;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PluginLoader  extends ClassLoader implements JarClassLoader {

    private static final Logger log = Logger.getLogger(PluginLoader.class);

    private Map classesHash = new HashMap();
    public final String[] classPath;

    public PluginLoader(String[] classPath){
        this.classPath = classPath;
    }

    public synchronized Class loadClass (String name, String packageName) throws ClassNotFoundException {
        return loadClass(packageName+name, true);
    }

    @Override
    protected synchronized Class loadClass (String name, boolean resolve) throws ClassNotFoundException {
        Class result = findClass(name);
        if (resolve){
            resolveClass(result);
        }
        return result;
    }

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
