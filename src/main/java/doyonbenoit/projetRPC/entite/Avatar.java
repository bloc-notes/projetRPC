package doyonbenoit.projetRPC.entite;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Avatar")
public class Avatar {

    private String nom;
    private String imgAvatar;

    public Avatar() {
    }

    public Avatar(String nom, String imgAvatar) {
        this.nom = nom;
        this.imgAvatar = imgAvatar;
    }

    @Id
    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Column(name = "Avatar", columnDefinition = "TEXT")
    public String getImgAvatar() {
        return imgAvatar;
    }

    public void setImgAvatar(String imgAvatar) {
        this.imgAvatar = imgAvatar;
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "nom='" + nom + '\'' +
                ", imgAvatar='" + imgAvatar + '\'' +
                '}';
    }
}
