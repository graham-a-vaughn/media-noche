package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.domain.MediaUser;
import com.gvaughn.medianoche.repository.MediaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service Implementation for managing MediaUser.
 */
@Service
@Transactional
public class MediaUserService {

    private final Logger log = LoggerFactory.getLogger(MediaUserService.class);

    private final MediaUserRepository mediaUserRepository;

    public MediaUserService(MediaUserRepository mediaUserRepository) {
        this.mediaUserRepository = mediaUserRepository;
    }

    /**
     * Save a mediaUser.
     *
     * @param mediaUser the entity to save
     * @return the persisted entity
     */
    public MediaUser save(MediaUser mediaUser) {
        log.debug("Request to save MediaUser : {}", mediaUser);
        MediaUser result = mediaUserRepository.save(mediaUser);
        return result;
    }

    /**
     * Returns the user to use as the default association.
     * @return the user to use as the default association.
     */
    public MediaUser getDefaultUser() {
        return mediaUserRepository.findAll(new Sort(Sort.Direction.ASC,"id")).stream().findFirst()
            .orElseThrow(() -> new IllegalStateException("No media users in system."));
    }

    /**
     *  Get all the mediaUsers.
     *
     *  @param pageable the pagination information
     *  @return the list of entities
     */
    @Transactional(readOnly = true)
    public Page<MediaUser> findAll(Pageable pageable) {
        log.debug("Request to get all MediaUsers");
        Page<MediaUser> result = mediaUserRepository.findAll(pageable);
        return result;
    }

    /**
     *  Get one mediaUser by id.
     *
     *  @param id the id of the entity
     *  @return the entity
     */
    @Transactional(readOnly = true)
    public MediaUser findOne(Long id) {
        log.debug("Request to get MediaUser : {}", id);
        MediaUser mediaUser = mediaUserRepository.findOne(id);
        return mediaUser;
    }

    /**
     *  Delete the  mediaUser by id.
     *
     *  @param id the id of the entity
     */
    public void delete(Long id) {
        log.debug("Request to delete MediaUser : {}", id);
        mediaUserRepository.delete(id);
    }
}
