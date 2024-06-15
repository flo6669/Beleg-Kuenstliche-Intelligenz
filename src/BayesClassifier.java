import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class BayesClassifier {
    static List<String> vocabulary= new ArrayList<>();
    static List<Double> probabilitiesA= new ArrayList<>();
    static List<Double> probabilitiesB= new ArrayList<>();
    static List<List<String>> TrainDataA= new ArrayList<>();
    static List<List<String>> TrainDataB= new ArrayList<>();
    static List<String> wordsInFileToClassify= new ArrayList<>();

    public static void readFileAndBuildVocab(File file, List<List<String>> documents) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] sentenceTokens = line.split(";"); // Trenne die Zeile anhand des Semikolons

            for (String sentenceToken : sentenceTokens) {
                String[] words = sentenceToken.trim().split("[,.\\s]+"); // Trenne den Satz in Wörter, wobei Kommas, Punkte und Leerzeichen entfernt werden

                List<String> sentenceWords = new ArrayList<>();
                for (String word : words) {
                    if(!vocabulary.contains(word)) // Füge das Wort zum Vokabular hinzu, wenn es noch nicht enthalten ist
                        vocabulary.add(word);
                    sentenceWords.add(word); // Füge jedes Wort zur Liste hinzu
                }
                documents.add(sentenceWords); // Füge die Liste der Wörter des Satzes zur Liste der Sätze hinzu
            }
        }
        reader.close();
    }

    public static void readFileToClassify(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            String[] words = line.trim().split("[,.\\s]+"); // Trenne den Satz in Wörter, wobei Kommas, Punkte und Leerzeichen entfernt werden

            for (String word : words) {
                wordsInFileToClassify.add(word); // Füge jedes Wort zur Liste hinzu
            }
        }
        reader.close();
        for(String word : wordsInFileToClassify) {
            if(!vocabulary.contains(word)) // Füge das Wort zum Vokabular hinzu, wenn es noch nicht enthalten ist
                wordsInFileToClassify.remove(word);
        }
    }

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
            double numerator= count + 1;
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
            double numerator= count + 1;
            double denominator= TrainDataB.size() + 2;
            probabilitiesB.add(numerator/denominator);
        }
    }

    static double calcProbDoc(String classLabel) {
        double prob= 1;
        if(Objects.equals(classLabel, "A")) {
            double probClassA= (double) TrainDataA.size() / (TrainDataA.size() + TrainDataB.size());
            prob*= probClassA;
            int count= 0;
            for (String word : vocabulary) {
                if(wordsInFileToClassify.contains(word)) {
                    prob*= probabilitiesA.get(count);
                } else {
                    prob*= 1 - probabilitiesA.get(count);
                }
                count++;
            }
            return prob;
        }
        if(Objects.equals(classLabel, "B")) {
            double probClassB= (double) TrainDataB.size() / (TrainDataA.size() + TrainDataB.size());
            prob*= probClassB;
            int count= 0;
            for (String word : vocabulary) {
                if(wordsInFileToClassify.contains(word)) {
                    prob*= probabilitiesB.get(count);
                } else {
                    prob*= 1 - probabilitiesB.get(count);
                }
                count++;
            }
            return prob;
        }
        return -1;
    }

    public static void main(String[] args) {
        if(args.length != 1) {
            System.out.println("Usage: java BayesClassifier <fileToClassify>");
            System.exit(1);
        }
        File docA= new File("src/docA.txt");
        File docB= new File("src/docB.txt");
        try {
            readFileAndBuildVocab(docA, TrainDataA);
            readFileAndBuildVocab(docB, TrainDataB);
            readFileToClassify(new File(args[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Vocabulary: " + vocabulary);
        for (List<String> words : TrainDataB) {
            System.out.println(words);
        }
        calcProbWords();
        System.out.println("Words in file to classify: " + wordsInFileToClassify);
        System.out.println("Probabilitie for document to be in class A: " + calcProbDoc("A"));
        System.out.println("Probabilitie for document to be in class B: " + calcProbDoc("B"));
    }
}