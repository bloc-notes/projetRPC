package doyonbenoit.projetRPC.entite;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Examens")
public class Examen {
    private Integer id;
    private Date date;
    private Compte cmJuger;
    private Compte cmExaminateur;
    private Boolean booReussit;

    public Examen(Date date, Compte cmJuger, Compte cmExaminateur, Boolean booReussit) {
        this.date = date;
        this.cmJuger = cmJuger;
        this.cmExaminateur = cmExaminateur;
        this.booReussit = booReussit;
    }

    public Examen() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDate() {
        return date;
    }

    @ManyToOne
    @JoinColumn(name = "courriel_Juger")
    public Compte getCmJuger() {
        return cmJuger;
    }

    @ManyToOne
    @JoinColumn(name = "courriel_Examinateur")
    public Compte getCmExaminateur() {
        return cmExaminateur;
    }

    public Boolean getBooReussit() {
        return booReussit;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setCmJuger(Compte cmJuger) {
        this.cmJuger = cmJuger;
    }

    public void setCmExaminateur(Compte cmExaminateur) {
        this.cmExaminateur = cmExaminateur;
    }

    public void setBooReussit(Boolean booReussit) {
        this.booReussit = booReussit;
    }
}
