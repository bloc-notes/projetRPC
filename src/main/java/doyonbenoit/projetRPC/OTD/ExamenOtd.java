package doyonbenoit.projetRPC.OTD;

import javax.validation.constraints.NotNull;

public class ExamenOtd {
    @NotNull
    private String juger;
    @NotNull
    private String examinateur;
    @NotNull
    private Boolean reussit;

    public String getJuger() {
        return juger;
    }

    public void setJuger(String juger) {
        this.juger = juger;
    }

    public String getExaminateur() {
        return examinateur;
    }

    public void setExaminateur(String examinateur) {
        this.examinateur = examinateur;
    }

    public Boolean getReussit() {
        return reussit;
    }

    public void setReussit(Boolean reussit) {
        this.reussit = reussit;
    }

    @Override
    public String toString() {
        return "ExamenOtd{" +
                "juger='" + juger + '\'' +
                ", examinateur='" + examinateur + '\'' +
                ", reussit=" + reussit +
                '}';
    }
}
