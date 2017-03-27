package de.dkt.eservices.emallet.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.Trial;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureVector;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelAlphabet;
import cc.mallet.types.Labeling;
import cc.mallet.types.PerLabelInfoGain;
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
			File outputFile = FileFactory.generateOrCreateFileInstance(modelOutputFile);
			System.out.println(outputFile.getAbsolutePath());
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream (outputFile));
			oos.writeObject (classifier);
			oos.close();

			// and also the alphabet
	        Alphabet dataAlphabet = instances.getDataAlphabet();
	        File outputAlphabetFile = FileFactory.generateOrCreateFileInstance(modelsDirectory+language+"-"+modelName+"_Alphabet.EXT");
			ObjectOutputStream oos2 = new ObjectOutputStream(new FileOutputStream (outputAlphabetFile));
			oos2.writeObject (dataAlphabet);
			oos2.close();
			
			
			
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
                            "(\\w+)\\s+(\\S+)\\s+(.*)",
                            3, 2, 1);  // (data, label, name) field indices               

        testInstances.addThruPipe(reader);
        
//        classifier.
        
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
					//content = TweetCleaner.clean(content); (de.dkt.erattlesnake.linguistic or something)
					
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
	
	public static double findDeviation(ArrayList<Double> nums) {
		double mean = findMean(nums);
		double squareSum = 0;
		for (int i = 0; i < nums.size(); i++) {
			squareSum += Math.pow(nums.get(i) - mean, 2);
		}
		return Math.sqrt((squareSum) / (nums.size() - 1));

	}
	
	public static double findMean(ArrayList<Double> nums) {
		double sum = 0;
		for (int i = 0; i < nums.size(); i++) {
			sum += nums.get(i);
		}
		return sum / nums.size();
	}


	
	private static double entropy (double pc, double pnc)
	{
		float log2 = (float)Math.log(2);
		assert (Math.abs((pc+pnc)-1) < 0.0001) : "pc="+pc+" pnc="+pnc;
		if (pc == 0 || pnc == 0)
			return (float) 0;
		else {
			float ret = (float) (- pc*Math.log(pc)/log2 - pnc*Math.log(pnc)/log2);
			assert (ret >= 0) : "pc="+pc+" pnc="+pnc;
			return ret;
		}
	}

	public static double[][] calcPerLabelInfoGains (InstanceList ilist) throws FileNotFoundException
	{
		//assert (binary);
		double[][] classFeatureCounts;
		int[] featureCounts;
		int[] classCounts;
		int numClasses = ilist.getTargetAlphabet().size();
		int numFeatures = ilist.getDataAlphabet().size();
		int numInstances = ilist.size();

		// Fill in the classFeatureCounts
		classFeatureCounts = new double[numClasses][numFeatures];
		featureCounts = new int[numFeatures];
		classCounts = new int[numClasses];
		/*
		for (int fi = 0; fi < numFeatures; fi++)
			featureCounts[fi] = 0;
		for (int ci = 0; ci < numClasses; ci++) {
			classCounts[ci] = 0;
			for (int fi = 0; fi < numFeatures; fi++)
				classFeatureCounts[ci][fi] = 0;
		}
		*/
		for (int i = 0; i < ilist.size(); i++) {
			Instance instance = ilist.get(i);
			FeatureVector fv = (FeatureVector) instance.getData();
			// xxx Note that this ignores uncertainly-labeled instances!
			int classIndex = instance.getLabeling().getBestIndex();
			classCounts[classIndex]++;
			for (int fvi = 0; fvi < fv.numLocations(); fvi++) {
				int featureIndex = fv.indexAtLocation(fvi);
				classFeatureCounts[classIndex][featureIndex]++;
				featureCounts[featureIndex]++;
				//System.out.println ("fi="+featureIndex+" ni="+numInstances+" fc="+featureCounts[featureIndex]+" i="+i);
				assert (featureCounts[featureIndex] <= numInstances)
					: "fi="+featureIndex+"ni="+numInstances+" fc="+featureCounts[featureIndex]+" i="+i;
			}
		}

		Alphabet v = ilist.getDataAlphabet();
//		if (print)
//			for (int ci = 0; ci < numClasses; ci++)
//				System.out.println (ilist.getTargetAlphabet().lookupObject(ci).toString()+"="+ci);

		// Let C_i be a random variable on {c_i, !c_i}
		// per-class entropy of feature f_j = H(C_i|f_j)
		// H(C_i|f_j) = - P(c_i|f_j) log(P(c_i|f_j) - P(!c_i|f_j) log(P(!c_i|f_j)

		// First calculate the per-class entropy, not conditioned on any feature
		// and store it in classCounts[]
		double[] classEntropies = new double[numClasses];
		for (int ci = 0; ci < numClasses; ci++) {
			double pc, pnc;
			pc = ((double)classCounts[ci])/numInstances;
			pnc = ((double)numInstances-classCounts[ci])/numInstances;
			classEntropies[ci] = entropy (pc, pnc);
		}

		// Calculate per-class infogain of each feature, and store it in classFeatureCounts[]
		for (int fi = 0; fi < numFeatures; fi++) {
			double pf = ((double)featureCounts[fi])/numInstances;
			double pnf = ((double)numInstances-featureCounts[fi])/numInstances;
			assert (pf >= 0);
			assert (pnf >= 0);
//			if (print && fi < 10000) {
//				System.out.print (v.lookupObject(fi).toString());
//				for (int ci = 0; ci < numClasses; ci++) {
//					System.out.print (" "+classFeatureCounts[ci][fi]);
//				}
//				System.out.println ("");
//			}
			//assert (sum == featureCounts[fi]);
			for (int ci = 0; ci < numClasses; ci++) {
				if (featureCounts[fi] == 0) {
					classFeatureCounts[ci][fi] = 0;
					continue;
				}
				double pc, pnc, ef;
				// Calculate the {ci,!ci}-entropy given that the feature does occur
				pc = ((double)classFeatureCounts[ci][fi]) / featureCounts[fi];
				pnc = ((double)featureCounts[fi]-classFeatureCounts[ci][fi]) / featureCounts[fi];
				ef = entropy (pc, pnc);
				// Calculate the {ci,!ci}-entropy given that the feature does not occur
				pc = ((double)classCounts[ci]-classFeatureCounts[ci][fi]) / (numInstances-featureCounts[fi]);
				pnc = ((double)(numInstances-featureCounts[fi])-(classCounts[ci]-classFeatureCounts[ci][fi])) / (numInstances-featureCounts[fi]);
				double enf = entropy(pc, pnc);
				classFeatureCounts[ci][fi] = classEntropies[ci] - (pf*ef + pnf*enf);
//				if (print && fi < 10000)
//					System.out.println ("pf="+pf+" ef="+ef+" pnf="+pnf+" enf="+enf+" e="+classEntropies[ci]+" cig="+classFeatureCounts[ci][fi]);
			}
		}

		// Print selected features
		if (1 == 1) {
			PrintWriter pwDebug = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug3.txt"));
			for (int fi = 0; fi < 100; fi++) {
				String featureName = v.lookupObject(fi).toString();
				for (int ci = 0; ci < numClasses; ci++) {
					String className = ilist.getTargetAlphabet().lookupObject(ci).toString();
					//if (classFeatureCounts[ci][fi] > .1) {
						pwDebug.write(featureName+','+className+'='+classFeatureCounts[ci][fi] + "\n");
					//}
				}
			}
			pwDebug.close();
		}
		return classFeatureCounts;
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
		//String[] algorithms = {"c45", "maxent", "mcmaxent", "bayesem", "bayes", "winnow"}; // c45 takes a looooong time
		String[] algorithms = {"c45"};
		
		
		PrintWriter out = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug.txt"));
		PrintWriter debugOut = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug2.txt"));
		
		
		int max = 10;
		for (String alg : algorithms){
			Double totalPrecision = 0.0;
			Double totalRecall = 0.0;
			Double totalF1 = 0.0;
			Double totalAccuracy = 0.0;
			HashMap<String, HashMap<String, Double>> featureMap = new HashMap<String, HashMap<String, Double>>(); // WARNING: this only works for single algorithm executions (overwrites every time)
			ArrayList<Double> ps = new ArrayList<Double>();
			ArrayList<Double> rs = new ArrayList<Double>();
			ArrayList<Double> fs = new ArrayList<Double>();
			ArrayList<Double> as = new ArrayList<Double>();
			try{
				for (int i = 0; i < max; i++) {
					//HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\naacl_id_class_text.tsv");
					//HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\germanHatespeechRating.tsv");
					ArrayList<String> allClasses = new ArrayList<String>();
					//HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\annatationSemevalForMalletBinary.tsv");
					HashMap<String, List<String>> data = splitDataRandom("C:\\Users\\pebo01\\Desktop\\AWL2017\\wikiTalkData\\wikiTalkAggressionRating2013-2015.tsv");
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
					
					ImportData id = new ImportData("en");
//					File trainingFile = FileFactory.generateFileInstance(tempTrainPath);
					InstanceList testInstances = id.readFile(testFile);
					//InstanceList testInstances = new InstanceList(classifier.getInstancePipe());
					
					Alphabet testAlpha = testInstances.getAlphabet();
					Alphabet classifierAlpha = classifier.getAlphabet();

//					InstanceList il2 = new InstanceList(testAlpha, classifierAlpha);

					CsvIterator reader = new CsvIterator(new FileReader(tempTrainPath), "(\\w+)\\s+(\\S+)\\s+(.*)", 3, 2, 1);

					testInstances.addThruPipe(reader);
					PerLabelInfoGain plig = new PerLabelInfoGain(testInstances);
					Alphabet alpha = classifier.getAlphabet();
					LabelAlphabet la = classifier.getLabelAlphabet();
					//calcPerLabelInfoGains(testInstances);

					for (int q = 0; q < la.size(); q++) { // this loops through
															// all classes (q ==
															// index of class)
						// debugOut.println("Class: " + la.lookupLabel(q));
						String cl = la.lookupLabel(q).toString();
//						System.out.println("DEBUGGING clasS:" + cl);
						HashMap<String, Double> im = (featureMap.containsKey(cl) ? featureMap.get(cl)
								: new HashMap<String, Double>());
						for (int j = 0; j < alpha.size(); j++) { // this gets
																	// the top-n
																	// ranked
																	// labels
																	// (the docs
																	// here are
																	// quite
																	// concise...
																	// so not
																	// sure if
																	// this is
																	// really
																	// what I
																	// want)
																	// http://mallet.cs.umass.edu/api/cc/mallet/types/RankedFeatureVector.html#getIndexAtRank(int)
							int alphaId = plig.getInfoGain(q).getIndexAtRank(j);
							Alphabet localAlpha = plig.getInfoGain(q).getAlphabet();
//							System.out.println(j+"--"+alphaId);
//							System.out.println(j+"--"+plig.getInfoGain(q).getValueAtRank(j));
							String label = (String) localAlpha.lookupObject(alphaId);
							//String label = (String) classifierAlpha.lookupObject(alphaId);
							
							
							//String label = (String) alpha.lookupIndex(alphaId);
							double d = (im.containsKey(label) ? im.get(label) + plig.getInfoGain(q).getValueAtRank(j)
									: plig.getInfoGain(q).getValueAtRank(j));
							im.put(label, d);
							// debugOut.println(cl + "|" + j + "\t" +
							// plig.getInfoGain(q).getValueAtRank(j) + "\t" +
							// label); // TODO: put the score in the hashMap
							// structure!
						}
						featureMap.put(cl, im);
						// debugOut.println("===============");
					}

					// debugOut.println("================================");
					Double[] scores = evaluate(classifier, testFile, debugOut, allClasses);
					totalAccuracy += scores[0];
					totalPrecision += scores[1];
					totalRecall += scores[2];
					totalF1 += scores[3];
					ps.add(scores[1]);
					rs.add(scores[2]);
					fs.add(scores[3]);
					as.add(scores[0]);

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
						if (q == 100){
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
			out.println("\tAccuracy: " + a + "\t(" + findDeviation(as) + ")");
			out.println("\tPrecision: " + p + "\t(" + findDeviation(ps) + ")");
			out.println("\tRecall: " + r + "\t(" + findDeviation(rs) + ")");
			out.println("\tF1: " + f + "\t(" + findDeviation(fs) + ")");
			out.print("==========================\n");
			
		}
		

		out.close();
		debugOut.close();

	}
}
