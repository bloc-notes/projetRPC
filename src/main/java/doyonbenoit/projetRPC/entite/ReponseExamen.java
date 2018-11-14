package doyonbenoit.projetRPC.entite;

public class ReponseExamen {
    private Compte compte;
    private int intSoldeCredit;
    private boolean booHonte;

    public ReponseExamen(Compte compte, int intSoldeCredit, boolean booHonte) {
        this.compte = compte;
        this.intSoldeCredit = intSoldeCredit;
        this.booHonte = booHonte;
    }

    public ReponseExamen(Compte compte, int intSoldeCredit) {
        this.compte = compte;
        this.intSoldeCredit = intSoldeCredit;
    }

    public ReponseExamen() {
    }

    public Compte getCompte() {
        return compte;
    }

    public void setCompte(Compte compte) {
        this.compte = compte;
    }

    public int getIntSoldeCredit() {
        return intSoldeCredit;
    }

    public void setIntSoldeCredit(int intSoldeCredit) {
        this.intSoldeCredit = intSoldeCredit;
    }

    public boolean isBooHonte() {
        return booHonte;
    }

    public void setBooHonte(boolean booHonte) {
        this.booHonte = booHonte;
    }
}
