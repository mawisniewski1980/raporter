package raporter;

public class RaportDTO {

   String testName;
    String messageFailed;
    int scenariosFailed;
    int scenariosPending;
    int scenariosSuccessful;
    String link;
    int takenToRaport;
    String testCategory = "XXX";


    public RaportDTO(String testName, String messageFailed, int scenariosFailed, int scenariosPending, int scenariosSuccessful, String link, int takenToRaport) {
        this.testName = testName;
        this.messageFailed = messageFailed;
        this.scenariosFailed = scenariosFailed;
        this.scenariosPending = scenariosPending;
        this.scenariosSuccessful = scenariosSuccessful;
        this.link = link;
        this.takenToRaport = takenToRaport;
    }

    public String getTestName() {
        return testName;
    }

    public String getTestNameWithOtherExt(String ext) {
        return getTestName().substring(0, getTestName().lastIndexOf('.')) + ext;
    }

    public String getMessageFailed() {
        messageFailed = messageFailed.replace("class=\"message failed\"", "style=\"color: #ff0033;\"");
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

    public int getTakenToRaport() {
        return takenToRaport;
    }

    public void setTakenToRaport(int takenToRaport) {
        this.takenToRaport = takenToRaport;
    }

    public String getTestCategory() {
        String groupByRegex = "[AFP]{1}\\d{2}(?!\\d)";
        Pattern pattern = Pattern.compile(groupByRegex);
        Matcher matcher = pattern.matcher(this.getTestName());
        if(matcher.find()) {
            this.testCategory = matcher.group();
        }
        return this.testCategory;
    }
}
