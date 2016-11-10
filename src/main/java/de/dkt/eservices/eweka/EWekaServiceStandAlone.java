package de.dkt.eservices.eweka;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.json.JSONObject;
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
import eu.freme.common.conversion.rdf.RDFConstants;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.ExternalServiceFailedException;
import eu.freme.common.rest.BaseRestController;
import eu.freme.common.rest.NIFParameterSet;

@RestController
public class EWekaServiceStandAlone extends BaseRestController{
    
	Logger logger = Logger.getLogger(EWekaServiceStandAlone.class);

	@Autowired
	EWekaService service;
		
	@Autowired
	RDFConversionService rdfConversionService;

	@RequestMapping(value = "/e-weka/testURL", method = { RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> testURL(
			@RequestParam(value = "preffix", required = false) String preffix,
			@RequestBody(required = false) String postBody) throws Exception {

	    HttpHeaders responseHeaders = new HttpHeaders();
	    responseHeaders.add("Content-Type", "text/plain");
	    ResponseEntity<String> response = new ResponseEntity<String>("The restcontroller is working properly", responseHeaders, HttpStatus.OK);
	    return response;
	}
	
	@RequestMapping(value = "/e-clustering/generateClusters", method = {RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> documentClustering(
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

			@RequestParam(value = "option", required = false) String option,
			@RequestParam(value = "encodingG", required = false) String encoding,
			@RequestParam(value = "language", required = false) String language,
			@RequestParam(value = "algorithm", required = false) String algorithm,
			@RequestBody(required = false) String postBody) throws Exception {
		
//		System.err.println(postBody);
		try {
	        MultipartFile file1 = null;
        	String text ="";
			if (request instanceof MultipartHttpServletRequest){
		           MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
		           file1 = multipartRequest.getFile("inputFile");
		   		if(file1==null){
	    			String msg = "No file received in request";
	    			logger.error(msg);
	    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", msg, 
	    					"", "Exception", msg, "");
	    			throw new BadRequestException(msg);
				}
		        if (!file1.isEmpty()) {
	        		String fileContent = "";
		        	try {
		        		BufferedReader br = new BufferedReader(new InputStreamReader(file1.getInputStream(), "UTF-8"));
		        		String line = br.readLine();
		        		while(line!=null){
		        			fileContent += line+"\n";
		        			line = br.readLine();
		        		}
		        		br.close();
		        	} catch (Exception e) {
		    			String msg = "Fail at reading input file.";
		    			logger.error(msg);
		    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", msg, 
		    					"", "Exception", msg, "");
		    			throw new BadRequestException(msg);
		        	}
		        	text = fileContent;
		        } else {
	    			String msg = "The given file was empty.";
	    			logger.error(msg);
	    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", msg, 
	    					"", "Exception", msg, "");
	    			throw new BadRequestException(msg);
		        }
	        }
			else{
				if(input!=null){
					text = input;
				}
				else if(postBody!=null){
					text = postBody;
				}
				else{
	    			String msg = "No input found: nor file, neither input, neither body content.";
	    			logger.error(msg);
	    			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", msg, 
	    					"", "Exception", msg, "");
	    			throw new BadRequestException(msg);

				}
//				text = new String(bytes, "UTF-8");
			}
//	   		//File tmpFile = FileFactory.generateOrCreateFileInstance(tmpFolder + tmpFileName);
//	   		File tmpFile = File.createTempFile("temp", Long.toString(System.nanoTime())+".arff");
//	   		//System.out.println("DEBUG: "+tmpFile.getAbsolutePath());
//
//	   		
//        	try {
//        		BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(tmpFile));
//        		stream.write(bytes);
//        		stream.close();
//        	} catch (Exception e) {
//        		throw new BadRequestException("Fail at uploading the file.");
//        	}
//        	String path = tmpFile.getAbsolutePath();
            JSONObject outObject;
            outObject = service.generateClusters("content", text, algorithm, language);
            
            String result = outObject.toString(1);
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-Clustering/generateClusters", "Success", "", "Exception", "", "");

//            if(option.equalsIgnoreCase("normal")){
//                outObject = service.generateClusters("content", text, algorithm, language);
//            	result = outObject.toString();
//            }
//            else if(option.equalsIgnoreCase("loopback")){
//            	result = text;
//            }
//            else if(option.equalsIgnoreCase("testDataLoader")){
////    			Instances isTrainingSet = DataLoader.loadDataFromString(text);
//    			InputStream is = null;
//    			if(encoding!=null){
//    				is = new ByteArrayInputStream( text.getBytes(encoding) );
//    			}
//    			else{
//    				is = new ByteArrayInputStream( text.getBytes() );
//    			}
//    			Instances data1 = DataSource.read(is);
//    			for (int j = 0; j < data1.numInstances(); j++) {
//    				result += "label: "+data1.attribute(j).name()+"\n";
//				}
//            }
//            else if(option.equalsIgnoreCase("testDataLoader2")){
////    			Instances isTrainingSet = DataLoader.loadDataFromString(text);
//    			InputStream is = null;
//    			BufferedReader br = null;
//    			if(encoding!=null){
//    				is = new ByteArrayInputStream( text.getBytes(encoding) );
//    				br = new BufferedReader(new InputStreamReader(is,encoding));
//    			}
//    			else{
//    				is = new ByteArrayInputStream( text.getBytes() );
//    				br = new BufferedReader(new InputStreamReader(is));
//    			}
//    			Instances data1 = new Instances(br);
//    			for (int j = 0; j < data1.numInstances(); j++) {
//    				result += "label: "+data1.attribute(j).name()+"\n";
//				}
//            }
//            else if(option.equalsIgnoreCase("inputstream")){
//    			InputStream is = null;
//    			if(encoding!=null){
//    				is = new ByteArrayInputStream( text.getBytes(encoding) );
//    			}
//    			else{
//    				is = new ByteArrayInputStream( text.getBytes() );
//    			}
//    			byte[] bb = new byte[50000];
//    			is.read(bb, 0, 50000);
//    			result = new String(bb);
//            }
//            else if(option.equalsIgnoreCase("inputstream2")){
//    			InputStream is = null;
//    			BufferedReader br = null;
//    			if(encoding!=null){
//    				is = new ByteArrayInputStream( text.getBytes(encoding) );
//    				br = new BufferedReader(new InputStreamReader(is,encoding));
//    			}
//    			else{
//    				is = new ByteArrayInputStream( text.getBytes() );
//    				br = new BufferedReader(new InputStreamReader(is));
//    			}
//    			String line = br.readLine();
//    			while(line!=null){
//    				result += line + "\n";
//    				line = br.readLine();
//    			}
//            }
            
            HttpHeaders responseHeaders = new HttpHeaders();
//			responseHeaders.add("Content-Type", "text/plain; charset=utf-8");
			ResponseEntity<String> response = new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
			return response;
		} catch (BadRequestException e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		} catch (ExternalServiceFailedException e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		}
	}
	
	@RequestMapping(value = "/e-clustering/clusterCollection", method = {RequestMethod.POST, RequestMethod.GET })
	public ResponseEntity<String> clusterCollection(
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
			@RequestParam(value = "algorithm", required = false) String algorithm,
			@RequestParam(value = "arffGeneratorType", required = false) String arffGeneratorType,
			@RequestParam(value = "arffDataSetName", required = false) String arffDataSetName,
			@RequestBody(required = false) String postBody) throws Exception {
		try {
			if(input==null || input.equalsIgnoreCase("")){
				input=postBody;
				if(input==null || input.equalsIgnoreCase("")){
					String msg = "No input found: nor file, neither input, neither body content.";
					logger.error(msg);
					InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/clusterCollection", msg, "", "Exception", msg, "");
					throw new BadRequestException(msg);
				}
			}
	        NIFParameterSet nifParameters = this.normalizeNif(input, informat, outformat, postBody, acceptHeader, contentTypeHeader, prefix);
	        Model inModel = null;
	        if (nifParameters.getInformat().equals(RDFConstants.RDFSerialization.PLAINTEXT)) {
				rdfConversionService.plaintextToRDF(inModel, nifParameters.getInput(),language, nifParameters.getPrefix());
	        } else {
	            inModel = rdfConversionService.unserializeRDF(nifParameters.getInput(), nifParameters.getInformat());
	        }
            JSONObject outObject;
            outObject = service.clusterNIF(inModel, algorithm, language, arffGeneratorType, arffDataSetName);
            String result = outObject.toString(1);
//	        Model outputModel = service.processDocumentsCollection(inModel, hyperlinkingType, granularity,limit);
//			String nifOutput = NIFReader.model2String(outputModel, nifParameters.getOutformat());
			HttpHeaders responseHeaders = new HttpHeaders();
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "usage", "e-Clustering/generateClusters", "Success", "", "Exception", "", "");
			return new ResponseEntity<String>(result, responseHeaders, HttpStatus.OK);
		} catch (Exception e) {
			logger.error("EXCEPTION: "+e.getMessage());
			InteractionManagement.sendInteraction("dkt-usage@"+request.getRemoteAddr(), "error", "e-Clustering/generateClusters", e.getMessage(), "", "Exception", e.getMessage(), "");
			throw e;
		}
	}

	
}
