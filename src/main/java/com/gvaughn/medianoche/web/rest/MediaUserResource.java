package com.gvaughn.medianoche.web.rest;

import com.codahale.metrics.annotation.Timed;
import com.gvaughn.medianoche.domain.MediaUser;
import com.gvaughn.medianoche.service.MediaUserService;
import com.gvaughn.medianoche.web.rest.util.HeaderUtil;
import com.gvaughn.medianoche.web.rest.util.PaginationUtil;
import io.swagger.annotations.ApiParam;
import io.github.jhipster.web.util.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing MediaUser.
 */
@RestController
@RequestMapping("/api")
public class MediaUserResource {

    private final Logger log = LoggerFactory.getLogger(MediaUserResource.class);

    private static final String ENTITY_NAME = "mediaUser";
        
    private final MediaUserService mediaUserService;

    public MediaUserResource(MediaUserService mediaUserService) {
        this.mediaUserService = mediaUserService;
    }

    /**
     * POST  /media-users : Create a new mediaUser.
     *
     * @param mediaUser the mediaUser to create
     * @return the ResponseEntity with status 201 (Created) and with body the new mediaUser, or with status 400 (Bad Request) if the mediaUser has already an ID
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PostMapping("/media-users")
    @Timed
    public ResponseEntity<MediaUser> createMediaUser(@Valid @RequestBody MediaUser mediaUser) throws URISyntaxException {
        log.debug("REST request to save MediaUser : {}", mediaUser);
        if (mediaUser.getId() != null) {
            return ResponseEntity.badRequest().headers(HeaderUtil.createFailureAlert(ENTITY_NAME, "idexists", "A new mediaUser cannot already have an ID")).body(null);
        }
        MediaUser result = mediaUserService.save(mediaUser);
        return ResponseEntity.created(new URI("/api/media-users/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(ENTITY_NAME, result.getId().toString()))
            .body(result);
    }

    /**
     * PUT  /media-users : Updates an existing mediaUser.
     *
     * @param mediaUser the mediaUser to update
     * @return the ResponseEntity with status 200 (OK) and with body the updated mediaUser,
     * or with status 400 (Bad Request) if the mediaUser is not valid,
     * or with status 500 (Internal Server Error) if the mediaUser couldnt be updated
     * @throws URISyntaxException if the Location URI syntax is incorrect
     */
    @PutMapping("/media-users")
    @Timed
    public ResponseEntity<MediaUser> updateMediaUser(@Valid @RequestBody MediaUser mediaUser) throws URISyntaxException {
        log.debug("REST request to update MediaUser : {}", mediaUser);
        if (mediaUser.getId() == null) {
            return createMediaUser(mediaUser);
        }
        MediaUser result = mediaUserService.save(mediaUser);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(ENTITY_NAME, mediaUser.getId().toString()))
            .body(result);
    }

    /**
     * GET  /media-users : get all the mediaUsers.
     *
     * @param pageable the pagination information
     * @return the ResponseEntity with status 200 (OK) and the list of mediaUsers in body
     */
    @GetMapping("/media-users")
    @Timed
    public ResponseEntity<List<MediaUser>> getAllMediaUsers(@ApiParam Pageable pageable) {
        log.debug("REST request to get a page of MediaUsers");
        Page<MediaUser> page = mediaUserService.findAll(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(page, "/api/media-users");
        return new ResponseEntity<>(page.getContent(), headers, HttpStatus.OK);
    }

    /**
     * GET  /media-users/:id : get the "id" mediaUser.
     *
     * @param id the id of the mediaUser to retrieve
     * @return the ResponseEntity with status 200 (OK) and with body the mediaUser, or with status 404 (Not Found)
     */
    @GetMapping("/media-users/{id}")
    @Timed
    public ResponseEntity<MediaUser> getMediaUser(@PathVariable Long id) {
        log.debug("REST request to get MediaUser : {}", id);
        MediaUser mediaUser = mediaUserService.findOne(id);
        return ResponseUtil.wrapOrNotFound(Optional.ofNullable(mediaUser));
    }

    /**
     * DELETE  /media-users/:id : delete the "id" mediaUser.
     *
     * @param id the id of the mediaUser to delete
     * @return the ResponseEntity with status 200 (OK)
     */
    @DeleteMapping("/media-users/{id}")
    @Timed
    public ResponseEntity<Void> deleteMediaUser(@PathVariable Long id) {
        log.debug("REST request to delete MediaUser : {}", id);
        mediaUserService.delete(id);
        return ResponseEntity.ok().headers(HeaderUtil.createEntityDeletionAlert(ENTITY_NAME, id.toString())).build();
    }

}
