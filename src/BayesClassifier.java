import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class BayesClassifier {
    static List<String> vocabulary= new ArrayList<>();
    static List<Double> probabilitiesA= new ArrayList<>(); // list of probabilities for each word in vocabulary to be in class A
    static List<Double> probabilitiesB= new ArrayList<>(); // list of probabilities for each word in vocabulary to be in class B
    static List<List<String>> TrainDataA= new ArrayList<>(); // list of training documents for class A
    static List<List<String>> TrainDataB= new ArrayList<>(); // list of training documents for class B
    static List<String> wordsInFileToClassify= new ArrayList<>(); // list of words in file to classify

    /**
     * Separates training documents out of a file, adds them to a list and builds a vocabulary.
     * @param file training file to read
     * @param documents list to which the documents are added
     * @throws IOException if an error occurs while reading the file
     */
    public static void readFileAndBuildVocab(File file, List<List<String>> documents) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] sentenceTokens = line.split(";"); // split documents if there are multiple in one line

            for (String sentenceToken : sentenceTokens) {
                String[] words = sentenceToken.trim().split("[,.\\s]+"); //split words by whitespaces, commas and dots

                List<String> documentWords = new ArrayList<>();
                for (String word : words) {
                    if(!vocabulary.contains(word)) // add word to vocabulary if it is not already in it
                        vocabulary.add(word);
                    documentWords.add(word); // add word to list of this document
                }
                documents.add(documentWords); // add document to list of documents
            }
        }
        reader.close();
    }

    /**
     * Reads a file to classify and adds the words to a list.
     * @param file file to classify
     * @throws IOException if an error occurs while reading the file
     */
    public static void readFileToClassify(File file) throws IOException {
        Scanner scanner = new Scanner(file);
        while (scanner.hasNext()) {     //read all words in file
            String word = scanner.next();
            wordsInFileToClassify.add(word);
        }
        scanner.close();
        List<String> wordsToRemove = new ArrayList<>();
        for(String word : wordsInFileToClassify) {
            if(!vocabulary.contains(word))
                wordsToRemove.add(word);
        }
        wordsInFileToClassify.removeAll(wordsToRemove); // remove words that are not in vocabulary
    }

    /**
     * Calculates the probabilities for each word in the vocabulary to be in class A and B
     * and adds them to the corresponding list.
     */
    static void calcProbWords() {
        for (String word : vocabulary) {
            int count= 0;
            for(List<String> document : TrainDataA) {
                for (String w : document) {
                    if(Objects.equals(w, word)){
                        count++;
                        break;
                    }
                }
            }
            double numerator= count + 1;    // Laplace smoothing
            double denominator= TrainDataA.size() + 2;
            probabilitiesA.add(numerator/denominator);
        }
        for(String word : vocabulary) {
            int count= 0;
            for(List<String> document : TrainDataB) {
                for (String w : document) {
                    if(Objects.equals(w, word)){
                        count++;
                        break;
                    }
                }
            }
            double numerator= count + 1;   // Laplace smoothing
            double denominator= TrainDataB.size() + 2;
            probabilitiesB.add(numerator/denominator);
        }
    }

    /**
     * Calculates the probability for a document to be in the given class.
     * @param classLabel class to calculate the probability for
     * @return probability for document to be in class
     */
    static double calcProbDoc(String classLabel) {
        double prob= 1;
        if(Objects.equals(classLabel, "A")) {
            double probClassA= (double) TrainDataA.size() / (TrainDataA.size() + TrainDataB.size());
            prob*= probClassA;  //multiply with probability of class A
            for (String word : wordsInFileToClassify) {
                int count= vocabulary.indexOf(word);
                prob*= probabilitiesA.get(count); //multiply probabilities
            }
            return prob;
        }
        if(Objects.equals(classLabel, "B")) {
            double probClassB= (double) TrainDataB.size() / (TrainDataA.size() + TrainDataB.size());
            prob*= probClassB; //multiply with probability of class B
            for (String word : wordsInFileToClassify) {
                int count= vocabulary.indexOf(word);
                prob*= probabilitiesB.get(count); //multiply probabilities
            }
            return prob;
        }
        return -1;
    }

    /**
     * Main method to classify a document.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java BayesClassifier <fileToClassify.txt>");
            System.exit(1);
        }
        File docA= new File("src/docA.txt");
        File docB= new File("src/docB.txt");
        try {
            readFileAndBuildVocab(docA, TrainDataA);
            readFileAndBuildVocab(docB, TrainDataB);
            readFileToClassify(new File("src/"+args[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        calcProbWords();
        double probDocA=calcProbDoc("A");
        double probDocB=calcProbDoc("B");
        if(probDocA>probDocB){
            System.out.println("Document is classified to class A");
            System.out.println("Probability for document to be in this class: " + probDocA);
        }
        else if(probDocA<probDocB){
            System.out.println("Document is classified to class B");
            System.out.println("Probability for document to be in this class: " + probDocB);
        }
        else {
            System.out.println("Document can not be classified because the probabilities are equal.");
        }
    }
}