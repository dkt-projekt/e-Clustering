package de.dkt.eservices.eweka.modules;

import java.util.HashMap;
import org.json.JSONObject;

public class ClusteringOutputConversion {
	
	public static HashMap<String, HashMap<String, Double>> convertJSONtoMap(JSONObject j){
		
		HashMap<String, HashMap<String, Double>> result = new HashMap<String, HashMap<String, Double>>();
		
		JSONObject results = j.getJSONObject("results");
		JSONObject clusters = results.getJSONObject("clusters");
			
		for (Object clusterId : clusters.keySet()){
			JSONObject cid = clusters.getJSONObject(clusterId.toString());
			JSONObject entities = cid.getJSONObject("entities");
			HashMap<String, Double> innerMap = new HashMap<String, Double>();
			for (Object entity : entities.keySet()){
				JSONObject jEnt = entities.getJSONObject(entity.toString());
				Object label = jEnt.get("label");
				Object meanVal = jEnt.get("meanValue");
				innerMap.put(label.toString(), Double.parseDouble(meanVal.toString()));
				
			}
			result.put(clusterId.toString(), innerMap);
		}
		return result;
		
	}
	
	
	public static void main(String[] args) throws Exception {
		
		String jsonString = 
				"{\"results\": {\n" +
						" \"numberClusters\": 2,\n" +
						" \"clusters\": {\n" +
						"  \"cluster1\": {\n" +
						"   \"clusterId\": 1,\n" +
						"   \"entities\": {\n" +
						"    \"entity1\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"chris(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity2\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"1.0\"\n" +
						"    },\n" +
						"    \"entity9\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"hunter(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity7\": {\n" +
						"     \"meanValue\": 2,\n" +
						"     \"label\": \"geoff(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity8\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"chris gaskill(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity5\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"1968\"\n" +
						"    },\n" +
						"    \"entity6\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"march\"\n" +
						"    },\n" +
						"    \"entity3\": {\n" +
						"     \"meanValue\": 2,\n" +
						"     \"label\": \"colin tonks(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity4\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"2001\"\n" +
						"    }\n" +
						"   }\n" +
						"  },\n" +
						"  \"cluster2\": {\n" +
						"   \"clusterId\": 2,\n" +
						"   \"entities\": {\n" +
						"    \"entity1\": {\n" +
						"     \"meanValue\": 4,\n" +
						"     \"label\": \"chris(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity12\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"david(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity2\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"1.0\"\n" +
						"    },\n" +
						"    \"entity10\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"december 13, 2001\"\n" +
						"    },\n" +
						"    \"entity11\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"26-02\"\n" +
						"    },\n" +
						"    \"entity7\": {\n" +
						"     \"meanValue\": 4,\n" +
						"     \"label\": \"geoff(file:///c:/users/pebo01/workspace/e-nlp/dummyuri)\"\n" +
						"    },\n" +
						"    \"entity4\": {\n" +
						"     \"meanValue\": 1,\n" +
						"     \"label\": \"2001\"\n" +
						"    }\n" +
						"   }\n" +
						"  }\n" +
						" }\n" +
						"}}";
		
		JSONObject jo = new JSONObject(jsonString);
		convertJSONtoMap(jo);
		
	}

}
