import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

public class RaporterNew {

    private static final String embeddedMailHtmlStart = "<html>\n\t<body style=\"background-color:#f0f0f0;\">\n\t\t<table cellspacing=\"5\" style=\"width:1024px;\">\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\">\n\t\t\t\t\tDzienny raport z wykonania test&oacute;w automatycznych <br/> %s\n\t\t\t\t</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\">\n\t\t\t\t\t<table cellspacing=\"5\" cellpadding=\"5\" style=\"width:1024px; border: 0px; font-size:12px; font-family:arial,helvetica,sans-serif; font-weight:bold;\">\n";
    private static final String embeddedMailHtmlHeaderMiddle = "\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#9DFF9D; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">Liczba test&#xf3;w: %s</td>\n\t\t\t\t\t\t\t\t\t<td style=\"background-color:#ff8181; width:405px;\">Testy pomini&#x119;te: %s</td>\n\t\t\t\t\t\t\t\t\t</tr>\n\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#9DFF9D; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td style=\"background-color:#9DFF9D; width:405px;\">Testy poprawne: %s</td>\n\t\t\t\t\t\t\t\t\t<td style=\"background-color:#ff0000; width:405px;\">Testy b&#x142;&#x119;dne: %s</td>\n\t\t\t\t\t\t\t\t\t</table>\n\t\t\t\t</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"center\">\n\t\t\t\t<td align=\"center\">\n\t\t\t\t\t<table cellspacing=\"5\" cellpadding=\"5\" style=\"width:1024px; border: 0px; font-size:12px; font-family:arial,helvetica,sans-serif;\">\n\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#cecccc; color:#000000; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">Historyjka</td>\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">Nieudany krok</td>\n\t\t\t\t\t\t\t\t\t</tr>\n";
    private static final String embeddedMailHtmlMiddle = "\t\t\t\t\t\t\t\t\t<tr style=\"background-color:#cecccc; height:40px;\">\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">%s</td>\n\t\t\t\t\t\t\t\t\t\t<td width=\"405px\">%s</td>\n\t\t\t\t\t\t\t\t\t</tr>\n";
    private static final String embeddedMailHtmlEnd = "\n\t\t\t\t\t</table>\n\t\t\t\t</td>\n\t\t\t</tr>\n\t\t\t<tr align=\"right\">\n\t\t\t\t<td style=\"font-size:12px; font-family:arial,helvetica,sans-serif;\">Copyright WAK @ ING</td>\n\t\t\t</tr>\n\t\t</table>\n\t</body>\n</html>";

    private static Map<String, List<String>> mapStats = new TreeMap<>();
    private static Map<String, List<String>> mapHtml = new TreeMap<>();
    private static List<RaportDTO> listRaportDTO = new LinkedList<>();
    private static String pathRead;
    private static String pathWrite;
    private String emailFileRaport = "01_EmailReport.html";

    private int scenariosFailed = 0;
    private int scenariosPending = 0;
    private int scenariosSuccessful = 0;

    public static void main(String[] args) {

        RaporterNew raport = new RaporterNew();
        try {
            raport.initFolders(args);
            raport.listAllFiles(pathRead, mapStats, ".stats");
            raport.listAllFiles(pathRead, mapHtml, ".html");
            raport.agregateStats();
            raport.setDTOBasedOnStatsMap();
            raport.setDTOBasedOnHtmlMap();
            raport.writeHtmlRaport(raport.createHtmlRaport(listRaportDTO));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initFolders(String[] args) throws Exception {
        try {
            this.pathRead = args[0];
            this.pathWrite = this.pathRead.substring(0, this.pathRead.lastIndexOf("\\")) + "\\" + "raport";
        } catch (ArrayIndexOutOfBoundsException | NullPointerException ex) {
            throw new Exception("Brak parametru, przekaz pelna sciezke do raportu, folderu jbehave (D:\\Pobieranie\\reporter\\jbehave)");
        }
    }

    private void listAllFiles(String path, Map<String, List<String>> map, final String ext) {

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
                        e.printStackTrace();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
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
                this.listRaportDTO.add(new RaportDTO(k, "failText", getNumber(v, "scenariosFailed"), getNumber(v, "scenariosPending"), getNumber(v, "scenariosSuccessful"), "link"));
            }
        });
    }

    private void setDTOBasedOnHtmlMap() {
        this.mapHtml.forEach((k, v) -> {
            if (!k.equalsIgnoreCase("BeforeStories") && !k.equalsIgnoreCase("AfterStories")) {
                this.getRaportDTO(k).setMessageFailed(v.get(getIdMessageFailed(v, "(FAILED)")));
            }
        });
    }

    private RaportDTO getRaportDTO(String testName) {
        return this.listRaportDTO.stream().filter(raportDTO -> raportDTO.getTestName().equalsIgnoreCase(testName)).findFirst().get();
    }

    private int getNumber(List<String> list, String kind) {
        String value = null;
        Optional<String> opt = list.stream().filter(str -> str.contains(kind)).findFirst();
        if (opt.isPresent()) {
            value = opt.get().substring(opt.get().lastIndexOf("=") + 1);
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

    private String createHtmlRaport(List<RaportDTO> listRaportDTO) {
        String tmp = null;
        StringBuffer str = new StringBuffer();
        str.append(String.format(embeddedMailHtmlStart, LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE: yyyy-MM-dd HH:mm:ss"))));
        str.append(String.format(embeddedMailHtmlHeaderMiddle, listRaportDTO.size(), scenariosPending, scenariosSuccessful, scenariosFailed));
        listRaportDTO.stream()
                .filter(raport -> raport.getScenariosSuccessful() != 1)
                .forEach(raport -> str.append(String.format(embeddedMailHtmlMiddle, raport.getTestName(), raport.getMessageFailed())));
        str.append(String.format(embeddedMailHtmlEnd));
        tmp = str.toString();
        return tmp;
    }

    private void writeHtmlRaport(String raport) throws Exception {

        if (raport != null && !raport.isEmpty()) {
            Path path = Paths.get(this.pathWrite);
            Files.createDirectories(path);
            path = Paths.get(this.pathWrite + "\\" + this.emailFileRaport);
            Files.deleteIfExists(path);
            Files.write(path, raport.getBytes());
        } else {
            throw new Exception("Brak danych do raportu");
        }
    }

    private class RaportDTO {

        String testName;
        String messageFailed;
        int scenariosFailed = 0;
        int scenariosPending = 0;
        int scenariosSuccessful = 0;
        String link;

        public RaportDTO() {
            this.testName = "testName";
            this.messageFailed = "messageFailed";
            this.scenariosFailed = 0;
            this.scenariosPending = 0;
            this.scenariosSuccessful = 0;
            this.link = "link";
        }

        public RaportDTO(String testName, String messageFailed, int scenariosFailed, int scenariosPending, int scenariosSuccessful, String link) {
            this.testName = testName;
            this.messageFailed = messageFailed;
            this.scenariosFailed = scenariosFailed;
            this.scenariosPending = scenariosPending;
            this.scenariosSuccessful = scenariosSuccessful;
            this.link = link;
        }

        public String getTestName() {
            return testName;
        }

        public String getTestNameWithOtherExt(String ext) {
            return getTestName().substring(0, getTestName().lastIndexOf('.')) + ext;
        }

        public void setTestName(String testName) {
            this.testName = testName;
        }

        public String getMessageFailed() {
            return messageFailed;
        }

        public void setMessageFailed(String messageFailed) {
            this.messageFailed = messageFailed;
        }

        public int getScenariosFailed() {
            return scenariosFailed;
        }

        public void setScenariosFailed(int scenariosFailed) {
            this.scenariosFailed = scenariosFailed;
        }

        public int getScenariosPending() {
            return scenariosPending;
        }

        public void setScenariosPending(int scenariosPending) {
            this.scenariosPending = scenariosPending;
        }

        public int getScenariosSuccessful() {
            return scenariosSuccessful;
        }

        public void setScenariosSuccessful(int scenariosSuccessful) {
            this.scenariosSuccessful = scenariosSuccessful;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }
    }
}
