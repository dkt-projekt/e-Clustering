package de.dkt.eservices.eweka.exceptions;

import org.springframework.http.HttpStatus;

import eu.freme.common.exception.FREMEHttpException;

@SuppressWarnings("serial")
public class UnsupportedARFFGeneratorException extends FREMEHttpException{

	public UnsupportedARFFGeneratorException(){
		super("Unsupported ARFF Generator Type.", HttpStatus.BAD_REQUEST);
	}
}
