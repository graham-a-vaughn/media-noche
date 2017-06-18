package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.domain.Album;
import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.domain.Song;
import com.gvaughn.medianoche.repository.AlbumRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Album.
 */
@Service
@Transactional
public class AlbumService {

    private final Logger log = LoggerFactory.getLogger(AlbumService.class);

    private final AlbumRepository albumRepository;

    public AlbumService(AlbumRepository albumRepository) {
        this.albumRepository = albumRepository;
    }

    public Album addAlbum(Artist artist, String name) {
        //TODO: Date?
        Album album = new Album(name, artist);
        return save(album);
    }

    public Album setSongs(Long albumId, List<Song> songs) {
        Album album = albumRepository.findOne(albumId);
        album.getSongs().clear();
        album.getSongs().addAll(songs);
        return save(album);
    }

    /**
     * Save a album.
     *
     * @param album the entity to save
     * @return the persisted entity
     */
    public Album save(Album album) {
        log.debug("Request to save Album : {}", album);
        Album result = albumRepository.save(album);
        return result;
    }

    /**
     *  Get all the albums.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Album> findAll(Pageable pageable) {
        log.debug("Request to get all Albums");
        Page<Album> result = albumRepository.findAll(pageable);
        return result;
    }

    @Transactional(readOnly = true)
    public List<Album> findAll() {
        return albumRepository.findAll();
    }

    /**
     *  Get one album by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Album findOne(Long id) {
        log.debug("Request to get Album : {}", id);
        Album album = albumRepository.findOne(id);
        return album;
    }

    /**
     *  Delete the  album by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Album : {}", id);
        albumRepository.delete(id);
    }
}
