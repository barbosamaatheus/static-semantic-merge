package csvManager;

import gitManager.CollectedMergeMethodData;
import services.dataCollectors.modifiedLinesCollector.ModifiedMethod;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertTrue;

public class CsvManager {

    private static final String CSV_FILE_NAME = "/data/results-with-build-information.csv";

    public CsvManager(){}

    public String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(";"));
    }

    public String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    public void givenDataArray_whenConvertToCSV_thenOutputCreated(List<String[]> dataLines, String destPath) throws IOException {
        System.out.println(new File(destPath + "/data").mkdirs());
        File csvOutputFile = new File(destPath + CSV_FILE_NAME);
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
        assertTrue(csvOutputFile.exists());
    }

    public void transformCollectedDataIntoCsv(List<CollectedMergeMethodData> collectedMergeMethodDataList, List<ModifiedMethod> entrypoints, String destPath) throws IOException {
        List<String[]> dataLines = new ArrayList<>();
        dataLines.add(new String[] {"project", "merge commit", "className", "method", "left modifications","has_build","left deletions", "right modifications", "right deletions", "entrypoints"});

        for(CollectedMergeMethodData c : collectedMergeMethodDataList){
            dataLines.add(new String[] {c.getProject().getName(), c.getMergeCommit().getSHA(), c.getClassName(), c.getMethodSignature(), c.getLeftAddedLines().toString(),"true",
                    c.getLeftDeletedLines().toString(), c.getRightAddedLines().toString(), c.getRightDeletedLines().toString(), entrypoints.toString()});
        }
        this.givenDataArray_whenConvertToCSV_thenOutputCreated(dataLines, destPath);
    }

    public Boolean hasConflict(File file) throws IOException {
        if(file.exists()){
            String lines = "";
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(line.contains("true")){
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public void trimBlankLines(File file) throws IOException {
        if(file.exists()) {
            System.out.println(file.getAbsolutePath());
            String lines = "";
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if(!line.equals("")){
                        if(!lines.equals("")) lines = lines + "\n";
                        lines = lines + line ;
                    }
                }
            }

            PrintWriter writer = new PrintWriter(file);
            writer.write(lines);
            writer.close();
        }else{
            System.out.println("file does not exist at: " + file.getAbsolutePath());
        }
    }

    public void trimSpacesAndSpecialChars(File file) throws IOException {
        if (file.exists()) {
            System.out.println(file.getAbsolutePath());
            StringBuilder lines = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String header = br.readLine();
                lines.append(header).append("\n");

                String line;
                while ((line = br.readLine()) != null) {
                    String[] columns = line.split(";");
                    for (int i = 0; i < columns.length; i++) {
                        if (i != columns.length - 1) { // Apply trimming to all columns except the last one (entrypoints)
                            columns[i] = columns[i].replaceAll(" ", "");
                            columns[i] = columns[i].replaceAll("[+^?<>|]*", "");
                        }
                    }
                    String adjustedLine = String.join(";", columns);
                    System.out.println(adjustedLine);
                    lines.append(adjustedLine).append("\n");
                }
            }

            try (PrintWriter writer = new PrintWriter(file)) {
                writer.write(lines.toString());
            }
        } else {
            System.out.println("file does not exist at: " + file.getAbsolutePath());
        }
    }
}
