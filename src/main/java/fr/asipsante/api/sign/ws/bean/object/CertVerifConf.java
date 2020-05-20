package fr.asipsante.api.sign.ws.bean.object;

public class CertVerifConf {

    private String idVerifCert;

    private String description;

    private String metadata;

    private String rules;

    public CertVerifConf() {
    }

    public CertVerifConf(String idVerifCert, String description, String metadata, String rules) {
        this.idVerifCert = idVerifCert;
        this.description = description;
        this.metadata = metadata;
        this.rules = rules;
    }

    public String getIdVerifCert() {
        return idVerifCert;
    }

    public void setIdVerifCert(String idVerifCert) {
        this.idVerifCert = idVerifCert;
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
        return "CertVerifConf{" +
                "idVerifCert='" + idVerifCert + '\'' +
                ", description='" + description + '\'' +
                ", metadata='" + metadata + '\'' +
                ", rules='" + rules + '\'' +
                '}';
    }
}
