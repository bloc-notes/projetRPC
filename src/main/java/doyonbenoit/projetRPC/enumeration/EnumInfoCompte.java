package doyonbenoit.projetRPC.enumeration;

public enum EnumInfoCompte {
    COMPTE("Compte"),
    POINT("NbPoint"),
    CREDIT("Credit"),
    ARBITE("NbCombatArbiter");

    private final String strNom;

    EnumInfoCompte(String strNom) {
        this.strNom = strNom;
    }

    public String getNom() {
        return strNom;
    }
}
