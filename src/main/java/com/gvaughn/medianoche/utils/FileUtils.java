package com.gvaughn.medianoche.utils;

import com.gvaughn.medianoche.service.UserService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.function.*;

/**
 * Created by graham on 6/17/17.
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
public class FileUtils {

    public static void deleteDirectoryRecursive(File dir, Logger log) throws IOException {
        Files.walkFileTree(dir.toPath(), new DeleteFileVisitor(log));
    }

    public static boolean isDirectoryEmpty(File dir, int maxDepth, Predicate<Path> pathPredicate, Logger log) throws IOException {
        DetectEmptyDirectoriesVisitor visitor = new DetectEmptyDirectoriesVisitor(maxDepth, dir.toPath(), pathPredicate, log);
        Files.walkFileTree(dir.toPath(), visitor);
        return visitor.isEmpty();
    }

    static abstract class AbstractFileVisitor implements FileVisitor<Path> {

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            return Files.exists(dir, getLinkOptions()) ? FileVisitResult.CONTINUE : FileVisitResult.SKIP_SUBTREE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
            throws IOException
        {
            consume(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
            return getFileErrorHandler().apply(file, exc);
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException e)
            throws IOException
        {
            if (e == null) {
                consume(dir);
                return FileVisitResult.CONTINUE;
            } else {
                // directory iteration failed
                return getDirErrorHandler().apply(dir, e);
            }
        }

        protected LinkOption[] getLinkOptions() {
            return new LinkOption[] { LinkOption.NOFOLLOW_LINKS };
        }

        protected BiFunction<Path, IOException, FileVisitResult> getFileErrorHandler() {
            return (path, e) -> FileVisitResult.CONTINUE;
        }

        protected BiFunction<Path, IOException, FileVisitResult> getDirErrorHandler() {
            return (path, e) -> {
                throw new IllegalStateException(e);
            };
        }

        protected abstract void consume(Path file) throws IOException;
    }

    static abstract class DepthConstrainedFileVisitor extends AbstractFileVisitor {

        protected final int maxDepth;
        protected Predicate<Path> validFilePredicate = (f) -> { return true; };
        protected Logger log = LoggerFactory.getLogger(UserService.class);


        protected DepthConstrainedFileVisitor(int maxDepth, Path root, Predicate<Path> validFilePredicate, Logger log) {
            super();
            this.maxDepth = maxDepth + (root != null ? root.getNameCount() : 0);
            log.info("Max depth: " + maxDepth);
            this.validFilePredicate = validFilePredicate;
            if (log != null) {
                this.log = log;
            }
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (!Files.exists(dir, getLinkOptions())) {
                return FileVisitResult.SKIP_SUBTREE;
            }
            return continueOrHalt(dir);
        }

        protected FileVisitResult continueOrHalt(Path path) {
            log.info("Checking maxDepth for halting threshold");
            int nameCount = path.getNameCount();
            log.info("Name count: " + nameCount);
            FileVisitResult result = nameCount <= maxDepth ? FileVisitResult.CONTINUE
                : FileVisitResult.SKIP_SUBTREE;
            log.info("Result: " + result);
            return  result;
        }
    }

    static class DetectEmptyDirectoriesVisitor extends DepthConstrainedFileVisitor {

        private boolean populated = false;

        public DetectEmptyDirectoriesVisitor(int maxDepth, Path root, Predicate<Path> validFilePredicate, Logger log) {
            super(maxDepth, root, validFilePredicate, log);
        }

        public boolean isEmpty() {
            return !populated;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (!populated) {
                consume(file);
                return FileVisitResult.CONTINUE;
            }
            return FileVisitResult.TERMINATE;
        }

        @Override
        protected void consume(Path file) throws IOException {
            if (validFilePredicate.test(file)) {
                populated = true;
            }
        }

        @Override
        protected FileVisitResult continueOrHalt(Path path) {
            FileVisitResult result;
            if (populated) {
                result = FileVisitResult.TERMINATE;
            } else {
                result = super.continueOrHalt(path);
            }
            log.info("Concrete instance result: " + result);
            return result;
        }
    }

    static class DeleteFileVisitor extends AbstractFileVisitor {

        private Logger log = LoggerFactory.getLogger(DeleteFileVisitor.class);

        public DeleteFileVisitor() {
            super();
        }

        public DeleteFileVisitor(Logger log) {
            this.log = log != null ? log : this.log;
        }

        @Override
        protected void consume(Path file) throws IOException {
            Files.deleteIfExists(file);
        }

        @Override
        protected BiFunction<Path, IOException, FileVisitResult> getFileErrorHandler() {
            return (p, e) -> {
                log.error("Error visiting file for deletion, continuing to walk directory.", e);
                return FileVisitResult.CONTINUE;
            };
        }

        @Override
        protected BiFunction<Path, IOException, FileVisitResult> getDirErrorHandler() {
            return (p, e) -> {
                log.error("Error visiting file for deletion, continuing to walk directory.", e);
                return FileVisitResult.CONTINUE;
            };
        }
    }
}
