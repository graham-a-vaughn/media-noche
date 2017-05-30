package com.gvaughn.medianoche.web.rest;

import com.gvaughn.medianoche.MedianocheApp;

import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.repository.ArtistRepository;
import com.gvaughn.medianoche.rest.TestUtil;
import com.gvaughn.medianoche.service.ArtistService;
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
 * Test class for the ArtistResource REST controller.
 *
 * @see ArtistResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MedianocheApp.class)
public class ArtistResourceIntTest {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private MappingJackson2HttpMessageConverter jacksonMessageConverter;

    @Autowired
    private PageableHandlerMethodArgumentResolver pageableArgumentResolver;

    @Autowired
    private ExceptionTranslator exceptionTranslator;

    @Autowired
    private EntityManager em;

    private MockMvc restArtistMockMvc;

    private Artist artist;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        ArtistResource artistResource = new ArtistResource(artistService);
        this.restArtistMockMvc = MockMvcBuilders.standaloneSetup(artistResource)
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
    public static Artist createEntity(EntityManager em) {
        Artist artist = new Artist()
            .name(DEFAULT_NAME);
        return artist;
    }

    @Before
    public void initTest() {
        artist = createEntity(em);
    }

    @Test
    @Transactional
    public void createArtist() throws Exception {
        int databaseSizeBeforeCreate = artistRepository.findAll().size();

        // Create the Artist
        restArtistMockMvc.perform(post("/api/artists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(artist)))
            .andExpect(status().isCreated());

        // Validate the Artist in the database
        List<Artist> artistList = artistRepository.findAll();
        assertThat(artistList).hasSize(databaseSizeBeforeCreate + 1);
        Artist testArtist = artistList.get(artistList.size() - 1);
        assertThat(testArtist.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    @Transactional
    public void createArtistWithExistingId() throws Exception {
        int databaseSizeBeforeCreate = artistRepository.findAll().size();

        // Create the Artist with an existing ID
        artist.setId(1L);

        // An entity with an existing ID cannot be created, so this API call must fail
        restArtistMockMvc.perform(post("/api/artists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(artist)))
            .andExpect(status().isBadRequest());

        // Validate the Alice in the database
        List<Artist> artistList = artistRepository.findAll();
        assertThat(artistList).hasSize(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    public void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = artistRepository.findAll().size();
        // set the field null
        artist.setName(null);

        // Create the Artist, which fails.

        restArtistMockMvc.perform(post("/api/artists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(artist)))
            .andExpect(status().isBadRequest());

        List<Artist> artistList = artistRepository.findAll();
        assertThat(artistList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    public void getAllArtists() throws Exception {
        // Initialize the database
        artistRepository.saveAndFlush(artist);

        // Get all the artistList
        restArtistMockMvc.perform(get("/api/artists?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(artist.getId().intValue())))
            .andExpect(jsonPath("$.[*].name").value(hasItem(DEFAULT_NAME.toString())));
    }

    @Test
    @Transactional
    public void getArtist() throws Exception {
        // Initialize the database
        artistRepository.saveAndFlush(artist);

        // Get the artist
        restArtistMockMvc.perform(get("/api/artists/{id}", artist.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.id").value(artist.getId().intValue()))
            .andExpect(jsonPath("$.name").value(DEFAULT_NAME.toString()));
    }

    @Test
    @Transactional
    public void getNonExistingArtist() throws Exception {
        // Get the artist
        restArtistMockMvc.perform(get("/api/artists/{id}", Long.MAX_VALUE))
            .andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    public void updateArtist() throws Exception {
        // Initialize the database
        artistService.save(artist);

        int databaseSizeBeforeUpdate = artistRepository.findAll().size();

        // Update the artist
        Artist updatedArtist = artistRepository.findOne(artist.getId());
        updatedArtist
            .name(UPDATED_NAME);

        restArtistMockMvc.perform(put("/api/artists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(updatedArtist)))
            .andExpect(status().isOk());

        // Validate the Artist in the database
        List<Artist> artistList = artistRepository.findAll();
        assertThat(artistList).hasSize(databaseSizeBeforeUpdate);
        Artist testArtist = artistList.get(artistList.size() - 1);
        assertThat(testArtist.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    @Transactional
    public void updateNonExistingArtist() throws Exception {
        int databaseSizeBeforeUpdate = artistRepository.findAll().size();

        // Create the Artist

        // If the entity doesn't have an ID, it will be created instead of just being updated
        restArtistMockMvc.perform(put("/api/artists")
            .contentType(TestUtil.APPLICATION_JSON_UTF8)
            .content(TestUtil.convertObjectToJsonBytes(artist)))
            .andExpect(status().isCreated());

        // Validate the Artist in the database
        List<Artist> artistList = artistRepository.findAll();
        assertThat(artistList).hasSize(databaseSizeBeforeUpdate + 1);
    }

    @Test
    @Transactional
    public void deleteArtist() throws Exception {
        // Initialize the database
        artistService.save(artist);

        int databaseSizeBeforeDelete = artistRepository.findAll().size();

        // Get the artist
        restArtistMockMvc.perform(delete("/api/artists/{id}", artist.getId())
            .accept(TestUtil.APPLICATION_JSON_UTF8))
            .andExpect(status().isOk());

        // Validate the database is empty
        List<Artist> artistList = artistRepository.findAll();
        assertThat(artistList).hasSize(databaseSizeBeforeDelete - 1);
    }

    @Test
    @Transactional
    public void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Artist.class);
        Artist artist1 = new Artist();
        artist1.setId(1L);
        Artist artist2 = new Artist();
        artist2.setId(artist1.getId());
        assertThat(artist1).isEqualTo(artist2);
        artist2.setId(2L);
        assertThat(artist1).isNotEqualTo(artist2);
        artist1.setId(null);
        assertThat(artist1).isNotEqualTo(artist2);
    }
}
