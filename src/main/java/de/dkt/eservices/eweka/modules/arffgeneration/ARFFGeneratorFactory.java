package de.dkt.eservices.eweka.modules.arffgeneration;

import de.dkt.eservices.eweka.exceptions.UnsupportedARFFGeneratorException;

public class ARFFGeneratorFactory {

	public static ARFFGenerator generateARFFGenerator(String generatorType) throws UnsupportedARFFGeneratorException {
		if(generatorType.equalsIgnoreCase("entityfrequencyappearance")){
			return new EntityAppearanceFrequency();
		}
		else if(generatorType.equalsIgnoreCase("entityappearance")){
			return new EntityAppearance();
		}
		else if(generatorType.equalsIgnoreCase("wordfrequencyappearance")){
			return new WordAppearanceFrequency();
		}
		else if(generatorType.equalsIgnoreCase("wordappearance")){
			return new WordAppearance();
		}
		throw new UnsupportedARFFGeneratorException();
	}
}
