package it.unipi.dii.inginf.dmml.voiceidnotesapp.classification;

//CLASSE DA MODIFICARE IN BASE AL CLASSIFICATORE SCELTO

import it.unipi.dii.inginf.dmml.voiceidnotesapp.config.Config;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Standardize;
import weka.filters.unsupervised.instance.RemoveDuplicates;

import java.io.File;
import java.io.IOException;

public class Classifier {
    //private final String DATASET_PATH = "MFCCExtractor/data.csv";
    private static volatile Classifier classifierInstance;
    private final Standardize standardizeFilter;
    private final AttributeSelection attributeSelectionFilter;
    private final RandomForest randomForestClassifier;

    public Classifier() throws IOException {
        standardizeFilter = buildStandardizeFilter();
        attributeSelectionFilter = createAttributeSelectionFilter();
        randomForestClassifier = createRandomForestClassifier();
    }

    public static Classifier getClassifierInstance(boolean forceCreation) throws IOException {
        if(classifierInstance == null || forceCreation)
            synchronized (Classifier.class){
                classifierInstance = new Classifier();
            }
        return classifierInstance;
    }

    private Standardize buildStandardizeFilter() {
        Standardize filter = new Standardize();
        try {
            Instances tuples = Utils.loadDataset(Config.getInstance().getDatasetPath());
            Instances noDuplicates = removeDuplicates(tuples);
            filter.setInputFormat(noDuplicates);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter;
    }

    private AttributeSelection createAttributeSelectionFilter(){
        AttributeSelection filter = new AttributeSelection();
        try {
            Instances dataset = Utils.loadDataset(Config.getInstance().getDatasetPath());
            Instances modifiedDataset = removeDuplicates(dataset);
            CfsSubsetEval eval = new CfsSubsetEval();
            BestFirst search = new BestFirst();
            filter.setEvaluator(eval);
            filter.setSearch(search);
            filter.setInputFormat(modifiedDataset);
        } catch(Exception e){
            e.printStackTrace();
        }
        return filter;
    }

    private RandomForest createRandomForestClassifier(){
        RandomForest localRFClassifier = null;
        try {
            Instances dataset = Utils.loadDataset(Config.getInstance().getDatasetPath());
            Instances modifiedDataset = removeDuplicates(dataset);
            modifiedDataset = standardize(modifiedDataset);
            modifiedDataset = selectAttributes(modifiedDataset);
            localRFClassifier = new RandomForest();
            localRFClassifier.buildClassifier(modifiedDataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localRFClassifier;
    }

    private Instances selectAttributes(Instances dataset) throws Exception{
        Instances newDataset = Filter.useFilter(dataset, attributeSelectionFilter);
        return newDataset;
    }

    private Instances standardize(Instances dataset) throws Exception {
        Instances newDataset = Filter.useFilter(dataset, standardizeFilter);
        return newDataset;
    }

    private Instances removeDuplicates(Instances dataset) throws Exception{
        RemoveDuplicates filter = new RemoveDuplicates();
        filter.setInputFormat(dataset);
        Instances newDataset = Filter.useFilter(dataset, filter);
        return newDataset;
    }

    public String classify(Instances instanceToClassify){
        String label = null;

        try{
            Instances filteredInstances = standardize(instanceToClassify);
            filteredInstances = selectAttributes(filteredInstances);
            Instances dataset = Utils.loadDataset(Config.getInstance().getDatasetPath());
            double index = randomForestClassifier.classifyInstance(filteredInstances.firstInstance());
            label = dataset.classAttribute().value((int) index);
        } catch (Exception e){
            e.printStackTrace();
        }
        return label;
    }

    public void oversampleNewVoices() {
        weka.filters.supervised.instance.SMOTE smote = new weka.filters.supervised.instance.SMOTE();
        try {
            Instances voices = Utils.loadDataset(Utils.REGISTERED_DATASET_PATH);
            smote.setInputFormat(voices);
            smote.setPercentage(900);
            smote.setClassValue("last");
            Instances voices_smoted = Filter.useFilter(voices, smote);
            CSVSaver saver = new CSVSaver();
            saver.setInstances(voices_smoted);
            saver.setFile(new File(Utils.REGISTERED_DATASET_PATH));
            saver.writeBatch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}