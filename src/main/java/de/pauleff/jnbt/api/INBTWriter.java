package de.pauleff.jnbt.api;

import java.io.IOException;

/**
 * Writes binary NBT with the selected compression. Writers take ownership of
 * supplied streams after successful construction and support one write operation.
 *
 * @since 1.3
 */
public interface INBTWriter extends AutoCloseable
{
    /**
     * Writes the root compound and closes the writer, including on validation failure.
     *
     * @param root the root compound
     * @throws NBTSaveCleanupException   if saving succeeded but backup deletion failed
     * @throws IOException              if the writer is closed or validation, writing, or cleanup fails
     * @throws IllegalArgumentException if root is null
     */
    void write(ICompoundTag root) throws IOException;

    /**
     * Closes the writer and its output stream.
     *
     * @throws IOException if closing fails
     */
    @Override
    void close() throws IOException;
}
