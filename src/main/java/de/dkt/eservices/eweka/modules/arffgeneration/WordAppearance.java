package de.dkt.eservices.eweka.modules.arffgeneration;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIFManagement;
import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class WordAppearance implements ARFFGenerator{

	public String generateARFF(Model inModel,String dataSetName){
		if(dataSetName==null || dataSetName.equalsIgnoreCase("")){
			dataSetName = "AUTOMATIC_GENERATED_ARFF_"+(new Date()).getTime();
		}
		List<Model> documentModels = NIFManagement.extractDocumentsModels(inModel);
		Instances instances = createNumericClusteringInputFromNifModelsList(documentModels, dataSetName);
//		System.out.println(instances.toString());
//		System.exit(0);
		return instances.toString();
	}
	
	public Instances createNumericClusteringInputFromNifModelsList(List<Model> nifModels, String dataSetName){
		HashMap<Model,HashMap<String, Integer>> countMap = new HashMap<Model,HashMap<String, Integer>>();
		ArrayList<String> attributeList = new ArrayList<String>();
		for (Model nifModel : nifModels){
//			System.out.println(NIFReader.model2String(nifModel, RDFSerialization.TURTLE));

			List<String> wordList = extractWords(nifModel); // note that this also extracts temporal expressions, which by design have no uri
			if(wordList!=null && !wordList.isEmpty()){
//				System.out.println(wordList.size());
				HashMap<String, Integer> innerMap = new HashMap<String, Integer>();
				for (String sa : wordList){
					// note that for now, an anchor always has the same URI. We don't do disambiguation. Maybe if we do that at some point, this needs to be more sophisticated.
					String attributeString = sa;
					attributeString = attributeString.toLowerCase(); // think it is a good idea to not make any difference between casing, but perhaps we'll want to make it into a user-controlled parameter
					if (!(attributeList.contains(attributeString))){
						attributeList.add(attributeString);
					}
					int c = 1;
//					if (innerMap.containsKey(attributeString)){
//						c += innerMap.get(attributeString);
//					}
					innerMap.put(attributeString, c);
				}
				countMap.put(nifModel, innerMap);
			}
		}
		
		FastVector atts = new FastVector();
		atts.addElement(new Attribute("filename", (FastVector) null));
		for (String attributeName : attributeList){
			atts.addElement(new Attribute(attributeName));
		}
		Instances data = new Instances(dataSetName, atts, 0);		
		for (Model nifModel : countMap.keySet()){
			HashMap<String, Integer> innerMap = countMap.get(nifModel);
			double[] vals = new double[data.numAttributes()];
			vals[0] = data.attribute(0).addStringValue(NIFReader.extractDocumentWholeURI(nifModel));
			for (int i = 0; i < attributeList.size(); i++){
				String attributeName = attributeList.get(i);
				int v = 0;
				if (innerMap.containsKey(attributeName)){
					v = innerMap.get(attributeName);
				}
				vals[i] = v;
			}
			Instance ins = new Instance(1.0, vals);
			data.add(ins);
		}
		
		return data;
	}
	
	
	private List<String> extractWords(Model nifModel) {
		try{
			List<String> words = new LinkedList<String>();
			String content = NIFReader.extractIsString(nifModel);
			EnglishAnalyzer en_an = new EnglishAnalyzer();
			QueryParser parser = new QueryParser("", en_an);
			String str = "amenities";
//			System.out.println("result: " + parser.parse(str)); //amenit
//			System.out.println("result2: " + parser.parse(QueryParser.escape(content))); //amenit
			Query q = parser.parse(QueryParser.escape(content));
			String cleanString = q.toString();
//			cleanString = cleanCharacters(cleanString);
//			System.out.println("result3: " + cleanString);
			String[] parts = cleanString.split(" ");
			for (String s : parts) {
				words.add(s);
			}
			return words;
		}
		catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	public String cleanCharacters(String s){
		s = s.replace('?', ' ').replace('"', ' ').replace('?', ' ');
		s = s.replace('(', ' ').replace(')', ' ');
		s = s.replace('[', ' ').replace(']', ' ');
		s = s.replace('\n', ' ');
		s = s.replaceAll("\\s\\s\\s?", " ");
		return s;
	}
	
	public static void main(String[] args) throws Exception {
		FastVector      atts;
		FastVector      attsRel;
		FastVector      attVals;
		FastVector      attValsRel;
		Instances       data;
		Instances       dataRel;
		double[]        vals;
		double[]        valsRel;
		int             i;

		// 1. set up attributes
		atts = new FastVector();
		// - numeric
		atts.addElement(new Attribute("att1"));
		// - nominal
		attVals = new FastVector();
		for (i = 0; i < 5; i++)
			attVals.addElement("val" + (i+1));
		atts.addElement(new Attribute("att2", attVals));
		// - string
		atts.addElement(new Attribute("att3", (FastVector) null));
		// - date
		atts.addElement(new Attribute("att4", "yyyy-MM-dd"));
		// - relational
		attsRel = new FastVector();
		// -- numeric
		attsRel.addElement(new Attribute("att5.1"));
		// -- nominal
		attValsRel = new FastVector();
		for (i = 0; i < 5; i++)
			attValsRel.addElement("val5." + (i+1));
		attsRel.addElement(new Attribute("att5.2", attValsRel));
		dataRel = new Instances("att5", attsRel, 0);
		atts.addElement(new Attribute("att5", dataRel, 0));

		// 2. create Instances object
		data = new Instances("MyRelation", atts, 0);

		// 3. fill with data
		// first instance
		vals = new double[data.numAttributes()];
		// - numeric
		vals[0] = Math.PI;
		// - nominal
		vals[1] = attVals.indexOf("val3");
		// - string
		vals[2] = data.attribute(2).addStringValue("This is a string!");
		// - date
		vals[3] = data.attribute(3).parseDate("2001-11-09");
		// - relational
		dataRel = new Instances(data.attribute(4).relation(), 0);
		// -- first instance
		valsRel = new double[2];
		valsRel[0] = Math.PI + 1;
		valsRel[1] = attValsRel.indexOf("val5.3");
		dataRel.add(new Instance(1.0, valsRel));
		// -- second instance
		valsRel = new double[2];
		valsRel[0] = Math.PI + 2;
		valsRel[1] = attValsRel.indexOf("val5.2");
		dataRel.add(new Instance(1.0, valsRel));
		vals[4] = data.attribute(4).addRelation(dataRel);
		// add
		data.add(new Instance(1.0, vals));

		// second instance
		vals = new double[data.numAttributes()];  // important: needs NEW array!
		// - numeric
		vals[0] = Math.E;
		// - nominal
		vals[1] = attVals.indexOf("val1");
		// - string
		vals[2] = data.attribute(2).addStringValue("And another one!");
		// - date
		vals[3] = data.attribute(3).parseDate("2000-12-01");
		// - relational
		dataRel = new Instances(data.attribute(4).relation(), 0);
		// -- first instance
		valsRel = new double[2];
		valsRel[0] = Math.E + 1;
		valsRel[1] = attValsRel.indexOf("val5.4");
		dataRel.add(new Instance(1.0, valsRel));
		// -- second instance
		valsRel = new double[2];
		valsRel[0] = Math.E + 2;
		valsRel[1] = attValsRel.indexOf("val5.1");
		dataRel.add(new Instance(1.0, valsRel));
		vals[4] = data.attribute(4).addRelation(dataRel);
		// add
		data.add(new Instance(1.0, vals));

		// 4. output data
		//System.out.println(data);
		
		String nifFolder = "C:\\Users\\pebo01\\Desktop\\data\\enronCorpus\\twoNIFs";
		File nifList = new File(nifFolder);
		ArrayList<Model> nifModels = new ArrayList<Model>();
		for (File f : nifList.listFiles()){
			byte[] encoded;
			try {
				encoded = Files.readAllBytes(Paths.get(f.getAbsolutePath()));String fileContent = new String(encoded, StandardCharsets.UTF_8);
				Model nifModel = NIFWriter.initializeOutputModel();
				nifModel = NIFReader.extractModelFromFormatString(fileContent, RDFSerialization.TURTLE);
				nifModels.add(nifModel);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		WordAppearance ea = new WordAppearance();
		Instances arff = ea.createNumericClusteringInputFromNifModelsList(nifModels, "dummy");
		PrintWriter out = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug.txt"));
		out.write(arff.toString());
		out.close();
		
	}
}
