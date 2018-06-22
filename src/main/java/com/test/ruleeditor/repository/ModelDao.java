package com.test.ruleeditor.repository;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Srilakshmi on 26/08/17.
 */
@Repository
public class ModelDao {

    public List<Class> getAllModels() {
        List<Class> models = new ArrayList<>();
        try {
            ClassPath p = ClassPath.from(ClassLoader.getSystemClassLoader()); // might need to provide different ClassLoader
            ImmutableSet<ClassPath.ClassInfo> classes = p.getTopLevelClassesRecursive("com.mundio.drools.model");
            //p.getAllClasses()
            for (ClassPath.ClassInfo classInfo : classes) {
                Class clazz = classInfo.load();
                models.add(clazz);
            }
        }catch (IOException ex){

        }
        return models;
    }
}
