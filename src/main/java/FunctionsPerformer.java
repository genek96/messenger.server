import org.apache.log4j.Logger;
import plugins.JarClassLoader;
import plugins.JarLoader;
import plugins.PluginLoader;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

public class FunctionsPerformer implements Runnable{
    private JarClassLoader loader;

    private static final Logger log = Logger.getLogger(FunctionsPerformer.class);
    private ArrayList<String> tasks = new ArrayList<>();
    private final MessageReciver router; //required to notify about completed task
    private final int id;

    public static final Object locker = new Object(); //locker for synchronization
    /**
     *
     * @param classPath - way from resources folder to folder with jar plugins.
     */
    public FunctionsPerformer (String classPath, MessageReciver router, int id){
//        this.loader = new JarLoader(classPath);
        this.loader = new PluginLoader(new String[] {""});
        this.router = router;
        this.id = id;
    }

    /**
     *
     * @param task task, which should be added to order of tasks
     */
    void addTask(String task){
        if (task.startsWith("/")){
            synchronized (locker){
                tasks.add(task);
            }
        }
    }

    /**
     *
     * @param command - name of command which should be performed. Format: /commandName
     * @param params - parameters of the command
     * @return result of performing of the command or null, if the command is not found
     */
    private String doCommand(String command, String[] params){

        //checking the command
        if (!command.startsWith("/")){
            return null;
        }
        command = command.substring(1);

        //Loading class and method to perform this function
        Method function = null;
        try{
            Class performer = loader.loadClass(command, null);
            function = findMethodByName("count", performer.getMethods());
            if (function == null){
                throw new NoSuchMethodException("");
            }
        } catch (ClassNotFoundException | NoSuchMethodException e){
            log.warn("Command not found: "+command + " : "+ Arrays.toString(e.getStackTrace()));
            return "Command not found "+command;
        }
        //Checking number of parameters
        if (function.getParameterCount() != params.length){
            log.warn("Wrong number of arguments in : " + Arrays.toString((new Exception()).getStackTrace()));
            return "Wrong number of arguments!";
        }

        //invoking function
        String result;
        try{
            switch (params.length){
                case 0:
                    result = (String) function.invoke(null);
                    break;
                case 1:
                    result = (String) function.invoke(null, params[0]);
                    break;
                case 2:
                    result = (String) function.invoke(null, params[0], params[1]);
                    break;
                default:
                    throw new IllegalArgumentException("Wrong number of arguments was given!");
            }
        } catch (Throwable e){
            log.error(e.getMessage()+" : "+ Arrays.toString(e.getStackTrace()));
            return e.getMessage();
        }
        return result;

    }

    /**
     *
     * @param name - name of method which should be founded
     * @param methods - array of methods to search
     * @return - first founded method with required name or null, if method was not found
     */
    private Method findMethodByName(String name, Method[] methods){
        for (Method method : methods) {
            if (method.getName() == name) {
                return method;
            }
        }
        return null;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()){
            if (tasks.size() == 0){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error(e.getMessage()+" : "+ Arrays.toString(e.getStackTrace()));
                    return;
                }
                continue;
            }
            while (tasks.size() > 0){
                String[] parts;
                String[] params;
                synchronized (locker) {
                    parts = tasks.get(0).split(" ");
                    params = new String[0];
                    if (parts.length > 1) {
                        params = (tasks.get(0).substring(parts[0].length() + 1)).split(" ");
                    }
                }
                String result = doCommand(parts[0], params);
                router.getAnswer(result, id);
                synchronized (locker){
                    tasks.remove(0);
                }
            }
        }
    }
}
