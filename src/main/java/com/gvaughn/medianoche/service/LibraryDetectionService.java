package com.gvaughn.medianoche.service;

import com.gvaughn.medianoche.config.ApplicationProperties;
import com.gvaughn.medianoche.domain.Album;
import com.gvaughn.medianoche.domain.Artist;
import com.gvaughn.medianoche.domain.FileSystemLibraryScan;
import com.gvaughn.medianoche.domain.Song;
import com.gvaughn.medianoche.repository.ArtistRepository;
import com.gvaughn.medianoche.repository.FileSystemLibraryScanRepository;
import com.gvaughn.medianoche.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by graham on 6/2/17.
 */
@Service
@Transactional
public class LibraryDetectionService {

    private final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private FileSystemLibraryScanRepository repository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private ArtistService artistService;

    @Autowired
    private AlbumService albumService;

    @Autowired
    private SongService songService;

    @Transactional
    public FileSystemLibraryScan scanForLibraryUpdates() {
        FileSystemLibraryScan scan = new FileSystemLibraryScan();
        scan.setStartTime(ZonedDateTime.now());

        try {
            String root = applicationProperties.getStub().getMusicdir();
            File rootDir = new File(root);
            scan.setScanDirectories(rootDir.getAbsolutePath());

            // Find unpersisted artist directories
            List<File>  newArtistFiles = getNewArtistFiles(rootDir);

            // persist artists
            List<String> names = new ArrayList<>(newArtistFiles.stream().map(File::getName).collect(Collectors.toList()));
            //List<Artist> newArtists = artistService.addArtists(names);
            List<Artist> newArtists = new ArrayList<>();


            //persist each song per album
            for (File artistFile : newArtistFiles) {
                if (FileUtils.isDirectoryEmpty(artistFile, 2, getValidSongFilePredicate(), log)) {
                    continue;
                }
                Artist artist = artistService.addArtist(artistFile.getName());
                newArtists.add(artist);
                List<File> albumFiles = getAllAlbumFiles(artistFile);
                for (File file : albumFiles) {
                    if (FileUtils.isDirectoryEmpty(file, 2, getValidSongFilePredicate(), log)) {
                        continue;
                    }
                    Album album = albumService.addAlbum(artist, file.getName());
                    scan.setNewAlbumCount(scan.getNewAlbumCount() + 1);
                    List<File> songFiles = getAllSongFiles(file);
                    List<Song> songs = new ArrayList<>();
                    for (File songFile : songFiles) {
                        //log.info("Saving song file: " + songFile.getName());
                        Song song = new Song(songFile.getAbsolutePath(), songFile.getName(), artist);
                        song = songService.save(song);
                        songs.add(song);
                        scan.setNewSongCount(scan.getNewSongCount() + 1);
                    }
                    albumService.setSongs(album.getId(), songs);
                }
            }

            scan.setNewArtistCount(newArtists.size());
            scan.setSuccess(true);
        } catch (Exception e) {
            log.error("Exception performing file scan: ", e);
            scan.zero();
            scan.setSuccess(false);
        } finally {
            scan.setEndTime(ZonedDateTime.now());
        }

        try {
            repository.save(scan);
        } catch (Exception e) {
            log.error("Unable to save library scan: ", e);
            //TODO: throw
            scan.zero();
            scan.setSuccess(false);
        }
        return scan;
    }

    protected List<File> getNewArtistFiles(File dir) {
        List<File> artists = new ArrayList<>();
        List<File> unknown = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Attempted to find new artists in an invalid directory: " + dir.getAbsolutePath());
        }

        File[] children = dir.listFiles();
        for (File file: children) {
            if (!file.exists() || !file.isDirectory()) {
                unknown.add(file);
            } else {
                if (artistRepository.findByName(file.getName()).isEmpty()) {
                    artists.add(file);
                }
            }
        }
        log.debug("Unknown files found in artist directory: " + unknown.size());
        return artists;
    }

    protected List<File> getAllAlbumFiles(File dir) {
        List<File> albums = new ArrayList<>();
        List<File> unknown = new ArrayList<>();

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Attempted to find new artist/albums in an invalid directory: " + dir.getAbsolutePath());
        }

        File[] children = dir.listFiles();
        for (File file: children) {
            if (!file.exists() || !file.isDirectory()) {
                unknown.add(file);
            } else {
                albums.add(file);
            }
        }

        log.debug("Unknown files found in album directory: " + dir.getAbsolutePath() + ": " + unknown.size());
        return albums;
    }

    protected List<File> getAllSongFiles(File dir) {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Attempted to find new songs in an invalid directory: " + dir.getAbsolutePath());
        }
        List<File> songs = new ArrayList<>();
        List<File> unknown = new ArrayList<>();

        File[] children = dir.listFiles();
        String[] extensions = applicationProperties.getFileType().getAudioExtensionTypes();
        //log.info("Searching directory for songs: " + dir.getAbsolutePath());
        //log.info("Extensions: " + extensions.length);
        //log.info("Children: " + children.length);
        for (File child: children) {
            //log.info("Child file: " + child.getName());
            String ext = getFileExtension(child);
            //log.info("File extension: " + ext);
            if (Arrays.binarySearch(extensions, ext) > 0) {
                //log.info("Extension legit");
                songs.add(child);
            } else {
                //log.info("Extension fail");
            }
        }
        //songs.addAll(Arrays.stream(children).filter(f -> Arrays.binarySearch(extensions, getFileExtension(f),
        //    String.CASE_INSENSITIVE_ORDER) == 0).collect(Collectors.toList()));
        //unknown.addAll(Arrays.stream(children).filter(f -> Arrays.binarySearch(extensions, getFileExtension(f),
        //    String.CASE_INSENSITIVE_ORDER) < 0).collect(Collectors.toList()));

        log.info("Unknown files found in song directory: " + dir.getAbsolutePath() + ": " + unknown.size());
        return  songs;
    }

    private String getFileExtension(File file) {
        int index = file.getName().indexOf('.');
        return  index > 0 ? file.getName().substring(index) : StringUtils.EMPTY;
    }

    private Predicate<Path> getValidSongFilePredicate() {
        return (p) -> Arrays.binarySearch(applicationProperties.getFileType().getAudioExtensionTypes(),
            getFileExtension(p.toFile())) > 0;
    }
}
