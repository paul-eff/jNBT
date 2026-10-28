package de.pauleff.jnbt.api;

/**
 * Limits for one binary or SNBT operation. Existing constructors use {@link #DEFAULT}.
 * Container depth counts lists and compounds, including the root as level one. Arrays and scalar
 * values do not add a container level. Depth can be lowered, but cannot exceed 512.
 * Allocation accounting estimates operation storage; it is not an exact JVM heap measurement.
 * Increase byte, allocation, or count limits only for trusted data and an appropriately sized heap.
 *
 * @param maxDepth            maximum number of nested containers, from 1 to 512
 * @param maxBytes            maximum uncompressed binary bytes, including headers
 * @param maxAllocationBytes  maximum estimated operation allocation bytes
 * @param maxTags             maximum visited values, including temporary SNBT array elements
 * @param maxCollectionLength maximum elements in any list, compound, or array
 * @param maxTextLength       maximum SNBT input or output length in UTF-16 code units
 * @since 2.0
 */
public record NBTLimits(int maxDepth, long maxBytes, long maxAllocationBytes, long maxTags,
                        int maxCollectionLength, int maxTextLength)
{
    /**
     * Default limits: 512 container levels, 64 MiB binary/allocation budgets, 1,000,000 visited values
     * and elements per collection, and 16 Mi UTF-16 text units.
     */
    public static final NBTLimits DEFAULT = new NBTLimits(512, 64L * 1024 * 1024,
            64L * 1024 * 1024, 1_000_000, 1_000_000, 16 * 1024 * 1024);

    /**
     * Creates validated limits. Zero collection length permits only empty collections.
     *
     * @param maxDepth            maximum number of nested containers, from 1 to 512
     * @param maxBytes            maximum uncompressed binary bytes, including headers
     * @param maxAllocationBytes  maximum estimated operation allocation bytes
     * @param maxTags             maximum visited values, including temporary SNBT array elements
     * @param maxCollectionLength maximum elements in any list, compound, or array
     * @param maxTextLength       maximum SNBT input or output length in UTF-16 code units
     * @throws IllegalArgumentException if depth is outside 1–512, a budget is not positive,
     *                                  or collection length is negative
     */
    public NBTLimits
    {
        if (maxDepth < 1 || maxDepth > 512 || maxBytes < 1 || maxAllocationBytes < 1 || maxTags < 1
                || maxCollectionLength < 0 || maxTextLength < 1)
        {
            throw new IllegalArgumentException("Invalid NBT limits: depth must be 1–512 and budgets must be positive");
        }
    }
}
