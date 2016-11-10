package de.dkt.eservices.eweka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIFReader;
import de.dkt.eservices.eweka.modules.EMClustering;
import de.dkt.eservices.eweka.modules.SimpleKMeansClustering;
import de.dkt.eservices.eweka.modules.arffgeneration.ARFFGenerator;
import de.dkt.eservices.eweka.modules.arffgeneration.ARFFGeneratorFactory;
import eu.freme.common.conversion.rdf.RDFConversionService;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.ExternalServiceFailedException;

/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 * The whole documentation about WEKA examples can be found in ...
 *
 */
@Component
public class EWekaService {
    
	Logger logger = Logger.getLogger(EWekaService.class);

	@Autowired
	RDFConversionService rdfConversionService;

    public JSONObject generateClusters(String mode, String inputFile, String algorithm, String language) 
    		throws ExternalServiceFailedException, BadRequestException {
    	try {
        	if(algorithm.equalsIgnoreCase("em")){
        		return EMClustering.trainModelAndClusterInstances(mode, inputFile, language);
        	}
        	else if(algorithm.equalsIgnoreCase("kmeans")){
        		return SimpleKMeansClustering.trainModelAndClusterInstances(mode, inputFile, language);
        	}
        	else{
        		throw new BadRequestException("Unsupported algorithm. Only EM/KMeans are available for now.");
        	}
        } catch (BadRequestException e) {
			logger.error("EXCEPTION: "+e.getMessage());
            throw e;
    	} catch (ExternalServiceFailedException e2) {
			logger.error("EXCEPTION: "+e2.getMessage());
    		throw e2;
    	} catch (Exception e) {
			logger.error("EXCEPTION: "+e.getMessage());
    		throw new ExternalServiceFailedException(e.getMessage());
    	}
    }

	public JSONObject clusterNIF(Model inModel, String algorithm, String language, String generatorType, String dataSetName) {
		String textARFFData;
		try{
			ARFFGenerator arffGenerator = ARFFGeneratorFactory.generateARFFGenerator(generatorType);
			textARFFData = arffGenerator.generateARFF(inModel,dataSetName);
		}
		catch(Exception e){
			logger.error("EXCEPTION: "+e.getMessage());
    		throw e;
		}
		return generateClusters("text", textARFFData, algorithm, language);
	}

    public static void main(String[] args) throws Exception {
		List<Model> modelsList = new LinkedList<Model>();
    	//Get a folder with all the nif documents.
    	String folderPath = "";
    	File folder = new File(folderPath);
    	File[] filesArray = folder.listFiles();
    	for (File f2 : filesArray) {
        	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f2), "utf-8"));
        	String content = "";
        	String line = br.readLine();
        	while(line!=null){
        		content += line+"\n";
        		line = br.readLine();
        	}
        	br.close();
        	Model m = NIFReader.extractModelFromFormatString(content, RDFSerialization.TURTLE);
        	modelsList.add(m);
    	}
    	
    	//Geenrate the ARFF file for this documents depending on what we want to do:
    	String arffContentString = "";
    	
    	
    	
    	
    	
    	EWekaService service = new EWekaService();
    	
    	JSONObject obj = service.generateClusters("string", arffContentString, "em", "en");
    	System.out.println("===========================================");
    	System.out.println("===========================================");
    	System.out.println("OUTPUT: ");
    	System.out.println(obj.toString());
    	System.out.println("===========================================");
    	System.out.println("===========================================");
	}

}
