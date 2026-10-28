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
     * Writes a compound as the file root.
     *
     * @param root the {@link ICompoundTag} to write as the file root
     * @throws IOException              if an error occurs during file writing
     * @throws IllegalArgumentException if root is null
     */
    void write(ICompoundTag root) throws IOException;

    /**
     * Closes the writer.
     *
     * @throws IOException if an error occurs during resource cleanup
     */
    @Override
    void close() throws IOException;
}
