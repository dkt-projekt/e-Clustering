package de.dkt.eservices.eweka.modules;

import java.util.LinkedList;
import java.util.List;

public class SortedAttributesList {

	List<EntityValuePair> attributes = null;
	
	public SortedAttributesList() {
		super();
		attributes = new LinkedList<>();
	}

	public void addInstance(EntityValuePair evp){
		if(attributes.isEmpty()){
			attributes.add(evp);
		}
		else{
			for (int i = 0; i < attributes.size(); i++) {
//				System.out.println(evp.value +"--"+attributes.get(i).value);
				if(evp.value>attributes.get(i).value){
					attributes.add(i, evp);
					break;
				}
			}
			attributes.add(evp);
		}
	}

	public List<EntityValuePair> getLimitedList(int limit){
		List<EntityValuePair> limitedList = new LinkedList<>();
		for (int i = 0; i < attributes.size() && i<limit; i++) {
			limitedList.add(attributes.get(i));
		}
		return limitedList;
	}
}
