package raporter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RaportDTO {

   private String testName;
   private String messageFailed;
   private int scenariosFailed;
   private int scenariosPending;
   private int scenariosSuccessful;
   private String link;
   private int takenToRaport;
   private String testCategory = "XXX";


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
        Pattern pattern = Pattern.compile("[AFP]{1}\\d{2}(?!\\d)");
        Matcher matcher = pattern.matcher(this.getTestName());
        if(matcher.find()) {
            this.testCategory = matcher.group();
        }
        return this.testCategory;
    }
}
