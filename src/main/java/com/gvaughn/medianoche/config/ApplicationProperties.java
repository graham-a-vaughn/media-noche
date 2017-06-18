package com.gvaughn.medianoche.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Properties specific to JHipster.
 *
 * <p>
 *     Properties are configured in the application.yml file.
 * </p>
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private Stub stub = new Stub();
    private FileType fileType = new FileType();

    public Stub getStub() {
        return stub;
    }

    public void setStub(Stub stub) {
        this.stub = stub;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public class Stub {
        private String musicdir;

        public String getMusicdir() {
            return musicdir;
        }

        public void setMusicdir(String musicdir) {
            this.musicdir = musicdir;
        }
    }

    public class FileType {
        private String audioExtensions;

        public void setAudioExtensions(String audioExtensions) {
            this.audioExtensions = audioExtensions;
        }

        public String[] getAudioExtensionTypes() {
            String[] ext = StringUtils.split(audioExtensions, ',');
            Arrays.sort(ext);
            return Arrays.stream(ext).map(s -> StringUtils.prependIfMissing(s, ".")).collect(Collectors.toList()).toArray(new String[ext.length]);
        }
    }

}
