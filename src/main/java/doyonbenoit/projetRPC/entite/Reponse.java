package doyonbenoit.projetRPC.entite;

public class Reponse {

    private String contenu;
    private String privilege;
    private String img;
    private String date;


    public Reponse(String contenu) {
        this.contenu = contenu;
    }

    public Reponse(String contenu, String privilege, String img, String date) {
        this.contenu = contenu;
        this.privilege = privilege;
        this.img = img;
        this.date = date;
    }

    public Reponse() {
    }

    public String getContenu() {
        return contenu;
    }

    public String getImg() {
        return img;
    }

    public String getDate() {
        return date;
    }

    public String getPrivilege() {
        return privilege;
    }
}
