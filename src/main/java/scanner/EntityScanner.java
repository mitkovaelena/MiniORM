package scanner;

import annotations.Entity;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EntityScanner {
    public List<Class> getAllEntities(String path) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        path += File.separator + "src" + File.separator + "main" + File.separator + "java" + File.separator + "entities";
        File dir = new File(path);
        File[] files = dir.listFiles();
        List<Class> entities = new ArrayList<>();

        for (File file : files) {
            if (file.isFile()) {
                String filename = file.getName();

                Class newClass = Class.forName("entities."+filename.substring(0, filename.length()-5));

                if(!newClass.isAnnotationPresent(Entity.class)){
                    continue;
                }
                entities.add(newClass);
            } else {
                entities.addAll(getAllEntities(path + File.separator + file.getName()));
            }
        }
        return entities;
    }
}
