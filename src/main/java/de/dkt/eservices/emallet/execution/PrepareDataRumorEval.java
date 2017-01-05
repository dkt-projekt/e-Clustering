package de.dkt.eservices.emallet.execution;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.sax.BodyContentHandler;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;

public class PrepareDataRumorEval {

	public static void main(String[] args) throws Exception {
		
		String testName = "File_All_WithSource_cleanedTweets_addingUrlText";
		
		String trainingFile = "test/training"+testName+".txt";
		String testFile = "test/test"+testName+".txt";
		double threshold = 0.1;
		
		boolean addSourceText = true;
		boolean cleaningTweet = true;
		
		boolean addingUrlText = true;
		
		HashMap<String, String> urlContents = new HashMap<String, String>();
		
		String doc1 = "/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/traindev/rumoureval-subtaskA-dev.json";
		String doc2 = "/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/traindev/rumoureval-subtaskA-train.json";
		String doc3 = "/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/traindev/rumoureval-subtaskB-dev.json";
		String doc4 = "/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/traindev/rumoureval-subtaskB-train.json";
		
		String dataFolder = "/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/charliehebdo/";

		String [] folders = {"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/charliehebdo/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/ebola-essien/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/ferguson/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/germanwings-crash/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/ottawashooting/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/prince-toronto/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/putinmissing/",
				"/Users/jumo04/Documents/DFKI/Conferences/SemEval2017/SharedTasks/RumourEval/semeval2017-task8-dataset/rumoureval-data/sydneysiege/"
		};
		List<String> trainingInstances = new LinkedList<String>();
		List<String> testInstances = new LinkedList<String>();

		JSONObject obj1 = new JSONObject(IOUtils.toString(new FileInputStream(doc1)));
		JSONObject obj2 = new JSONObject(IOUtils.toString(new FileInputStream(doc2)));
		JSONObject obj3 = new JSONObject(IOUtils.toString(new FileInputStream(doc3)));
		JSONObject obj4 = new JSONObject(IOUtils.toString(new FileInputStream(doc4)));

		HashMap<String, String> labelsTraining = new HashMap<String, String>();
		HashMap<String, String> labelsTest = new HashMap<String, String>();
		
		Iterator it1 = obj1.keys();
		while(it1.hasNext()){
			String ss = (String)it1.next();
//			System.out.println("TE1:"+ss+"--"+obj1.getString(ss));
			labelsTest.put(ss, obj1.getString(ss));
		}
		Iterator it2 = obj2.keys();
		while(it2.hasNext()){
			String ss = (String)it2.next();
//			System.out.println("TR1:"+ss+"--"+obj2.getString(ss));
			labelsTraining.put(ss, obj2.getString(ss));
		}
//		Iterator it3 = obj3.keys();
//		while(it3.hasNext()){
//			String ss = (String)it3.next();
//			System.out.println("TE2:"+ss+"--"+obj3.getString(ss));
//			labelsTest.put(ss, obj3.getString(ss));
//		}
//		Iterator it4 = obj4.keys();
//		while(it4.hasNext()){
//			String ss = (String)it4.next();
//			System.out.println("TR4:"+ss+"--"+obj4.getString(ss));
//			labelsTraining.put(ss, obj4.getString(ss));
//		}
		
		int counter = 1;
		//Recorrer la carepta y sacar todos los tuits de soporte. Poner cada uno en una linea.
		for (String folS : folders) {
//			File folder = new File(dataFolder);
			File folder = new File(folS);
			File fs [] = folder.listFiles();
						
			for (int i = 0; i < fs.length; i++) {
				if(fs[i].getName().startsWith(".")){
					continue;
				}
				File f = fs[i];
				String folderName = f.getName();
				File fs2[] = f.listFiles();
				for (int j = 0; j < fs2.length; j++) {
					if(fs2[j].getName().startsWith(".")){
						continue;
					}
					if(fs2[j].getName().equalsIgnoreCase("replies")){
						File fil = new File(fs2[j].getParent()+"/source-tweet");
						File[] sources = fil.listFiles();
						String sourceContent = "";
						for (int k = 0; k < sources.length; k++) {
							if(sources[k].getName().startsWith(".")){
								continue;
							}
							JSONObject sourcetweet = new JSONObject(IOUtils.toString(new FileInputStream(sources[k])));
							sourceContent = sourcetweet.getString("text");
						}
							
						File jsonFiles[] = fs2[j].listFiles();
						for (int k = 0; k < jsonFiles.length; k++) {
							if(jsonFiles[k].getName().startsWith(".")){
								continue;
							}
							JSONObject tweet = new JSONObject(IOUtils.toString(new FileInputStream(jsonFiles[k])));
//							System.out.println("-------------\n--------------\n------------");
//							System.out.println(tweet.toString(1));
//							System.out.println(tweet.getString("text"));

							String tweetText = tweet.getString("text");
							String urlTexts = "";
							if(addingUrlText){
								List<String> urls = getTweetUrls(tweetText);
								for (String url : urls) {
									String urlText = "";
									if(urlContents.containsKey(url)){
										urlText = urlContents.get(url);
										System.out.println("=========================ADDED BEFORE");
									}
									else{
										urlText = getTextFromUrl(url);
										urlContents.put(url, urlText);
									}
									urlTexts += " " + urlText;
								}
							}
							if(cleaningTweet){
								tweetText = cleanTweet(tweetText);
								sourceContent = cleanTweet(sourceContent);
							}
							if(!labelsTest.containsKey(jsonFiles[k].getName().substring(0, jsonFiles[k].getName().indexOf('.')))){
								String label = labelsTraining.get(jsonFiles[k].getName().substring(0, jsonFiles[k].getName().indexOf('.')));
//								System.out.println(label);
								String trainingLine = counter+" "+label+" " +tweetText;
								if(addingUrlText){
									trainingLine += " " + urlTexts;
								}
								if(addSourceText){
									trainingLine += " " + sourceContent;
								}
								trainingInstances.add(trainingLine);
							}
							else{
								String label = labelsTest.get(jsonFiles[k].getName().substring(0, jsonFiles[k].getName().indexOf('.')));
//								System.out.println(label);
								String testLine = counter+" "+label+" " +tweetText;
								if(addingUrlText){
									testLine += " " + urlTexts;
								}
								if(addSourceText){
									testLine += " " + sourceContent;
								}
								testInstances.add(testLine);
							}
//							String label = labels.get(jsonFiles[k].getName().substring(0, jsonFiles[k].getName().indexOf('.')));
////							System.out.println(label);
//							trainingInstances.add(counter+" "+label+" " +tweet.getString("text") + " " + sourceContent);
							counter++;
						}
					}
				}
			}
		}
		
		FileOutputStream outputTraining = new FileOutputStream(trainingFile);
		FileOutputStream outputTest = new FileOutputStream(testFile);
		if(testInstances.isEmpty()){
			for (String s : trainingInstances) {
				s = s.replace("\n", "\\n");
				double value = Math.random();
				if(value>threshold){
					IOUtils.write(s+"\n", outputTraining, "utf-8");
				}
				else{
					IOUtils.write(s+"\n", outputTest, "utf-8");
				}
//				System.out.println(s);
			}
		}
		else{
			for (String s : trainingInstances) {
				s = s.replace("\n", "\\n");
				IOUtils.write(s+"\n", outputTraining, "utf-8");
	//			double value = Math.random();
	//			if(value>threshold){
	//				IOUtils.write(s+"\n", outputTraining, "utf-8");
	//			}
	//			else{
	//				IOUtils.write(s+"\n", outputTest, "utf-8");
	//			}
	//			System.out.println(s);
			}
			for (String s : testInstances) {
				s = s.replace("\n", "\\n");
				IOUtils.write(s+"\n", outputTest, "utf-8");
			}
		}
		outputTraining.close();
		outputTest.close();
	}
	
	private static String getTextFromUrl(String url) {
		try{
			System.out.println("Checking: "+url+"...");
			String result = "";
			
//			
//			
//			InputStream in = new URL( url ).openStream();
			//
			URL urlObj = new URL(url);
			URLConnection urlc = urlObj.openConnection();
			urlc.setConnectTimeout(1500);
			InputStream in = urlc.getInputStream();

			try {
				result = IOUtils.toString( in );
			} finally {
				IOUtils.closeQuietly(in);
			}

			BodyContentHandler handler = new BodyContentHandler();

			AutoDetectParser parser = new AutoDetectParser();
			HtmlParser hparser = new HtmlParser();
			Metadata metadata = new Metadata();
			ParseContext context = new ParseContext();
//			System.out.println("-------------\n" + result.substring(0, 100));
			InputStream is = new ByteArrayInputStream( result.getBytes( "utf-8" ) );
			
			hparser.parse(is, handler, metadata, context);
			result = handler.toString();
//			System.out.println("=========================================");
//			System.out.println(result);
			result = result.replaceAll("\\s+", " ");
			System.out.println(result);
			System.out.println("...DONE");
			return result;

			//			return null;
		}catch(Exception e){
			e.printStackTrace();
			System.out.println("...NOTDONE");
			return null;
		}
	}

	private static List<String> getTweetUrls(String tweetText) {
		List<String> list = new LinkedList<String>();
		int i=-1;
		String regex = "\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
		Matcher m = Pattern.compile(regex).matcher(tweetText);
		while (m.find()) {
			//Span as = new Span(m.start() + 14, m.end() - 6);
			String st = m.group().trim();
//			System.out.println(st);
			list.add(st); 
		}
		return list;
	}

	public static String cleanTweet(String s){
		String r = s;
//		System.out.println(s);
		r = r.replace("RT", "");
		r = r.replaceAll("(@|http)\\S+", "");
		
		r = r.replace("  ", " ");		
//		System.out.println("\t"+r.trim());
		return r.trim();
	}
}
