package com.gvaughn.medianoche.web.rest;

import com.gvaughn.medianoche.MedianocheApp;

import com.gvaughn.medianoche.domain.MediaUser;
import com.gvaughn.medianoche.repository.MediaUserRepository;
import com.gvaughn.medianoche.rest.TestUtil;
import com.gvaughn.medianoche.service.MediaUserService;
import com.gvaughn.medianoche.web.rest.errors.ExceptionTranslator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the MediaUserResource REST controller.
 *
 * @see MediaUserResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MedianocheApp.class)
public class MediaUserResourceIntTest {

    private static final String DEFAULT_USERNAME = "AAAAAAAAAA";
    private static final String UPDATED_USERNAME = "BBBBBBBBBB";

    @Autowired
    private MediaUserRepository mediaUserRepository;

    @Autowired
    private MediaUserService mediaUserService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restMediaUserMockMvc;

    private MediaUser mediaUser;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        MediaUserResource mediaUserResource = new MediaUserResource(mediaUserService);
        this.restMediaUserMockMvc = MockMvcBuilders.standaloneSetup(mediaUserResource)
            .setCustomArgumentResolvers(pageableArgumentResolver)
            .setControllerAdvice(exceptionTranslator)
            .setMessageConverters(jacksonMessageConverter).build();
    }

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static MediaUser createEntity(EntityManager em) {
        MediaUser mediaUser = new MediaUser()
            .username(DEFAULT_USERNAME);
        return mediaUser;
    }

    @Before
    public void initTest() {
        mediaUser = createEntity(em);
    }

    @Test
    @Transactional
    public void createMediaUser() throws Exception {
        int databaseSizeBeforeCreate = mediaUserRepository.findAll().size();

        // Create the MediaUser
        restMediaUserMockMvc.perform(post("/api/media-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(mediaUser)))
            .andExpect(status().isCreated());

        // Validate the MediaUser in the database
        List<MediaUser> mediaUserList = mediaUserRepository.findAll();
        assertThat(mediaUserList).hasSize(databaseSizeBeforeCreate + 1);
        MediaUser testMediaUser = mediaUserList.get(mediaUserList.size() - 1);
        assertThat(testMediaUser.getUsername()).isEqualTo(DEFAULT_USERNAME);
    }

    @Test
    @Transactional
    public void createMediaUserWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = mediaUserRepository.findAll().size();

        // Create the MediaUser with an existing ID
        mediaUser.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restMediaUserMockMvc.perform(post("/api/media-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(mediaUser)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<MediaUser> mediaUserList = mediaUserRepository.findAll();
        assertThat(mediaUserList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkUsernameIsRequired() throws Exception {
        int databaseSizeBeforeTest = mediaUserRepository.findAll().size();
        // set the field null
        mediaUser.setUsername(null);

        // Create the MediaUser, which fails.

        restMediaUserMockMvc.perform(post("/api/media-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(mediaUser)))
            .andExpect(status().isBadRequest());

        List<MediaUser> mediaUserList = mediaUserRepository.findAll();
        assertThat(mediaUserList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllMediaUsers() throws Exception {
        // Initialize the database
        mediaUserRepository.saveAndFlush(mediaUser);

        // Get all the mediaUserList
        restMediaUserMockMvc.perform(get("/api/media-users?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(mediaUser.getId().intValue())))
            .andExpect(jsonPath("$.[*].username").value(hasItem(DEFAULT_USERNAME.toString())));
    }

    @Test
    @Transactional
    public void getMediaUser() throws Exception {
        // Initialize the database
        mediaUserRepository.saveAndFlush(mediaUser);

        // Get the mediaUser
        restMediaUserMockMvc.perform(get("/api/media-users/{id}", mediaUser.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(mediaUser.getId().intValue()))
            .andExpect(jsonPath("$.username").value(DEFAULT_USERNAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingMediaUser() throws Exception {
        // Get the mediaUser
        restMediaUserMockMvc.perform(get("/api/media-users/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateMediaUser() throws Exception {
        // Initialize the database
        mediaUserService.save(mediaUser);

        int databaseSizeBeforeUpdate = mediaUserRepository.findAll().size();

        // Update the mediaUser
        MediaUser updatedMediaUser = mediaUserRepository.findOne(mediaUser.getId());
        updatedMediaUser
            .username(UPDATED_USERNAME);

        restMediaUserMockMvc.perform(put("/api/media-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedMediaUser)))
            .andExpect(status().isOk());

        // Validate the MediaUser in the database
        List<MediaUser> mediaUserList = mediaUserRepository.findAll();
        assertThat(mediaUserList).hasSize(databaseSizeBeforeUpdate);
        MediaUser testMediaUser = mediaUserList.get(mediaUserList.size() - 1);
        assertThat(testMediaUser.getUsername()).isEqualTo(UPDATED_USERNAME);
    }

    @Test
    @Transactional
    public void updateNonExistingMediaUser() throws Exception {
        int databaseSizeBeforeUpdate = mediaUserRepository.findAll().size();

        // Create the MediaUser

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restMediaUserMockMvc.perform(put("/api/media-users")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(mediaUser)))
            .andExpect(status().isCreated());

        // Validate the MediaUser in the database
        List<MediaUser> mediaUserList = mediaUserRepository.findAll();
        assertThat(mediaUserList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteMediaUser() throws Exception {
        // Initialize the database
        mediaUserService.save(mediaUser);

        int databaseSizeBeforeDelete = mediaUserRepository.findAll().size();

        // Get the mediaUser
        restMediaUserMockMvc.perform(delete("/api/media-users/{id}", mediaUser.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<MediaUser> mediaUserList = mediaUserRepository.findAll();
        assertThat(mediaUserList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(MediaUser.class);
        MediaUser mediaUser1 = new MediaUser();
        mediaUser1.setId(1L);
        MediaUser mediaUser2 = new MediaUser();
        mediaUser2.setId(mediaUser1.getId());
        assertThat(mediaUser1).isEqualTo(mediaUser2);
        mediaUser2.setId(2L);
        assertThat(mediaUser1).isNotEqualTo(mediaUser2);
        mediaUser1.setId(null);
        assertThat(mediaUser1).isNotEqualTo(mediaUser2);
    }
}
