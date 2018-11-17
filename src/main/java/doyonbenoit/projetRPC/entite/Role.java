package doyonbenoit.projetRPC.entite;

import doyonbenoit.projetRPC.enumeration.EnumRole;

import javax.persistence.*;

@Entity
@Table(name = "ROLES")
public class Role {
    private Integer id;
    private EnumRole role;

    public Role(Integer id, EnumRole role) {
        this.id = id;
        this.role = role;
    }

    public Role() {
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
    public EnumRole getRole() {
        return role;
    }

    public void setRole(EnumRole role) {
        this.role = role;
    }
}
