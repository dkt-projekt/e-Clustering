package de.dkt.eservices.emallet.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import org.apache.log4j.Logger;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import de.dkt.common.filemanagement.FileFactory;
import eu.freme.common.exception.BadRequestException;
import eu.freme.common.exception.ExternalServiceFailedException;

/**
 * @author Julian Moreno Schneider julian.moreno_schneider@dfki.de
 *
 */
public class DocumentClassification {
	
	static Logger logger = Logger.getLogger(DocumentClassification.class);

	private static String modelsDirectory = "trainedModels" + File.separator + "documentClassification" + File.separator;

	public static String classifyString(String inputText, String modelPath, String modelName, String language) throws ExternalServiceFailedException {
		if(modelPath!=null){
			if(modelPath.endsWith(File.separator)){
				modelsDirectory = modelPath;
			}
			else{
				modelsDirectory = modelPath+File.separator;
			}
		}
		
		Classifier classifier;
		try{
			File modelFile = FileFactory.generateFileInstance(modelsDirectory + language + "-" + modelName + ".EXT");
//			File modelFile = new File(modelsDirectory + language + "-" + modelName + ".EXT");
	        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (modelFile));
	        classifier = (Classifier) ois.readObject();
	        ois.close();

	        System.out.println(classifier.getInstancePipe());
	        ImportData importData = new ImportData("vector",language);
	        InstanceList testing = new InstanceList(classifier.getInstancePipe());
//	        InstanceList testing = new InstanceList(classifier.getInstancePipe());
	        System.out.println("DEBUG: " + inputText);
	        testing.addThruPipe(new Instance(inputText, classifier.getLabelAlphabet().iterator().next(), "test instance", null));
	        
	        String output="";
	        
	        System.out.println(testing.get(0));

	        Classification classification = classifier.classify(testing.get(0));
	        Labeling labeling = classification.getLabeling();
        	output = labeling.getLabelAtRank(0).toString();
	        return output;
		}
		catch(Exception e){
			e.printStackTrace();

			logger.error("failed", e);
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}
	
	public static String classifyFile(String inputDataFile, String modelPath, String modelName, String language) throws ExternalServiceFailedException {
		
		if(modelPath!=null){
			if(modelPath.endsWith(File.separator)){
				modelsDirectory = modelPath;
			}
			else{
				modelsDirectory = modelPath+File.separator;
			}
		}
		
		Classifier classifier;
		try{
			File modelFile = FileFactory.generateFileInstance(modelsDirectory + language + "-" + modelName + ".EXT");
	        ObjectInputStream ois = new ObjectInputStream (new FileInputStream (modelFile));
	        classifier = (Classifier) ois.readObject();
	        ois.close();
	        
	        CsvIterator reader = new CsvIterator(new FileReader(inputDataFile),"(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1);  // (data, label, name) field indices               

	        // Create an iterator that will pass each instance through the same pipe that was used to create the training data for the classifier.
	        Iterator<Instance> instances = classifier.getInstancePipe().newIteratorFrom(reader);
	        // Classifier.classify() returns a Classification object  that includes the instance, the classifier, and the classification results (the labeling). Here we only care about the Labeling.                                                                       
	        String output = "";
	        while (instances.hasNext()) {
	        	Labeling labeling = classifier.classify(instances.next()).getLabeling();

	        	// print the labels with their weights in descending order (ie best first)
	        	for (int rank = 0; rank < labeling.numLocations(); rank++){
	        		System.out.print(labeling.getLabelAtRank(rank) + ":" +
	        				labeling.getValueAtRank(rank) + " ");
	        	}
	        	System.out.println();
	        	output = output + ";" + labeling.getLabelAtRank(0);
	        }
	        return output;
		}
		catch(Exception e){
			e.printStackTrace();

			logger.error(e.getMessage());
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}
	
	/**
	 * 
	 * 
	 * @param inputTrainData Stream of training data
	 * @param modelName Name to be assigned to the model
	 * @return true if the model has been successfully trained
	 */
	public static String trainClassifier (String inputTrainFile, String modelPath, String modelName, String language, String algorithm) throws BadRequestException, ExternalServiceFailedException {
		if(modelPath!=null){
			if(modelPath.endsWith(File.separator)){
				modelsDirectory = modelPath;
			}
			else{
				modelsDirectory = modelPath+File.separator;
			}
		}
		try{
			ImportData id = new ImportData(language);
			File trainingFile = FileFactory.generateFileInstance(inputTrainFile);
			InstanceList instances = id.readFile(trainingFile);

			//Train the model with the training instances
			ClassifierTrainer trainer = ClassificationTrainerFactory.generateTrainer(algorithm);
			Classifier classifier;
			try{
				classifier = trainer.train(instances);
			}
			catch(Exception e){
				throw e;
//				throw new ExternalServiceFailedException("Fail at training the model: it is possible that you need to use a bigger amount of Data.");
			}
			
			//Save the generated classifier
			String modelOutputFile = modelsDirectory + language + "-" + modelName + ".EXT";
//			File outputFile = new File(modelOutputFile);
			File outputFile = FileFactory.generateOrCreateFileInstance(modelOutputFile);
			System.out.println(outputFile.getAbsolutePath());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream (outputFile));
			oos.writeObject (classifier);
			oos.close();
			
			return modelOutputFile;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}

	public static void main(String[] args) throws Exception{
		//String result = DocumentClassification.classifyString("This is the text that I need to classify", "en-sent.bin", "en");
		//System.out.println(result);
		
//		dc.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/3pc/3PC_CATEGORIES.txt", "recursos", "3pc", "de");
//		dc.classifyString("recursos/data.txt", "recursos","test1", "de");

		/**
		 * Generate models for 3pc data
		 */
//		DocumentClassification.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/3pc/3PC_CATEGORIES.txt", "recursos", "3pc_stop", "de");
		/**
		 * Generate models for condat data using categories and types as labels for classification
		 */
//		DocumentClassification.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/Condat/Condat_CATEGORIES.txt", "recursos", "condat_categories_stop", "de");
//		DocumentClassification.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/Condat/Condat_TYPES.txt", "recursos", "condat_types_stop", "de");
//		/**
//		 * Generate models for Kreuzwereker data using topics, categories and subcategories as labels for classification
//		 */
//		DocumentClassification.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/Kreuzwerker/KW_TOPICS2.txt", "recursos", "kreuzwerker_topics_stop", "de");
//		DocumentClassification.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/Kreuzwerker/KW_CATEGORIES.txt", "recursos", "kreuzwerker_categories_stop", "de");
//		DocumentClassification.trainClassifier("/Users/jumo04/Documents/DFKI/DataCollections/DKT/Kreuzwerker/KW_SUBCATEGORIES2.txt", "recursos", "kreuzwerker_subcategories_stop", "de");

		String inputText = 
				"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
				"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
				"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
				"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
				"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
				"\n" +
				"<http://dkt.dfki.de/examples/#char=0,806>\n" +
				"        a                    nif:RFC5147String , nif:String , nif:Context ;\n" +
				"        nif:beginIndex       \"0\"^^xsd:nonNegativeInteger ;\n" +
				"        nif:centralGeoPoint  \"42.17561833333333_-4.075817777777777\"^^xsd:string ;\n" +
				"        nif:endIndex         \"806\"^^xsd:nonNegativeInteger ;\n" +
				"        nif:geoStandardDevs  \"1.4446184970002673_2.202362615152696\"^^xsd:string ;\n" +
				"        nif:isString         \"1936\\n\\nCoup leader Sanjurjo was killed in a plane crash on 20 July, leaving an effective command split between Mola in the North and Franco in the South. On 21 July, the fifth day of the rebellion, the Nationalists captured the main Spanish naval base at Ferrol in northwestern Spain. A rebel force under Colonel Beorlegui Canet, sent by General Emilio Mola, undertook the Campaign of Guipuzcoa from July to September. The capture of Guipuzcoa isolated the Republican provinces in the north. On 5 September, after heavy fighting the force took Irun, closing the French border to the Republicans. On 13 September, the Basques surrendered Madrid to the Nationalists, who then advanced toward their capital, Bilbao. The Republican militias on the border of Viscaya halted these forces at the end of September.\\n\"^^xsd:string ;\n" +
				"        nif:meanDateRange    \"19360531090000_19361022090000\"^^xsd:string .\n" +
				"\n" +
				"";
		DocumentClassification.classifyString(inputText, null, "testModel", "de");
	}
}
