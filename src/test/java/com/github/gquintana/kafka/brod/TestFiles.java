package com.github.gquintana.kafka.brod;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

public class TestFiles {
    public static void deleteDirRecursively(File directory) throws IOException {
        deleteDirRecursively(directory.toPath());
    }

    public static void deleteDirRecursively(Path directory) throws IOException {
        Files.walkFileTree(directory, new DeleteFileVisitor());
    }

    private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}
