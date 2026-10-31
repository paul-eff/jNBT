package de.pauleff.jnbt.formats.binary;

import de.pauleff.jnbt.api.NBTFileFactory;
import de.pauleff.jnbt.api.NBTSaveCleanupException;
import de.pauleff.jnbt.core.Tag_Compound;
import de.pauleff.jnbt.core.Tag_Int;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.Arguments;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class AtomicFileSaveTest
{
    @TempDir
    Path directory;

    private static final byte[] OLD_BACKUP = {9, 8, 7};

    private Path destination()
    {
        return directory.resolve("save.nbt");
    }

    private Path backup()
    {
        return directory.resolve("save.nbt.bak");
    }

    // Independent binary fixture, never produced by jNBT's writer.
    private static byte[] fixture(int value) throws IOException
    {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream output = new DataOutputStream(bytes))
        {
            output.writeByte(10);
            output.writeUTF("");
            output.writeByte(3);
            output.writeUTF("value");
            output.writeInt(value);
            output.writeByte(0);
        }
        return bytes.toByteArray();
    }

    private static byte[] compressed(byte[] bytes, Compression_Types compression) throws IOException
    {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        try (OutputStream output = switch (compression)
        {
            case NONE -> result;
            case GZIP -> new GZIPOutputStream(result);
            case ZLIB -> new DeflaterOutputStream(result);
            default -> throw new IllegalArgumentException();
        })
        {
            output.write(bytes);
        }
        return result.toByteArray();
    }

    private static byte[] decoded(byte[] bytes, Compression_Types compression) throws IOException
    {
        ByteArrayInputStream input = new ByteArrayInputStream(bytes);
        try (InputStream decoded = switch (compression)
        {
            case NONE -> input;
            case GZIP -> new GZIPInputStream(input);
            case ZLIB -> new InflaterInputStream(input);
            default -> throw new IllegalArgumentException();
        })
        {
            // Read to EOF to verify compression trailers as well as the NBT payload.
            return decoded.readAllBytes();
        }
    }

    private static Tag_Compound root()
    {
        Tag_Compound root = new Tag_Compound("");
        root.addInt("value", 42);
        return root;
    }

    private void assertFiles(String... names) throws IOException
    {
        try (Stream<Path> paths = Files.list(directory))
        {
            assertEquals(Set.of(names), paths.map(p -> p.getFileName().toString()).collect(Collectors.toSet()));
        }
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void savesNewFileWithCompleteCompressionAndNoBackup(Compression_Types compression) throws IOException
    {
        try (NBTWriter writer = new NBTWriter(destination().toFile(), compression))
        {
            assertFiles();
            writer.write(root());
            assertArrayEquals(fixture(42), decoded(Files.readAllBytes(destination()), compression));
        }
        assertFiles("save.nbt");
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void successfulReplacementRemovesBackupForValidAndMalformedOriginals(Compression_Types compression) throws IOException
    {
        byte[] original = compressed(fixture(-5), compression);
        for (byte[] source : new byte[][]{original, {1, 2, 3, 4, 5}})
        {
            Files.write(destination(), source);
            Files.write(backup(), OLD_BACKUP);
            NBTFileFactory.writeNBTFile(destination().toFile(), root(), compression);
            assertFalse(Files.exists(backup()));
            assertArrayEquals(fixture(42), decoded(Files.readAllBytes(destination()), compression));
            assertFiles("save.nbt");
        }
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void factoryPreservesDetectedCompression(Compression_Types compression) throws IOException
    {
        byte[] original = compressed(fixture(-5), compression);
        Files.write(destination(), original);
        NBTFileFactory.writeNBTFile(destination().toFile(), root());
        assertFalse(Files.exists(backup()));
        assertArrayEquals(fixture(42), decoded(Files.readAllBytes(destination()), compression));
    }

    @Test
    void factoryCreatesMissingFileWithNoCompression() throws IOException
    {
        NBTFileFactory.writeNBTFile(destination().toFile(), root());
        assertArrayEquals(fixture(42), Files.readAllBytes(destination()));
        assertFiles("save.nbt");
    }

    @Test
    void factoryDoesNotRetryFileNotFoundFailureDuringSerialization() throws IOException
    {
        byte[] original = compressed(fixture(-5), Compression_Types.GZIP);
        Files.write(destination(), original);
        FileNotFoundException failure = new FileNotFoundException("payload source missing");
        int[] attempts = {0};
        Tag_Compound root = root();
        root.addTag(new Tag_Int("external", 0)
        {
            @Override
            public Integer getData()
            {
                attempts[0]++;
                return throwUnchecked(failure);
            }
        });
        assertSame(failure, assertThrows(FileNotFoundException.class,
                () -> NBTFileFactory.writeNBTFile(destination().toFile(), root)));
        assertEquals(1, attempts[0]);
        assertArrayEquals(original, Files.readAllBytes(destination()));
        assertArrayEquals(original, Files.readAllBytes(backup()));
        assertFiles("save.nbt", "save.nbt.bak");
    }

    // Inject a checked I/O failure from a custom tag without changing its getter signature.
    @SuppressWarnings("unchecked")
    private static <E extends Throwable, T> T throwUnchecked(Throwable failure) throws E
    {
        throw (E) failure;
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void serializationFailureAfterValidationPreservesFilesAcrossRepeatedAttempts(Compression_Types compression)
            throws IOException
    {
        byte[] original = compressed(fixture(-5), compression);
        Files.write(destination(), original);
        Files.write(backup(), OLD_BACKUP);
        IllegalStateException failure = new IllegalStateException("payload failed during serialization");
        Tag_Compound root = root();
        root.addTag(new Tag_Int("bad", 1)
        {
            @Override
            public Integer getData()
            {
                throw failure;
            }
        });
        for (int attempt = 0; attempt < 2; attempt++)
        {
            try (NBTWriter writer = new NBTWriter(destination().toFile(), compression))
            {
                IOException error = assertThrows(IOException.class, () -> writer.write(root));
                assertSame(failure, error.getCause());
                assertThrows(IOException.class, () -> writer.write(root()));
            }
            assertArrayEquals(original, Files.readAllBytes(destination()));
            assertArrayEquals(original, Files.readAllBytes(backup()));
            assertFiles("save.nbt", "save.nbt.bak");
        }
        Path absent = directory.resolve("new.nbt");
        assertThrows(IOException.class, () -> new NBTWriter(absent.toFile(), compression).write(root));
        assertFalse(Files.exists(absent));
        assertFiles("save.nbt", "save.nbt.bak");
        NBTFileFactory.writeNBTFile(destination().toFile(), root(), compression);
        assertArrayEquals(fixture(42), decoded(Files.readAllBytes(destination()), compression));
        assertFiles("save.nbt");
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void backupExistsThroughoutWritingAndReplacementThenIsRemoved(Compression_Types compression) throws IOException
    {
        byte[] original = fixture(-5);
        Files.write(destination(), original);
        Files.write(backup(), OLD_BACKUP);
        NBTFileSave.FileOperations operations = new NBTFileSave.FileOperations()
        {
            @Override
            DataOutputStream open(Path path, Compression_Types type) throws IOException
            {
                DataOutputStream output = super.open(path, type);
                return new DataOutputStream(new FilterOutputStream(output)
                {
                    @Override
                    public void close() throws IOException
                    {
                        assertArrayEquals(original, Files.readAllBytes(destination()));
                        assertArrayEquals(original, Files.readAllBytes(backup()));
                        super.close();
                    }
                });
            }

            @Override
            void move(Path source, Path target) throws IOException
            {
                if (target.equals(destination()))
                {
                    assertArrayEquals(original, Files.readAllBytes(backup()));
                }
                super.move(source, target);
            }
        };
        NBTFileSave.write(destination(), compression, output ->
        {
            output.write(fixture(42));
            assertArrayEquals(original, Files.readAllBytes(destination()));
            assertArrayEquals(original, Files.readAllBytes(backup()));
        }, operations);
        assertArrayEquals(fixture(42), decoded(Files.readAllBytes(destination()), compression));
        assertFalse(Files.exists(backup()));
        assertFiles("save.nbt");
    }

    private enum Failure
    {
        CREATE, OPEN, WRITE, CLOSE, PERMISSIONS, BACKUP_CREATE, COPY,
        BACKUP_MOVE, DESTINATION_MOVE, ATOMIC_BACKUP_UNSUPPORTED, ATOMIC_DESTINATION_UNSUPPORTED
    }

    private static Stream<Arguments> saveFailures()
    {
        return Stream.of(Compression_Types.NONE, Compression_Types.GZIP, Compression_Types.ZLIB)
                .flatMap(compression -> Stream.of(Failure.values()).map(failure -> Arguments.of(compression, failure)));
    }

    @ParameterizedTest
    @MethodSource("saveFailures")
    void failedSavePreservesDestinationAndCleansStagingFiles(Compression_Types compression, Failure stage)
            throws IOException
    {
        byte[] original = compressed(fixture(-5), compression);
        Files.write(destination(), original);
        Files.write(backup(), OLD_BACKUP);
        IOException failure = stage == Failure.ATOMIC_BACKUP_UNSUPPORTED || stage == Failure.ATOMIC_DESTINATION_UNSUPPORTED
                ? new AtomicMoveNotSupportedException("source", "target", "injected") : new IOException("injected " + stage);
        NBTFileSave.FileOperations operations = new NBTFileSave.FileOperations()
        {
            private int creates;

            @Override
            Path createTemp(Path path) throws IOException
            {
                creates++;
                if (stage == Failure.CREATE || (stage == Failure.BACKUP_CREATE && creates % 2 == 0)) throw failure;
                return super.createTemp(path);
            }

            @Override
            DataOutputStream open(Path path, Compression_Types type) throws IOException
            {
                if (stage == Failure.OPEN) throw failure;
                DataOutputStream output = super.open(path, type);
                return new DataOutputStream(new FilterOutputStream(output)
                {
                    @Override
                    public void write(byte[] bytes, int offset, int length) throws IOException
                    {
                        if (stage == Failure.WRITE)
                        {
                            out.write(bytes, offset, Math.min(2, length));
                            throw failure;
                        }
                        out.write(bytes, offset, length);
                    }

                    @Override
                    public void close() throws IOException
                    {
                        super.close();
                        if (stage == Failure.CLOSE) throw failure;
                    }
                });
            }

            @Override
            void copyPermissions(Path source, Path target) throws IOException
            {
                if (stage == Failure.PERMISSIONS) throw failure;
                super.copyPermissions(source, target);
            }

            @Override
            void copy(Path source, Path target) throws IOException
            {
                if (stage == Failure.COPY)
                {
                    Files.write(target, new byte[]{1});
                    throw failure;
                }
                super.copy(source, target);
            }

            @Override
            void move(Path source, Path target) throws IOException
            {
                boolean isBackup = target.equals(backup());
                if ((isBackup && (stage == Failure.BACKUP_MOVE || stage == Failure.ATOMIC_BACKUP_UNSUPPORTED))
                        || (!isBackup && (stage == Failure.DESTINATION_MOVE || stage == Failure.ATOMIC_DESTINATION_UNSUPPORTED)))
                {
                    throw failure;
                }
                super.move(source, target);
            }
        };
        for (int attempt = 0; attempt < 2; attempt++)
        {
            assertSame(failure, assertThrows(IOException.class,
                    () -> NBTFileSave.write(destination(), compression, output -> output.write(fixture(42)), operations)));
            assertArrayEquals(original, Files.readAllBytes(destination()));
            boolean backupCommitted = switch (stage)
            {
                case OPEN, WRITE, CLOSE, PERMISSIONS, DESTINATION_MOVE, ATOMIC_DESTINATION_UNSUPPORTED -> true;
                default -> false;
            };
            assertArrayEquals(backupCommitted ? original : OLD_BACKUP, Files.readAllBytes(backup()));
            assertFiles("save.nbt", "save.nbt.bak");
        }
    }

    @Test
    void closeAndCleanupFailuresAreSuppressedUnderPrimaryFailure() throws IOException
    {
        IOException serialization = new IOException("serialization");
        IOException closing = new IOException("closing");
        IOException cleanup = new IOException("cleanup");
        NBTFileSave.FileOperations operations = new NBTFileSave.FileOperations()
        {
            @Override
            DataOutputStream open(Path path, Compression_Types compression) throws IOException
            {
                return new DataOutputStream(new FilterOutputStream(super.open(path, compression))
                {
                    @Override
                    public void close() throws IOException
                    {
                        super.close();
                        throw closing;
                    }
                });
            }

            @Override
            void delete(Path path) throws IOException
            {
                throw cleanup;
            }
        };
        IOException error = assertThrows(IOException.class, () -> NBTFileSave.write(destination(), Compression_Types.NONE,
                output -> { throw serialization; }, operations));
        assertSame(serialization, error);
        assertArrayEquals(new Throwable[]{closing, cleanup}, error.getSuppressed());
        assertFalse(Files.exists(destination()));
        assertFalse(Files.exists(backup()));
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"GZIP", "ZLIB"})
    void compressionTrailerWriteFailurePreventsCommit(Compression_Types compression) throws IOException
    {
        byte[] original = fixture(-5);
        Files.write(destination(), original);
        Files.write(backup(), OLD_BACKUP);
        IOException failure = new IOException("trailer write failed");
        boolean[] finishing = {false};
        boolean[] rawClosed = {false};
        NBTFileSave.FileOperations operations = new NBTFileSave.FileOperations()
        {
            @Override
            DataOutputStream open(Path path, Compression_Types type) throws IOException
            {
                OutputStream raw = new FilterOutputStream(Files.newOutputStream(path))
                {
                    @Override
                    public void write(byte[] bytes, int offset, int length) throws IOException
                    {
                        if (finishing[0]) throw failure;
                        out.write(bytes, offset, length);
                    }

                    @Override
                    public void close() throws IOException
                    {
                        rawClosed[0] = true;
                        super.close();
                    }
                };
                OutputStream compressed = type == Compression_Types.GZIP
                        ? new GZIPOutputStream(raw) : new DeflaterOutputStream(raw);
                return new DataOutputStream(new FilterOutputStream(compressed)
                {
                    @Override
                    public void close() throws IOException
                    {
                        finishing[0] = true;
                        super.close();
                    }
                });
            }
        };
        assertSame(failure, assertThrows(IOException.class,
                () -> NBTFileSave.write(destination(), compression, output -> output.write(fixture(42)), operations)));
        assertTrue(rawClosed[0]);
        assertArrayEquals(original, Files.readAllBytes(destination()));
        assertArrayEquals(original, Files.readAllBytes(backup()));
        assertFiles("save.nbt", "save.nbt.bak");
    }

    @Test
    void unsupportedAtomicMoveDoesNotCreateNewDestination() throws IOException
    {
        AtomicMoveNotSupportedException failure = new AtomicMoveNotSupportedException("source", "target", "injected");
        NBTFileSave.FileOperations operations = new NBTFileSave.FileOperations()
        {
            @Override
            void move(Path source, Path target) throws IOException
            {
                throw failure;
            }
        };
        assertSame(failure, assertThrows(AtomicMoveNotSupportedException.class,
                () -> NBTFileSave.write(destination(), Compression_Types.NONE, output -> output.write(fixture(42)), operations)));
        assertFiles();
    }

    @ParameterizedTest
    @EnumSource(value = Compression_Types.class, names = {"NONE", "GZIP", "ZLIB"})
    void backupRemovalFailureReportsSavedFileAndRetainsBackup(Compression_Types compression) throws IOException
    {
        byte[] original = compressed(fixture(-5), compression);
        Files.write(destination(), original);
        IOException failure = new IOException("backup deletion failed");
        NBTFileSave.FileOperations operations = new NBTFileSave.FileOperations()
        {
            @Override
            void delete(Path path) throws IOException
            {
                if (path.equals(backup())) throw failure;
                super.delete(path);
            }
        };
        NBTSaveCleanupException error = assertThrowsExactly(NBTSaveCleanupException.class,
                () -> NBTFileSave.write(destination(), compression, output -> output.write(fixture(42)), operations));
        assertSame(failure, error.getCause());
        assertEquals("NBT file saved; could not remove backup: " + backup(), error.getMessage());
        assertArrayEquals(fixture(42), decoded(Files.readAllBytes(destination()), compression));
        assertArrayEquals(original, Files.readAllBytes(backup()));
        assertFiles("save.nbt", "save.nbt.bak");
    }

    @Test
    void callerStreamPreservesPrimaryFailureAndClosesOnlyOnce() throws IOException
    {
        IOException writing = new IOException("writing");
        IOException closing = new IOException("closing");
        int[] closes = {0};
        OutputStream failing = new OutputStream()
        {
            @Override
            public void write(int value) throws IOException
            {
                throw writing;
            }

            @Override
            public void close() throws IOException
            {
                closes[0]++;
                throw closing;
            }
        };
        try (NBTWriter writer = new NBTWriter(new DataOutputStream(failing)))
        {
            assertSame(writing, assertThrows(IOException.class, () -> writer.write(root())));
            assertArrayEquals(new Throwable[]{closing}, writing.getSuppressed());
        }
        assertEquals(1, closes[0]);
    }

    @Test
    void newFileFailureKeepsExistingBackup() throws IOException
    {
        Files.write(backup(), OLD_BACKUP);
        IOException failure = new IOException("serialization");
        assertSame(failure, assertThrows(IOException.class,
                () -> NBTFileSave.write(destination(), Compression_Types.GZIP, output ->
                {
                    output.write(fixture(42));
                    throw failure;
                })));
        assertArrayEquals(OLD_BACKUP, Files.readAllBytes(backup()));
        assertFiles("save.nbt.bak");
        new NBTWriter(destination().toFile(), Compression_Types.NONE).write(root());
        assertArrayEquals(OLD_BACKUP, Files.readAllBytes(backup()));
        assertFiles("save.nbt", "save.nbt.bak");
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void directoryDestinationOrBackupIsRejected(boolean backupDirectory) throws IOException
    {
        if (backupDirectory)
        {
            Files.write(destination(), fixture(-5));
            Files.createDirectory(backup());
        } else
        {
            Files.createDirectory(destination());
        }
        assertThrows(IOException.class, () -> new NBTWriter(destination().toFile(), Compression_Types.NONE).write(root()));
        if (backupDirectory)
        {
            assertArrayEquals(fixture(-5), Files.readAllBytes(destination()));
            assertTrue(Files.isDirectory(backup()));
            assertFiles("save.nbt", "save.nbt.bak");
        } else
        {
            assertTrue(Files.isDirectory(destination()));
            assertFiles("save.nbt");
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void symbolicLinksIncludingDanglingLinksAreRejected(int scenario) throws IOException
    {
        Path referent = directory.resolve("referent");
        boolean dangling = scenario >= 2;
        boolean backupLink = scenario % 2 == 1;
        if (!dangling) Files.write(referent, fixture(-5));
        if (backupLink) Files.write(destination(), fixture(-5));
        Path link = backupLink ? backup() : destination();
        Files.createSymbolicLink(link, referent);
        assertThrows(IOException.class, () -> new NBTWriter(destination().toFile(), Compression_Types.NONE).write(root()));
        assertTrue(Files.isSymbolicLink(link));
        if (dangling) assertFalse(Files.exists(referent));
        else assertArrayEquals(fixture(-5), Files.readAllBytes(referent));
        if (backupLink) assertArrayEquals(fixture(-5), Files.readAllBytes(destination()));
    }

    @Test
    void retainsPosixDestinationPermissions() throws IOException
    {
        Files.write(destination(), fixture(-5));
        assumeTrue(Files.getFileAttributeView(destination(), PosixFileAttributeView.class) != null);
        var permissions = PosixFilePermissions.fromString("rw-r-----");
        Files.setPosixFilePermissions(destination(), permissions);
        new NBTWriter(destination().toFile(), Compression_Types.NONE).write(root());
        assertEquals(permissions, Files.getPosixFilePermissions(destination()));
    }

    @Test
    void unsupportedCompressionPreservesDestinationAndBackup() throws IOException
    {
        Files.write(destination(), fixture(-5));
        Files.write(backup(), OLD_BACKUP);
        assertThrows(IOException.class, () -> new NBTWriter(destination().toFile(), Compression_Types.LZ4).write(root()));
        assertArrayEquals(fixture(-5), Files.readAllBytes(destination()));
        assertArrayEquals(fixture(-5), Files.readAllBytes(backup()));
        assertFiles("save.nbt", "save.nbt.bak");
    }
}
