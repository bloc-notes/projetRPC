package doyonbenoit.projetRPC.entite;

import doyonbenoit.projetRPC.enumeration.EnumGroupe;

import javax.persistence.*;

@Entity
@Table(name = "GROUPES")
public class Groupe {
    private Integer id;
    private EnumGroupe groupe;

    public Groupe(Integer id, EnumGroupe groupe) {
        this.id = id;
        this.groupe = groupe;
    }

    public Groupe() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    @Enumerated(EnumType.STRING)
    public EnumGroupe getGroupe() {
        return groupe;
    }

    public void setGroupe(EnumGroupe groupe) {
        this.groupe = groupe;
    }
}
