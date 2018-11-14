package doyonbenoit.projetRPC.entite;

public class ReponseKumite {
    private Compte compte;
    private String strAction;

    public ReponseKumite(Compte compte, String strAction) {
        this.compte = compte;
        this.strAction = strAction;
    }

    public ReponseKumite() {
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public String getStrAction() {
        return strAction;
    }

    public void setStrAction(String strAction) {
        this.strAction = strAction;
    }
}
