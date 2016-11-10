package de.dkt.eservices.emallet;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.feedback.InteractionManagement;
import de.dkt.common.filemanagement.FileFactory;
import de.dkt.common.niftools.NIFReader;
import de.dkt.common.tools.ParameterChecker;
import eu.freme.common.conversion.rdf.JenaRDFConversionService;
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.ExternalServiceFailedException;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;


@RestController
public class EMalletServiceStandAlone extends BaseRestController{
	
	Logger logger = Logger.getLogger(EMalletServiceStandAlone.class);

	@Autowired
	EMalletService service;
	
	public EMalletServiceStandAlone() {
	}
	
	@RequestMapping(value = "/e-mallet/testURL", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> testURL(
			@RequestParam(value = "preffix", required = false) String preffix,
			@RequestBody(required = false) String postBody) throws Exception {

		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.add("Content-Type", "text/plain");
		ResponseEntity<String> response = new ResponseEntity<String>("The restcontroller is working properly", responseHeaders, HttpStatus.OK);
		return response;
	}
		
	@RequestMapping(value = "/e-mallet/trainModel", method = {RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> trainModel(
			HttpServletRequest request,
			@RequestParam(value = "trainDataFile", required = false) String trainDataFile,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "modelName", required = false) String modelName,
			@RequestParam(value = "modelPath", required = false) String modelPath,
			@RequestParam(value = "analysis", required = false) String analysis,
			@RequestBody(required = false) String postBody) throws Exception {
		try {
//			ParameterChecker.checkNotNullOrEmpty(trainDataFile, "trainDataFile");
			ParameterChecker.checkInList(analysis, "topicmodelling;classification", "analysis", logger);
			ParameterChecker.checkNotNullOrEmpty(modelName, "modelName", logger);
			ParameterChecker.checkNotNullOrEmpty(language, "language", logger);
			
	        MultipartFile file = null;//= multipartRequest.getFile("file");
			if (request instanceof MultipartHttpServletRequest){
		           MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		           file = multipartRequest.getFile("trainingFile");
		   		if(file==null){
	    			String msg = "No file received in request";
	    			logger.error(msg);
	    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, 
	    					"", "Exception", msg, "");
	    			throw new BadRequestException(msg);
				}
		        if (!file.isEmpty()) {
			        trainDataFile="tmpFiles" + File.separator+file.getOriginalFilename();
	        		File tmpFile;
		        	byte[] bytes;
		        	try {
		        		tmpFile = FileFactory.generateOrCreateFileInstance(trainDataFile);
		        	} catch (Exception e) {
		    			String msg = "Fail at generating temporary file.";
		    			logger.error(msg);
		    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, 
		    					"", "Exception", msg, "");
		    			throw new BadRequestException(msg);
		        	}
		        	try {
		        		bytes = file.getBytes();
		        	} catch (Exception e) {
		    			String msg = "Fail at reading input file.";
		    			logger.error(msg);
		    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, 
		    					"", "Exception", msg, "");
		    			throw new BadRequestException(msg);
		        	}
		        	try {
		        	    FileOutputStream fos = new FileOutputStream(tmpFile); 
		        	    fos.write(bytes);
		        	    fos.close();
		        	} catch (Exception e) {
		    			String msg = "Fail at writting temporary file.";
		    			logger.error(msg);
		    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, 
		    					"", "Exception", msg, "");
		    			throw new BadRequestException(msg);
		        	}
		        } else {
	    			String msg = "The given file was empty.";
	    			logger.error(msg);
	    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, 
	    					"", "Exception", msg, "");
	    			throw new BadRequestException(msg);
		        }
	        }
			else{
    			String msg = "No training file provided: HTTPREQUEST is no multipart.";
    			logger.error(msg);
    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, "", "Exception", msg, "");
    			throw new BadRequestException(msg);
			}
            
			ParameterChecker.checkNotNullOrEmpty(trainDataFile, "trainingDataFile", logger);
			String outString;
			if(analysis.equalsIgnoreCase("topicmodelling")){
				outString = service.trainModelTopic(trainDataFile, modelPath, modelName, language);
			}
			else if(analysis.equalsIgnoreCase("classification")){
				outString = service.trainModelClassification(trainDataFile, modelPath, modelName, language);
			}
			else{
    			String msg = "The input analysis ["+analysis+"] is not supported. Only topicmodelling/classification are available.";
    			logger.error(msg);
    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", msg, 
    					"", "Exception", msg, "");
    			throw new BadRequestException(msg);
			}
            HttpHeaders responseHeaders = new HttpHeaders();
            responseHeaders.add("Content-Type", "text/plain");
    		ResponseEntity<String> response = new ResponseEntity<String>(outString, responseHeaders, HttpStatus.OK);
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-mallet/trainModel", "Success", "", "", "", "");
			return response;
		} catch (BadRequestException e) {
			logger.error(e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		} catch (ExternalServiceFailedException e) {
			logger.error(e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-mallet/trainModel", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		}
	}
	
	@RequestMapping(value = "/e-documentclassification", method = {RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> documentClassification(
			HttpServletRequest request, 
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "i", required = false) String i,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "outformat", required = false) String outformat,
			@RequestParam(value = "o", required = false) String o,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "p", required = false) String p,
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam Map<String, String> allParams,

			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "modelName", required = false) String modelName,
			@RequestParam(value = "modelPath", required = false) String modelPath,
			@RequestBody(required = false) String postBody) throws Exception {
		try {
			ParameterChecker.checkNotNullOrEmpty(modelName, "modelName", logger);
			ParameterChecker.checkNotNullOrEmpty(language, "language", logger);

			NIFParameterSet nifParameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
            String textForProcessing = null;
            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
                textForProcessing = nifParameters.getInput();
    			//rdfConversionService.plaintextToRDF(inModel, textForProcessing,language, nifParameters.getPrefix());
            } else {
            	
            	if(nifParameters.getInput()!=null){
                    textForProcessing = nifParameters.getInput();
            	}
            	else if(postBody!=null){
                    textForProcessing = postBody;
            	}
                if (textForProcessing == null) {
        			String msg = "No text to process.";
        			logger.error(msg);
        			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification", msg, 
        					"", "Exception", msg, "");
        			throw new BadRequestException(msg);
                }
            }
            Model outModel;
            outModel = service.analyzeText(textForProcessing, "documentclassification", modelPath, modelName, language, informat, outformat);
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-documentclassification", "Success", "", "", "", "");
			return createSuccessResponse(outModel, nifParameters.getOutformat());
		} catch (BadRequestException e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		} catch (ExternalServiceFailedException e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		}
	}
	
	@RequestMapping(value = "/e-documentclassification/models", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> documentclassificationModels(
			HttpServletRequest request, 
			@RequestBody(required = false) String postBody) throws Exception {

		File f = FileFactory.generateOrCreateDirectoryInstance("trainedModels" + File.separator + "documentClassification" + File.separator);
		if(f.isDirectory()){
			
			String sFiles = "";
			File [] files = f.listFiles();
			for (File ff: files) {
				sFiles += ff.getName().replaceAll("\\.EXT$", "").substring(ff.getName().indexOf('-')+1) + "\n";
				
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", "text/plain");
			ResponseEntity<String> response = new ResponseEntity<String>(sFiles, responseHeaders, HttpStatus.OK);
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-documentclassification/models", "Success", "", "", "", "");
			return response;
		}
		String msg = "Error in the model directory: it is not a directory or there are no models.";
		logger.error(msg);
		InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification/models", msg, "", "Exception", msg, "");
		throw new ExternalServiceFailedException(msg);
	}

	@RequestMapping(value = "/e-topicmodelling/models", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> topicModellingModels(
			HttpServletRequest request, 
			@RequestBody(required = false) String postBody) throws Exception {
		File f = FileFactory.generateOrCreateDirectoryInstance("trainedModels" + File.separator + "topicModelling" + File.separator);
		if(f.isDirectory()){
			String sFiles = "";
			File [] files = f.listFiles();
			for (File ff: files) {
				if(!ff.getName().contains("Alphabet")){
					sFiles += ff.getName().replaceAll("_TOPIC\\.EXT$", "").substring(ff.getName().indexOf('-')+1) + "\n";
				}
				
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.add("Content-Type", "text/plain");
			ResponseEntity<String> response = new ResponseEntity<String>(sFiles, responseHeaders, HttpStatus.OK);
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-topicmodelling/models", "Success", "", "", "", "");
			return response;
		}
		String msg = "Error in the model directory: it is not a directory or there are no models.";
		logger.error(msg);
		InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-topicmodelling/models", msg, "", "Exception", msg, "");
		throw new ExternalServiceFailedException(msg);
	}
	
	@RequestMapping(value = "/e-topicmodelling", method = {RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> topicModelling(
			HttpServletRequest request, 
			@RequestParam(value = "input", required = false) String input,
			@RequestParam(value = "i", required = false) String i,
			@RequestParam(value = "informat", required = false) String informat,
			@RequestParam(value = "f", required = false) String f,
			@RequestParam(value = "outformat", required = false) String outformat,
			@RequestParam(value = "o", required = false) String o,
			@RequestParam(value = "prefix", required = false) String prefix,
			@RequestParam(value = "p", required = false) String p,
			@RequestHeader(value = "Accept", required = false) String acceptHeader,
			@RequestHeader(value = "Content-Type", required = false) String contentTypeHeader,
            @RequestParam Map<String, String> allParams,

			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "modelName", required = false) String modelName,
			@RequestParam(value = "modelPath", required = false) String modelPath,
			@RequestBody(required = false) String postBody) throws Exception {
		try {
			ParameterChecker.checkNotNullOrEmpty(modelName, "modelName",logger);
			ParameterChecker.checkNotNullOrEmpty(language, "language",logger);

			NIFParameterSet nifParameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
            String textForProcessing = null;

            if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
                textForProcessing = nifParameters.getInput();
    			//rdfConversionService.plaintextToRDF(inModel, textForProcessing,language, nifParameters.getPrefix());
            } else {
            	
            	if(nifParameters.getInput()!=null){
                    textForProcessing = nifParameters.getInput();
            	}
            	else if(postBody!=null){
                    textForProcessing = postBody;
            	}
                if (textForProcessing == null) {
        			String msg = "No text to process.";
        			logger.error(msg);
        			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification", msg, "", "Exception", msg, "");
        			throw new BadRequestException(msg);
                }
            }
            Model outModel;
			outModel = service.analyzeText(textForProcessing, "topicmodelling", modelPath, modelName, language, informat, outformat);
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-topicmodelling", "Success", "", "", "", "");
			return createSuccessResponse(outModel, nifParameters.getOutformat());
		} catch (BadRequestException e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		} catch (ExternalServiceFailedException e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-documentclassification", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		}
	}
}
