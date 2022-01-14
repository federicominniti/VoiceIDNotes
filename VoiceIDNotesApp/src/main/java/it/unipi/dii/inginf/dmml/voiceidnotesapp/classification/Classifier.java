package it.unipi.dii.inginf.dmml.voiceidnotesapp.classification;

import it.unipi.dii.inginf.dmml.voiceidnotesapp.config.Config;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.CSVManager;
import it.unipi.dii.inginf.dmml.voiceidnotesapp.utils.Utils;
import weka.attributeSelection.CorrelationAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.lazy.IBk;
import weka.core.Instances;
import weka.core.converters.CSVSaver;
import weka.core.converters.ConverterUtils;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Standardize;
import weka.filters.supervised.instance.SMOTE;

import java.io.File;
import java.io.IOException;

public class Classifier {
    private static volatile Classifier classifierInstance;
    private final Standardize standardizeFilter;
    private final AttributeSelection attributeSelectionFilter;
    private final IBk ibkClassifier;

    /**
     * Classifier constructor, builds the standardization and attribute selection filters
     * and finally the IBk classifier
     * @throws IOException when the dataset cannot be loaded
     */
    private Classifier() throws IOException {
        standardizeFilter = buildStandardizeFilter();
        attributeSelectionFilter = createAttributeSelectionFilter();
        ibkClassifier = createIbkClassifier();
    }

    /**
     * Implementation of the singleton pattern
     * @param forceCreation is true when there is the need to recreate the classifier
     * @return a Classifier instance that can be used for classification
     * @throws IOException when the dataset cannot be loaded
     */
    public static Classifier getClassifierInstance(boolean forceCreation) throws IOException {
        if(classifierInstance == null || forceCreation)
            synchronized (Classifier.class){
                classifierInstance = new Classifier();
            }
        return classifierInstance;
    }

    /**
     * Builds the standardization filter
     * @return the standardization filter
     */
    private Standardize buildStandardizeFilter() {
        Standardize filter = new Standardize();
        try {
            Instances tuples = Utils.loadDataset(Utils.MERGED_DATASET);
            filter.setInputFormat(tuples);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filter;
    }

    /**
     * Creates the attribute selection filter, consisting of CorrelationAttributeEval
     * and Ranker with a threshold of 0.1
     * @return the attribute selection filter
     */
    private AttributeSelection createAttributeSelectionFilter(){
        AttributeSelection filter = new AttributeSelection();
        try {
            Instances dataset = Utils.loadDataset(Utils.MERGED_DATASET);
            CorrelationAttributeEval eval = new CorrelationAttributeEval();
            Ranker search = new Ranker();
            search.setThreshold(0.1);
            filter.setEvaluator(eval);
            filter.setSearch(search);
            filter.setInputFormat(dataset);
        } catch(Exception e){
            e.printStackTrace();
        }
        return filter;
    }

    /**
     * Creates the IBk classifier used for classification
     * @return the IBk classifier
     */
    private IBk createIbkClassifier(){
        IBk localIbkClassifier = null;
        try {
            Instances dataset = Utils.loadDataset(Utils.MERGED_DATASET);
            Instances modifiedDataset = standardize(dataset);
            modifiedDataset = selectAttributes(modifiedDataset);
            localIbkClassifier = new IBk();
            localIbkClassifier.setKNN(5);
            localIbkClassifier.buildClassifier(modifiedDataset);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return localIbkClassifier;
    }

    /**
     * Applies the attribute selection
     * @param dataset are the instances that will be filtered
     * @return a new set of filtered instances
     */
    private Instances selectAttributes(Instances dataset) throws Exception{
        Instances newDataset = Filter.useFilter(dataset, attributeSelectionFilter);
        return newDataset;
    }

    /**
     * Applies the standardization
     * @param dataset are the instances that will be filtered
     * @return a new set of filtered instances
     */
    private Instances standardize(Instances dataset) throws Exception {
        Instances newDataset = Filter.useFilter(dataset, standardizeFilter);
        return newDataset;
    }

    /**
     * Classifies a new tuple
     * @param instanceToClassify the instance to be classified
     * @return a String containing the username of the user whose voice has been recognized
     */
    public String classify(Instances instanceToClassify){
        String label = null;

        try{
            Instances filteredInstances = standardize(instanceToClassify);
            filteredInstances = selectAttributes(filteredInstances);
            Instances dataset = Utils.loadDataset(Utils.MERGED_DATASET);
            double index = ibkClassifier.classifyInstance(filteredInstances.firstInstance());
            label = dataset.classAttribute().value((int) index);
        } catch (Exception e){
            e.printStackTrace();
        }
        return label;
    }

    /**
     * Uses SMOTE to oversample the voice tuples of the last registered user.
     * The new synthetic instances are then saved in the CSV for future use in classification.
     */
    public static void oversampleNewVoices() {
        try {
            SMOTE smote = new SMOTE();
            ConverterUtils.DataSource dataSource = new ConverterUtils.DataSource(Utils.REGISTERED_DATASET_PATH);
            Instances voices = dataSource.getDataSet();
            voices.setClassIndex(voices.numAttributes()-1);
            smote.setInputFormat(voices);
            smote.setPercentage(900);
            smote.setClassValue("last");
            Instances voices_smoted = Filter.useFilter(voices, smote);
            CSVSaver saver = new CSVSaver();
            saver.setInstances(voices_smoted);
            saver.setFile(new File(Utils.REGISTERED_DATASET_PATH));
            saver.writeBatch();
            CSVManager.mergeCSV(Utils.REGISTERED_DATASET_PATH, Config.getInstance().getDatasetPath());
            Classifier.getClassifierInstance(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}