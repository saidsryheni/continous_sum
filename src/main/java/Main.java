import java.io.*;
import java.text.DecimalFormat;

public class Main {

    private static DecimalFormat df2 = new DecimalFormat("0.00");
    private static DecimalFormat df4 = new DecimalFormat("0.0000");
    private static DecimalFormat df6 = new DecimalFormat("0.000000");
    private static BufferedWriter resultWriter;
    private static BufferedWriter overallWriter;

    static {
        try {
            resultWriter = new BufferedWriter(new FileWriter("Detailed_Results.txt"));
            overallWriter = new BufferedWriter(new FileWriter("Overall_Results.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Main() throws IOException {
    }

    public static void main(String[] args) throws IOException {
        System.out.println("========= Start Loading Graph =========");
        long startTime = System.currentTimeMillis();
        loadGraph();
        long endTime = System.currentTimeMillis();
        printExecutionTime("Loading Graph:", endTime - startTime);
        System.out.println("========= Finish Loading Graph =========");

        System.out.println("Nodes in Graph: " + Graph.getInstance().getNodes().size());
        int leaves = 0;
        for(Node node : Graph.getInstance().getNodes()){
            if(node.getChildren().size() == 0) leaves++;
        }
        System.out.println("Num of Leaves in Graph: " + leaves);
        int roots = 0;
        for(Node node : Graph.getInstance().getNodes()){
            if(node.getParents().size() == 0) roots++;
        }
        System.out.println("Num of Roots in Graph: " + roots);

        System.out.println("========= Start Loading All Datasets =========");
        startTime = System.currentTimeMillis();
        loadTriples();
        endTime = System.currentTimeMillis();
        printExecutionTime("Loading All Datasets:", endTime - startTime);
        System.out.println("========= Finish Loading All Datasets =========");

    }

    public static void printExecutionTime(String message, long time){
        System.out.println(message + " " + time + "ms | " + df2.format(time / 1000.0) + "s | " + df2.format(time / 60_000.0) + "m");
    }

    public static void loadGraph() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Constants.ANCESTORS_FILE_PATH));
        String line = reader.readLine();
        int lineCount = 1;
        Graph graph = Graph.getInstance();
        while((line = reader.readLine()) != null){
            ++lineCount;
            String[] lineArray = line.split("\t");
            if(lineArray.length != 2){
                System.out.println("Graph file doesn't contain 2 values on line " + lineCount);
                System.exit(0);
            }
            String nodeName = lineArray[0], ancestorsLine = lineArray[1];
            String[] ancestorsArray = ancestorsLine.split(";http");
            for(String ancestorName : ancestorsArray){
                if(!ancestorName.startsWith("http")) ancestorName = "http" + ancestorName;
                graph.addEdge(ancestorName, nodeName);
            }
        }
        reader.close();
    }

    public static void loadTriples() throws IOException {
        File datasetFolders = new File(Constants.DATASET_FOLDER_PATH);
        if(!datasetFolders.isDirectory()){
            System.out.println("Dataset folder is not a folder: " + Constants.DATASET_FOLDER_PATH);
            System.exit(0);
        }
        double accuracy = 0.0;
        int numOfDatasets = 0;
        for(File datasetFolder : datasetFolders.listFiles()){
            if(!datasetFolder.isDirectory()){
                System.out.println("One of dataset folders is not a folder: " + datasetFolder.getAbsolutePath());
                System.exit(0);
            }
            String folderName = datasetFolder.getName();
            double folderAccuracy = 0.0;
            int folderDatasets = 0;
            double numOfExplicitTriples = 0, numOfFacts = 0, numOfConflicts = 0, numOfSources = 0, numOfTotalTriples = 0;
            double tp = 0, fn = 0, fp = 0, tn = 0;
            for(File folder : datasetFolder.listFiles()){
                if(!folder.isDirectory()){
                    System.out.println("One of dataset folders is not a folder: " + folder.getAbsolutePath());
                    System.exit(0);
                }
                numOfDatasets++;
                folderDatasets++;
                System.out.println("========= Start Loading Dataset: " + folderName + "\\" + folder.getName() + " =========");
                long startTime = System.currentTimeMillis();
                numOfExplicitTriples += loadDatasetTriples(folder.getAbsolutePath());
                long endTime = System.currentTimeMillis();
                printExecutionTime("Loading Dataset: " + folderName + "\\" + folder.getName(), endTime - startTime);
                System.out.println("========= Finish Loading Dataset: " + folderName + "\\" + folder.getName() + " =========");

                System.out.println("========= Triple Store Statistics =========");
                System.out.println("Num of Triples: " + TripleStore.getInstance().getTriples().size());
                System.out.println("Num of Sources: " + TripleStore.getInstance().getSources().size());
                System.out.println("Num of Conflicts: " + TripleStore.getInstance().getConflicts().size());
                System.out.println("Num of Facts: " + TripleStore.getInstance().getFacts().size());

                System.out.println("========= Start Calculating Correct Facts =========");
                startTime = System.currentTimeMillis();
                calcConflictCorrectFacts();
                endTime = System.currentTimeMillis();
                printExecutionTime("Calculating Correct Facts:", endTime - startTime);
                System.out.println("========= Finish Calculating Correct Facts =========");

                System.out.println("========= Start Calculating Source Accuracy =========");
                startTime = System.currentTimeMillis();
                calcSourcesAccuracy();
                endTime = System.currentTimeMillis();
                printExecutionTime("Calculating Source Accuracy:", endTime - startTime);
                System.out.println("========= Finish Calculating Source Accuracy =========");

                System.out.println("========= Start Calculating Dataset Accuracy =========");
                startTime = System.currentTimeMillis();
                double datasetAccuracy = calcDatasetAccuracy(folder.getAbsolutePath());
                accuracy += datasetAccuracy;
                folderAccuracy += datasetAccuracy;
                endTime = System.currentTimeMillis();
                printExecutionTime("Calculating Dataset Accuracy:", endTime - startTime);
                String datasetNum = folder.getAbsolutePath().substring(folder.getAbsolutePath().lastIndexOf('_') + 1);
                System.out.println(folderName + "\\dataset" + datasetNum + " Error: " + df4.format(datasetAccuracy));
                System.out.println(folderName + "\\dataset" + datasetNum + " Accuracy: " + df2.format((1 - datasetAccuracy) * 100) + "%");
                resultWriter.write(String.format("%-25s", folderName + "\\dataset" + datasetNum) + String.format("%20s", "Error: " + df4.format(datasetAccuracy)) + String.format("%20s\n", "Accuracy: " + df2.format((1 - datasetAccuracy) * 100) + "%"));
                System.out.println("========= Finish Calculating Dataset Accuracy =========");
                System.out.println("========= Start Calculating Measure =========");
                startTime = System.currentTimeMillis();
                loadCorrectFacts();
                endTime = System.currentTimeMillis();
                printExecutionTime("Calculating Measures:", endTime - startTime);
                System.out.println("========= Finish Calculating Measures =========");
                numOfFacts += TripleStore.getInstance().getFacts().size();
                numOfConflicts += TripleStore.getInstance().getConflicts().size();
                numOfSources += TripleStore.getInstance().getSources().size();
                numOfTotalTriples += TripleStore.getInstance().getTriples().size();
                tp += TruthStore.getInstance().getTp();
                fn += TruthStore.getInstance().getFn();
                fp += TruthStore.getInstance().getFp();
                tn += TruthStore.getInstance().getTn();
            }
            overallWriter.write(String.format("%-8s", folderName) + "ExplicitTriples: " + df4.format(numOfExplicitTriples / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "Facts: " + df4.format(numOfFacts / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "Conflicts: " + df4.format(numOfConflicts / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "Sources: " + df4.format(numOfSources / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "TotalTriples: " + df4.format(numOfTotalTriples / folderDatasets) + "\n");

            overallWriter.write(String.format("%-8s", folderName) + "TP: " + df4.format(tp / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "FN: " + df4.format(fn / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "FP: " + df4.format(fp / folderDatasets) + "\n");
            overallWriter.write(String.format("%-8s", folderName) + "TN: " + df4.format(tn / folderDatasets) + "\n");

            overallWriter.write(String.format("%-8s", folderName) + String.format("%20s", "Error: " + df4.format(folderAccuracy / folderDatasets)) + String.format("%20s\n", "Accuracy: " + df2.format((1 - folderAccuracy / folderDatasets) * 100) + "%"));
        }
        System.out.println("Algorithm Average Error: " + df4.format(accuracy / numOfDatasets));
        System.out.println("Algorithm Average Accuracy: " + df2.format((1 - accuracy / numOfDatasets) * 100) + "%");

        overallWriter.write(String.format("%-30s", "Algorithm Average Error: ") + df4.format(accuracy / numOfDatasets) + "\n");
        overallWriter.write(String.format("%-30s", "Algorithm Average Accuracy: ") + df2.format((1 - accuracy / numOfDatasets) * 100) + "%\n");
        resultWriter.close();
        overallWriter.close();
    }

    public static int loadDatasetTriples(String folderPath) throws IOException {
        TripleStore.clearInstance();
        String datasetNum = folderPath.substring(folderPath.lastIndexOf('_') + 1);
        BufferedReader reader = new BufferedReader(new FileReader(folderPath + "\\facts_" + datasetNum + ".csv"));
        String line = reader.readLine();
        int lineCount = 1;
        TripleStore tripleStore = TripleStore.getInstance();
        System.out.print("Triples Loaded: ");
        while((line = reader.readLine()) != null) {
            ++lineCount;
            if(lineCount % 100_000 == 0){
                if(lineCount % 500_000 == 0) System.out.print("\nTriples Loaded: " + lineCount);
                else if(lineCount == 100_000) System.out.print(lineCount);
                else System.out.print(" \\ " + lineCount);
            }
            String[] lineArray = line.split("\t");
            if(lineArray.length != 4){
                System.out.println("Dataset file doesn't contain 4 values on line " + lineCount);
                System.exit(0);
            }
            String subject = lineArray[1];
            String predicate = "b";
            String object = lineArray[2];
            String sourceName = "source" + lineArray[3];
            tripleStore.addTriple(sourceName, subject, predicate, object);
        }
        System.out.println();
        return lineCount - 1;
    }

    public static void calcConflictCorrectFacts(){
        TripleStore.getInstance().getConflicts().forEach(Conflict::calcCorrectFacts);
    }

    public static void calcSourcesAccuracy(){
        TripleStore.getInstance().getSources().forEach(Source::calcAccuracy);
    }

    public static double calcDatasetAccuracy(String folderPath) throws IOException {
        String datasetNum = folderPath.substring(folderPath.lastIndexOf('_') + 1);
        String fullDatasetNum = datasetNum;
        if(folderPath.contains("-")) fullDatasetNum = folderPath.substring(folderPath.lastIndexOf('-') + 1);
        BufferedReader reader = new BufferedReader(new FileReader(folderPath + "\\Output_acc_" + datasetNum + ".txt"));
        BufferedWriter writer = new BufferedWriter(new FileWriter("source_accuracy\\Accuracy_" + fullDatasetNum + ".csv"));
        writer.write("Source,Correct_Triples,Total_Triples,Algorithm_Accuracy,Actual_Accuracy,Error\n");
        String line;
        int lineCount = 0;
        double accuracyDiff = 0;
        while((line = reader.readLine()) != null){
            lineCount++;
            String[] lineArray = line.split("\t");
            if(lineArray.length != 2){
                System.out.println("Accuracy file doesn't contain 2 values on line " + lineCount);
                System.exit(0);
            }
            String sourceName = lineArray[0];
            double actualAccuracy = Double.parseDouble(lineArray[1]);
            Source source = TripleStore.getInstance().getSource(sourceName);
            writer.write(sourceName + "," + source.getCorrectTriples() + "," + source.getNumOfTriples() + "," + df6.format(source.getAccuracy()) + "," + df6.format(actualAccuracy) + "," + df6.format(Math.abs(actualAccuracy - source.getAccuracy())) + "\n");
            accuracyDiff += Math.abs(actualAccuracy - source.getAccuracy());
        }
        writer.close();
        reader.close();
        return accuracyDiff / lineCount;
    }

    public static void loadCorrectFacts() throws IOException {
        TruthStore.clearInstance();
        BufferedReader reader = new BufferedReader(new FileReader(Constants.GROUND_TRUTH_FILE_PATH));
        String line = reader.readLine();
        int lineCount = 1;
        TruthStore truthStore = TruthStore.getInstance();
        while((line = reader.readLine()) != null) {
            ++lineCount;
            String[] lineArray = line.split("\t");
            if(lineArray.length != 3){
                System.out.println("Ground truth file doesn't contain 3 values on line " + lineCount);
                System.exit(0);
            }
            String subject = lineArray[0];
            String predicate = "b";
            String object = lineArray[2];
            truthStore.addFact(subject, predicate, object);
        }
        reader.close();
        truthStore.calcMeasures();
    }
}
