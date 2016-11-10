package de.dkt.eservices.eweka.modules.arffgeneration;

import com.hp.hpl.jena.rdf.model.Model;

public interface ARFFGenerator {

	public String generateARFF(Model inModel,String dataSetName);
	
}
