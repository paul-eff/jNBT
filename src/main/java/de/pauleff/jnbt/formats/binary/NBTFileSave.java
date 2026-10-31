package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.NBTSaveCleanupException;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFileAttributeView;
import java.util.ArrayList;
import java.util.List;

/**
 * Writes temporary output and atomically replaces the destination.
 */
final class NBTFileSave implements AutoCloseable
{
    private final FileOperations files;
    private final List<Path> pending = new ArrayList<>();

    /**
     * Creates a save scope with the supplied filesystem operations.
     *
     * @param files the filesystem operations
     */
    private NBTFileSave(FileOperations files)
    {
        this.files = files;
    }

    /**
     * Saves using the default filesystem operations.
     *
     * @param target the destination file
     * @param compression the output compression
     * @param content the serializer
     * @throws NBTSaveCleanupException if saving succeeded but backup removal failed
     * @throws IOException if saving fails
     */
    static void write(Path target, Compression_Types compression, ContentWriter content) throws IOException
    {
        write(target, compression, content, new FileOperations());
    }

    /**
     * Saves using the supplied filesystem operations. Retains the backup on failure
     * and deletes it after successful replacement.
     *
     * @param target the destination file
     * @param compression the output compression
     * @param content the serializer
     * @param files the filesystem operations
     * @throws NBTSaveCleanupException if saving succeeded but backup removal failed
     * @throws IOException if saving fails
     */
    static void write(Path target, Compression_Types compression, ContentWriter content,
                      FileOperations files) throws IOException
    {
        Path destination = target.toAbsolutePath();
        Path backup = destination.resolveSibling(destination.getFileName() + ".bak");
        boolean exists = regularFileExists(destination);
        if (exists) regularFileExists(backup);

        try (NBTFileSave save = new NBTFileSave(files))
        {
            Path output = save.stage(destination.getParent());
            if (exists)
            {
                Path backupOutput = save.stage(destination.getParent());
                files.copy(destination, backupOutput);
                save.replace(backupOutput, backup);
            }
            try (DataOutputStream stream = files.open(output, compression))
            {
                content.write(stream);
            }
            if (exists)
            {
                files.copyPermissions(destination, output);
            }
            save.replace(output, destination);
            if (exists)
            {
                try
                {
                    files.delete(backup);
                } catch (IOException e)
                {
                    throw new NBTSaveCleanupException(backup, e);
                }
            }
        }
    }

    /**
     * Checks whether a regular file exists without following symbolic links.
     *
     * @param path the destination or backup
     * @return whether a regular file exists
     * @throws IOException if the path is not a regular file or cannot be inspected
     */
    private static boolean regularFileExists(Path path) throws IOException
    {
        try
        {
            BasicFileAttributes attributes = Files.readAttributes(path, BasicFileAttributes.class,
                    LinkOption.NOFOLLOW_LINKS);
            if (!attributes.isRegularFile())
            {
                throw new IOException("Save destination and backup must be regular files: " + path);
            }
            return true;
        } catch (NoSuchFileException e)
        {
            return false;
        }
    }

    /**
     * Creates a temporary file and tracks it for cleanup.
     *
     * @param directory the destination directory
     * @return the temporary file
     * @throws IOException if creation fails
     */
    private Path stage(Path directory) throws IOException
    {
        Path path = files.createTemp(directory);
        pending.add(path);
        return path;
    }

    /**
     * Atomically replaces a path with a staged file.
     *
     * @param source the tracked temporary file
     * @param target the path to replace
     * @throws IOException if atomic replacement fails or is unsupported
     */
    private void replace(Path source, Path target) throws IOException
    {
        files.move(source, target);
        pending.remove(source);
    }

    /**
     * Deletes remaining temporary files, preserving cleanup errors.
     *
     * @throws IOException if cleanup fails
     */
    @Override
    public void close() throws IOException
    {
        IOException failure = null;
        for (Path path : pending)
        {
            try
            {
                files.delete(path);
            } catch (IOException e)
            {
                if (failure == null) {
                    failure = e;
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) throw failure;
    }

    /**
     * Writes serialized data to a temporary output stream.
     */
    @FunctionalInterface
    interface ContentWriter
    {
        /**
         * Writes all content without closing the stream.
         *
         * @param stream the staging output
         * @throws IOException if serialization fails
         */
        void write(DataOutputStream stream) throws IOException;
    }

    /**
     * Filesystem operations used by a save.
     */
    static class FileOperations
    {
        /**
         * Creates a uniquely named sibling file.
         */
        Path createTemp(Path directory) throws IOException
        {
            return Files.createTempFile(directory, ".jnbt-", ".tmp");
        }

        /**
         * Opens staged binary output with compression.
         */
        DataOutputStream open(Path path, Compression_Types compression) throws IOException
        {
            return NBTFileHandler.openOutput(path, compression);
        }

        /**
         * Copies original bytes into the staged backup.
         */
        void copy(Path source, Path target) throws IOException
        {
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        }

        /**
         * Retains the existing destination's POSIX permissions when supported.
         */
        void copyPermissions(Path source, Path target) throws IOException
        {
            PosixFileAttributeView view = Files.getFileAttributeView(source, PosixFileAttributeView.class);
            if (view != null)
            {
                Files.setPosixFilePermissions(target, view.readAttributes().permissions());
            }
        }

        /**
         * Requires atomic replacement on the destination filesystem.
         */
        void move(Path source, Path target) throws IOException
        {
            Files.move(source, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        }

        /**
         * Deletes an abandoned staging file.
         */
        void delete(Path path) throws IOException
        {
            Files.deleteIfExists(path);
        }
    }
}
