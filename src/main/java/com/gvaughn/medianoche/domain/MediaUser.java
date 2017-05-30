package com.gvaughn.medianoche.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A MediaUser.
 */
@Entity
@Table(name = "media_user")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MediaUser extends AbstractAuditingEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "username", nullable = false)
    private String username;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "media_user_playlists", joinColumns = {
        @JoinColumn(name = "media_user_id", nullable = false) },
        inverseJoinColumns = { @JoinColumn(name = "playlist_id",
            nullable = false) })
    @OrderColumn(name = "index")
    private Set<Playlist> playlists = new HashSet<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public MediaUser username(String username) {
        this.username = username;
        return this;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Playlist> getPlaylist() {
        return playlists;
    }

    public void setPlaylist(Set<Playlist> playlists) {
        this.playlists = playlists;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaUser mediaUser = (MediaUser) o;
        if (mediaUser.getId() == null || getId() == null) {
            return false;
        }
        return Objects.equals(getId(), mediaUser.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }

    @Override
    public String toString() {
        return "MediaUser{" +
            "id=" + getId() +
            ", username='" + getUsername() + "'" +
            "}";
    }
}
