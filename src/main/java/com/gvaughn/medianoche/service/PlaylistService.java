package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.domain.Playlist;
import com.gvaughn.medianoche.repository.PlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing Playlist.
 */
@Service
@Transactional
public class PlaylistService {

    private final Logger log = LoggerFactory.getLogger(PlaylistService.class);

    private final PlaylistRepository playlistRepository;

    @Autowired
    private MediaUserService mediaUserService;

    public PlaylistService(PlaylistRepository playlistRepository) {
        this.playlistRepository = playlistRepository;
    }

    /**
     * Save a playlist.
     *
     * @param playlist the entity to save
     * @return the persisted entity
     */
    public Playlist save(Playlist playlist) {
        log.debug("Request to save Playlist : {}", playlist);
        if (playlist.getMediaUser() == null) {
            playlist.setMediaUser(mediaUserService.getDefaultUser());
        }
        Playlist result = playlistRepository.save(playlist);
        return result;
    }

    /**
     *  Get all the playlists.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<Playlist> findAll(Pageable pageable) {
        log.debug("Request to get all Playlists");
        Page<Playlist> result = playlistRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one playlist by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public Playlist findOne(Long id) {
        log.debug("Request to get Playlist : {}", id);
        Playlist playlist = playlistRepository.findOne(id);
        return playlist;
    }

    /**
     *  Delete the  playlist by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete Playlist : {}", id);
        playlistRepository.delete(id);
    }
}
