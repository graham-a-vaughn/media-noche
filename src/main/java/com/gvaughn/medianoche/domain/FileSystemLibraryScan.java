package com.gvaughn.medianoche.domain;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.ZonedDateTime;

/**
 * Created by graham on 6/2/17.
 * <p>
 * <p>
 * Media Project - streaming media system.
 * Copyright (C) 2017  Graham Vaughn
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@Entity
@Table(name = "file_system_library_scan")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class FileSystemLibraryScan implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @Column(name = "start_time")
    @NotNull
    private ZonedDateTime startTime;

    @Column(name = "end_time")
    @NotNull
    private ZonedDateTime endTime;

    @NotNull
    private Boolean success = Boolean.FALSE;

    @Column(name = "new_artist_count")
    @NotNull
    private Integer newArtistCount = 0;

    @Column(name = "new_album_count")
    @NotNull
    private Integer newAlbumCount = 0;

    @Column(name = "new_song_count")
    @NotNull
    private Integer newSongCount = 0;

    @Column(name = "scan_directories")
    @NotNull
    private String scanDirectories;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonedDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(ZonedDateTime startTime) {
        this.startTime = startTime;
    }

    public ZonedDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(ZonedDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Integer getNewArtistCount() {
        return newArtistCount;
    }

    public void setNewArtistCount(Integer newArtistCount) {
        this.newArtistCount = newArtistCount;
    }

    public Integer getNewAlbumCount() {
        return newAlbumCount;
    }

    public void setNewAlbumCount(Integer newAlbumCount) {
        this.newAlbumCount = newAlbumCount;
    }

    public Integer getNewSongCount() {
        return newSongCount;
    }

    public void setNewSongCount(Integer newSongCount) {
        this.newSongCount = newSongCount;
    }

    public String getScanDirectories() {
        return scanDirectories;
    }

    public void setScanDirectories(String scanDirectories) {
        this.scanDirectories = scanDirectories;
    }

    @Transient
    public void zero() {
        newArtistCount = 0;
        newAlbumCount = 0;
        newSongCount = 0;
    }
}
