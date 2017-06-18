package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.domain.Song;
import com.gvaughn.medianoche.repository.SongRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Song.
 */
@Service
@Transactional
public class SongService {

    private final Logger log = LoggerFactory.getLogger(SongService.class);

    private final SongRepository songRepository;

    public SongService(SongRepository songRepository) {
        this.songRepository = songRepository;
    }

    /**
     * Save a song.
     *
     * @param song the entity to save
     * @return the persisted entity
     */
    public Song save(Song song) {
        log.debug("Request to save Song : {}", song);
        Song result = songRepository.save(song);
        return result;
    }

    /**
     *  Get all the songs.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Song> findAll(Pageable pageable) {
        log.debug("Request to get all Songs");
        Page<Song> result = songRepository.findAll(pageable);
        return result;
    }

    public List<Song> findAll() {
        return songRepository.findAll();
    }
    /**
     *  Get one song by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Song findOne(Long id) {
        log.debug("Request to get Song : {}", id);
        Song song = songRepository.findOne(id);
        return song;
    }

    /**
     *  Delete the  song by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Song : {}", id);
        songRepository.delete(id);
    }
}
