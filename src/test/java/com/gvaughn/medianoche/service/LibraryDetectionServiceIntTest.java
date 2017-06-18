package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.MedianocheApp;
import com.gvaughn.medianoche.config.ApplicationProperties;
import com.gvaughn.medianoche.domain.Album;
import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.domain.FileSystemLibraryScan;
import com.gvaughn.medianoche.domain.Song;
import com.gvaughn.medianoche.utils.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by graham on 6/11/17.
 * <p>
 * <p>
 * Media Project - streaming media system.
 * Copyright (C) 2017  Graham Vaughn
 * <p>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MedianocheApp.class)
@Transactional
public class LibraryDetectionServiceIntTest {

    private static final String[] ARTISTS = new String[]{ "Mastodon", "Jesus Lizard", "Duran Duran" };
    private static final String[] ARTISTS_UPDATE = new String[]{ "Budgie", "Rush" };
    private static final int TOTAL_ARTISTS_AFTER_UPDATE = 5;
    private static final String[][] ALBUMS = new String[][]{ new String[]{"Blood Mountain", "Leviathan"}, new String[]{ "Liar", "Goat", "Show" }, new String[]{ "Rio" }};
    private static final String[][] ALBUMS_UPDATE = new String[][]{ new String[]{"Money", "Poop"}, new String[]{ "2112", "Power Windows" }};
    private static final Integer[] SONG_COUNTS = new Integer[]{ 3, 4, 2, 5, 4, 6 };
    private static final Integer[] SONG_COUNTS_UPDATE = new Integer[]{ 5, 4, 2, 5 };
    private static final int ALBUM_COUNT = Arrays.stream(ALBUMS).map(a -> a.length).reduce(0, (a, b) -> a + b);
    private static final int UPDATE_ALBUM_COUNT = 4;
    private static final int TOTAL_ALBUM_COUNT_AFTER_UPDATE = ALBUM_COUNT + UPDATE_ALBUM_COUNT;
    private static final int SONG_COUNT = Arrays.stream(SONG_COUNTS).mapToInt(Integer::new).sum();
    private static final int TOTAL_SONG_COUNT_AFTER_UPDATE = SONG_COUNT + 16;
    private static final String FILE_EXT = ".wav";
    private static final String INVALID_FILE = "README.txt";
    private static final String NO_EXTENSION_FILE = "README";
    private static final char[] WEIRD_CHARS = new char[]{'\t', '\n', '.', '&', '%', '$', '~'}; //TODO Track this for manual entry

    @Autowired
    private LibraryDetectionService libraryDetectionService;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private SongService songService;

    @Autowired
    private ApplicationProperties applicationProperties;

    private File rootDir;
    private Map<String, File> artistDirs = new HashMap<>();
    private Map<String, File> albumDirs = new HashMap<>();

    @Before
    public void setUp() throws IOException {
        rootDir = mkdirsRoot();

        createLibrary(ARTISTS, ALBUMS, SONG_COUNTS);
    }

    private void updateLibrary() throws IOException {
        createLibrary(ARTISTS_UPDATE, ALBUMS_UPDATE, SONG_COUNTS_UPDATE);
    }
    private void createLibrary(String[] art, String[][] alb, Integer[] counts) throws IOException {
        List<Integer> songCounts = Arrays.asList(counts);
        Iterator<Integer> songIter = songCounts.iterator();
        for (int i = 0; i < art.length; i++) {
            String artist = art[i];
            File artistDir = mkdir(rootDir, artist);
            artistDirs.put(artist, artistDir);
            for (int j = 0; j < alb[i].length; j++) {
                String album = alb[i][j];
                File albumDir = mkdir(artistDir, album);
                albumDirs.put(album, albumDir);
                int count = songIter.next();
                for (int k = 0; k < count; k++) {
                    String song = k + "_Song" + FILE_EXT;
                    String songPath = albumDir.getAbsolutePath() + File.separatorChar + song;
                    File songFile = new File(songPath);
                    songFile.createNewFile();
                }
            }
        }
    }

    private File mkEmptyArtistDir(String name) throws IOException {
        return mkdir(rootDir, name);
    }

    private File mkEmptyAlbumDir(String artist, String name) throws IOException {
        File artistDir = artistDirs.get(artist);
        return mkdir(artistDir, name);
    }

    private File makeSongFile(String album, String name) throws IOException {
        String songPath = albumDirs.get(album).getAbsolutePath() + File.separatorChar + name;
        File songFile = new File(songPath);
        songFile.createNewFile();
        return songFile;
    }

    @Test
    public void fullScanCountsAreCorrect() {
        FileSystemLibraryScan scan = libraryDetectionService.scanForLibraryUpdates();
        verifyScanCounts(scan);
    }

    private void verifyScanCounts(FileSystemLibraryScan scan) {
        assertThat(scan.getSuccess()).isTrue();
        assertThat(scan.getNewArtistCount()).isEqualTo(ARTISTS.length);
        assertThat(scan.getNewAlbumCount()).isEqualTo(ALBUM_COUNT);
    }

    @Test
    public void filesArePersistedAsLibrary() {
        List<Artist> pre = artistService.findAll();
        assertThat(pre.size()).isEqualTo(0);
        libraryDetectionService.scanForLibraryUpdates();
        verifyPersistedLibrary();
    }

    private void verifyPersistedLibrary() {
        List<Artist> artists = artistService.findAll();
        assertThat(artists.size()).isEqualTo(ARTISTS.length);
        List<Album> albums = albumService.findAll();
        assertThat(albums.size()).isEqualTo(ALBUM_COUNT);
        List<Song> songs = songService.findAll();
        assertThat(songs.size()).isEqualTo(SONG_COUNT);
    }

    private void verifyUpdateScanCounts(FileSystemLibraryScan scan) {
        assertThat(scan.getSuccess()).isTrue();
        assertThat(scan.getNewArtistCount()).isEqualTo(ARTISTS_UPDATE.length);
        assertThat(scan.getNewAlbumCount()).isEqualTo(UPDATE_ALBUM_COUNT);
    }

    private void verifyUpdatedPersistedLibrary() {
        List<Artist> artists = artistService.findAll();
        assertThat(artists.size()).isEqualTo(TOTAL_ARTISTS_AFTER_UPDATE);
        List<Album> albums = albumService.findAll();
        assertThat(albums.size()).isEqualTo(TOTAL_ALBUM_COUNT_AFTER_UPDATE);
        List<Song> songs = songService.findAll();
        assertThat(songs.size()).isEqualTo(TOTAL_SONG_COUNT_AFTER_UPDATE);
    }

    @Test
    public void emptyAlbumDirectoriesAreIgnored() throws IOException {
        mkEmptyAlbumDir(ARTISTS[1], "Poop");
        scanAndVerifyAll();
    }

    private void scanAndVerifyAll() {
        FileSystemLibraryScan scan = libraryDetectionService.scanForLibraryUpdates();
        verifyScanCounts(scan);
        verifyPersistedLibrary();
    }

    private void scanAndVerifyAllUpdates() {
        FileSystemLibraryScan scan = libraryDetectionService.scanForLibraryUpdates();
        verifyUpdateScanCounts(scan);
        verifyUpdatedPersistedLibrary();
    }

    @Test
    public void emptyArtistDirectoriesAreIgnored() throws IOException {
        mkEmptyArtistDir("Poop Kings");
        scanAndVerifyAll();
    }

    @Test
    public void invalidSongFilesAreIgnored() throws IOException {
        makeSongFile(ALBUMS[0][0], INVALID_FILE);
        makeSongFile(ALBUMS[0][0], "Another" + INVALID_FILE);
        makeSongFile(ALBUMS[0][0], NO_EXTENSION_FILE);
        scanAndVerifyAll();
    }

    @Test
    public void newFilesAreScannedAndAddedToLibrary() throws IOException {
        scanAndVerifyAll();
        updateLibrary();
        scanAndVerifyAllUpdates();
    }

    @Ignore
    public void weirdNamesAreHandledGracefully() {
        //TODO: Is this anything?
    }

    //TODO: Test graceful exception handling
    @After
    public void tearDown() throws IOException {
        if (rootDir != null) {
            FileUtils.deleteDirectoryRecursive(rootDir, null);
        }
    }

    private File mkdirsRoot() throws IOException {
        File dir = new File(applicationProperties.getStub().getMusicdir());
        return mkdir(dir.getParentFile(), dir.getName());
    }


    private File mkdir(File parent, String dir) throws IOException {
        Path parentPath = parent.toPath();
        Path dirPath = parentPath.resolve(dir);
        Path result = Files.createDirectories(dirPath);
        return result.toFile();
    }
}
