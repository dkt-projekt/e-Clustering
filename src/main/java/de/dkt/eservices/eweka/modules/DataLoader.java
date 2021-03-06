package de.dkt.eservices.eweka.modules;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.freme.common.exception.BadRequestException;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class DataLoader {

	public static Instances loadDataFromFile(String filePath) throws Exception {
		String fileType = filePath.substring(filePath.lastIndexOf('.')+1);
		if(fileType.equalsIgnoreCase("csv") || fileType.equalsIgnoreCase("arff") || fileType.equalsIgnoreCase("xrff")){
			InputStream is = new FileInputStream(filePath);
			   //Instances data1 = DataSource.read(filePath);
			   Instances data1 = DataSource.read(is);
			   return data1;
//			Instances data1 = DataSource.read(filePath);
//			return data1;
		}
		else{
			throw new BadRequestException("Unsupported input file format.");
		}
	}
	
	public static Instances loadDataFromString(String content) throws Exception {
		String encoding = "utf-8";
		InputStream is = null;
		BufferedReader br = null;
		is = new ByteArrayInputStream( content.getBytes(encoding) );
		br = new BufferedReader(new InputStreamReader(is,encoding));
		Instances data1 = new Instances(br);
//		
//		InputStream is = new ByteArrayInputStream( content.getBytes() );
//		Instances data1 = DataSource.read(is);
		return data1;
	}
	
}
