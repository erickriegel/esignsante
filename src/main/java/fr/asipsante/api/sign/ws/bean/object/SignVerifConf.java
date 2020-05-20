package fr.asipsante.api.sign.ws.bean.object;

public class SignVerifConf {

    private String idVerifSign;

    private String description;

    private String metadata;

    private String rules;

    public SignVerifConf() {
    }

    public SignVerifConf(String idVerifSign, String description, String metadata, String rules) {
        this.idVerifSign = idVerifSign;
        this.description = description;
        this.metadata = metadata;
        this.rules = rules;
    }

    public String getIdVerifSign() {
        return idVerifSign;
    }

    public void setIdVerifSign(String idVerifSign) {
        this.idVerifSign = idVerifSign;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getRules() {
        return rules;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    @Override
    public String toString() {
        return "SignVerifConf{" +
                "idVerifSign='" + idVerifSign + '\'' +
                ", description='" + description + '\'' +
                ", metadata='" + metadata + '\'' +
                ", rules='" + rules + '\'' +
                '}';
    }
}
