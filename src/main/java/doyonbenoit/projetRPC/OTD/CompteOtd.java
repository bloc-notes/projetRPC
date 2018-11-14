package doyonbenoit.projetRPC.OTD;

import javax.validation.constraints.NotNull;

public class CompteOtd {

    @NotNull
    private String courriel;

    @NotNull
    private String mdp;

    @NotNull
    private String alias;

    @NotNull
    private String avatar;

    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "CompteOtd{" +
                "courriel='" + courriel + '\'' +
                ", mdp='" + mdp + '\'' +
                ", alias='" + alias + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }
}
