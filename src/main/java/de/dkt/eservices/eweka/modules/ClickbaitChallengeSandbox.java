package de.dkt.eservices.eweka.modules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.bag.SynchronizedSortedBag;
import org.bouncycastle.util.Arrays;

import de.dkt.common.filemanagement.FileFactory;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.StringToNominal;

public class ClickbaitChallengeSandbox {
	
	//Class for playing around with WEKA for clickbait challenge (http://www.clickbait-challenge.org/).
	
	private static String modelsDirectory = "trainedModels" + File.separator + "classification" + File.separator;
	
	
	private static HashMap<Double, HashMap<String, Double>> getCharNgrams(HashMap<Double, ArrayList<String>> matrix, int min, int max, int index){ // index controls for which element in the vector we want ngrams
		
		HashMap<Double, HashMap<String, Double>> ngramMap = new HashMap<Double, HashMap<String, Double>>();
		
		for (Double id : matrix.keySet()){
			ArrayList<String> data = matrix.get(id);
			if (index < data.size()){
				String text = data.get(index);
				HashMap<Integer, Integer> length2count = new HashMap<Integer, Integer>();
				HashMap<String, Double> localMap = new HashMap<String, Double>();
				for (int i = min; i <= max; i++) {
					for (int j = 0; j < text.length() - i; j++) {
						int l = length2count.containsKey(i) ? length2count.get(i) + 1 : 1;
						length2count.put(i, l);
						String ngram = text.substring(j, j + i);
						double c = localMap.containsKey(ngram) ? localMap.get(ngram) + 1 : 1;
						localMap.put(ngram, c);
					}
				}
				for (String ngram : localMap.keySet()) {
					localMap.put(ngram, localMap.get(ngram) / length2count.get(ngram.length()));
				}
				ngramMap.put(id, localMap);
			}
		}
		
		return ngramMap;
		
	}
	
		
	private ArrayList<String> getColumnHeaders(List<String> flines){
		String[] sa = flines.get(0).split("\t");
		ArrayList<String> rl = new ArrayList<String>();
		for (int i = 1; i < sa.length; i++){
			rl.add(sa[i]);
		}
		return rl;
		
	}
		
	private static HashMap<Double, ArrayList<String>> parseTsv(List<String> flines){ // assumes that the first line is the column headers, and that the first column contains id (integer)
		
		HashMap<Double, ArrayList<String>> matrix = new HashMap<Double, ArrayList<String>>();
		
		for (int i = 1; i < flines.size(); i++){
			String[] parts = flines.get(i).split("\t");
			double id = Double.parseDouble(parts[0]);
			ArrayList<String> sl = new ArrayList<String>();
			for (int j = 1; j < parts.length; j++){
				sl.add(parts[j]);
			}
			matrix.put(id, sl);
		}
		
		return matrix;
	}
	
	private static Set<String> getNgramVocabulary(HashMap<Double, HashMap<String, Double>> ngramMap){
		HashMap<String, Integer> vocabMap = new HashMap<String, Integer>();
		for (double id : ngramMap.keySet()){
			for (String ngram : ngramMap.get(id).keySet()){
				int d = vocabMap.containsKey(ngram) ? vocabMap.get(ngram) + 1 : 1;
				vocabMap.put(ngram,  d);
			}
		}
		return vocabMap.keySet();
	}
	
	private static Instances getWekaMatrix(ArrayList<String> vocab, HashMap<Double, HashMap<String, Double>> ngramMap, HashMap<Double, ArrayList<String>> matrix, String datasetName){

		FastVector atts = new FastVector();
		Instances data = new Instances(datasetName, atts, 0);
		for (String ngram : vocab){
			atts.addElement(new Attribute(ngram));
		}
		
		for (double id : ngramMap.keySet()){
			double[] vals = new double[data.numAttributes()+1];
			for (int i = 0; i < vocab.size(); i++){
				String ngram = vocab.get(i);
				double v = 0;
				if (ngramMap.get(id).containsKey(ngram)){
					v = ngramMap.get(id).get(ngram);
				}
				vals[i] = v;
			}
			String s = matrix.get(id).get(0);
						
			data.add(new Instance(1.0, vals));
			Attribute classValue = new Attribute("classValue");
			data.insertAttributeAt(classValue, data.numAttributes()); // TODO this complains Attribute name already in use in this position. If I skip this, it complains that class value is not there (or; class as numeral is not allowed)
			//data.instance(data.numAttributes()-1).setValue(data.attribute("classValue").index(), s);
						
		}
		
		return data;
	}
	
	private static Classifier trainClassifier(Instances is, String classifierName, String classifierType, String language){
		
		Classifier classifier = ClassifierFactory.getClassifier(classifierType);
		try {
			// Set class index
			if (is.classIndex() == -1) {
				is.setClassIndex(is.numAttributes()-1); // last val is the class
			}
			classifier.buildClassifier(is);
			String modelOutputFile = modelsDirectory + language + "-" + classifierName + ".EXT";
			File outputFile = FileFactory.generateOrCreateFileInstance(modelOutputFile);
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream (outputFile));
			oos.writeObject(classifier);
			oos.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return classifier;
		
	}
	
	public static void main(String[] args){
		
		Path p = new File("C:\\Users\\pebo01\\Desktop\\ubuntuShare\\clickbaitChallenge\\tweets.tsv").toPath();
		List<String> flines;
		try {
			flines = Files.readAllLines(p);
			HashMap<Double, ArrayList<String>> matrix = parseTsv(flines);
			HashMap<Double, HashMap<String, Double>> ngramMap = getCharNgrams(matrix, 1, 3, 5);
			Set<String> vocabulary = getNgramVocabulary(ngramMap);
			System.out.println("INFO: data rows:" + matrix.size());
			System.out.println("INFO: vector lenght:" + vocabulary.size());
			ArrayList<String> vocabArray = new ArrayList<String>(vocabulary);
			Instances inst = getWekaMatrix(vocabArray, ngramMap, matrix, "sandbox-Sam");
			
			Classifier randomForestClassifier = trainClassifier(inst, "sandbox-Sam", "randomForest", "en");
			System.out.println("done.");
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
		
		
		
//		ArrayList<String[]> playData = new ArrayList<String[]>();
//		playData.add(new String[]{"1", "aapje", "This is a text about aapjes."});
//		playData.add(new String[]{"1", "boompje", "This is a clickbait example about food."});
//		playData.add(new String[]{"1", "beestje", "This is about beestjes, and then more beestjes."});
//		
//		HashMap<String, HashMap<String, Double>> ngramMap = getCharNgrams(playData, 1, 3);
//		for (String id : ngramMap.keySet()){
//			HashMap<String, Double> im = ngramMap.get(id);
//			for (String ngram : im.keySet()){
//				System.out.println("id, ngram, val:" + id + "," + ngram + "," + im.get(ngram));
//			}
//		}
		
	}
	
}
