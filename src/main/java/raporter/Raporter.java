package raporter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Raporter {

    private static final String embeddedMailHtmlStart = "<html>\n\t<head><meta charset=\"UTF-8\"></head>\n\t<body style=\"background-color:#f0f0f0;\">\n\t\t<table cellspacing=\"5\" style=\"width:1024px;\">\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\">\n\t\t\t\t\tDzienny raport z wykonania test&oacute;w automatycznych <br/> %s\n\t\t\t\t</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\">\n\t\t\t\t\t<table cellspacing=\"5\" cellpadding=\"5\" style=\"width:1024px; border: 0px; font-size:12px; font-family:arial,helvetica,sans-serif; font-weight:bold;\">\n";
    private static final String embeddedMailHtmlHeaderMiddle = "\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#9DFF9D; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">Liczba test&#xf3;w: %s</td>\n\t\t\t\t\t\t\t\t\t<td style=\"background-color:#ff8181; width:405px;\">Testy pomini&#x119;te: %s</td>\n\t\t\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#9DFF9D; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td style=\"background-color:#9DFF9D; width:405px;\">Testy poprawne: %s</td>\n\t\t\t\t\t\t\t\t\t<td style=\"background-color:#d60000; width:405px;\">Testy b&#x142;&#x119;dne: %s</td>\n\t\t\t\t\t\t\t\t\t</table>\n\t\t\t\t</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\">\n\t\t\t\t\t<table cellspacing=\"5\" cellpadding=\"5\" style=\"width:1024px; border: 0px; font-size:12px; font-family:arial,helvetica,sans-serif;\">\n\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#d60000; color:#000000; height:40px; font-weight: bold;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">Historyjka</td>\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">Nieudany krok</td>\n\t\t\t\t\t\t\t\t\t</tr>\n";
    private static final String embeddedMailHtmlMiddleGroupBy = "\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#d60000; height:40px; font-weight: bold;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"810px\" colspan=\"2\">%s</td>\n\t\t\t\t\t\t\t\t\t</tr>\n";
    private static final String embeddedMailHtmlMiddle = "\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#cecccc; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">%s</td>\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">%s</td>\n\t\t\t\t\t\t\t\t\t</tr>\n";
    private static final String embeddedMailHtmlEnd = "\n\t\t\t\t\t</table>\n\t\t\t\t</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"right\">\n\t\t\t\t<td style=\"font-size:12px; font-family:arial,helvetica,sans-serif;\">Copyright WAK @ ING</td>\n\t\t\t</tr>\n\t\t</table>\n\t</body>\n</html>";

    private Map<String, List<String>> mapStats = new TreeMap<>();
    private Map<String, List<String>> mapHtml = new TreeMap<>();
    private List<RaportDTO> listRaportDTO = new LinkedList<>();
    private String pathRead;
    private String pathWrite;
    private String emailFileRaport = "01_EmailReportEmbedded.html";
    private String groupByRegex = "[AFP]{1}\\d{2}(?!\\d)";
    private List<String> categories = new LinkedList<>();

    private int scenariosFailed = 0;
    private int scenariosPending = 0;
    private int scenariosSuccessful = 0;

    public static void main(String[] args) {

        Raporter raport = new Raporter();
        try {
            raport.initArguments(args);
            raport.listAllFiles(raport.getPathRead(), raport.getMapStats(), ".stats");
            raport.listAllFiles(raport.getPathRead(), raport.getMapHtml(), ".html");
            raport.agregateStats();
            raport.setDTOBasedOnStatsMap();
            raport.setDTOBasedOnHtmlMap();
            raport.prepareCategories();
            //raport.writeHtmlRaport(raport.createHtmlRaport());
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

    private String getGroupByRegex() {
        return groupByRegex;
    }

    private List<String> getCategories() {
        return categories.stream().sorted().distinct().collect(Collectors.toList());
    }

    private String getEmailFileRaport() {
        return emailFileRaport;
    }

    private void initArguments(String[] args) throws Exception {
        try {
            this.pathRead = args[0];
            this.pathWrite = this.pathRead.substring(0, this.pathRead.lastIndexOf("\\")) + "\\" + "raport";
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
            throw new Exception("Brak parametru, przekaz pelna sciezke do raportu, folderu jbehave (...jenkins\\reporter\\jbehave)");
        }
    }

    private void listAllFiles(final String path, Map<String, List<String>> map, final String ext) throws Exception {

        try (Stream<Path> paths = Files.walk(Paths.get(path), 1)) {
            paths.forEach(filePath -> {
                if (Files.isRegularFile(filePath) && filePath.toString().endsWith(ext)) {
                    try {
                        ArrayList<String> list = new ArrayList();
                        list.addAll(Files.readAllLines(filePath));
                        Collections.sort(list);
                        String key = filePath.getFileName().toString().substring(0, filePath.getFileName().toString().lastIndexOf('.'));
                        map.put(key, list);
                    } catch (Exception e) {
                        System.out.printf("Brak plików JBehave do raportu.\n\n" + e.getMessage());
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
                this.scenariosSuccessful = scenariosSuccessful + getNumber(v, "scenariosSuccessful");
                this.scenariosFailed = scenariosFailed + getNumber(v, "scenariosFailed");
                this.scenariosPending = scenariosPending + getNumber(v, "scenariosPending");
            }
        });
    }

    private void setDTOBasedOnStatsMap() {
        this.mapStats.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("BeforeStories") && !k.equalsIgnoreCase("AfterStories")) {
                this.listRaportDTO.add(new RaportDTO(k, "failText", getNumber(v, "scenariosFailed"), getNumber(v, "scenariosPending"), getNumber(v, "scenariosSuccessful"), "link", 0));
            }
        });
    }

    private void setDTOBasedOnHtmlMap() {
        this.mapHtml.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("BeforeStories") && !k.equalsIgnoreCase("AfterStories")) {
                String failText = v.get(getIdMessageFailed(v, "(FAILED)"));
                String normalize =  Normalizer.normalize(failText, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
                this.getRaportDTO(k).setMessageFailed(normalize);
            }
        });
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

    private void prepareCategories() {
        Pattern p = Pattern.compile(this.getGroupByRegex());
        this.getListRaportDTO().forEach(raportDTO -> {
            Matcher m = p.matcher(raportDTO.getTestName());
            if(m.find()) {
                this.categories.add(m.group());
            }
        });
    }

    private String createHtmlRaport() {
        StringBuffer str = new StringBuffer();
        str.append(String.format(embeddedMailHtmlStart, LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE: yyyy-MM-dd HH:mm:ss"))));
        str.append(String.format(embeddedMailHtmlHeaderMiddle, this.getListRaportDTO().size(), scenariosPending, scenariosSuccessful, scenariosFailed));

        for(int y = 0; y < this.getListRaportDTO().size(); y++) {
            if(this.getListRaportDTO().get(y).getScenariosFailed() == 1 && this.getListRaportDTO().get(y).getTakedToRaport() == 0){
                str.append(String.format(embeddedMailHtmlMiddle, this.getListRaportDTO().get(y).getTestName(), this.getListRaportDTO().get(y).getMessageFailed()));
                this.getListRaportDTO().get(y).setTakedToRaport(1);
            }
        }

        str.append(String.format(embeddedMailHtmlEnd));
        return str.toString();
    }

    private String createHtmlRaportGroupedBy() {
        StringBuffer str = new StringBuffer();
        str.append(String.format(embeddedMailHtmlStart, LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE: yyyy-MM-dd HH:mm:ss"))));
        str.append(String.format(embeddedMailHtmlHeaderMiddle, this.getListRaportDTO().size(), scenariosPending, scenariosSuccessful, scenariosFailed));

        for(int x = 0; x < this.getCategories().size(); x++) {
            str.append(String.format(embeddedMailHtmlMiddleGroupBy, this.getCategories().get(x)));
            for(int y = 0; y < this.getListRaportDTO().size(); y++) {
                if(this.getListRaportDTO().get(y).getScenariosFailed() == 1 && this.getListRaportDTO().get(y).getTestName().contains(this.getCategories().get(x)) && this.getListRaportDTO().get(y).getTakedToRaport() == 0){
                    str.append(String.format(embeddedMailHtmlMiddle, this.getListRaportDTO().get(y).getTestName(), this.getListRaportDTO().get(y).getMessageFailed()));
                    this.getListRaportDTO().get(y).setTakedToRaport(1);
                }
            }
        }

        str.append(String.format(embeddedMailHtmlMiddleGroupBy, "INNE"));
        for(int x = 0; x < this.getListRaportDTO().size(); x++) {
            if(this.getListRaportDTO().get(x).getTakedToRaport() == 0 && this.getListRaportDTO().get(x).getScenariosFailed() == 1) {
                str.append(String.format(embeddedMailHtmlMiddle, this.getListRaportDTO().get(x).getTestName(), this.getListRaportDTO().get(x).getMessageFailed()));
                this.getListRaportDTO().get(x).setTakedToRaport(1);
            }
        }

        str.append(String.format(embeddedMailHtmlEnd));

        return str.toString();
    }

    private void writeHtmlRaport(String raport) throws Exception {

        if (raport != null && !raport.isEmpty()) {
            Path path = Paths.get(this.getPathWrite());
            Files.createDirectories(path);
            path = Paths.get(this.getPathWrite() + "\\" + this.getEmailFileRaport());
            Files.deleteIfExists(path);
            Files.write(path, raport.getBytes(StandardCharsets.UTF_8));
        } else {
            throw new Exception("Brak danych do raportu");
        }
    }  
}
