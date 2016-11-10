package de.dkt.eservices.eweka.modules;

import java.io.File;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import de.dkt.common.filemanagement.FileFactory;
import de.dkt.eservices.eweka.EWekaService;
import eu.freme.common.exception.ExternalServiceFailedException;
import weka.clusterers.EM;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 * @author Julian Moreno Schneider - julian.moreno_schneider@dfki.de
 *
 */
public class EMClustering {

	static Logger logger = Logger.getLogger(EWekaService.class);

	private EMClustering() {
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
//			EMClustering.trainModelAndClusterInstances(file, 0, "em", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/weka/ACLRuns/", "test1", "de");
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

			String[] options = new String[2];
			options[0] = "-R";                                    // "range"
			options[1] = "1";                                     // first attribute
			Remove remove = new Remove();                         // new instance of filter
			remove.setOptions(options); 
			 
//			remove.setAttributeIndices("0-0");
			remove.setInputFormat(isTrainingSet);
			isTrainingSet = Filter.useFilter(isTrainingSet, remove);

			Instances labeled = new Instances(isTrainingSet);

			EM em = new EM();
			String[] options2 = new String[4];
			options2[0] = "-I";                 // max. iterations
			options2[1] = "100";
			options2[2] = "-N";                 // max. iterations
			options2[3] = "5";
//			em.setOptions(options2);     // set the options
			em.buildClusterer(isTrainingSet);

			double [][][] arr3 = em.getClusterModelsNumericAtts();
//			double upperthreshold = 0.99;
			double threshold = 0;

			JSONObject obj = new JSONObject();
			JSONObject joResults = new JSONObject();
			joResults.put("numberClusters", em.getNumClusters());
			
			System.out.println(arr3.length);
			JSONObject joDocuments = new JSONObject();
			for (int i = 0; i < arr3.length; i++) {
				
				SortedAttributesList sal = new SortedAttributesList();

//				System.out.println("-----------CLUSTER "+i+"----------");
				JSONObject resultJSON = new JSONObject();
				resultJSON.put("clusterId", i+1);
				double [][] d1 = arr3[i];
				JSONObject objEntities = new JSONObject();
				int counter = 1;
				for (int j = 0; j < d1.length; j++) {
					if(d1[j][0]>threshold){
						JSONObject objE = new JSONObject();
//						System.out.print("\t"+labeled.attribute(j).name());
//						System.out.println("\t"+"["+d1[j][0]+"--"+d1[j][1]+"--"+d1[j][2]+"]");
//						System.out.println("\t"+"["+d1[j][0]+"]");
						
						sal.addInstance(new EntityValuePair(labeled.attribute(j).name(), d1[j][0]));

						objE.put("label", labeled.attribute(j).name());
						objE.put("meanValue", d1[j][0]);
						objEntities.put("entity"+counter, objE);
						counter++;
					}
				}
				resultJSON.put("entities", objEntities);
				joDocuments.put("cluster"+(i+1),resultJSON);
				List<EntityValuePair> evplist = sal.getLimitedList(10);
				System.out.print("--");
				for (EntityValuePair evp : evplist) {
					System.out.print(""+evp.entity+"["+evp.value+"]|");
				}
				System.out.println();

			}

			joResults.put("clusters", joDocuments);
//			listResults.put(new JSONObject().put("documents", listDocuments));
//			obj.put("results", listResults);
			obj.put("results", joResults);
			return obj;
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("Error training classification module in WEKA service.", e);
			throw new ExternalServiceFailedException(e.getMessage());
		}
	}

}
