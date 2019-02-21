package raporter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Raporter {

    private static final String embeddedMailHtmlStart = "<html>\n\t<head><meta charset=\"UTF-8\"></head>\n\t<body style=\"background-color:#f0f0f0;\">\n\t\t<table cellspacing=\"5\" cellpadding=\"5\" width=\"1024px\">\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\" colspan=\"2\">\n\t\t\t\t\tDzienny raport z wykonania test&oacute;w automatycznych <br/> %s\n\t\t\t\t</td>\n\t\t\t</tr>";
    private static final String embeddedMailHtmlHeaderMiddle = "\t\t\t<tr align=\"left\">\n\t\t\t\t<td style=\"background-color:#9DFF9D;\" width=\"405px\">Liczba test&#xf3;w: %s</td>\n\t\t\t\t<td style=\"background-color:#ff8181;\">Testy pomini&#x119;te: %s</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"left\">\n\t\t\t\t<td style=\"background-color:#9DFF9D;\">Testy poprawne: %s</td>\n\t\t\t\t<td style=\"background-color:#ff0033;\">Testy b&#x142;&#x119;dne: %s</td>\n\t\t\t</tr>\n\t\t\t<tr style=\"background-color:#A9A9A9; color:#ffffff;\">\n\t\t\t\t<td>Historyjka</td>\n\t\t\t\t<td>Nieudany krok</td>\n\t\t\t</tr>";
    private static final String embeddedMailHtmlMiddleGroupBy = "\t\t\t<tr style=\"background-color:#A9A9A9; color:#ffffff;\">\n\t\t\t\t<td colspan=\"2\">%s</td>\n\t\t\t</tr>";
    private static final String embeddedMailHtmlMiddle = "\t\t\t<tr style=\"background-color:#cecccc;\">\n\t\t\t\t<td>%s</td>\n\t\t\t\t<td>%s</td>\n\t\t\t</tr>";
    private static final String embeddedMailHtmlEnd = "\t\t\t<tr align=\"right\">\n\t\t\t\t<td colspan=\"2\" style=\"font-size:12px; font-family:arial,helvetica,sans-serif;\">Copyright WAK @ ING</td>\n\t\t\t</tr>\n\t\t</table>\n\t</body>\n</html>";

    private static final String SUCCESSFUL = "scenariosSuccessful";
    private static final String FAILED = "scenariosFailed";
    private static final String PENDING = "scenariosPending";
    private static final String EMAIL_FILE_RAPORT = "01_EmailReportEmbedded.html";

    private String pathRead;
    private String pathWrite;

    private int scenariosSuccessful = 0;
    private int scenariosFailed = 0;
    private int scenariosPending = 0;

    private Map<String, List<String>> mapStats = new TreeMap<>();
    private Map<String, List<String>> mapHtml = new TreeMap<>();
    private List<RaportDTO> listRaportDTO = new ArrayList<>();

    public static void main(String[] args) {

        Raporter raport = new Raporter();
        try {
            raport.initArguments(args);
            raport.listAllFiles(raport.getPathRead(), raport.getMapStats(), ".stats");
            raport.listAllFiles(raport.getPathRead(), raport.getMapHtml(), ".html");
            raport.agregateStats();
            raport.setDTOBasedOnStatsMap();
            raport.setDTOBasedOnHtmlMap();

            // metoda stworzy posortowany raport, jednak bez grupowania wyników
            // raport.writeHtmlRaport(raport.createHtmlRaport());

            raport.writeHtmlRaport(raport.createHtmlRaportGroupedBy());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, List<String>> getMapStats() {
        return mapStats;
    }

    private Map<String, List<String>> getMapHtml() {
        return mapHtml;
    }

    private List<RaportDTO> getListRaportDTO() {
        return listRaportDTO;
    }

    private String getPathRead() {
        return pathRead;
    }

    private String getPathWrite() {
        return pathWrite;
    }

    private void initArguments(String[] args) throws Exception {
        try {
            this.pathRead = args[0];
            this.pathWrite = this.pathRead.substring(0, this.pathRead.lastIndexOf("\\")) + "\\" + "raport";
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
            throw new Exception("Brak parametru, przekaz pelna sciezke do raportu, folderu jbehave (...jenkins\\reporter\\jbehave)");
        }
    }

    private void listAllFiles(final String path, Map<String, List<String>> mapAllFiles, final String fileExtension) throws Exception {

        try (Stream<Path> paths = Files.walk(Paths.get(path), 1)) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath) && filePath.toString().endsWith(fileExtension)) {
                    try {
                        ArrayList<String> tempList = new ArrayList<>();
                        tempList.addAll(Files.readAllLines(filePath));
                        Collections.sort(tempList);
                        String key = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf('.'));
                        mapAllFiles.put(key, tempList);
                    } catch (Exception e) {
                        System.out.println("Brak plików JBehave do raportu.\n\n" + e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            throw new Exception("Brak plików JBehave do raportu.");
        }
    }

    private void agregateStats() {
        this.mapStats.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("BeforeStories") && !k.equalsIgnoreCase("AfterStories")) {
                this.scenariosSuccessful = scenariosSuccessful + this.getNumber(v, this.SUCCESSFUL);
                this.scenariosFailed = scenariosFailed + this.getNumber(v, this.FAILED);
                this.scenariosPending = scenariosPending + this.getNumber(v, this.PENDING);
            }
        });
    }

    private void setDTOBasedOnStatsMap() {
        this.mapStats.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("BeforeStories") && !k.equalsIgnoreCase("AfterStories")) {
                this.listRaportDTO.add(new RaportDTO(k, "failText", this.getNumber(v, this.FAILED), this.getNumber(v, this.PENDING), this.getNumber(v, this.SUCCESSFUL), "link", 0));
            }
        });
    }

    private void setDTOBasedOnHtmlMap() {
        this.mapHtml.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("BeforeStories") && !k.equalsIgnoreCase("AfterStories")) {
                String failText = v.get(getIdMessageFailed(v, "(FAILED)"));
                String normalize = replaceMultiple(failText, "ąćęłńóśźżĄĆĘŁŃÓŚĆŹŻ", "acelnoszzACELNOSZZ");
                this.getRaportDTO(k).setMessageFailed(normalize);
            }
        });
    }

    private List<String> getCategories() {
        return this.getListRaportDTO()
                .stream()
                .filter(raportDTO -> raportDTO.getScenariosFailed() == 1)
                .map(RaportDTO::getTestCategory)
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    private RaportDTO getRaportDTO(String testName) {
        return this.getListRaportDTO().stream().filter(raportDTO -> raportDTO.getTestName().equalsIgnoreCase(testName)).findFirst().get();
    }

    private int getNumber(List<String> list, String kind) {
        String value = null;
        Optional<String> opt = Optional.of(list.stream().filter(str -> str.contains(kind)).findFirst().orElse("0"));
        if (opt.isPresent() && !opt.get().equals("0")) {
            value = opt.get().substring(opt.get().lastIndexOf("=") + 1);
        } else {
            value = opt.get();
        }
        return Integer.parseInt(value);
    }

    private int getIdMessageFailed(List<String> list, String messageFailed) {
        int index = 0;
        Optional<String> opt = list.stream().filter(str -> str.contains(messageFailed)).findFirst();
        if (opt.isPresent()) {
            index = list.indexOf(opt.get());
        }
        return index;
    }

    private String createHtmlRaport() {
        //reset raportDTO objects
        this.getListRaportDTO().forEach(raportDTO -> raportDTO.setTakenToRaport(0));

        StringBuffer stringHtmlRaport = new StringBuffer();
        stringHtmlRaport.append(String.format(embeddedMailHtmlStart, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        stringHtmlRaport.append(String.format(embeddedMailHtmlHeaderMiddle, this.getListRaportDTO().size(), scenariosPending, scenariosSuccessful, scenariosFailed));

        for(int i = 0; i < this.getListRaportDTO().size(); i++) {
            if(this.getListRaportDTO().get(i).getScenariosFailed() == 1 && this.getListRaportDTO().get(i).getTakenToRaport() == 0){
                stringHtmlRaport.append(String.format(embeddedMailHtmlMiddle, this.getListRaportDTO().get(i).getTestName(), this.getListRaportDTO().get(i).getMessageFailed()));
                this.getListRaportDTO().get(i).setTakenToRaport(1);
            }
        }

        stringHtmlRaport.append(String.format(embeddedMailHtmlEnd));
        return stringHtmlRaport.toString();
    }

    private String createHtmlRaportGroupedBy() {
        //reset raportDTO objects
        this.getListRaportDTO().forEach(raportDTO -> raportDTO.setTakenToRaport(0));

        StringBuffer stringHtmlRaport = new StringBuffer();
        stringHtmlRaport.append(String.format(embeddedMailHtmlStart, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        stringHtmlRaport.append(String.format(embeddedMailHtmlHeaderMiddle, this.getListRaportDTO().size(), scenariosPending, scenariosSuccessful, scenariosFailed));

        for(int i = 0; i < this.getCategories().size(); i++) {
            stringHtmlRaport.append(String.format(embeddedMailHtmlMiddleGroupBy, this.getCategories().get(i).equals("XXX") ? "INNE" : this.getCategories().get(i)));
            for(int j = 0; j < this.getListRaportDTO().size(); j++) {
                if(this.getListRaportDTO().get(j).getScenariosFailed() == 1 && this.getListRaportDTO().get(j).getTestName().contains(this.getCategories().get(i)) && this.getListRaportDTO().get(j).getTakenToRaport() == 0){
                    stringHtmlRaport.append(String.format(embeddedMailHtmlMiddle, this.getListRaportDTO().get(j).getTestName(), this.getListRaportDTO().get(j).getMessageFailed()));
                    this.getListRaportDTO().get(j).setTakenToRaport(1);
                }
            }
        }

        for(int i = 0; i < this.getListRaportDTO().size(); i++) {
            if(this.getListRaportDTO().get(i).getTakenToRaport() == 0 && this.getListRaportDTO().get(i).getScenariosFailed() == 1) {
                stringHtmlRaport.append(String.format(embeddedMailHtmlMiddle, this.getListRaportDTO().get(i).getTestName(), this.getListRaportDTO().get(i).getMessageFailed()));
                this.getListRaportDTO().get(i).setTakenToRaport(1);
            }
        }

        stringHtmlRaport.append(String.format(embeddedMailHtmlEnd));

        return stringHtmlRaport.toString();
    }

    private void writeHtmlRaport(String raport) throws Exception {

        if (raport != null && !raport.isEmpty()) {
            Path path = Paths.get(this.pathWrite);
            Files.createDirectories(path);
            path = Paths.get(this.getPathWrite() + "\\" + this.EMAIL_FILE_RAPORT);
            Files.deleteIfExists(path);
            Files.write(path, raport.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new Exception("Brak danych do raportu");
        }
    }

    private static String replaceMultiple(String mainText, String toBeReplaces, String forWhichWillBeReplaced) {
        List<String> toBeReplacesList = Arrays.asList(toBeReplaces.split(""));
        List<String> forWhichWillBeReplacedList = Arrays.asList(forWhichWillBeReplaced.split(""));
        for (String textChar : toBeReplacesList) {
            if (mainText.contains(textChar)) {
                int inCharIndex = toBeReplaces.indexOf(textChar);
                mainText = mainText.replace(textChar, forWhichWillBeReplacedList.get(inCharIndex));
            }
        }
        return mainText;
    }
    
}
