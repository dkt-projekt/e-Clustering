package de.dkt.eservices.eclustering;

import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Iterator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.context.ApplicationContext;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequestWithBody;

import cc.mallet.classify.Classification;
import cc.mallet.classify.Classifier;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.Labeling;
import de.dkt.common.filemanagement.FileFactory;
import de.dkt.eservices.emallet.modules.DocumentClassification;
import de.dkt.eservices.emallet.modules.ImportData;
import eu.freme.bservices.testhelper.TestHelper;
import eu.freme.bservices.testhelper.ValidationHelper;
import eu.freme.bservices.testhelper.api.IntegrationTestSetup;
import eu.freme.common.exception.ExternalServiceFailedException;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EClusteringTest {

	TestHelper testHelper;
	ValidationHelper validationHelper;

	@Before
	public void setup() {
		ApplicationContext context = IntegrationTestSetup
				.getContext(TestConstants.pathToPackage);
		testHelper = context.getBean(TestHelper.class);
		validationHelper = context.getBean(ValidationHelper.class);
	}

	private HttpRequestWithBody baseMALLETRequest() {
		String url = testHelper.getAPIBaseUrl() + "/e-mallet/testURL";
		return Unirest.post(url);
	}

	private HttpRequestWithBody documentClassificationRequest() {
		String url = testHelper.getAPIBaseUrl() + "/e-documentclassification";
		return Unirest.post(url);
	}
	
	private HttpRequestWithBody topicModellingRequest() {
		String url = testHelper.getAPIBaseUrl() + "/e-topicmodelling";
		return Unirest.post(url);
	}
	
	private HttpRequestWithBody baseWEKARequest() {
		String url = testHelper.getAPIBaseUrl() + "/e-weka/testURL";
		return Unirest.post(url);
	}
	
	private HttpRequestWithBody clusteringRequest() {
		String url = testHelper.getAPIBaseUrl() + "/e-clustering/generateClusters";
		return Unirest.post(url);
	}
	
	private HttpRequestWithBody trainModelRequest() {
		String url = testHelper.getAPIBaseUrl() + "/e-mallet/trainModel";
		return Unirest.post(url);
	}

	@Test
	public void testM_0_EMalletBasic() throws UnirestException, IOException,
	Exception {
		HttpResponse<String> response = baseMALLETRequest()
				.queryString("informat", "text")
				.queryString("input", "hello world")
				.queryString("outformat", "turtle").asString();
		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
	}

	@Test
	public void testM_2_AnalyzeTextDocumentClassification() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = documentClassificationRequest()
				.queryString("informat", "text")
				.queryString("input", "Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin")
				.queryString("outformat", "turtle")
//				.queryString("modelName", "testModel")
				.queryString("modelName", "condat_types")
//				.queryString("modelPath", "/Users/jumo04/Documents/DFKI/DKT/dkt-test/malletTest/trainedModels/documentClassification/")
				.queryString("language", "de")
				.asString();
		Assert.assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);
		Assert.assertEquals(TestConstants.expectedResponseClassification, response.getBody());
	}

	@Test
	public void testM_3_AnalyzeNIFTextDocumentClassification() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = documentClassificationRequest()
				.queryString("informat", "turtle")
				.queryString("input", TestConstants.inputText)
				.queryString("outformat", "turtle")
				.queryString("modelName", "condat_types")
//				.queryString("modelPath", "recursos/")
				.queryString("language", "de")
				.asString();
		Assert.assertEquals(response.getStatus(),200);
		assertTrue(response.getBody().length() > 0);
		Assert.assertEquals(TestConstants.expectedResponseClassification2, response.getBody());
	}

	@Test
	public void testM_4_AnalyzeTextTopicModelling() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = topicModellingRequest()
				.queryString("informat", "text")
				.queryString("input", "Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin")
				.queryString("outformat", "turtle")
				.queryString("modelName", "condat")
//				.queryString("modelPath", "recursos/")
				.queryString("language", "de")
				.asString();
		Assert.assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);
		Assert.assertTrue(response.getBody().startsWith(TestConstants.expectedStartingResponseTopic));
	}

	@Test
	public void testM_5_AnalyzeNIFTextTopicModelling() throws UnirestException, IOException,Exception {
		HttpResponse<String> response = topicModellingRequest()
				.queryString("informat", "turtle")
				.queryString("input", TestConstants.inputText)
				.queryString("outformat", "turtle")
				.queryString("modelName", "condat")
//				.queryString("modelPath", "recursos/")
				.queryString("language", "de")
				.asString();
		Assert.assertEquals(response.getStatus(), 200);
		assertTrue(response.getBody().length() > 0);
		Assert.assertTrue(response.getBody().startsWith(TestConstants.expectedStartingResponseTopic2));
	}
//////
//	@Test
//	public void test2_EMalletTrainClassificationModel() throws UnirestException, IOException,Exception {
//		File f = FileFactory.generateFileInstance("rdftest" + File.separator + "temptrainingdata.txt");
//		HttpResponse<String> response = trainModelRequest()
//				.queryString("modelName", "testModelCLASSIFICATION")
//				.queryString("analysis", "classification")
//				.queryString("language", "de")
//				.field("trainingFile", f)
//				.asString();
//		//			System.out.println(response.getStatus());
//		//			System.out.println(response.getStatusText());
//		//			System.out.println(response.getBody());
//		Assert.assertEquals(response.getStatus(), 200);
//		assertTrue(response.getBody().length() > 0);
//		Assert.assertEquals("Text Classification Model [de-testModelCLASSIFICATION.EXT] successfully trained!!!", response.getBody());
//	}
//
//////	@Test
//////	public void test1_EMalletTrainTopicModellingModel() throws UnirestException, IOException,Exception {
//////		File f = FileFactory.generateFileInstance("rdftest" + File.separator + "temptrainingdata.txt");
//////		HttpResponse<String> response = trainModelRequest()
//////				.queryString("modelName", "testModelTOPIC")
////////				.queryString("modelPath", "recursos/")
//////				.queryString("analysis", "topicmodelling")
//////				.queryString("language", "de")
//////				//					.field("file", f)
//////				.field("trainingFile", f)
//////				.asString();
//////		//			System.out.println(response.getStatus());
//////		//			System.out.println(response.getStatusText());
//////		//			System.out.println(response.getBody());
//////		Assert.assertEquals(response.getStatus(), 200);
//////		assertTrue(response.getBody().length() > 0);
//////		Assert.assertEquals("Topic modelling Model [de-testModelTOPIC.EXT] successfully trained!!!", response.getBody());
//////	}
	
	@Test
	public void testW_0_EWekaBasic() throws UnirestException, IOException,
			Exception {

		HttpResponse<String> response = baseWEKARequest()
				.queryString("informat", "text")
				.queryString("input", "hello world")
				.queryString("outformat", "turtle").asString();

		System.out.println("BODY: "+response.getBody());
		System.out.println("STATUS:" + response.getStatus());

		assertTrue(response.getStatus() == 200);
		assertTrue(response.getBody().length() > 0);
		
	}
	
	@Test
	public void testW_1_simpleEMTest() throws UnirestException, IOException,
			Exception {

		String input = ""
+ "	@RELATION CondatTest6_Clustering\n"
+ " @ATTRIBUTE DOCUMENT_NAME  STRING\n"
+ "	@ATTRIBUTE Washington  NUMERIC\n"
+ "	@ATTRIBUTE Jonathan_Pollard  NUMERIC\n"
+ "	@ATTRIBUTE Suchstichwörter  NUMERIC\n"
+ "	@ATTRIBUTE Fähigkeiten_von_Ländern  NUMERIC\n"
+ "	@ATTRIBUTE Alistair_Baskey  NUMERIC\n"
+ "	@ATTRIBUTE Kerry  NUMERIC\n"
+ "	@ATTRIBUTE Einem_von_Pollards_Anwälten  NUMERIC\n"
+ "	@ATTRIBUTE John_Kerry  NUMERIC\n"
+ "	@ATTRIBUTE Angelika_Engler  NUMERIC\n"
+ "	@ATTRIBUTE Saudi  NUMERIC\n"
+ "	@ATTRIBUTE Sydney  NUMERIC\n"
+ "	@ATTRIBUTE Iran  NUMERIC\n"
+ "	@ATTRIBUTE Israels__Haltung  NUMERIC\n"
+ "	@ATTRIBUTE November_1985  NUMERIC\n"
+ "	@ATTRIBUTE Bewährungsauflagen_von_Pollard  NUMERIC\n"
+ "	@ATTRIBUTE Barack_Obama  NUMERIC\n"
+ "	@ATTRIBUTE Fall  NUMERIC\n"
+ "	@ATTRIBUTE November  NUMERIC\n"
+ "	@ATTRIBUTE US  NUMERIC\n"
+ "	@ATTRIBUTE Gegenleistung  NUMERIC\n"
+ "	@ATTRIBUTE Uwe_Käding  NUMERIC\n"
+ "	@ATTRIBUTE Bernard_Darko  NUMERIC\n"
+ "	@ATTRIBUTE European_News_Wire  NUMERIC\n"
+ "	@ATTRIBUTE Kim_Alexander_Zickenheiner  NUMERIC\n"
+ "	@ATTRIBUTE Württemberg  NUMERIC\n"
+ "	@ATTRIBUTE Hybridbusse  NUMERIC\n"
+ "	@ATTRIBUTE Dresden  NUMERIC\n"
+ "	@ATTRIBUTE Frank_Metzger  NUMERIC\n"
+ "	@ATTRIBUTE Ende_Juni_Strom  NUMERIC\n"
+ "	@ATTRIBUTE MAN  NUMERIC\n"
+ "	@ATTRIBUTE Baden  NUMERIC\n"
+ "	@ATTRIBUTE Preise  NUMERIC\n"
+ "	@ATTRIBUTE Matthias_Schröter  NUMERIC\n"
+ "	@ATTRIBUTE Susanne_Schupp  NUMERIC\n"
+ "	@ATTRIBUTE 2852  NUMERIC\n"
+ "	@ATTRIBUTE René_Weintz  NUMERIC\n"
+ "	@ATTRIBUTE 2015  NUMERIC\n"
+ "	@ATTRIBUTE Henrik_Petermann  NUMERIC\n"
+ "	@ATTRIBUTE 2020  NUMERIC\n"
+ "	@ATTRIBUTE Lars_Wagner  NUMERIC\n"
+ "	@ATTRIBUTE 2012  NUMERIC\n"
+ "	@ATTRIBUTE Hamburg  NUMERIC\n"
+ "	@ATTRIBUTE Volvo  NUMERIC\n"
+ "	@ATTRIBUTE Jan  NUMERIC\n"
+ "	@ATTRIBUTE Christoph_Kreienbaum  NUMERIC\n"
+ "	@ATTRIBUTE Pressestelle_Verkehrsministerium_Baden  NUMERIC\n"
+ "	@ATTRIBUTE Hannover  NUMERIC\n"
+ "	@ATTRIBUTE Unterstützung_von_Elektromotoren  NUMERIC\n"
+ "	@ATTRIBUTE Thomas_Tuchel  NUMERIC\n"
+ "	@ATTRIBUTE April  NUMERIC\n"
+ "	@ATTRIBUTE Axel_Schweitzer  NUMERIC\n"
+ "	@ATTRIBUTE Sasa_Obradovic  NUMERIC\n"
+ "	@ATTRIBUTE Scouting  NUMERIC\n"
+ "	@ATTRIBUTE Berlin  NUMERIC\n"
+ "	@ATTRIBUTE Schwarz  NUMERIC\n"
+ "	@ATTRIBUTE Bayern  NUMERIC\n"
+ "	@ATTRIBUTE Frankfurter_Museums_Junge_Kunst_Armin_Hauer  NUMERIC\n"
+ "	@ATTRIBUTE Deutsche_Bahn  NUMERIC\n"
+ "	@ATTRIBUTE Rathaus  NUMERIC\n"
+ "	@ATTRIBUTE 1936  NUMERIC\n"
+ "\n"
+ "	@DATA\n"

+ "	CondatTest6_3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,0,0,0,0,0\n"
+ "	CondatTest6_2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0\n"
+ "	CondatTest6_1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0\n"
+ "	CondatTest6_6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1\n"
+ "	CondatTest6_5,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,0\n"
+ "	CondatTest6_4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0\n";
		
		
		HttpResponse<String> response2 = clusteringRequest()
//		HttpResponse<String> response2 = Unirest.post("http://dev.digitale-kuratierung.de/api/e-clustering/generateClusters")
				.queryString("language", "en")
				.queryString("algorithm", "kmeans")
//				.body(TestConstants.sampleARFF)
				.body(input)
				.asString();
				
		System.out.println("DEBUGGING output here:" + response2.getBody());
		//System.out.println("BODY: "+response.getBody());
		//System.out.println("STATUS:" + response.getStatus());

		//assertTrue(response.getStatus() == 200);
		//assertTrue(response.getBody().length() > 0);
		
	}

////	@Test
////	public void simpleEMTest() throws UnirestException, IOException,
////			Exception {
////
////		ImportData id = new ImportData("vector","de");
////		File trainingFile = FileFactory.generateFileInstance("rdftest" + File.separator + "temptrainingdata.txt");
////		InstanceList instances = id.readFile(trainingFile);
////
////		//Train the model with the training instances
////		ClassifierTrainer trainer = new MaxEntTrainer();
////		Classifier classifier;
////		try{
////			classifier = trainer.train(instances);
////		}
////		catch(Exception e){
////			throw new ExternalServiceFailedException("Fail at training the model: it is possible that you need to use a bigger amount of Data.");
////		}
////		
////        ImportData importData = new ImportData("vector","de");
////        InstanceList testing = new InstanceList(id.getPipe());
//////        InstanceList testing = new InstanceList(classifier.getInstancePipe());
////        testing.addThruPipe(new Instance("Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin", classifier.getLabelAlphabet().iterator().next(), "test_instance", null));
////        
////        Classification classification = classifier.classify(testing.get(0));
////		Labeling labeling = classification.getLabeling();
////		String output = labeling.getLabelAtRank(0).toString();
////		System.out.println(output);
////	}	
}
