package doyonbenoit.projetRPC.entite;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "Examens")
public class Examen {
    private Integer id;
    private Long date;
    private Compte cmJuger;
    private Compte cmExaminateur;
    private Boolean booReussit;

    public Examen(Long date, Compte cmJuger, Compte cmExaminateur, Boolean booReussit) {
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

    //@Temporal(TemporalType.TIMESTAMP)
    /*
    public Date gObjDate() {
        return date;
    }*/

    public Long getDate() {
        return date;
    }

    @ManyToOne
    @JoinColumn(name = "evalue_id")
    public Compte getCmJuger() {
        return cmJuger;
    }

    @ManyToOne
    @JoinColumn(name = "evaluateur_id")
    public Compte getCmExaminateur() {
        return cmExaminateur;
    }

    @Column(name = "aReussi")
    public Boolean getBooReussit() {
        return booReussit;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    /*
    public void sObjDate(Date date) {
        this.date = date;
    }*/

    public void setDate(Long date) {
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
