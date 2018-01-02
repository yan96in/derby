package org.apache.derby.agg;

import java.io.Serializable;

/**
 * Behavior of a user-defined Derby aggregator. Aggregates values
 * of type V and returns a result of type R. In addition to the methods
 * in the interface, implementing classes must have a 0-arg public
 * constructor.
 *  用户定义的derby聚合器行为.聚集V类型的值并返回R类型的结果.
 */
public interface Aggregator<V,R,A extends Aggregator<V,R,A>>    extends Serializable
{
    /** Initialize the Aggregator */
    public void init();

    /** Accumulate the next scalar value */
    public  void    accumulate( V value );

    /**
     * For merging another partial result into this Aggregator.
     * This lets the SQL interpreter divide the incoming rows into
     * subsets, aggregating each subset in isolation, and then merging
     * the partial results together. This method can be called when
     * performing a grouped aggregation with a large number of groups.
     * While processing such a query, Derby may write intermediate grouped
     * results to disk. The intermediate results may be retrieved and merged
     * with later results if Derby encounters later rows which belong to groups
     * whose intermediate results have been written to disk. This situation can
     * occur with a query like the following:
     * <pre>
     * select a, mode( b ) from mode_inputs group by a order by a
     * </pre>
     */
    public  void    merge( A otherAggregator );

    /** Return the result scalar value */
    public  R   terminate();
}

