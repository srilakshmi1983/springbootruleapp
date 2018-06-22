package com.test.ruleeditor.controller;

import com.test.ruleeditor.domain.CreationResponse;
import com.test.ruleeditor.exception.RuleCreationException;
import com.test.ruleeditor.service.RuleCreationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Created by sinchon 26/08/17.
 */
@RestController(value = "/rule")
public class RuleCreationController {

    @Autowired
    private RuleCreationService ruleCreationService;

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/create",consumes = MediaType.TEXT_PLAIN_VALUE,produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.POST)
    public ResponseEntity<CreationResponse> createRule(@RequestBody String ruleXml){


            String jobId = "";
            CreationResponse creationResponse = new CreationResponse();
            try{
                jobId = ruleCreationService.createRule(ruleXml);
            }catch (RuleCreationException ruleEx){
                creationResponse.setStatus("Rule Creation Failed");
                creationResponse.setMessage(ruleEx.getMessage());
                return new ResponseEntity<CreationResponse>(creationResponse,HttpStatus.EXPECTATION_FAILED);

            }catch (Exception e){
                creationResponse.setStatus("Rule Creation Failed");
                creationResponse.setMessage(e.getMessage());
                return new ResponseEntity<CreationResponse>(creationResponse,HttpStatus.INTERNAL_SERVER_ERROR);

            }
            if(jobId.equalsIgnoreCase("NA")){
                creationResponse.setStatus("Rule Creation Failed");
                creationResponse.setMessage("Reason Unknown");
                return new ResponseEntity<CreationResponse>(creationResponse,HttpStatus.INTERNAL_SERVER_ERROR);
            }
            creationResponse.setStatus("Rule Creation Submitted Successfully");
            creationResponse.setJobId(jobId);
            return new ResponseEntity<CreationResponse>(creationResponse,HttpStatus.CREATED);
            //result.setResult(ResponseEntity.ok(now()));

    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/check",consumes = MediaType.TEXT_PLAIN_VALUE,produces = MediaType.APPLICATION_JSON_VALUE,method = RequestMethod.POST)
    public ResponseEntity<CreationResponse> checkRuleCreation(@RequestParam String jobId){

        return new ResponseEntity<CreationResponse>(HttpStatus.NOT_IMPLEMENTED);
    }

}
