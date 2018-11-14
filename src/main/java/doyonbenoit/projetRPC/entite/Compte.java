package doyonbenoit.projetRPC.entite;
import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "COMPTES")
public class Compte {
    private String courriel;
    private String motDePasse;
    private String alias;
    private Avatar avatar;
    private Role role;
    private Groupe groupe;

    public Compte(String courriel, String motDePasse, String alias, Role role, Groupe groupe) {
        this.courriel = courriel;
        this.motDePasse = motDePasse;
        this.alias = alias;
        this.role = role;
        this.groupe = groupe;
    }

    public Compte(String courriel, String motDePasse, String alias) {
        this.courriel = courriel;
        this.motDePasse = motDePasse;
        this.alias = alias;
        this.role = Role.NOUVEAU;
    }

    public Compte() {
    }

    @Id
    public String getCourriel() {
        return courriel;
    }

    public void setCourriel(String courriel) {
        this.courriel = courriel;
    }

    public String getMotDePasse() {
        return motDePasse;
    }

    public void setMotDePasse(String motDePasse) {
        this.motDePasse = motDePasse;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Enumerated(EnumType.STRING)
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Enumerated(EnumType.STRING)
    public Groupe getGroupe() {
        return groupe;
    }

    public void setGroupe(Groupe groupe) {
        this.groupe = groupe;
    }

    @ManyToOne
    @JoinColumn(name = "avatar_nom")
    public Avatar getAvatar() {
        return avatar;
    }

    public void setAvatar(Avatar avatar) {
        this.avatar = avatar;
    }


    @Override
    public String toString() {
        return "Compte{" +
                ", courriel='" + courriel + '\'' +
                ", motDePasse='" + motDePasse + '\'' +
                ", alias='" + alias + '\'' +
                ", avatar=" + avatar.getNom() +
                ", role=" + role +
                ", groupe=" + groupe +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compte compte = (Compte) o;
        return Objects.equals(getCourriel(), compte.getCourriel());
    }
}
