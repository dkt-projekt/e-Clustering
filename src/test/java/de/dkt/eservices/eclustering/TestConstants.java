package de.dkt.eservices.eclustering;


public class TestConstants {
	
	public static final String pathToPackage = "rdftest/eclustering-test-package.xml";
	
	public static String sampleARFF = 
			"@RELATION iris\n" +
			"@ATTRIBUTE sepallength  NUMERIC\n" +
			"@ATTRIBUTE sepalwidth   NUMERIC\n" +
			"@ATTRIBUTE petallength  NUMERIC\n" +
			"@ATTRIBUTE petalwidth   NUMERIC\n" +
			"\n" +
			"@DATA\n" +
			"5.1,3.5,1.4,0.2\n" +
			"4.9,3.0,1.4,0.2\n" +
			"4.7,3.2,1.3,0.2\n" +
			"4.6,3.1,1.5,0.2\n" +
			"5.0,3.6,1.4,0.2\n" +
			"5.4,3.9,1.7,0.4\n" +
			"4.6,3.4,1.4,0.3\n" +
			"5.0,3.4,1.5,0.2\n" +
			"4.4,2.9,1.4,0.2\n" +
			"4.9,3.1,1.5,0.1";
	
	public static String inputText = 
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
			"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"<http://dkt.dfki.de/examples/#char=0,806>\n" +
			"        a                    nif:RFC5147String , nif:String , nif:Context ;\n" +
			"        nif:beginIndex       \"0\"^^xsd:nonNegativeInteger ;\n" +
			"        nif:centralGeoPoint  \"42.17561833333333_-4.075817777777777\"^^xsd:string ;\n" +
			"        nif:endIndex         \"806\"^^xsd:nonNegativeInteger ;\n" +
			"        nif:geoStandardDevs  \"1.4446184970002673_2.202362615152696\"^^xsd:string ;\n" +
			"        nif:isString         \"1936\\n\\nCoup leader Sanjurjo was killed in a plane crash on 20 July, leaving an effective command split between Mola in the North and Franco in the South. On 21 July, the fifth day of the rebellion, the Nationalists captured the main Spanish naval base at Ferrol in northwestern Spain. A rebel force under Colonel Beorlegui Canet, sent by General Emilio Mola, undertook the Campaign of Guipuzcoa from July to September. The capture of Guipuzcoa isolated the Republican provinces in the north. On 5 September, after heavy fighting the force took Irun, closing the French border to the Republicans. On 13 September, the Basques surrendered Madrid to the Nationalists, who then advanced toward their capital, Bilbao. The Republican militias on the border of Viscaya halted these forces at the end of September.\\n\"^^xsd:string ;\n" +
			"        nif:meanDateRange    \"19360531090000_19361022090000\"^^xsd:string .\n" +
			"\n" +
			"";
	public static String expectedResponseClassification = 
			"@prefix dktnif: <http://dkt.dfki.de/ontologies/nif#> .\n" +
	"@prefix nif-ann: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-annotation#> .\n" +
					"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
					"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
					"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
					"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
					"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
					"\n" +
					"<http://dkt.dfki.de/documents/#char=0,74>\n" +
					"        a               nif:RFC5147String , nif:String , nif:Context ;\n" +
					"        nif:beginIndex  \"0\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:documentClassificationLabel\n" +
					"                \"Agenturmeldung\"^^xsd:string ;\n" +
					"        nif:endIndex    \"74\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:isString    \"Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin\"^^xsd:string .\n" +
					"";

	public static String expectedResponseClassification2 = 
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
					"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
					"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
					"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
					"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
					"\n" +
					"<http://dkt.dfki.de/examples/#char=0,806>\n" +
					"        a                    nif:RFC5147String , nif:String , nif:Context ;\n" +
					"        nif:beginIndex       \"0\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:centralGeoPoint  \"42.17561833333333_-4.075817777777777\"^^xsd:string ;\n" +
			        "        nif:documentClassificationLabel\n" +
	                "                \"Agenturmeldung\"^^xsd:string ;\n" +
					"        nif:endIndex         \"806\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:geoStandardDevs  \"1.4446184970002673_2.202362615152696\"^^xsd:string ;\n" +
					"        nif:isString         \"1936\\n\\nCoup leader Sanjurjo was killed in a plane crash on 20 July, leaving an effective command split between Mola in the North and Franco in the South. On 21 July, the fifth day of the rebellion, the Nationalists captured the main Spanish naval base at Ferrol in northwestern Spain. A rebel force under Colonel Beorlegui Canet, sent by General Emilio Mola, undertook the Campaign of Guipuzcoa from July to September. The capture of Guipuzcoa isolated the Republican provinces in the north. On 5 September, after heavy fighting the force took Irun, closing the French border to the Republicans. On 13 September, the Basques surrendered Madrid to the Nationalists, who then advanced toward their capital, Bilbao. The Republican militias on the border of Viscaya halted these forces at the end of September.\\n\"^^xsd:string ;\n" +
					"        nif:meanDateRange    \"19360531090000_19361022090000\"^^xsd:string .\n" +
					"";
	
	public static String expectedResponseTopic = 
			"@prefix dktnif: <http://dkt.dfki.de/ontologies/nif#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
			"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"<http://dkt.dfki.de/documents/#char=0,74>\n" +
			"        a                        nif:RFC5147String , nif:String , nif:Context ;\n" +
			"        nif:beginIndex           \"0\"^^xsd:nonNegativeInteger ;\n" +
			"        nif:endIndex             \"74\"^^xsd:nonNegativeInteger ;\n" +
			"        nif:isString             \"Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin\"^^xsd:string ;\n" +
	        "        nif:topicModellingLabel  \"cb (63) 072915 (62) na (50) flüchtlinge (41) 30 (41) \"^^xsd:string .\n" +
			"";
	
	public static String expectedStartingResponseTopic = 
			"@prefix dktnif: <http://dkt.dfki.de/ontologies/nif#> .\n" +
					"@prefix nif-ann: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-annotation#> .\n" +
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
			"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
			"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
			"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
			"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
			"\n" +
			"<http://dkt.dfki.de/documents/#char=0,74>\n" +
			"        a                        nif:RFC5147String , nif:String , nif:Context ;\n" +
			"        nif:beginIndex           \"0\"^^xsd:nonNegativeInteger ;\n" +
			"        nif:endIndex             \"74\"^^xsd:nonNegativeInteger ;\n" +
			"        nif:isString             \"Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin\"^^xsd:string ;\n" +
	        "        nif:topicModellingLabel  \"";

	public static String expectedResponseTopic2 = 
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
					"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
					"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
					"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
					"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
					"\n" +
					"<http://dkt.dfki.de/examples/#char=0,806>\n" +
					"        a                        nif:RFC5147String , nif:String , nif:Context ;\n" +
					"        nif:beginIndex           \"0\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:centralGeoPoint      \"42.17561833333333_-4.075817777777777\"^^xsd:string ;\n" +
					"        nif:endIndex             \"806\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:geoStandardDevs      \"1.4446184970002673_2.202362615152696\"^^xsd:string ;\n" +
					"        nif:isString             \"1936\\n\\nCoup leader Sanjurjo was killed in a plane crash on 20 July, leaving an effective command split between Mola in the North and Franco in the South. On 21 July, the fifth day of the rebellion, the Nationalists captured the main Spanish naval base at Ferrol in northwestern Spain. A rebel force under Colonel Beorlegui Canet, sent by General Emilio Mola, undertook the Campaign of Guipuzcoa from July to September. The capture of Guipuzcoa isolated the Republican provinces in the north. On 5 September, after heavy fighting the force took Irun, closing the French border to the Republicans. On 13 September, the Basques surrendered Madrid to the Nationalists, who then advanced toward their capital, Bilbao. The Republican militias on the border of Viscaya halted these forces at the end of September.\\n\"^^xsd:string ;\n" +
					"        nif:meanDateRange        \"19360531090000_19361022090000\"^^xsd:string ;\n" +
			        "        nif:topicModellingLabel  \"br (55443) span (9608) und (9024) die (7442) der (7143) \"^^xsd:string .\n" +
					"";

	public static String expectedStartingResponseTopic2 = 
			"@prefix rdf:   <http://www.w3.org/1999/02/22-rdf-syntax-ns#> .\n" +
					"@prefix xsd:   <http://www.w3.org/2001/XMLSchema#> .\n" +
					"@prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> .\n" +
					"@prefix nif:   <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> .\n" +
					"@prefix rdfs:  <http://www.w3.org/2000/01/rdf-schema#> .\n" +
					"\n" +
					"<http://dkt.dfki.de/examples/#char=0,806>\n" +
					"        a                        nif:RFC5147String , nif:String , nif:Context ;\n" +
					"        nif:beginIndex           \"0\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:centralGeoPoint      \"42.17561833333333_-4.075817777777777\"^^xsd:string ;\n" +
					"        nif:endIndex             \"806\"^^xsd:nonNegativeInteger ;\n" +
					"        nif:geoStandardDevs      \"1.4446184970002673_2.202362615152696\"^^xsd:string ;\n" +
					"        nif:isString             \"1936\\n\\nCoup leader Sanjurjo was killed in a plane crash on 20 July, leaving an effective command split between Mola in the North and Franco in the South. On 21 July, the fifth day of the rebellion, the Nationalists captured the main Spanish naval base at Ferrol in northwestern Spain. A rebel force under Colonel Beorlegui Canet, sent by General Emilio Mola, undertook the Campaign of Guipuzcoa from July to September. The capture of Guipuzcoa isolated the Republican provinces in the north. On 5 September, after heavy fighting the force took Irun, closing the French border to the Republicans. On 13 September, the Basques surrendered Madrid to the Nationalists, who then advanced toward their capital, Bilbao. The Republican militias on the border of Viscaya halted these forces at the end of September.\\n\"^^xsd:string ;\n" +
					"        nif:meanDateRange        \"19360531090000_19361022090000\"^^xsd:string ;\n" +
			        "        nif:topicModellingLabel  \"";

	 
}
