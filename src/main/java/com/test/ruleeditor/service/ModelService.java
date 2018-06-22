package com.test.ruleeditor.service;

import com.test.ruleeditor.domain.ModelDetail;
import com.test.ruleeditor.domain.ModelsOverview;
import com.test.ruleeditor.domain.Property;
import com.test.ruleeditor.repository.ModelDao;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Srilakshmi on 24/08/17.
 */
@Service
public class ModelService {

    @Autowired
    ModelDao modelDao;

    @Autowired
    CacheManager cacheManager;

    @PostConstruct
    public void init() {
        update();
    }

    public void update() {
        for (Class clazz : modelDao.getAllModels()) {
            String packageName = clazz.getName();
            String uri = "/"+packageName.substring(packageName.indexOf("com.mundio.drools.model.")+"com.mundio.drools.model.".length());
            cacheManager.getCache("models").put(uri, clazz);
        }
    }

    public List<ModelsOverview> getAllModels() throws IOException {
        //ClassPath p = ClassPath.from(ClassLoader.getSystemClassLoader()); // might need to provide different ClassLoader
        //ImmutableSet<ClassPath.ClassInfo> classes = p.getTopLevelClassesRecursive("com.mundio.drools");
        //p.getAllClasses()
        Ehcache ehcache = (Ehcache)cacheManager.getCache("models").getNativeCache();
        List<ModelsOverview> models = new ArrayList<>();

        for(Object key: ehcache.getKeys()){
            ModelsOverview overview = new ModelsOverview();
            overview.setUri((String)key);
            Element valueElement = ehcache.get(key);
            Class clazz = (Class)valueElement.getObjectValue();
            overview.setClassName(clazz.getName());
            models.add(overview);
        }
        return models;
    }

   public ModelDetail getModel(String uri){
        Class clazz = cacheManager.getCache("models").get(uri,Class.class);
        ModelDetail detail = new ModelDetail();
        detail.setName(clazz.getName());
        Field[] fields = clazz.getDeclaredFields();

       for (Field field:fields
            ) {
           Property property = new Property();
           property.setName(field.getName());
           if (Collection.class.isAssignableFrom(field.getType())) {
               property.setList(true);
               property.setDataType(field.getGenericType().getTypeName());
           }else {
               property.setDataType(field.getType().getName());
           }

           detail.add(property);
       }
       return detail;
   }
}
