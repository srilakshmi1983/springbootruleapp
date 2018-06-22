package com.test.ruleeditor.controller;

import com.test.ruleeditor.domain.ModelDetail;
import com.test.ruleeditor.domain.ModelsOverview;
import com.test.ruleeditor.service.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by Srilakshmi on 22/06/17.
 */

@RestController
public class ModelController {

    @Autowired
    private ModelService modelService;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/models",produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.GET)
    public ResponseEntity<List<ModelsOverview>> getModels(){
        List<ModelsOverview> models= null;
        try {
            models = modelService.getAllModels();
        }catch (Exception ex){
            return new ResponseEntity(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(models==null){
            return new ResponseEntity<List<ModelsOverview>>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<List<ModelsOverview>>(models,HttpStatus.OK);
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/models/detail/{modelUri}",produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.GET)
    public ResponseEntity<ModelDetail> getModel(@PathVariable("modelUri")String modelUri){
        ModelDetail model= null;
        try {
            model = modelService.getModel("/"+modelUri);
        }catch (Exception ex){
            return new ResponseEntity(ex.getMessage(),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if(model==null){
            return new ResponseEntity<ModelDetail>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<ModelDetail>(model,HttpStatus.OK);
    }
}
