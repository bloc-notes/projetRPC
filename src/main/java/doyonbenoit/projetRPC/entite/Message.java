package doyonbenoit.projetRPC.entite;

public class Message {

    private String de;
    private String type;
    private String privilege;
    private String contenu;

    public Message(String de, String type, String contenu, String privilege) {
        this.de = de;
        this.type = type;
        this.contenu = contenu;
        this.privilege = privilege;

    }

    public Message() {
    }

    public String getDe() {
        return de;
    }

    public String getType() {
        return type;
    }

    public String getContenu() {
        return contenu;
    }

    public String getPrivilege() {
        return privilege;
    }
}
