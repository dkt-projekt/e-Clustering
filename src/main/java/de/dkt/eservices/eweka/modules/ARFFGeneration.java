package de.dkt.eservices.eweka.modules;

import java.io.File;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;

import de.dkt.common.niftools.NIFReader;
import de.dkt.common.niftools.NIFWriter;
import eu.freme.common.conversion.rdf.RDFConstants.RDFSerialization;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ARFFGeneration {

	public static Instances createNumericClusteringInputFromNifModelsList(ArrayList<Model> nifModels, String dataSetName, RDFSerialization nifFormat){
		
		HashMap<Model,HashMap<String, Integer>> countMap = new HashMap<Model,HashMap<String, Integer>>();
		ArrayList<String> attributeList = new ArrayList<String>();
		for (Model nifModel : nifModels){
			
			List<String[]> entityList = NIFReader.extractEntities(nifModel); // note that this also extracts temporal expressions, which by design have no uri
			HashMap<String, Integer> innerMap = new HashMap<String, Integer>();
			if (entityList != null){
				for (String[] sa : entityList){
					String anchorOf = sa[1];
					String entityURI = sa[0];
					// note that for now, an anchor always has the same URI. We don't do disambiguation. Maybe if we do that at some point, this needs to be more sophisticated.
					String attributeString = null;
					if (entityURI != null){
						attributeString = String.format("%s(%s)", anchorOf, entityURI); // This may crash at some point because there are characters in the anchorOf that are not allowed in the arff file, if so: anchorOf.replaceAll("\\W", "_");
					}
					else{
						attributeString = anchorOf;
					}
					attributeString = attributeString.toLowerCase(); // think it is a good idea to not make any difference between casing, but perhaps we'll want to make it into a user-controlled parameter
					if (!(attributeList.contains(attributeString))){
						attributeList.add(attributeString);
					}
					int c = 1;
					if (innerMap.containsKey(attributeString)){
						c += innerMap.get(attributeString);
					}
					innerMap.put(attributeString, c);
				}
			}
			countMap.put(nifModel, innerMap);
		}
		
		FastVector atts = new FastVector();
		Instances data = new Instances(dataSetName, atts, 0);
		for (String attributeName : attributeList){
			atts.addElement(new Attribute(attributeName));
		}
		
		for (Model nifModel : countMap.keySet()){
			HashMap<String, Integer> innerMap = countMap.get(nifModel);
			double[] vals = new double[data.numAttributes()];
			for (int i = 0; i < attributeList.size(); i++){
				String attributeName = attributeList.get(i);
				int v = 0;
				if (innerMap.containsKey(attributeName)){
					v = innerMap.get(attributeName);
				}
				vals[i] = v;
			}
			data.add(new Instance(1.0, vals));
		}
		
		return data;
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
		
		
		Instances arff = createNumericClusteringInputFromNifModelsList(nifModels, "dummy", RDFSerialization.TURTLE);
		PrintWriter out = new PrintWriter(new File("C:\\Users\\pebo01\\Desktop\\debug.txt"));
		out.write(arff.toString());
		out.close();
		
	}
}
