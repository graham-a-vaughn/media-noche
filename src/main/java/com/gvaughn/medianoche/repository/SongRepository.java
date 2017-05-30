package com.gvaughn.medianoche.repository;

import com.gvaughn.medianoche.domain.Song;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.*;


/**
 * Spring Data JPA repository for the Song entity.
 */
@SuppressWarnings("unused")
@Repository
public interface SongRepository extends JpaRepository<Song,Long> {

}
