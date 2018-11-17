package doyonbenoit.projetRPC.entite;

import javax.persistence.*;

@Entity
@Table(name = "Avatar")
public class Avatar {

    private Integer id;
    private String nom;
    private String imgAvatar;

    public Avatar() {
    }

    public Avatar(String nom, String imgAvatar) {
        this.nom = nom;
        this.imgAvatar = imgAvatar;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    @Column(name = "AVATAR", columnDefinition = "TEXT")
    public String getImgAvatar() {
        return imgAvatar;
    }

    public void setImgAvatar(String imgAvatar) {
        this.imgAvatar = imgAvatar;
    }

    @Override
    public String toString() {
        return "Avatar{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", imgAvatar='" + imgAvatar + '\'' +
                '}';
    }
}
