package de.dkt.eservices.emallet.execution;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;

import cc.mallet.classify.examples.DocumentClassifier;
import de.dkt.eservices.emallet.modules.DocumentClassification;

public class RumorEval {
	
	public static void main(String[] args) throws Exception {
		String testName = "File_All_WithSource_cleanedTweets_addingUrlText";
//		String testName = "File_All_WithoutSource_cleanedTweets";
//		String testName = "File_All_WithSource";
//		String testName = "File_All_WithoutSource";
//		String testName = "File_1_WithSource";
		
		String trainingDataPath = "test/training"+testName+".txt";
		String testDataPath = "test/test"+testName+".txt";
		
//		File trainingData = new File(trainingDataPath);
//		File testData = new File(testDataPath);
		
//		DocumentClassification dc = new DocumentClassification();

//		String[] algorithms = {"balancedwinnow","c45","maxentrange","maxent","maxentge","maxentpr","mcmaxent","bayesem","bayes","winnow"};
//		String[] algorithms = {"maxent"};
////		String[] algorithms = {"maxentge"};
////		String[] algorithms = {"maxentpr"};
		String[] algorithms = {"mcmaxent"};
//		String[] algorithms = {"bayes"};
//		String[] algorithms = {"c45"};
//		String[] algorithms = {"winnow"};

//		String[] algorithms = {"c45","maxent","bayes","winnow"};

		//Train models.
		for (int i = 0; i < algorithms.length; i++) {
			String alg = algorithms[i];
			DocumentClassification.trainClassifier(trainingDataPath, null, "rumoreval_1_"+alg, "en", alg);
		}
	
		InputStream in = new FileInputStream(testDataPath);
		List<String> lines = IOUtils.readLines(in);
		
		String titles="",qs="Q",ss="S",ds="D",cs="C";
		
		for (int i = 0; i < algorithms.length; i++) {
			String alg = algorithms[i];
			System.out.println(alg);
			int[][] results = new int[4][4];
			for (String line : lines) {
				int firstWhite = line.indexOf(' ');
				int secondWhite = line.indexOf(' ', firstWhite+1);
				String id = line.substring(0, firstWhite);
				String expectedLabel = line.substring(firstWhite+1, secondWhite);
				String text = line.substring(secondWhite+1);
				String label = DocumentClassification.classifyString(text, null, "rumoreval_1_"+alg, "en");
				System.out.println("\t" + expectedLabel + "-->" + label);
				
				int index1 = -1;
				if(expectedLabel.equalsIgnoreCase("query")){
					index1=0;
				}
				else if(expectedLabel.equalsIgnoreCase("support")){
					index1=1;
				}
				else if(expectedLabel.equalsIgnoreCase("deny")){
					index1=2;
				}
				else if(expectedLabel.equalsIgnoreCase("comment")){
					index1=3;
				}
				int index2 = -1;
				if(label.equalsIgnoreCase("query")){
					index2=0;
				}
				else if(label.equalsIgnoreCase("support")){
					index2=1;
				}
				else if(label.equalsIgnoreCase("deny")){
					index2=2;
				}
				else if(label.equalsIgnoreCase("comment")){
					index2=3;
				}
//				System.out.println(index2 + "--" + index1);
				if(index2!=-1 && index1!=-1){
					results[index2][index1]++;
				}
			}
			titles += " "+alg+"(Q-S-D-C)";
			qs += " "+results[0][0]+" "+results[1][0]+" "+results[2][0]+" "+results[3][0]+"";
			ss += " "+results[0][1]+" "+results[1][1]+" "+results[2][1]+" "+results[3][1]+"";
			ds += " "+results[0][2]+" "+results[1][2]+" "+results[2][2]+" "+results[3][2]+"";
			cs += " "+results[0][3]+" "+results[1][3]+" "+results[2][3]+" "+results[3][3]+"";
		}

		System.out.println(titles);
		System.out.println(qs);
		System.out.println(ss);
		System.out.println(ds);
		System.out.println(cs);
//		System.out.println("\tQ\tS\tD\tC");
//		System.out.println("Q\t"+results[0][0]+"\t"+results[1][0]+"\t"+results[2][0]+"\t"+results[3][0]+"");
//		System.out.println("S\t"+results[0][1]+"\t"+results[1][1]+"\t"+results[2][1]+"\t"+results[3][1]+"");
//		System.out.println("D\t"+results[0][2]+"\t"+results[1][2]+"\t"+results[2][2]+"\t"+results[3][2]+"");
//		System.out.println("C\t"+results[0][3]+"\t"+results[1][3]+"\t"+results[2][3]+"\t"+results[3][3]+"");
	}
	
}
