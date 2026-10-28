package de.pauleff.jnbt.internal;

import de.pauleff.jnbt.api.NBTLimits;

import java.util.Objects;

/**
 * Internal per-operation accounting shared by the binary and SNBT implementations.
 *
 * @since 2.0
 */
public final class NBTBudget
{
    private final NBTLimits limits;
    private long allocations;
    private long tags;
    private long bytes;

    /**
     * Creates an empty budget for the supplied limits.
     *
     * @param limits the limits for this operation
     * @throws NullPointerException if limits is null
     */
    public NBTBudget(NBTLimits limits)
    {
        this.limits = Objects.requireNonNull(limits, "limits");
    }

    /**
     * Rejects a container deeper than the configured limit.
     *
     * @param depth the number of containers on the current path
     * @throws IllegalArgumentException if the depth limit is exceeded
     */
    public void depth(int depth)
    {
        if (depth > limits.maxDepth())
        {
            throw new IllegalArgumentException("NBT container depth limit exceeded: " + limits.maxDepth());
        }
    }

    /**
     * Counts a visited value and reserves estimated tag storage.
     *
     * @throws IllegalArgumentException if the value count or allocation limit is exceeded
     */
    public void tag()
    {
        requireTags(1);
        tags++;
        allocate(64);
    }

    /**
     * Checks that declared list elements fit the remaining value budget before parsing them.
     *
     * @param count the declared number of child values
     * @throws IllegalArgumentException if count is negative or exceeds the remaining value budget
     */
    public void requireTags(long count)
    {
        if (count < 0 || count > limits.maxTags() - tags)
        {
            throw new IllegalArgumentException("NBT tag count limit exceeded: " + limits.maxTags());
        }
    }

    /**
     * Validates a collection length before allocation or iteration.
     *
     * @param length the declared or actual element count
     * @throws IllegalArgumentException if length is negative or exceeds the collection limit
     */
    public void length(int length)
    {
        if (length < 0 || length > limits.maxCollectionLength())
        {
            throw new IllegalArgumentException("Invalid NBT collection length: " + length + " (limit " + limits.maxCollectionLength() + ")");
        }
    }

    /**
     * Reserves estimated storage without overflowing the cumulative counter.
     *
     * @param count the estimated allocation bytes to reserve
     * @throws IllegalArgumentException if count is negative or exceeds the remaining allocation budget
     */
    public void allocate(long count)
    {
        if (count < 0 || count > limits.maxAllocationBytes() - allocations)
        {
            throw new IllegalArgumentException("NBT allocation limit exceeded: " + limits.maxAllocationBytes());
        }
        allocations += count;
    }

    /**
     * Counts uncompressed binary output bytes before opening the destination.
     *
     * @param count the encoded bytes to reserve
     * @throws IllegalArgumentException if count is negative or exceeds the remaining binary byte budget
     */
    public void bytes(long count)
    {
        if (count < 0 || count > limits.maxBytes() - bytes)
        {
            throw new IllegalArgumentException("NBT byte limit exceeded: " + limits.maxBytes());
        }
        bytes += count;
    }
}
