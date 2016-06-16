# e-Clustering

This service contains endpoints for classification, topic modeling and clustering.

# e-DocumentClassification
This service determines the class of a given text. The different available classes depend on the model that is used (see next section). 

## Endpoint
`http://dev.digitale-kuratierung.de/api/e-documentclassification`

### Input
The API conforms to the general NIF API specifications. For more details, see: http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input, informat and outformat parameters, the following parameters have to be set to perform Document Classification on the input:  
  
`language`: the language of the input text. For now, this service only admits german (`de`).  
  
`modelName`: the model that is used for text classification. There are some models available:  
`3pc`: model generated using the data provided by 3pc (Mendelsohn letters) and their categories.  
`condat_types`: model generated using the data provided by Condat and the types associated to every document.  
`condat_categories`: model generated using the data provided by Condat and the categories associated to every document.  
`kreuzwerker_categories`: model generated using the data provided by Kreuzwerker and the categories associated to every document.  

`modelPath`: [optional] this parameter is only used is other location for models is used inside the server. This parameter has been meant for local installation of the service.

### Output
A document with NIF format annotated with the class assigned to the input text. The document class is included as an annotation in the Context element:  
Example cURL post:  
`curl -X POST "http://dev.digitale-kuratierung.de/api/e-documentclassification?language=de&modelName=3pc&informat=text/plain&input=Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin"`


# e-TopicModelling
This service determines the ‘topic’ of a given text. A set of documents is used by Mallet to train a topic modelling model. It clusters the terms into the documents to model the ‘topics’, so each ‘topic’ is defined as the terms (words) that are included in the corresponding ‘topic’ (cluster). 

## Endpoint
`http://dev.digitale-kuratierung.de/api/e-topicmodelling`

### Input
The API conforms to the general NIF API specifications. For more details, see: http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input, informat and outformat parameters, the following parameters have to be set to perform Topic Modelling on the input:  
`language`: the language of the input text. For now, this service only admits german (`de`).  
  
`modelName`: the model that is used for topic modelling. There are some models available:  
`condat`: model generated using the data provided by Condat.  
`kreuzwerker`: model generated using the data provided by Kreuzwerker.    
  
`modelPath`: [optional] this parameter is only used is other location for models is used inside the server. This parameter has been meant for local installation of the service.
Output
A document with NIF format annotated with the topic assigned to the input text. The topic information is included as an annotation in the Context element:  
Example cURL post:  
`curl -X POST "http://dev.digitale-kuratierung.de/api/e-topicmodelling?language=de&modelName=condat&informat=text/plain&input=Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin"`


# e-Clustering

This service clusters the input document collection. The document collection first has to be converted to a set of vectors. Note that this is not included in this service. The service expects the input in this particular format (see Output section for details and an example) and then proceeds to find clusters in this input data. The output contains information on the number of clusters found and specific values for the found clusters.

## Endpoint
`http://dev.digitale-kuratierung.de/api/e-clustering/generateClusters`

### Input
The following parameters have to be set to perform clustering on the input:  
`algorithm`: the algorithm to be used during clustering. Currently EM and Kmeans are supported.  
  
`language`: the language of the input files. Currently de and en are supported.  

`inputFile`: the input for this service has to be in the form of an .arff file. See http://www.cs.waikato.ac.nz/ml/weka/arff.html for an explanation of this format.
The .arff file can be posted directly in a variable called inputFile.


