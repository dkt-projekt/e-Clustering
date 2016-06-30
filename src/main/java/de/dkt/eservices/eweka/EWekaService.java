package de.dkt.eservices.eweka;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.dkt.eservices.eweka.modules.EMClustering;
import de.dkt.eservices.eweka.modules.SimpleKMeansClustering;
import eu.freme.common.conversion.rdf.RDFConversionService;
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

    public JSONObject generateClusters(String inputFile, String algorithm, String language) 
    		throws ExternalServiceFailedException, BadRequestException {
    	try {
        	if(algorithm.equalsIgnoreCase("em")){
        		return EMClustering.trainModelAndClusterInstances(inputFile, language);
        	}
        	else if(algorithm.equalsIgnoreCase("kmeans")){
        		return SimpleKMeansClustering.trainModelAndClusterInstances(inputFile, language);
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

}
