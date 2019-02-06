package raporter;

public class RaportDTO {

    String testName;
    String messageFailed;
    int scenariosFailed = 0;
    int scenariosPending = 0;
    int scenariosSuccessful = 0;
    String link;
    int takedToRaport = 0;

    public RaportDTO() {
        this.testName = "testName";
        this.messageFailed = "messageFailed";
        this.scenariosFailed = 0;
        this.scenariosPending = 0;
        this.scenariosSuccessful = 0;
        this.link = "link";
        this.takedToRaport = 0;
    }

    public RaportDTO(String testName, String messageFailed, int scenariosFailed, int scenariosPending, int scenariosSuccessful, String link, int takedToRaport) {
        this.testName = testName;
        this.messageFailed = messageFailed;
        this.scenariosFailed = scenariosFailed;
        this.scenariosPending = scenariosPending;
        this.scenariosSuccessful = scenariosSuccessful;
        this.link = link;
        this.takedToRaport = takedToRaport;
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
        messageFailed = messageFailed.replace("class=\"message failed\"", "style=\"color: #ff0000;\"");
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

    public int getTakedToRaport() {
        return takedToRaport;
    }

    public void setTakedToRaport(int takedToRaport) {
        this.takedToRaport = takedToRaport;
    }
}
