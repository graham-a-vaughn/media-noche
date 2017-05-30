package com.gvaughn.medianoche.web.rest;

import com.gvaughn.medianoche.MedianocheApp;

import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.domain.Song;
import com.gvaughn.medianoche.repository.SongRepository;
import com.gvaughn.medianoche.rest.TestUtil;
import com.gvaughn.medianoche.service.SongService;
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
 * Test class for the SongResource REST controller.
 *
 * @see SongResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MedianocheApp.class)
public class SongResourceIntTest {

    private static final String DEFAULT_FILE = "AAAAAAAAAA";
    private static final String UPDATED_FILE = "BBBBBBBBBB";

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private SongRepository songRepository;

    @Autowired
    private SongService songService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restSongMockMvc;

    private Song song;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        SongResource songResource = new SongResource(songService);
        this.restSongMockMvc = MockMvcBuilders.standaloneSetup(songResource)
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
    public static Song createEntity(EntityManager em) {
        Song song = new Song()
            .file(DEFAULT_FILE)
            .name(DEFAULT_NAME);
        Artist artist = ArtistResourceIntTest.createEntity(em);
        em.persist(artist);
        song.setArtist(artist);
        return song;
    }

    @Before
    public void initTest() {
        song = createEntity(em);
    }

    @Test
    @Transactional
    public void createSong() throws Exception {
        int databaseSizeBeforeCreate = songRepository.findAll().size();

        // Create the Song
        restSongMockMvc.perform(post("/api/songs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(song)))
            .andExpect(status().isCreated());

        // Validate the Song in the database
        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeCreate + 1);
        Song testSong = songList.get(songList.size() - 1);
        assertThat(testSong.getFile()).isEqualTo(DEFAULT_FILE);
        assertThat(testSong.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createSongWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = songRepository.findAll().size();

        // Create the Song with an existing ID
        song.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restSongMockMvc.perform(post("/api/songs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(song)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkFileIsRequired() throws Exception {
        int databaseSizeBeforeTest = songRepository.findAll().size();
        // set the field null
        song.setFile(null);

        // Create the Song, which fails.

        restSongMockMvc.perform(post("/api/songs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(song)))
            .andExpect(status().isBadRequest());

        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = songRepository.findAll().size();
        // set the field null
        song.setName(null);

        // Create the Song, which fails.

        restSongMockMvc.perform(post("/api/songs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(song)))
            .andExpect(status().isBadRequest());

        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllSongs() throws Exception {
        // Initialize the database
        songRepository.saveAndFlush(song);

        // Get all the songList
        restSongMockMvc.perform(get("/api/songs?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(song.getId().intValue())))
            .andExpect(jsonPath("$.[*].file").value(hasItem(DEFAULT_FILE.toString())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getSong() throws Exception {
        // Initialize the database
        songRepository.saveAndFlush(song);

        // Get the song
        restSongMockMvc.perform(get("/api/songs/{id}", song.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(song.getId().intValue()))
            .andExpect(jsonPath("$.file").value(DEFAULT_FILE.toString()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingSong() throws Exception {
        // Get the song
        restSongMockMvc.perform(get("/api/songs/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateSong() throws Exception {
        // Initialize the database
        songService.save(song);

        int databaseSizeBeforeUpdate = songRepository.findAll().size();

        // Update the song
        Song updatedSong = songRepository.findOne(song.getId());
        updatedSong
            .file(UPDATED_FILE)
            .name(UPDATED_NAME);

        restSongMockMvc.perform(put("/api/songs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedSong)))
            .andExpect(status().isOk());

        // Validate the Song in the database
        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeUpdate);
        Song testSong = songList.get(songList.size() - 1);
        assertThat(testSong.getFile()).isEqualTo(UPDATED_FILE);
        assertThat(testSong.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingSong() throws Exception {
        int databaseSizeBeforeUpdate = songRepository.findAll().size();

        // Create the Song

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restSongMockMvc.perform(put("/api/songs")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(song)))
            .andExpect(status().isCreated());

        // Validate the Song in the database
        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteSong() throws Exception {
        // Initialize the database
        songService.save(song);

        int databaseSizeBeforeDelete = songRepository.findAll().size();

        // Get the song
        restSongMockMvc.perform(delete("/api/songs/{id}", song.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Song> songList = songRepository.findAll();
        assertThat(songList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Song.class);
        Song song1 = new Song();
        song1.setId(1L);
        Song song2 = new Song();
        song2.setId(song1.getId());
        assertThat(song1).isEqualTo(song2);
        song2.setId(2L);
        assertThat(song1).isNotEqualTo(song2);
        song1.setId(null);
        assertThat(song1).isNotEqualTo(song2);
    }
}
