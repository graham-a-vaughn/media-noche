package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.repository.ArtistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Artist.
 */
@Service
@Transactional
public class ArtistService {

    private final Logger log = LoggerFactory.getLogger(ArtistService.class);
    
    private final ArtistRepository artistRepository;

    public ArtistService(ArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    /**
     * Save a artist.
     *
     * @param artist the entity to save
     * @return the persisted entity
     */
    public Artist save(Artist artist) {
        log.debug("Request to save Artist : {}", artist);
        Artist result = artistRepository.save(artist);
        return result;
    }

    /**
     *  Get all the artists.
     *  
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Artist> findAll(Pageable pageable) {
        log.debug("Request to get all Artists");
        Page<Artist> result = artistRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one artist by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Artist findOne(Long id) {
        log.debug("Request to get Artist : {}", id);
        Artist artist = artistRepository.findOne(id);
        return artist;
    }

    /**
     *  Delete the  artist by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Artist : {}", id);
        artistRepository.delete(id);
    }
}
