package fr.asipsante.api.sign.ws.bean.object;

import java.lang.reflect.Field;

public class CaConf {

    private String certificate;

    private String crl;

    public CaConf() {
    }

    public CaConf(String certificate, String crl) {
        this.certificate = certificate;
        this.crl = crl;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getCrl() {
        return crl;
    }

    public void setCrl(String crl) {
        this.crl = crl;
    }

    @Override
    public String toString() {
        return "CaConf{" +
                "certificate='" + certificate + '\'' +
                ", crl='" + crl + '\'' +
                '}';
    }

    public boolean checkValid() throws IllegalAccessException {
        for (Field f : getClass().getDeclaredFields())
            if (f.get(this) == null)
                return false;
        return true;
    }
}
