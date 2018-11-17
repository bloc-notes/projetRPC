package doyonbenoit.projetRPC.entite;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "COMBATS")
public class Combat {
    private Integer id;
    private Compte cmArbite;
    private Compte cmBlanc;
    private Compte cmRouge;
    private Long date;
    private Groupe ceintureBanc;
    private Groupe ceintureRouge;
    private Integer intGainPertePointBlanc;
    private Integer intGainPertePointRouge;
    private Integer intGainPerteCreditArbite;

    public Combat(Compte cmArbite, Compte cmBlanc, Compte cmRouge, Groupe ceintureBanc, Groupe ceintureRouge) {
        this.cmArbite = cmArbite;
        this.cmBlanc = cmBlanc;
        this.cmRouge = cmRouge;
        this.ceintureBanc = ceintureBanc;
        this.ceintureRouge = ceintureRouge;
        this.intGainPerteCreditArbite = 0;
        this.intGainPertePointBlanc = 0;
        this.intGainPertePointRouge = 0;
    }

    public Combat() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "arbitre_id")
    public Compte getCmArbite() {
        return cmArbite;
    }

    @ManyToOne
    @JoinColumn(name = "blanc_id")
    public Compte getCmBlanc() {
        return cmBlanc;
    }

    @ManyToOne
    @JoinColumn(name = "rouge_id")
    public Compte getCmRouge() {
        return cmRouge;
    }

    //@Temporal(TemporalType.TIMESTAMP)
    public Long getDate() {
        return date;
    }

    //@Enumerated(EnumType.STRING)
    @ManyToOne
    @JoinColumn(name = "ceintureBlanc_id")
    public Groupe getCeintureBanc() {
        return ceintureBanc;
    }

    //@Enumerated(EnumType.STRING)
    @ManyToOne
    @JoinColumn(name = "ceintureRouge_id")
    public Groupe getCeintureRouge() {
        return ceintureRouge;
    }

    @Column(name = "pointsBlanc")
    public Integer getIntGainPertePointBlanc() {
        return intGainPertePointBlanc;
    }

    @Column(name = "pointsRouge")
    public Integer getIntGainPertePointRouge() {
        return intGainPertePointRouge;
    }

    @Column(name = "creditsArbitre")
    public Integer getIntGainPerteCreditArbite() {
        return intGainPerteCreditArbite;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setCmArbite(Compte cmArbite) {
        this.cmArbite = cmArbite;
    }

    public void setCmBlanc(Compte cmBlanc) {
        this.cmBlanc = cmBlanc;
    }

    public void setCmRouge(Compte cmRouge) {
        this.cmRouge = cmRouge;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public void setCeintureBanc(Groupe ceintureBanc) {
        this.ceintureBanc = ceintureBanc;
    }

    public void setCeintureRouge(Groupe ceintureRouge) {
        this.ceintureRouge = ceintureRouge;
    }


    public void setIntGainPertePointBlanc(Integer intGainPertePointBlanc) {
        this.intGainPertePointBlanc = intGainPertePointBlanc;
    }

    public void setIntGainPertePointRouge(Integer intGainPertePointRouge) {
        this.intGainPertePointRouge = intGainPertePointRouge;
    }

    public void setIntGainPerteCreditArbite(Integer intGainPerteCreditArbite) {
        this.intGainPerteCreditArbite = intGainPerteCreditArbite;
    }

    @Override
    public String toString() {
        return "Combat{" +
                "id=" + id +
                ", cmArbite=" + cmArbite +
                ", cmBlanc=" + cmBlanc +
                ", cmRouge=" + cmRouge +
                ", date=" + date +
                ", ceintureBanc=" + ceintureBanc +
                ", ceintureRouge=" + ceintureRouge +
                '}';
    }
}
