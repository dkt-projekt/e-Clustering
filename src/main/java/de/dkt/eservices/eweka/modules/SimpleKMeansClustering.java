package de.dkt.eservices.eweka.modules;

import java.io.File;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import de.dkt.common.filemanagement.FileFactory;
import de.dkt.eservices.eweka.EWekaService;
import eu.freme.common.exception.ExternalServiceFailedException;
import weka.clusterers.Clusterer;
import weka.clusterers.SimpleKMeans;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * @author Julian Moreno Schneider - julian.moreno_schneider@dfki.de
 *
 */
public class SimpleKMeansClustering {

	static Logger logger = Logger.getLogger(EWekaService.class);

	private static Clusterer clusterer;
	
	private SimpleKMeansClustering() {
	}

//	public static void main(String[] args) {
//		try{
//			String file = "/Users/jumo04/Downloads/NERIDFVector.arff";
////			String file = "/Users/jumo04/Downloads/frequencyVector.arff";
//			
////			Clustering.trainModel(file, 0, "cobweb", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "em", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "simplekmeans", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "clope", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "dbscan", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "farthestfirst", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "filteredclusterer", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "hierarchicalclusterer", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "makedensitybasedclusterer", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "optics", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "sIB", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
////			Clustering.trainModel(file, 0, "xmeans", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
//			SimpleKMeansClustering.trainModelAndClusterInstances(file, 0, "simplekmeans", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
//		}
//		catch(Exception e){
//			e.printStackTrace();
//		}
//	}

	public static JSONObject trainModelAndClusterInstances(String mode, String trainDataFile, String language) throws ExternalServiceFailedException {
		try{
			Instances isTrainingSet = null;
			if(mode.equalsIgnoreCase("file")){
				File trainingData = FileFactory.generateFileInstance(trainDataFile);
				isTrainingSet = DataLoader.loadDataFromFile(trainingData.getAbsolutePath());
			}
			else{
				isTrainingSet = DataLoader.loadDataFromString(trainDataFile);
			}
			//System.out.println("TrainingDataFile:" + trainDataFile);
			//System.out.println("DEBUGGING training instances:" +isTrainingSet.toString());
			String[] options = new String[2];
			options[0] = "-R";                                    // "range"
			options[1] = "1";                                     // first attribute
			Remove remove = new Remove();                         // new instance of filter
			remove.setOptions(options); 
			 
			remove.setInputFormat(isTrainingSet);
			isTrainingSet = Filter.useFilter(isTrainingSet, remove);
			SimpleKMeans skm = new SimpleKMeans();
			String[] options2 = new String[4];
			options2[0] = "-I";                 // max. iterations
			options2[1] = "100";
			options2[2] = "-N";                 // max. iterations
			options2[3] = "5";
			skm.setOptions(options2);     // set the options
			double upperthreshold = 0.99;
			double threshold = 0;
			
			skm.buildClusterer(isTrainingSet);
			Instances centroids = skm.getClusterCentroids();
			
			clusterer = skm;

			JSONObject obj = new JSONObject();
			JSONObject joResults = new JSONObject();
			joResults.put("numberClusters", clusterer.numberOfClusters());
			JSONObject joDocuments = new JSONObject();
			for (int i = 0; i < centroids.numInstances(); i++) {
//				System.out.println("_---------------------------------");
				Instance inst = centroids.instance(i);
				JSONObject resultJSON = new JSONObject();
				resultJSON.put("clusterId", i+1);
				JSONObject objEntities = new JSONObject();
				int counter = 1;
				for (int j = 0; j < inst.numAttributes(); j++) {
//					if(inst.value(j)<upperthreshold && inst.value(j)>threshold){
					if(inst.value(j)>threshold){
						JSONObject objE = new JSONObject();
						Attribute at = inst.attribute(j);
						//System.out.println("DEBUGGINg at name:" + at.name());
//						System.out.println(inst.value(j));
//						System.out.println(" \t\t"+inst.toString());
						objE.put("label", at.name());
						objE.put("meanValue", inst.value(j));
						objEntities.put("entity"+counter, objE);
					}
					counter++;
				}
				resultJSON.put("entities", objEntities);
				joDocuments.put("cluster"+(i+1),resultJSON);

			}
			joResults.put("clusters", joDocuments);
			obj.put("results", joResults);
			return obj;		
		
//			// label instances
//			for (int i = 0; i < isTrainingSet.numInstances(); i++) {
//				double clsLabel = clusterer.clusterInstance(isTrainingSet.instance(i));
//				System.out.print("INSTANCE: " + instancesForId.instance(i).stringValue(0));
//				System.out.println(" " + clsLabel);
////				double clsLabel2 [] = clusterer.distributionForInstance(isTrainingSet.instance(i));
////				System.out.print("\t");
////				for (double d : clsLabel2) {
////					System.out.print(d + " ");
////				}
////				System.out.println();
//			}
//			System.out.println(clusterer.toString());

		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("Error training classification module in WEKA service.", e);
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}

}
