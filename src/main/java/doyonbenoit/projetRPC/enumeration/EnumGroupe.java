package doyonbenoit.projetRPC.enumeration;

public enum EnumGroupe {
    BLANCHE,
    JAUNE,
    ORANGE,
    VERTE,
    BLEUE,
    MARRON,
    NOIRE,
    PATATE;

    /**
     * Calcule valide pour la première ceinture
     */
    public int nbPointSelonCeinture(EnumGroupe ceinture2) {
        int intDifference = ceinture2.ordinal() - this.ordinal();
        int intNbPointPossible = 0;

        switch (intDifference) {
            case -6:
                intNbPointPossible = 1;
                break;
            case -5:
                intNbPointPossible = 2;
                break;
            case -4:
                intNbPointPossible = 3;
                break;
            case -3:
                intNbPointPossible = 5;
                break;
            case -2:
                intNbPointPossible = 7;
                break;
            case -1:
                intNbPointPossible = 9;
                break;
            case 0:
                intNbPointPossible = 10;
                break;
            case 1:
                intNbPointPossible = 12;
                break;
            case 2:
                intNbPointPossible = 15;
                break;
            case 3:
                intNbPointPossible = 20;
                break;
            case 4:
                intNbPointPossible = 25;
                break;
            case 5:
                intNbPointPossible = 30;
                break;
            case 6:
                intNbPointPossible = 50;
                break;
        }

        return intNbPointPossible;
    }
}
