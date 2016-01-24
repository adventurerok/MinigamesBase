package com.ithinkrok.minigames.util.io;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * Created by paul on 08/11/15.
 *
 * Utility for handling directories
 */
public class DirectoryUtils {

    public static void copy(Path from, Path to) throws IOException {
        StandardCopyOption copyOption = StandardCopyOption.REPLACE_EXISTING;

        Files.walkFileTree(from, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetPath = to.resolve(from.relativize(dir));
                if(!Files.exists(targetPath)){
                    Files.createDirectory(targetPath);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, to.resolve(from.relativize(file)), copyOption);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public static void delete(Path dir) throws IOException {
        Files.walkFileTree(dir, new SimpleFileVisitor<Path>(){
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                if(exc == null){
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
                throw exc;
            }
        });
    }
}
