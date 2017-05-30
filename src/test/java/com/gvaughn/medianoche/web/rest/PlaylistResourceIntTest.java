package com.gvaughn.medianoche.web.rest;

import com.gvaughn.medianoche.MedianocheApp;

import com.gvaughn.medianoche.domain.Playlist;
import com.gvaughn.medianoche.repository.PlaylistRepository;
import com.gvaughn.medianoche.rest.TestUtil;
import com.gvaughn.medianoche.service.MediaUserService;
import com.gvaughn.medianoche.service.PlaylistService;
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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.ZoneId;
import java.util.List;

import static com.gvaughn.medianoche.rest.TestUtil.sameInstant;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for the PlaylistResource REST controller.
 *
 * @see PlaylistResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MedianocheApp.class)
public class PlaylistResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final ZonedDateTime DEFAULT_CREATED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_CREATED = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    private static final ZonedDateTime DEFAULT_LAST_MODIFIED = ZonedDateTime.ofInstant(Instant.ofEpochMilli(0L), ZoneOffset.UTC);
    private static final ZonedDateTime UPDATED_LAST_MODIFIED = ZonedDateTime.now(ZoneId.systemDefault()).withNano(0);

    @Autowired
    private PlaylistRepository playlistRepository;

    @Autowired
    private PlaylistService playlistService;

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

    private MockMvc restPlaylistMockMvc;

    private Playlist playlist;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        PlaylistResource playlistResource = new PlaylistResource(playlistService);
        this.restPlaylistMockMvc = MockMvcBuilders.standaloneSetup(playlistResource)
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
    public static Playlist createEntity(EntityManager em) {
        Playlist playlist = new Playlist()
            .name(DEFAULT_NAME);
        return playlist;
    }

    @Before
    public void initTest() {
        playlist = createEntity(em);
    }

    @Test
    @Transactional
    public void createPlaylist() throws Exception {
        int databaseSizeBeforeCreate = playlistRepository.findAll().size();

        // Create the Playlist
        restPlaylistMockMvc.perform(post("/api/playlists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(playlist)))
            .andExpect(status().isCreated());

        // Validate the Playlist in the database
        List<Playlist> playlistList = playlistRepository.findAll();
        assertThat(playlistList).hasSize(databaseSizeBeforeCreate + 1);
        Playlist testPlaylist = playlistList.get(playlistList.size() - 1);
        assertThat(testPlaylist.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testPlaylist.getCreatedDate()).isNotNull();
        assertThat(testPlaylist.getLastModifiedDate()).isNotNull();
    }

    @Test
    @Transactional
    public void createPlaylistWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = playlistRepository.findAll().size();

        // Create the Playlist with an existing ID
        playlist.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restPlaylistMockMvc.perform(post("/api/playlists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(playlist)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Playlist> playlistList = playlistRepository.findAll();
        assertThat(playlistList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = playlistRepository.findAll().size();
        // set the field null
        playlist.setName(null);

        // Create the Playlist, which fails.

        restPlaylistMockMvc.perform(post("/api/playlists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(playlist)))
            .andExpect(status().isBadRequest());

        List<Playlist> playlistList = playlistRepository.findAll();
        assertThat(playlistList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllPlaylists() throws Exception {
        // Initialize the database
        saveAndFlush(playlist);

        // Get all the playlistList
        restPlaylistMockMvc.perform(get("/api/playlists?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(playlist.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getPlaylist() throws Exception {
        // Initialize the database
        saveAndFlush(playlist);

        // Get the playlist
        restPlaylistMockMvc.perform(get("/api/playlists/{id}", playlist.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(playlist.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingPlaylist() throws Exception {
        // Get the playlist
        restPlaylistMockMvc.perform(get("/api/playlists/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updatePlaylist() throws Exception {
        // Initialize the database
        playlistService.save(playlist);

        int databaseSizeBeforeUpdate = playlistRepository.findAll().size();

        // Update the playlist
        Playlist updatedPlaylist = playlistRepository.findOne(playlist.getId());
        updatedPlaylist
            .name(UPDATED_NAME)
            .setCreatedDate(UPDATED_CREATED.toInstant());
            updatedPlaylist.setLastModifiedDate(UPDATED_LAST_MODIFIED.toInstant());

        restPlaylistMockMvc.perform(put("/api/playlists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedPlaylist)))
            .andExpect(status().isOk());

        // Validate the Playlist in the database
        List<Playlist> playlistList = playlistRepository.findAll();
        assertThat(playlistList).hasSize(databaseSizeBeforeUpdate);
        Playlist testPlaylist = playlistList.get(playlistList.size() - 1);
        assertThat(testPlaylist.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testPlaylist.getCreatedDate()).isNotNull();
        assertThat(testPlaylist.getLastModifiedDate()).isNotNull();
    }

    @Test
    @Transactional
    public void updateNonExistingPlaylist() throws Exception {
        int databaseSizeBeforeUpdate = playlistRepository.findAll().size();

        // Create the Playlist

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restPlaylistMockMvc.perform(put("/api/playlists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(playlist)))
            .andExpect(status().isCreated());

        // Validate the Playlist in the database
        List<Playlist> playlistList = playlistRepository.findAll();
        assertThat(playlistList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deletePlaylist() throws Exception {
        // Initialize the database
        playlistService.save(playlist);

        int databaseSizeBeforeDelete = playlistRepository.findAll().size();

        // Get the playlist
        restPlaylistMockMvc.perform(delete("/api/playlists/{id}", playlist.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Playlist> playlistList = playlistRepository.findAll();
        assertThat(playlistList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Playlist.class);
        Playlist playlist1 = new Playlist();
        playlist1.setId(1L);
        Playlist playlist2 = new Playlist();
        playlist2.setId(playlist1.getId());
        assertThat(playlist1).isEqualTo(playlist2);
        playlist2.setId(2L);
        assertThat(playlist1).isNotEqualTo(playlist2);
        playlist1.setId(null);
        assertThat(playlist1).isNotEqualTo(playlist2);
    }

    private Playlist saveAndFlush(Playlist playlist) {
        playlist.setMediaUser(mediaUserService.getDefaultUser());
        return playlistRepository.saveAndFlush(playlist);
    }
}
