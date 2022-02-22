Fine-Grained Entity Recognizer (FIGER)
=============================

This distribution contains the source code for the experiments presented in the following research publication ([PDF](http://xiaoling.github.com/pubs/ling-aaai12.pdf)):

    Xiao Ling and Daniel S. Weld (2012).
    "Fine-Grained Entity Recognition",
    in Proceedings OF THE TWENTY-SIXTH AAAI CONFERENCE ON ARTIFICIAL INTELLIGENCE (AAAI), 2012.

## Download the model file

One can test the trained model on the evaluation data or new data as they wish.

Run [`./downloadModel.sh`](downloadModel.sh) to download the [Model](https://drive.google.com/open?id=0B52yRXcdpG6MWlVXaTFXWVZQYjg) and save it at the root directory. Run `./downloadModel.sh new` for
an updated model.

A better [model](https://drive.google.com/open?id=0B52yRXcdpG6Mbm1TdHhYdVBmSnM) has been trained and can be fetched by `./downloadModel.sh new`. Change the config value accordingly.

## Requirement

sbt >= 0.13.0

## Replicate the experiments

To run the experiments in the AAAI-12 paper, you can proceed as follows:

    $ ./run.sh "aaai/exp.conf" &> aaai/exp.log

## Run FIGER on new data

To make predictions on new data, please see `package edu.washington.cs.figer.FigerSystem` for example code or run:

    $ sbt "runMain edu.washington.cs.figer.FigerSystem <text_file>"

Alternatively, you can change the parameter values (e.g. the input file name) in `config/figer.conf` and get a more structured output by running:

    $ ./run.sh "config/figer.conf"

## Make a stand-alone jar

    $ sbt assembly
    # the actual path might be different
    $ java -Xmx81926m -jar ./target/scala-2.13/figer-assembly-1.jar <text_file>

## A simple web interface

Run

    $ sbt ~jetty:start

and go to `localhost:8081/index.html` for a simple web demo.

You will also find a simple API endpoint returning the mentions in JSON format at 
`localhost:8081/api?text=your+text+to+annotate`.

### Returned format

The result of calling the API endpoint will look like this:

```
{
  "status": 200,
  "data": [
    {
      "mention": "japan ",
      "label": "/location@4.4072539635642283E-4,/organization/company@1.0969357624212788,/location/country@0.29935229660185275",
      "tag": "NN",
      "start_char": 0,
      "end_char": 8
    },
    ...
  ],
  "sentence_offsets": [
    0,
    135
  ]
}
```

## Training Data

The training data `train.data.gz` (Download [link](https://drive.google.com/open?id=0B52yRXcdpG6MMnRNV3dTdGdYQ2M)) is gzipped and serialized in [Protocol Buffer](http://code.google.com/p/protobuf/). Please see [entity.proto](entity.proto) in the code package for the definitions. Each `Mention` represents an entity mention defined by its token offsets together with the tokens in the sentence where the mention appears. The `labels` field shows the Freebase types of the underlying entity for the mention. Use the mapping in the next section to get the corresponding FIGER types.

In `config/figer.conf`, make the following changes:

    useModel=false
    modelFile=<the output model file>
    # the training file has to follow the specs from `entity.proto`. See `train.data.gz` for example
    trainFile=<training file>

Then run `./run.sh config/figer.conf` to train a new model (It will need over 10G memory and about an hour to finish).

### Mapping between Freebase MIDs and Wikipedia Titles

A mapping file from mids to titles is available [here](https://drive.google.com/open?id=0B52yRXcdpG6MaHA5ZW9CZ21MbVk).
