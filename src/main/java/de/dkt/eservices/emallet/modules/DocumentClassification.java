package de.dkt.eservices.emallet.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.InfoGain;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Label;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.Labeling;
import cc.mallet.types.PerLabelInfoGain;
import de.dkt.common.filemanagement.FileFactory;
import de.dkt.eservices.erattlesnakenlp.linguistic.TweetCleaner;
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
	        
	        //System.out.println(classifier.getInstancePipe());
	        ImportData importData = new ImportData("vector",language);
	        InstanceList testing = new InstanceList(classifier.getInstancePipe());
//	        InstanceList testing = new InstanceList(classifier.getInstancePipe());
	        //System.out.println("DEBUG: " + inputText);
	        testing.addThruPipe(new Instance(inputText, classifier.getLabelAlphabet().iterator().next(), "test instance", null));
	        
	        String output="";
	        
	        //System.out.println(testing.get(0));

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
	        		//System.out.print(labeling.getLabelAtRank(rank) + ":" +
	        			//	labeling.getValueAtRank(rank) + " ");
	        	}
	        	//System.out.println();
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

	public static Double[] evaluate(Classifier classifier, File file, PrintWriter debugOut, ArrayList<String> allClasses) throws IOException {

        InstanceList testInstances = new InstanceList(classifier.getInstancePipe());
        CsvIterator reader =
            new CsvIterator(new FileReader(file),
                            "(\\w+)\\s+(\\w+)\\s+(.*)",
                            3, 2, 1);  // (data, label, name) field indices               

        testInstances.addThruPipe(reader);
        Trial trial = new Trial(classifier, testInstances);

        // why does java not allow returning multiple values? :P
        double totalP = 0.0;
        double totalR = 0.0;
        double totalF = 0.0;
        for (String cl : allClasses){
        	totalP += trial.getPrecision(cl);
        	totalR += trial.getRecall(cl);
        	totalF += trial.getF1(cl);
        }
        
//        Double averagePrecision = (trial.getPrecision("none") + trial.getPrecision("sexism") + trial.getPrecision("racism")) / 3;
//        Double averageRecall = (trial.getRecall("none") + trial.getRecall("sexism") + trial.getRecall("racism")) / 3;
//        Double averageF1 = (trial.getF1("none") + trial.getF1("sexism") + trial.getF1("racism")) / 3;
//        
        double averagePrecision = totalP / allClasses.size();
        double averageRecall = totalR / allClasses.size();
        double averageF1 = totalF / allClasses.size();
        
        Double[] d = {trial.getAccuracy(), averagePrecision, averageRecall, averageF1};
        
//        debugOut.println("==========================================");
//        debugOut.println("Features: " + classifier.getPerClassFeatureSelection());
//        debugOut.println("==========================================");
        
        return d;
    }
	
	
//	public static double classifyAndEvaluateFile(String testFilePath, String modelPath, String modelName, String language, PrintWriter debugout){
//		
//		double score = 0;
//		try {
//			Set<String> allClasses = new HashSet<String>();
//			allClasses.add("racism");
//			allClasses.add("sexism");
//			allClasses.add("none");
//			Path filePath = new File(testFilePath).toPath();
//			List<String> testLines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
//			HashMap<Integer, String> id2class = new HashMap<Integer, String>();
//			HashMap<Integer, String> id2content = new HashMap<Integer, String>();
//			for (String line : testLines){
//				String[] parts = line.split(" ");
//				int id = Integer.parseInt(parts[0]);
//				String cl = parts[1];
//				//allClasses.add(cl);
//				String content = "";
//				for (int i = 2; i < parts.length; i++){
//					content += " " + parts[i];
//				}
//				id2class.put(id, cl);
//				id2content.put(id,  content);
//			}
//			
//			double correct = 0;
//			HashMap<String, Double> truePosMap = new HashMap<String, Double>();
//			HashMap<String, Double> falsePosMap = new HashMap<String, Double>();
//			HashMap<String, Double> falseNegMap = new HashMap<String, Double>();
//			
//			for (int id : id2class.keySet()){
//				String realClass = id2class.get(id);
//				String content = id2content.get(id);
//				String cl = classifyString(content, modelPath, modelName, language);
//				
//				// CORRECT (either true positive or true negative
//				if (cl.equalsIgnoreCase(realClass)){
//					// true negative
////					if (cl.equalsIgnoreCase("none")){
////						//pass
////					}
////					// true positive
////					else{
//						double tp = (truePosMap.containsKey(cl) ? truePosMap.get(cl) + 1 : 1);
//						truePosMap.put(cl, tp);
////					}
//				}
//				// FALSE
//				else {
//					// false positive
//					if (realClass.equalsIgnoreCase("none")){
//						double fp = (falsePosMap.containsKey(cl) ? falsePosMap.get(cl) + 1 : 1);
//						falsePosMap.put(cl, fp);
//					}
//					// false negative
//					else {
//						double fn = (falseNegMap.containsKey(cl) ? falseNegMap.get(cl) + 1 : 1);
//						falseNegMap.put(cl, fn);
//					}
//				}
//				
//				
//				
////				if (realClass.equalsIgnoreCase(cl)){
////					correct += 1;
////				}
//				
//			}
//			
//			HashMap<String, Double> precisionMap = new HashMap<String, Double>();
//			HashMap<String, Double> recallMap = new HashMap<String, Double>();
//			
//			for (String c : allClasses){
//				debugout.println("Class:" + c);
//				debugout.println("tp:" + (truePosMap.containsKey(c) ? truePosMap.get(c) : "ZERO"));
//				debugout.println("fp:" + (falsePosMap.containsKey(c) ? falsePosMap.get(c) : "ZERO"));
//				debugout.println("fn:" + (falseNegMap.containsKey(c) ? falseNegMap.get(c) : "ZERO"));
//				debugout.println("\n");
//			}
//			
//			// p = tp / (tp + fp)
//			// r = tp / (tp + fn)
//			for (String c : allClasses){
//				double p = (truePosMap.get(c) / (truePosMap.get(c) + (falsePosMap.containsKey(c) ? falsePosMap.get(c) : 0)));
//				debugout.println("p for c:" + p + "|" + c);
//				precisionMap.put(c,  p);
//				double r = (truePosMap.get(c) / (truePosMap.get(c) + (falseNegMap.containsKey(c) ? falseNegMap.get(c) : 0)));
//				debugout.println("r for c:" + r + "|" + c);
//				recallMap.put(c, r);
//			}
//			
//			double pt = 0;
//			for (String c : precisionMap.keySet()){
//				pt += precisionMap.get(c);
//			}
//			double averagePrecision = pt / precisionMap.keySet().size();
//			
//			double rt = 0;
//			for (String c : recallMap.keySet()){
//				rt += recallMap.get(c);
//			}
//			double averageRecall = rt / recallMap.keySet().size();
//			// f = 2 * ((p * r) / (p + r))
//			double f = 2 * ((averagePrecision * averageRecall) / (averagePrecision + averageRecall));
//			score = f;
//			debugout.println("p:" + averagePrecision);
//			debugout.println("r:" + averageRecall);
//			debugout.println("f:" + f);
//			debugout.println("\n+++++++++++++++++++++++++++++++++++++++++++++++++\n");
////			score = correct / (double)id2class.size();
//			
//			
//			
//			
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return score;
//		
//	}
	
	public static ArrayList<String> cleanData(String filePath){
		
		ArrayList<String> clean = new ArrayList<String>();
		try {
			Path p = new File(filePath).toPath();
			List<String> flines = Files.readAllLines(p, StandardCharsets.UTF_8);
			for (String line : flines){
				String[] parts = line.split(" ");
				int id = Integer.parseInt(parts[0]);
				String cl = parts[1];
				String content = "";
				for (int i = 2; i < parts.length; i++){
					content += " " + parts[i];
				}
				if (content.matches("^\\s+\\w+\t.*")){
					//System.out.println("Old content:" + content);
					content = content.replaceAll("^\\s+\\w+\t", "");
					content = TweetCleaner.clean(content);
					
					//System.out.println("New content:" + content);
				}
				else{
					//System.out.println("org content:" + content);
				}
				clean.add(id + " " + cl + " " + content);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return clean;
	}
	
	public static HashMap<String, List<String>> splitDataRandom(String filePath){

	    HashMap<String, List<String>> returnMap = new HashMap<String, List<String>>();
		try {
			Path p = new File(filePath).toPath();
			List<String> flines = Files.readAllLines(p);
			List<String> cleanLines = new ArrayList<String>();
			for (String string : flines) {
				cleanLines.add(string);//TweetCleaner.clean(string));
			}
			flines = cleanLines;
			int n = (int)(flines.size() * 0.9);
			Collections.shuffle(flines);
		    List<String> training = flines.subList(0, n);
		    List<String> test = new ArrayList<String>();
		    for (String s : flines){
		    	if (!training.contains(s)){// this may be a bit slow, but no show-stopper...
		    		test.add(s);
		    	}
		    }

		    returnMap.put("training", training);
		    returnMap.put("test", test);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    return returnMap;
	}
	
	private static <K, V> Map<K, V> sortByValue(Map<K, V> map) {
	    List<Entry<K, V>> list = new LinkedList<>(map.entrySet());
	    Collections.sort(list, new Comparator<Object>() {
	        @SuppressWarnings("unchecked")
	        public int compare(Object o1, Object o2) {
	            return ((Comparable<V>) ((Map.Entry<K, V>) (o2)).getValue()).compareTo(((Map.Entry<K, V>) (o1)).getValue());
	        }
	    });

	    Map<K, V> result = new LinkedHashMap<>();
	    for (Iterator<Entry<K, V>> it = list.iterator(); it.hasNext();) {
	        Map.Entry<K, V> entry = (Map.Entry<K, V>) it.next();
	        result.put(entry.getKey(), entry.getValue());
	    }

	    return result;
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

//		ArrayList<String> cleanTrainData = cleanData("C:\\Users\\pebo01\\Desktop\\AWL2017\\combinedTrainData.txt");
//		PrintWriter cleanDataWriter = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\AWL2017\\combinedTrainDataClean2.txt"));
//		for (String s : cleanTrainData){
//			cleanDataWriter.println(s);
//		}
//		cleanDataWriter.close();
//		
//		ArrayList<String> cleanTestData = cleanData("C:\\Users\\pebo01\\Desktop\\AWL2017\\combinedTestData.txt");
//		PrintWriter cleanDataWriter2 = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\AWL2017\\combinedTestDataClean2.txt"));
//		for (String s : cleanTestData){
//			cleanDataWriter2.println(s);
//		}
//		cleanDataWriter2.close();
//		
//		System.exit(1);
		
		//String[] algorithms = {"balancedwinnow", "c45", "maxentrange", "maxent", "maxentge", "maxentpr", "mcmaxent", "bayesem", "bayes", "winnow"}; // some algorithms crashed on my training set, so leaving those out...
		String[] algorithms = {"c45", "maxent", "mcmaxent", "bayesem", "bayes", "winnow"}; // c45 takes a looooong time
		//String[] algorithms = {"maxent"};
		
		
		PrintWriter out = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug.txt"));
		PrintWriter debugOut = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug2.txt"));
		
		
		int max = 10;
		for (String alg : algorithms){
			Double totalPrecision = 0.0;
			Double totalRecall = 0.0;
			Double totalF1 = 0.0;
			Double totalAccuracy = 0.0;
			HashMap<String, HashMap<String, Double>> featureMap = new HashMap<String, HashMap<String, Double>>(); // WARNING: this only works for single algorithm executions (overwrites every time)
			
			try{
				for (int i = 0; i < max; i++) {
					//HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\naacl_id_class_text.tsv");
					HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\germanHatespeechRating.tsv");
					ArrayList<String> allClasses = new ArrayList<String>();
					//HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\annatationSemevalForMalletBinary.tsv");
					String tempTrainPath = "C:\\Users\\pebo01\\Desktop\\AWL2017\\tempTrain.txt";
					String tempTestPath = "C:\\Users\\pebo01\\Desktop\\AWL2017\\tempTest.txt";
					PrintWriter tempTrain = new PrintWriter(new File(tempTrainPath));
					PrintWriter tempTest = new PrintWriter(new File(tempTestPath));
					List<String> trainLines = data.get("training");
					List<String> testLines = data.get("test");
					for (String s : trainLines) {
						tempTrain.println(s);
						String[] parts = s.split(" ");
						allClasses.add(parts[1]);
					}
					tempTrain.close();
					for (String s : testLines) {
						tempTest.println(s);
					}
					tempTest.close();
					DocumentClassification.trainClassifier("C:\\Users\\pebo01\\Desktop\\AWL2017\\tempTrain.txt", "C:\\Users\\pebo01\\workspace\\e-Clustering\\src\\main\\resources\\trainedModels\\documentClassification", "tweetExperimentingModel", "en", alg);
					Classifier classifier;
					File modelFile = FileFactory.generateFileInstance(modelsDirectory + "en" + "-" + "tweetExperimentingModel" + ".EXT");
					ObjectInputStream ois = new ObjectInputStream(new FileInputStream(modelFile));
					classifier = (Classifier) ois.readObject();
					ois.close();
					File testFile = FileFactory.generateFileInstance(tempTestPath);
					
					
					
					InstanceList testInstances = new InstanceList(classifier.getInstancePipe());
			        CsvIterator reader = new CsvIterator(new FileReader(tempTrainPath), "(\\w+)\\s+(\\w+)\\s+(.*)", 3, 2, 1);           
			        testInstances.addThruPipe(reader);
					PerLabelInfoGain plig = new PerLabelInfoGain (testInstances);
					Alphabet alpha = classifier.getAlphabet();
					LabelAlphabet la = classifier.getLabelAlphabet();
					//debugOut.println("debugging label numbers: " + la.size());
					for (int q = 0 ; q < la.size(); q++){ // this loops through all classes (q == index of class)
						//debugOut.println("Class: " + la.lookupLabel(q));
						String cl = la.lookupLabel(q).toString();
						HashMap<String, Double> im = (featureMap.containsKey(cl) ? featureMap.get(cl) : new HashMap<String, Double>());
						for (int j = 0; j < alpha.size(); j++){ // this gets the top-n ranked labels (the docs here are quite concise... so not sure if this is really what I want) http://mallet.cs.umass.edu/api/cc/mallet/types/RankedFeatureVector.html#getIndexAtRank(int)
							int alphaId = plig.getInfoGain(q).getIndexAtRank(j);
							String label = (String)alpha.lookupObject(alphaId);
							double d = (im.containsKey(label) ? im.get(label) + plig.getInfoGain(q).getValueAtRank(j) : plig.getInfoGain(q).getValueAtRank(j));
							im.put(label,  d);
							//debugOut.println(cl + "|" + j + "\t" + plig.getInfoGain(q).getValueAtRank(j) + "\t" + label); // TODO: put the score in the hashMap structure!
						}
						featureMap.put(cl, im);
						//debugOut.println("===============");
					}
					
					//debugOut.println("================================");
					Double[] scores = evaluate(classifier, testFile, debugOut, allClasses);
					totalAccuracy += scores[0];
					totalPrecision += scores[1];
					totalRecall += scores[2];
					totalF1 += scores[3];
				}
				
				for (String l : featureMap.keySet()){
					debugOut.println("Class: " + l);
					HashMap<String, Double> im = featureMap.get(l);
					Map<String, Double> sortedMap = sortByValue(im);
					int q = 0;
					for (String s : sortedMap.keySet()) {
						double actualValue = sortedMap.get(s) / max;
						debugOut.println(s + "\t" + sortedMap.get(s));
						q ++;
						if (q == 10){
							break;
						}
					}
					debugOut.println("======================");
				}

			}
			catch (Exception e){
				e.printStackTrace();
				System.exit(1);
			}
			Double p = totalPrecision / max;
			Double r = totalRecall / max;
			Double f = totalF1 / max;
			Double a = totalAccuracy / max;
			
			out.println("Algorithm: " + alg);
			out.println("\tAccuracy: " + a);
			out.println("\tPrecision: " + p);
			out.println("\tRecall: " + r);
			out.println("\tF1: " + f);
			out.print("==========================\n");
		}
		

		out.close();
		debugOut.close();

	}
}
