package com.ninebudget.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * A Credential.
 */
@Entity
@Table(name = "credential")
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Credential implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @NotNull
    @Column(name = "password", nullable = false)
    private String password;

    @NotNull
    @Column(name = "custom", nullable = false)
    private String custom;

    @OneToOne(mappedBy = "credential")
    @JsonIgnore
    private Institution institution;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public Credential username(String username) {
        this.username = username;
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public Credential password(String password) {
        this.password = password;
        return this;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCustom() {
        return custom;
    }

    public Credential custom(String custom) {
        this.custom = custom;
        return this;
    }

    public void setCustom(String custom) {
        this.custom = custom;
    }

    public Institution getInstitution() {
        return institution;
    }

    public Credential institution(Institution institution) {
        this.institution = institution;
        return this;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Credential)) {
            return false;
        }
        return id != null && id.equals(((Credential) o).id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Credential{" +
            "id=" + getId() +
            ", username='" + getUsername() + "'" +
            ", password='" + getPassword() + "'" +
            ", custom='" + getCustom() + "'" +
            "}";
    }
}