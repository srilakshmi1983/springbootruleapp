package com.test.ruleeditor.service;

import com.test.ruleeditor.exception.RuleCreationException;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.drools.compiler.lang.DrlDumper;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.descr.PackageDescr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.lang.model.element.Modifier;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;


/**
 * Created by Srilakshmi on 31/08/17.
 */
@Service
public class RuleCreationService {

    @Value("${ruleCreator.base}")
    private String ruleCreatorProjectBasePath;

    @Value("${ruleCreator.relativeSource}")
    private String relativeSourcePath;

    @Value("${ruleCreator.drlBasePath}")
    private String drlBasePath;

    @Autowired
    TaskExecutor taskExecutor;


    public String createRule(String xml){
        String jobId= "NA";

        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("String drl = execute()")
                .addStatement("$T writer = new $T(DRL_FILE_PATH)",FileWriter.class,FileWriter.class)
                .addStatement("writer.write(drl)")
                .addStatement("writer.flush()")
                .addStatement("writer.close()")
                .addStatement("System.out.println(drl)")
                .addException(Exception.class)
                .build();
        MethodSpec.Builder excuteMethodBuilder = MethodSpec.methodBuilder("execute")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(String.class);
        FieldSpec.Builder drlFilePathBuilder = FieldSpec.builder(String.class, "DRL_FILE_PATH")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        String generatedClassName = "";
        DocumentBuilderFactory factory= DocumentBuilderFactory.newInstance();
        try{
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(xml));
            Document document=builder.parse(is);
            Element documentElement=document.getDocumentElement();
            System.out.println("doc:" + documentElement.getNodeName());
            NodeList rootsChildNodes = documentElement.getChildNodes();
            StringBuffer packagedescrString = new StringBuffer("$T pkg = $T");
            System.out.print("lenght:" + rootsChildNodes.getLength());
            for(int i=0;i<rootsChildNodes.getLength();i++){
                Node currNode = rootsChildNodes.item(i);
                if(currNode.getNodeType()==Node.ELEMENT_NODE){
                    String value = currNode.getTextContent();
                    String nodeName = currNode.getNodeName();
                    if(nodeName.equalsIgnoreCase("package")){
                        System.out.println("inside: "+currNode.getFirstChild().getNodeValue());
                        value=currNode.getFirstChild().getNodeValue();
                        packagedescrString.append(".newPackage().name(\"").append(value).append("\")");
                    }
                    if(nodeName.equalsIgnoreCase("import")){
                        value=currNode.getFirstChild().getNodeValue();
                        packagedescrString.append(".newImport().target(\"").append(value).append("\").end()");
                    }
                    if(nodeName.equalsIgnoreCase("rule")){
                        //value=currNode.getFirstChild().getNodeValue();
                        String ruleName=currNode.getAttributes().getNamedItem("name").getNodeValue();

                        packagedescrString.append(".newRule().name(\"").append(ruleName).append("\")");
                        generatedClassName = ruleName.replace(" ","_");
                        drlFilePathBuilder.initializer("$S", drlBasePath+generatedClassName+".drl");


                        NodeList ruleChildren = currNode.getChildNodes();
                        for (int ruleIndex=0;ruleIndex<ruleChildren.getLength();ruleIndex++){
                            Node currRuleNode = ruleChildren.item(ruleIndex);

                            if(currRuleNode.getNodeName().equalsIgnoreCase("lhs")){
                                packagedescrString.append(".lhs()");
                                NodeList lhsChildren = currRuleNode.getChildNodes();
                                for(int index=0;index<lhsChildren.getLength();index++){
                                    Node currLhsNode = lhsChildren.item(index);
                                    if(currLhsNode.getNodeType()==Node.ELEMENT_NODE){
                                        if(currLhsNode.getNodeName().equalsIgnoreCase("OR")){
                                            packagedescrString.append("or()");
                                        }
                                        if(currLhsNode.getNodeName().equalsIgnoreCase("AND")){
                                            packagedescrString.append("and()");
                                        }
                                        if(currLhsNode.getNodeName().equalsIgnoreCase("pattern")){
                                            String clazz=currLhsNode.getAttributes().getNamedItem("class").getNodeValue();
                                            packagedescrString.append(".pattern(\"").append(clazz).append("\")");
                                            NodeList patternChildren = currLhsNode.getChildNodes();

                                            for(int patternCounter=0;patternCounter < patternChildren.getLength(); patternCounter++){
                                                Node currPatterChild = patternChildren.item(patternCounter);

                                                if(currPatterChild.getNodeType()==Node.ELEMENT_NODE){

                                                    if(currPatterChild.getNodeName().equalsIgnoreCase("id")){
                                                        packagedescrString.append(".id(\"").append(currPatterChild.getFirstChild().getNodeValue()).append("\",false)");
                                                    }
                                                    if(currPatterChild.getNodeName().equalsIgnoreCase("constraint")){
                                                        packagedescrString.append(".constraint(\"").append(currPatterChild.getFirstChild().getNodeValue()).append("\")");
                                                    }

                                                    if(currPatterChild.getNodeName().equalsIgnoreCase("bind")){
                                                        packagedescrString.append(".bind(");

                                                        NodeList bindChildren = currPatterChild.getChildNodes();
                                                        for(int j=0;j<bindChildren.getLength();j++){
                                                            Node currBindChild = bindChildren.item(j);
                                                            if(currBindChild.getNodeType()==Node.ELEMENT_NODE){
                                                                if(currBindChild.getNodeName().equalsIgnoreCase("var")){
                                                                    packagedescrString.append("\"").append(currBindChild.getFirstChild().getNodeValue()).append("\"");
                                                                }
                                                                else if (currBindChild.getNodeName().equalsIgnoreCase("value")){
                                                                    packagedescrString.append(",").append("\"").append(currBindChild.getFirstChild().getNodeValue()).append("\"");
                                                                    packagedescrString.append(",").append("false");

                                                                }
                                                            }
                                                        }
                                                        packagedescrString.append(")");

                                                    }
                                                    else if(currPatterChild.getNodeName().equalsIgnoreCase("from")){
                                                        packagedescrString.append(".from()");
                                                        NodeList fromChildren = currPatterChild.getChildNodes();
                                                        for(int fromChildIndex =0;fromChildIndex<fromChildren.getLength();fromChildIndex++){
                                                            Node fromChild = fromChildren.item(fromChildIndex);
                                                            if(fromChild.getNodeName().equalsIgnoreCase("collect")){
                                                                packagedescrString.append(".collect()");
                                                                NodeList collectChildren = fromChild.getChildNodes();

                                                                for(int collectIndex=0;collectIndex<collectChildren.getLength();collectIndex++){
                                                                    Node collectChild = collectChildren.item(collectIndex);
                                                                    if(collectChild.getNodeName().equalsIgnoreCase("constraint")){
                                                                        packagedescrString.append(".constraint(\"").append(collectChild.getFirstChild().getNodeValue()).append("\")");
                                                                    }
                                                                    if(collectChild.getNodeName().equalsIgnoreCase("pattern")){
                                                                        String patclassName=collectChild.getAttributes().getNamedItem("class").getNodeValue();
                                                                        packagedescrString.append(".pattern(\"").append(patclassName).append("\")");

                                                                        NodeList collectpatChildChildren = collectChild.getChildNodes();

                                                                        for(int collectpatChildIndex=0;collectpatChildIndex<collectpatChildChildren.getLength();collectpatChildIndex++){
                                                                            Node collectpatChildChild = collectpatChildChildren.item(collectpatChildIndex);
                                                                            if(collectpatChildChild.getNodeName().equalsIgnoreCase("constraint")){
                                                                                packagedescrString.append(".constraint(\"").append(collectpatChildChild.getFirstChild().getNodeValue()).append("\")");
                                                                            }
                                                                            if(collectpatChildChild.getNodeName().equalsIgnoreCase("bind")) {
                                                                                packagedescrString.append(".bind(");

                                                                                NodeList bindChildren = currPatterChild.getChildNodes();
                                                                                for (int j = 0; j < bindChildren.getLength(); j++) {
                                                                                    Node currBindChild = bindChildren.item(j);
                                                                                    if (currBindChild.getNodeType() == Node.ELEMENT_NODE) {
                                                                                        if (currBindChild.getNodeName().equalsIgnoreCase("var")) {
                                                                                            packagedescrString.append("\"").append(currBindChild.getFirstChild().getNodeValue()).append("\"");
                                                                                        } else if (currBindChild.getNodeName().equalsIgnoreCase("value")) {
                                                                                            packagedescrString.append(",").append("\"").append(currBindChild.getFirstChild().getNodeValue()).append("\"");
                                                                                            packagedescrString.append(",").append("false");

                                                                                        }
                                                                                    }
                                                                                }
                                                                                packagedescrString.append(")");
                                                                            }
                                                                        }

                                                                        // Node collectpatChildChild = collectpatChildChildren.item(0);


                                                                        packagedescrString.append(".end()");

                                                                        //}
                                                                        //TODO: get collect children as pattern
                                                                        //pattern with only bind,id and constraint
                                                                    }
                                                                }
                                                            }
                                                            else if(fromChild.getNodeName().equalsIgnoreCase("accumulate")){
                                                                //TODO: get accumulate children as pattern and functions
                                                                //pattern with only bind,id and contraint
                                                                packagedescrString.append(".accumulate()");
                                                                NodeList accumChildren = fromChild.getChildNodes();

                                                                for(int childIndex=0;childIndex<accumChildren.getLength();childIndex++){
                                                                    Node accumChild = accumChildren.item(childIndex);

                                                                    if(accumChild.getNodeName().equalsIgnoreCase("source")){
                                                                        packagedescrString.append(".source()");
                                                                        NodeList sourceChildren = accumChild.getChildNodes();
                                                                        for(int sourceIndex=0;sourceIndex<sourceChildren.getLength();sourceIndex++){
                                                                            Node sourceChild = sourceChildren.item(sourceIndex);

                                                                            if(sourceChild.getNodeName().equalsIgnoreCase("pattern")){
                                                                                String accclassName=sourceChild.getAttributes().getNamedItem("class").getNodeValue();
                                                                                packagedescrString.append(".pattern(\"").append(accclassName).append("\")");

                                                                                NodeList accpatChildChildren = sourceChild.getChildNodes();
                                                                                for (int accpatChildIndex=0;accpatChildIndex<accpatChildChildren.getLength();accpatChildIndex++){
                                                                                    Node accpatChildChild = accpatChildChildren.item(accpatChildIndex);

                                                                                    if(accpatChildChild.getNodeName().equalsIgnoreCase("constraint")){
                                                                                        packagedescrString.append(".constraint(\"").append(accpatChildChild.getFirstChild().getNodeValue()).append("\")");
                                                                                    }



                                                                                    if(accpatChildChild.getNodeName().equalsIgnoreCase("bind")){

                                                                                        packagedescrString.append(".bind(");

                                                                                        NodeList bindChildren = accpatChildChild.getChildNodes();
                                                                                        for(int j=0;j<bindChildren.getLength();j++){
                                                                                            Node currBindChild = bindChildren.item(j);
                                                                                            if(currBindChild.getNodeType()==Node.ELEMENT_NODE){
                                                                                                if(currBindChild.getNodeName().equalsIgnoreCase("var")){
                                                                                                    packagedescrString.append("\"").append(currBindChild.getFirstChild().getNodeValue()).append("\"");
                                                                                                }
                                                                                                else if (currBindChild.getNodeName().equalsIgnoreCase("value")){
                                                                                                    packagedescrString.append(",").append("\"").append(currBindChild.getFirstChild().getNodeValue()).append("\"");
                                                                                                    packagedescrString.append(",").append("false");

                                                                                                }
                                                                                            }
                                                                                        }
                                                                                        packagedescrString.append(")");
                                                                                    }
                                                                                }

                                                                                packagedescrString.append(".end()");
                                                                            }
                                                                        }
                                                                        packagedescrString.append(".end()");
                                                                    }
                                                                    else if(accumChild.getNodeName().equalsIgnoreCase("function")){

                                                                    }
                                                                }






                                                            }


                                                        }


                                                        packagedescrString.append(".end()");
                                                    }

                                                }
                                            }

                                            packagedescrString.append(".end()");
                                        }
                                    }
                                }
                                packagedescrString.append(".end()");
                            }else if (currRuleNode.getNodeName().equalsIgnoreCase("rhs")){
                                packagedescrString.append(".rhs(");
                                packagedescrString.append("\"Action vAction = new Action()");
                                String id = null;
                                NodeList rhsChildren = currRuleNode.getChildNodes();
                                for(int index=0;index<rhsChildren.getLength();index++){
                                    Node currrhsNode = rhsChildren.item(index);
                                    if(currrhsNode.getNodeType()==Node.ELEMENT_NODE){
                                        if(currrhsNode.getNodeName().equalsIgnoreCase("actionwrapper")){
                                            id = currrhsNode.getFirstChild().getNodeValue();
                                            packagedescrString.append("\\n"+id+".action=vAction");
                                        }else if(currrhsNode.getFirstChild() !=null) {
                                            packagedescrString.append("\\nvAction.").append(currrhsNode.getNodeName()).append("=\\\"" + currrhsNode.getFirstChild().getNodeValue() + "\\\"");
                                        }
                                    }
                                }
                                packagedescrString.append("\")");
                            }
                        }
                        packagedescrString.append(".end()");
                    }


                }

            }

            packagedescrString.append(".getDescr()");
            excuteMethodBuilder.addStatement(packagedescrString.toString(), PackageDescr.class, DescrFactory.class);
            excuteMethodBuilder.addStatement("return new $T().dump( pkg )", DrlDumper.class);

            System.out.println(generatedClassName);
            TypeSpec classFile = TypeSpec.classBuilder(generatedClassName)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(main)
                    .addMethod(excuteMethodBuilder.build())
                    .addField(drlFilePathBuilder.build())
                    .build();

            JavaFile javaFile = JavaFile.builder("com.rulegenerator.generated", classFile)
                    .build();
            String sourceFileDirectory = "src"+ File.separator+"main"+File.separator+"java";
            File generatedFile = new File(sourceFileDirectory+File.separator+"com"+File.separator+"rulegenerator"+File.separator+"generated"+File.separator+generatedClassName+".java");
            javaFile.writeTo(System.out);
            File sourceDirectory = new File(sourceFileDirectory);
            //generatedFileDiretory.mkdirs();
            if(generatedFile.exists()){
                generatedFile.delete();
            }

            File sourcePath = new File(ruleCreatorProjectBasePath+relativeSourcePath);//"/Users/sinchan/Documents/RuleCreator/src/main/java"
            javaFile.writeTo(sourcePath);



            String generatedFileName = javaFile.toJavaFileObject().getName();
            String generatedFilePath = sourcePath + File.separator+generatedFileName;
            System.out.println(generatedFilePath);
            String generatedClassFile = generatedFileName.substring(0,generatedFileName.indexOf(".java")).replaceAll(File.separator,".");
            System.out.println(generatedClassFile);

            final long submitTime = System.nanoTime();

            Runnable runner = new Runnable() {

                private String jobName;

                @Override
                public void run() {
                    jobName = generatedFile+"-"+submitTime;
                    boolean isSuccess = false;
                    try {
                        int exitValue = runProcess("pwd");
                        if (exitValue != 0) {
                            throw new RuleCreationException("Failed to run pwd command");
                        }
                        System.out.println("**********");
                        exitValue = runProcess("mvn install -f " + ruleCreatorProjectBasePath + "pom.xml");
                        if (exitValue != 0) {
                            throw new RuleCreationException("Failed to run mvn install");
                        }
                        System.out.println("**********");
                        exitValue = runProcess("mvn exec:java -f " + ruleCreatorProjectBasePath + "pom.xml -Dexec.mainClass=" + generatedClassFile);
                        if (exitValue != 0) {
                            throw new RuleCreationException("Failed to run mvn exec for class:" + generatedClassFile);
                        } else {
                            isSuccess = true;
                        }
                    }catch (Exception e){
                        isSuccess = false;
                    }
                }
            };

            taskExecutor.execute(runner);

            jobId=generatedFile+"-"+submitTime;

        }catch (Exception e){
            e.printStackTrace();
            throw new RuleCreationException("Failed while rule creation due to :" + e.getMessage(),e);

        }

        return jobId;
    }


    private void printLines(String cmd, InputStream ins) throws Exception {
        String line = null;
        BufferedReader in = new BufferedReader(
                new InputStreamReader(ins));
        while ((line = in.readLine()) != null) {
            System.out.println(cmd + " " + line);
        }
    }

    private int runProcess(String command) throws Exception {
        Process pro = Runtime.getRuntime().exec(command);
        printLines(command + " stdout:", pro.getInputStream());
        printLines(command + " stderr:", pro.getErrorStream());
        pro.waitFor();
        System.out.println(command + " exitValue() " + pro.exitValue());
        return pro.exitValue();
    }

}
