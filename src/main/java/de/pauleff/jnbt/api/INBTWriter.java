package de.pauleff.jnbt.api;

import java.io.IOException;

/**
 * Writes binary NBT with the selected compression.
 *
 * @since 1.3
 */
public interface INBTWriter extends AutoCloseable
{
    /**
     * Writes the root compound.
     *
     * @param root the root compound
     * @throws NBTSaveCleanupException   if saving succeeded but backup deletion failed
     * @throws IOException              if validation or writing fails
     * @throws IllegalArgumentException if root is null
     */
    void write(ICompoundTag root) throws IOException;

    /**
     * Closes the writer.
     *
     * @throws IOException if closing fails
     */
    @Override
    void close() throws IOException;
}
