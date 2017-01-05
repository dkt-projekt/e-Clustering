# e-Clustering

This service contains endpoints for classification, topic modeling and clustering.

## Cluster a NIF Collection of Documents
This endpoint generates a set of clusters from a given set of documents (in NIF collection format). 

### Endpoint
`https://api.digitale-kuratierung.de/api/e-clustering/clusterCollection`

### Input
The API conforms to the general NIF API specifications. For more details, see: http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input, informat and outformat parameters, the following parameters have to be set to perform Document Classification on the input:  
  
`language`: The language of the input text.  

`algorithm`: The algorithm to be used during clustering. Currently `em` and `kmeans` supported.  

`arffGeneratorType`: Determines the information from nthe collections that will be used as features for the clustering. There are four available values: `entityfrequencyappearance` (each feature is the number of times that a concrete ENTITY appears in the document), `entityappearance` (each feature is 1/0 depending if an ENTITY appears or not in the document), `wordfrequencyappearance` (each feature is the number of times that a concrete WORD appears in the document) and `wordappearance` (each feature is 1/0 depending if an WORD appears or not in the document).

`arffDataSetName`: internal name used in the ARFF generation process. It has no influence in the final result.

### Output
A JSON document containing information about the generated clusters. @TODO Include EXAMPLE  

### Example cURL post

`curl -X POST "https://api.digitale-kuratierung.de/api/e-clustering/clusterCollection?language=en&algorithm=em&arffGeneratorType=entityfrequencyappearance&arffDataSetName=DATASET"`


## e-DocumentClassification
This service determines the class of a given text. The different available classes depend on the model that is used (see next section). 

### Endpoint
`https://api.digitale-kuratierung.de/api/e-documentclassification `

### Input
The API conforms to the general NIF API specifications. For more details, see: http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input, informat and outformat parameters, the following parameters have to be set to perform Document Classification on the input:  
  
`language`: The language of the input text. For now, this service only admits German (`de`).  
  
`modelName`: The model that is used for text classification. There are some models available:  
`3pc`: Model generated using the data provided by 3pc (Mendelsohn letters) and their categories.  
`condat_types`: Model generated using the data provided by Condat and the types associated to every document.  
`condat_categories`: Model generated using the data provided by Condat and the categories associated to every document.  
`kreuzwerker_categories`: Model generated using the data provided by Kreuzwerker and the categories associated to every document.  

`modelPath`: [optional] This parameter is only used if another location for models is used inside the server. This parameter is meant for local installation of the service.

### Output
A document with NIF format annotated with the class assigned to the input text. The document class is included as an annotation in the Context element:  
Example cURL post:  
`curl -X POST "https://api.digitale-kuratierung.de/api/e-documentclassification?language=de&modelName=3pc&informat=text/plain&input=Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin"`


## e-TopicModelling
This service determines the ‘topic’ of a given text. A set of documents is used by Mallet to train a topic modelling model. It clusters the terms into the documents to model the ‘topics’, so each ‘topic’ is defined as the terms (words) that are included in the corresponding ‘topic’ (cluster). 

### Endpoint
`https://api.digitale-kuratierung.de/api/e-topicmodelling`

### Input
The API conforms to the general NIF API specifications. For more details, see: http://persistence.uni-leipzig.org/nlp2rdf/specification/api.html
In addition to the input, informat and outformat parameters, the following parameters have to be set to perform Topic Modelling on the input:  
`language`: The language of the input text. For now, this service only admits German (`de`).  
  
`modelName`: The model that is used for topic modelling. There are some models available:  
`condat`: Model generated using the data provided by Condat.  
`kreuzwerker`: Model generated using the data provided by Kreuzwerker.    
  
`modelPath`: [optional] This parameter is only used if another location for models is used inside the server. This parameter is meant for local installation of the service.
Output
A document with NIF format annotated with the topic assigned to the input text. The topic information is included as an annotation in the Context element:  
Example cURL post:  
`curl -X POST "https://api.digitale-kuratierung.de/api/e-topicmodelling?language=de&modelName=condat&informat=text/plain&input=Einige interessanten Texte die etwas witchtiges drinnen haben über Medizin"`


## Clustering ARFF file

This service clusters the input document collection. The document collection first has to be converted to a set of vectors. Note that this is not included in this service. The service expects the input in this particular format (see Output section for details and an example) and then proceeds to find clusters in this input data. The output contains information on the number of clusters found and specific values for the found clusters.

### Endpoint
`https://api.digitale-kuratierung.de/api/e-clustering/generateClusters`

### Input
The following parameters have to be set to perform clustering on the input:  
`algorithm`: The algorithm to be used during clustering. Currently EM and Kmeans are supported.  
  
`language`: The language of the input files. Currently `de` and `en` are supported.  

`inputFile`: The input for this service has to be in the form of an .arff file. See http://www.cs.waikato.ac.nz/ml/weka/arff.html for an explanation of this format.
The .arff file can be posted directly in a variable called inputFile.


## Clustering NIF collections

This service clusters the input document collection in NIF format. The document collection first has to be converted to a set of vectors. Note that this is not included in this service. The service expects the input in this particular format (see Output section for details and an example) and then proceeds to find clusters in this input data. The output contains information on the number of clusters found and specific values for the found clusters.

### Endpoint
`https://api.digitale-kuratierung.de/api/e-clustering/clusterCollection`

### Input
The following parameters have to be set to perform clustering on the input:  

`algorithm`: The algorithm to be used during clustering. Currently EM and Kmeans are supported.  

`language`: The language of the input files. Currently `de` and `en` are supported.  

`arffGeneratorType`: The type of information that will be included in the ARFF file that willl be processed by WEKA. There are two options: `wordfrequencyappearance`, that counts frequency of appearance of entities in the documents and `wordappearance` that assigns a 0 or 1 value to each entity depending if it is contained in the document or not.

`arffDataSetName`: The name to be given to the dataset in the ARFF file. This is just an internal name, any set of letter canbe provided and will not affect the result. It has been made available jsut for future modifications.

`body`: The nif content of the collection must be provided as the body of the request. The informat of the content must also be provided.


