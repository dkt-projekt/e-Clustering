package de.dkt.eservices.emallet.modules;

import cc.mallet.classify.BalancedWinnowTrainer;
import cc.mallet.classify.C45Trainer;
import cc.mallet.classify.ClassifierTrainer;
import cc.mallet.classify.MCMaxEntTrainer;
import cc.mallet.classify.MaxEntGERangeTrainer;
import cc.mallet.classify.MaxEntGETrainer;
import cc.mallet.classify.MaxEntPRTrainer;
import cc.mallet.classify.MaxEntTrainer;
import cc.mallet.classify.NaiveBayesEMTrainer;
import cc.mallet.classify.NaiveBayesTrainer;
import cc.mallet.classify.WinnowTrainer;
import eu.freme.common.exception.ExternalServiceFailedException;

public class ClassificationTrainerFactory {

	public static ClassifierTrainer generateTrainer(String algorithm){
		
//		if(algorithm.equalsIgnoreCase("")){
//			return new AdaBoostTrainer(weakLearner);
//		}
//		if(algorithm.equalsIgnoreCase("")){
//			return new AdaBoostM2Trainer(weakLearner);
//		}
//		if(algorithm.equalsIgnoreCase("")){
//			return new BaggingTrainer(underlyingTrainerFactory);
//		}
		if(algorithm.equalsIgnoreCase("balancedwinnow")){
			return new BalancedWinnowTrainer();
		}
		if(algorithm.equalsIgnoreCase("c45")){
			return new C45Trainer();
		}
//		if(algorithm.equalsIgnoreCase("")){
//			return new cc.mallet.classify.ClassifierEnsembleTrainer(classifiers);
//		}
//		if(algorithm.equalsIgnoreCase("")){
//			return new ConfidencePredictingClassifierTrainer(underlyingClassifierTrainer, validationSet);
//		}
//		if(algorithm.equalsIgnoreCase("")){
//			return new DecisionTreeTrainer();
//		}
//		if(algorithm.equalsIgnoreCase("")){
//			return new FeatureSelectingClassifierTrainer(underlyingTrainer, featureSelector);
//		}
		if(algorithm.equalsIgnoreCase("maxentrange")){
			return new MaxEntGERangeTrainer();
		}
		if(algorithm.equalsIgnoreCase("maxent")){
			return new MaxEntTrainer();
		}
		if(algorithm.equalsIgnoreCase("maxentge")){
			return new MaxEntGETrainer();
		}
		if(algorithm.equalsIgnoreCase("maxentpr")){
			return new MaxEntPRTrainer();
		}
		if(algorithm.equalsIgnoreCase("mcmaxent")){
			return new MCMaxEntTrainer();
		}
		if(algorithm.equalsIgnoreCase("bayesem")){
			return new NaiveBayesEMTrainer();
		}
		if(algorithm.equalsIgnoreCase("bayes")){
			return new NaiveBayesTrainer();
		}
		if(algorithm.equalsIgnoreCase("winnow")){
			return new WinnowTrainer();
		}
		throw new ExternalServiceFailedException("Unsupported Agorithm");
	}
}
