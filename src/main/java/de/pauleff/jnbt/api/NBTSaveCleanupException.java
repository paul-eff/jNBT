package de.pauleff.jnbt.api;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Thrown when saving succeeded but backup deletion failed. The new file is retained.
 */
public class NBTSaveCleanupException extends IOException
{
    /**
     * Creates an exception for a backup deletion failure.
     *
     * @param backup the backup that could not be removed
     * @param cause the underlying deletion failure
     */
    public NBTSaveCleanupException(Path backup, IOException cause)
    {
        super("NBT file saved; could not remove backup: " + backup, cause);
    }
}
