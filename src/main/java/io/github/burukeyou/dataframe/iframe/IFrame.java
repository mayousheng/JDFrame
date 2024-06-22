package io.github.burukeyou.dataframe.iframe;

import io.github.burukeyou.dataframe.iframe.function.ReplenishFunction;
import io.github.burukeyou.dataframe.iframe.function.SetFunction;
import io.github.burukeyou.dataframe.iframe.item.FI2;
import io.github.burukeyou.dataframe.iframe.item.FI3;
import io.github.burukeyou.dataframe.iframe.item.FI4;
import io.github.burukeyou.dataframe.iframe.support.*;
import io.github.burukeyou.dataframe.iframe.window.Sorter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.*;
import java.util.stream.Stream;

/**
 * A Simple DataFrame Stream API Interface define
 *
 * @author caizhihao
 */
public interface IFrame<T> extends Iterable<T> {

    /**
     * Convert to list
     *
     * @return the list
     */
    List<T> toLists();

    /**
     * get stream
     *
     * @return the stream
     */
    Stream<T> stream();

    /**
     * Convert to other IFrame
     */
    <R> IFrame<R> from(Stream<R> data);

    /**
     * Performs the given action for each element of the Iterable until all elements have been processed or the action throws an exception.
     */
    IFrame<T> forEachDo(Consumer<? super T> action);


    /**
     * forEachDoWithIndex
     */
    default IFrame<T> forEachDoWithIndex(BiConsumer<? super T, Integer> action) {
        AtomicInteger integer = new AtomicInteger();
        return this.forEachDo(t -> action.accept(t, integer.getAndIncrement()));
    }

    /**
     * ===========================   Frame Setting =====================================
     **/
    /**
     * Set default decimal places
     */
    IFrame<T> defaultScale(int scale);

    /**
     * Set default decimal places
     */
    IFrame<T> defaultScale(int scale, RoundingMode roundingMode);

    /**
     * ===========================   Frame Info =====================================
     **/
    /**
     * print the 10 row to the console
     */
    void show();

    /**
     * print the n row to the console
     */
    void show(int n);

    /**
     * Get column headers
     */
    List<String> columns();

    /**
     * Get a column value
     */
    <R> List<R> col(Function<T, R> function);

    /**
     * Get paginated data
     *
     * @param page     The current page number is considered as the first page, regardless of whether it is passed as 0 or 1
     * @param pageSize page size
     */
    List<T> page(int page, int pageSize);

    /**
     * ===========================   Frame Join  =====================================
     **/
    /**
     * add element to Frame
     *
     * @param t element
     */
    IFrame<T> append(T t);

    /**
     * add other Frame to this
     *
     * @param other other Frame
     */
    IFrame<T> union(IFrame<T> other);

    /**
     * inner join Frame
     *
     * @param other other frame
     * @param on    connection conditions
     * @param join  Connection logic
     * @param <R>   new Frame type
     * @param <K>   other Frame type
     */
    <R, K> IFrame<R> join(IFrame<K> other, JoinOn<T, K> on, Join<T, K, R> join);

    /**
     * inner join Frame
     * such as {@link IFrame#join(IFrame, JoinOn, Join)}, but the default Join is {@link DefaultJoin},
     * it will automatically map to a new Frame based on the same name
     *
     * @param other other frame
     * @param on    connection conditions
     * @param <R>   new Frame type
     * @param <K>   other Frame type
     */
    <R, K> IFrame<R> join(IFrame<K> other, JoinOn<T, K> on);

    /**
     * left join Frame
     * if connection conditions false, The callback value K for Join will be null， always keep T
     *
     * @param other other frame
     * @param on    connection conditions
     * @param join  Connection logic
     * @param <R>   new Frame type
     * @param <K>   other Frame type
     */
    <R, K> IFrame<R> leftJoin(IFrame<K> other, JoinOn<T, K> on, Join<T, K, R> join);

    /**
     * left join Frame
     * such as {@link IFrame#leftJoin(IFrame, JoinOn, Join)}, but the default Join is {@link DefaultJoin},
     *
     * @param other other frame
     * @param on    connection conditions
     * @param <R>   new Frame type
     * @param <K>   other Frame type
     */
    <R, K> IFrame<R> leftJoin(IFrame<K> other, JoinOn<T, K> on);

    /**
     * right join Frame
     * if connection conditions false, The callback value T for Join will be null， always keep K
     *
     * @param other other frame
     * @param on    connection conditions
     * @param join  Connection logic
     * @param <R>   new Frame type
     * @param <K>   other Frame type
     */
    <R, K> IFrame<R> rightJoin(IFrame<K> other, JoinOn<T, K> on, Join<T, K, R> join);

    /**
     * right join Frame
     * such as {@link IFrame#rightJoin(IFrame, JoinOn, Join)}, but the default Join is {@link DefaultJoin},
     *
     * @param other other frame
     * @param on    connection conditions
     * @param <R>   new Frame type
     * @param <K>   other Frame type
     */
    <R, K> IFrame<R> rightJoin(IFrame<K> other, JoinOn<T, K> on);

    /**
     * ===========================   Frame Convert  =====================================
     */
    /**
     * convert to the new Frame
     *
     * @param map convert operation
     * @param <R> the new Frame type
     * @return the new Frame
     */
    <R> IFrame<R> map(Function<T, R> map);

    /**
     * @param map
     * @return {@link IFrame }<{@link R }>
     */
    default <R> IFrame<R> mapWithIndex(BiFunction<T, Integer, R> map) {
        AtomicInteger integer = new AtomicInteger();
        return from(stream().map(t -> map.apply(t, integer.getAndIncrement())));
    }

    /**
     * Percentage convert
     * you can convert the value of a certain field to a percentage,
     * Then assign a value to a certain column through SetFunction
     *
     * @param get   need percentage convert field
     * @param set   field for storing percentage values
     * @param scale percentage retain decimal places
     * @param <R>   the percentage field type
     */
    <R extends Number> IFrame<T> mapPercent(Function<T, R> get, SetFunction<T, BigDecimal> set, int scale);

    /**
     * Percentage convert
     * such as {@link IFrame#mapPercent(Function, SetFunction, int)}, but default scale is 2
     *
     * @param get need percentage convert field
     * @param set field for storing percentage values
     */
    <R extends Number> IFrame<T> mapPercent(Function<T, R> get, SetFunction<T, BigDecimal> set);

    /**
     * partition
     * cut the matrix into multiple small matrices, with each matrix size n
     *
     * @param n size of each zone
     */
    IFrame<List<T>> partition(int n);

    /**
     * add sort number to the {@link FI2#c2} field
     * Default sequence number from 1 to frame.length
     */
    IFrame<FI2<T, Integer>> addRowNumberCol();

    /**
     * Sort by sorter first, then add ordinal columns
     *
     * @param sorter the sorter
     */
    IFrame<FI2<T, Integer>> addRowNumberCol(Sorter<T> sorter);

    /**
     * Add a numbered column to a specific column
     *
     * @param set specific column
     */
    IFrame<T> addRowNumberCol(SetFunction<T, Integer> set);

    /**
     * Add a numbered column to a specific column
     *
     * @param sorter the sorter
     * @param set    specific column
     */
    IFrame<T> addRowNumberCol(Sorter<T> sorter, SetFunction<T, Integer> set);

    /**
     * Add ranking columns by comparator
     * Ranking logic, the same value means the Ranking is the same. This is different from {@link #addRowNumberCol}
     *
     * @param sorter the ranking  sorter
     */
    IFrame<FI2<T, Integer>> addRankCol(Sorter<T> sorter);


    /**
     * Add ranking column to a certain column by Comparator
     *
     * @param sorter the ranking  comparator
     * @param set    certain column
     */
    IFrame<T> addRankCol(Sorter<T> sorter, SetFunction<T, Integer> set);


    /**
     * ===========================   Sort Frame  =====================================
     **/

    /**
     * Descending order
     *
     * @param comparator comparator
     */
    IFrame<T> sortDesc(Comparator<T> comparator);

    /**
     * Descending order by field
     *
     * @param function sort field
     * @param <R>      the  sort field type
     */
    <R extends Comparable<? super R>> IFrame<T> sortDesc(Function<T, R> function);

    /**
     * Ascending order
     *
     * @param comparator comparator
     */
    IFrame<T> sortAsc(Comparator<T> comparator);

    /**
     * Ascending order
     *
     * @param function sort field
     */
    <R extends Comparable<R>> IFrame<T> sortAsc(Function<T, R> function);


    /** ===========================   Cut Frame  ===================================== **/

    /**
     * Cut the top n element
     *
     * @param n the top n
     */
    IFrame<T> cutFirst(int n);

    /**
     * Cut the last n element
     *
     * @param n the last n
     */
    IFrame<T> cutLast(int n);

    /**
     * cut elements within the scope
     */
    IFrame<T> cut(Integer startIndex, Integer endIndex);

    /**
     * cut paginated data
     *
     * @param page     The current page number is considered as the first page, regardless of whether it is passed as 0 or 1
     * @param pageSize page size
     */
    IFrame<T> cutPage(int page, int pageSize);


    /**
     * Cut the top N rankings data
     * The same value is considered to have the same ranking
     *
     * @param sorter the ranking sorter
     * @param n      the top n
     */
    IFrame<T> cutFirstRank(Sorter<T> sorter, int n);


    /** ===========================   View Frame  ===================================== **/

    /**
     * Get the first element
     */
    T head();

    /**
     * Get the first n elements
     */
    List<T> head(int n);

    /**
     * Get the last element
     */
    T tail();

    /**
     * Get the last n elements
     */
    List<T> tail(int n);

    /**
     * Get elements within the scope.  [startIndex,endIndex]
     */
    List<T> subList(Integer startIndex, Integer endIndex);

    /** ===========================   Distinct Frame  ===================================== **/

    /**
     * distinct by  T value
     */
    IFrame<T> distinct();


    /**
     * distinct by field value
     *
     * @param function the field
     * @param <R>      field value type
     */
    <R extends Comparable<R>> IFrame<T> distinct(Function<T, R> function);


    /**
     * distinct by  comparator
     *
     * @param comparator the comparator
     */
    <R extends Comparable<R>> IFrame<T> distinct(Comparator<T> comparator);

    /**
     * Calculate the quantity after deduplication
     */
    <R extends Comparable<R>> long countDistinct(Function<T, R> function);

    /**
     * Calculate the quantity after deduplication
     */
    long countDistinct(Comparator<T> comparator);

    /**
     * ===========================   Where Frame  =====================================
     **/

    /**
     * filter by predicate
     *
     * @param predicate the predicate
     */
    IFrame<T> where(Predicate<? super T> predicate);

    /**
     * Filter field values that are null, If it is string compatible, null and '' situations
     *
     * @param function the filter field
     * @param <R>      the filter field type
     */
    <R> IFrame<T> whereNull(Function<T, R> function);

    /**
     * Filter field values that are not null,If it is string compatible, null and '' situations
     *
     * @param function the filter field
     * @param <R>      the filter field type
     */
    <R> IFrame<T> whereNotNull(Function<T, R> function);

    /**
     * Screening within the interval,front closed and back closed.  [start,end]
     * [start,end]
     *
     * @param function the filter field
     * @param start    start value
     * @param end      end value
     */
    <R extends Comparable<R>> IFrame<T> whereBetween(Function<T, R> function, R start, R end);

    /**
     * Screening within the interval , front open and back open (start,end)
     *
     * @param function the filter field
     * @param start    start value
     * @param end      end value
     */
    <R extends Comparable<R>> IFrame<T> whereBetweenN(Function<T, R> function, R start, R end);

    /**
     * Screening within the interval , front open and back close (start,end]
     *
     * @param function the filter field
     * @param start    start value
     * @param end      end value
     */
    <R extends Comparable<R>> IFrame<T> whereBetweenR(Function<T, R> function, R start, R end);

    /**
     * Screening within the interval , front close and back open  [start,end)
     *
     * @param function the filter field
     * @param start    start value
     * @param end      end value
     */
    <R extends Comparable<R>> IFrame<T> whereBetweenL(Function<T, R> function, R start, R end);


    /**
     * Out of range screening, (front closed and back closed) [start,end]
     *
     * @param function the filter field
     * @param start    start value
     * @param end      end value
     */
    <R extends Comparable<R>> IFrame<T> whereNotBetween(Function<T, R> function, R start, R end);

    /**
     * Out of range screening, (front open and back open)  (start,end)
     *
     * @param function the filter field
     * @param start    start value
     * @param end      end value
     */
    <R extends Comparable<R>> IFrame<T> whereNotBetweenN(Function<T, R> function, R start, R end);

    /**
     * The query value is within the specified range
     *
     * @param function the filter field
     * @param list     specified range
     */
    <R> IFrame<T> whereIn(Function<T, R> function, List<R> list);

    /**
     * The query value is outside the specified range
     *
     * @param function the filter field
     * @param list     specified range
     */
    <R> IFrame<T> whereNotIn(Function<T, R> function, List<R> list);

    /**
     * filter true by predicate
     */
    IFrame<T> whereTrue(Predicate<T> predicate);

    /**
     * filter not true by predicate
     */
    IFrame<T> whereNotTrue(Predicate<T> predicate);

    /**
     * Filter equals
     *
     * @param function the field
     * @param value    need value
     */
    <R> IFrame<T> whereEq(Function<T, R> function, R value);

    /**
     * Filter not equals
     *
     * @param function the field
     * @param value    not need value
     */
    <R> IFrame<T> whereNotEq(Function<T, R> function, R value);

    /**
     * Filter Greater than value
     *
     * @param function the field
     * @param value    not need value
     */
    <R extends Comparable<R>> IFrame<T> whereGt(Function<T, R> function, R value);

    /**
     * Filter Greater than or equal to
     *
     * @param function the field
     * @param value    not need value
     */
    <R extends Comparable<R>> IFrame<T> whereGe(Function<T, R> function, R value);

    /**
     * Filter LESS than value
     *
     * @param function the field
     * @param value    not need value
     */
    <R extends Comparable<R>> IFrame<T> whereLt(Function<T, R> function, R value);

    /**
     * Filter less than or equal to
     *
     * @param function the field
     * @param value    not need value
     */
    <R extends Comparable<R>> IFrame<T> whereLe(Function<T, R> function, R value);

    /**
     * Fuzzy query contains specified values
     *
     * @param function the field
     * @param value    query value
     */
    <R> IFrame<T> whereLike(Function<T, R> function, R value);

    /**
     * Fuzzy query not contains specified values
     *
     * @param function the field
     * @param value    query value
     */
    <R> IFrame<T> whereNotLike(Function<T, R> function, R value);

    /**
     * prefix fuzzy query  contains specified values
     *
     * @param function the field
     * @param value    query value
     */
    <R> IFrame<T> whereLikeLeft(Function<T, R> function, R value);

    /**
     * suffix fuzzy query  contains specified values
     *
     * @param function the field
     * @param value    query value
     */
    <R> IFrame<T> whereLikeRight(Function<T, R> function, R value);

    /**
     * ===========================   Summary Frame  =====================================
     **/
    /**
     * Sum the values of the field
     *
     * @param function the  field
     */
    <R> BigDecimal sum(Function<T, R> function);

    /**
     * average the values of the field
     *
     * @param function the  field
     */
    <R> BigDecimal avg(Function<T, R> function);

    /**
     * Finding the maximum and minimum element
     *
     * @param function the  field
     */
    <R extends Comparable<? super R>> MaxMin<T> maxMin(Function<T, R> function);

    /**
     * Finding the maximum and minimum value
     *
     * @param function the  field
     */
    <R extends Comparable<? super R>> MaxMin<R> maxMinValue(Function<T, R> function);

    /**
     * Finding the maximum  element
     *
     * @param function the  field
     */
    <R extends Comparable<R>> T max(Function<T, R> function);

    /**
     * Finding the maximum  value
     *
     * @param function the  field
     */
    <R extends Comparable<? super R>> R maxValue(Function<T, R> function);

    /**
     * Finding the minimum  value
     *
     * @param function the  field
     */
    <R extends Comparable<? super R>> R minValue(Function<T, R> function);

    /**
     * Finding the minimum  element
     *
     * @param function the  field
     */
    <R extends Comparable<R>> T min(Function<T, R> function);

    /**
     * get row count
     */
    long count();


    /** ===========================   Group Frame  ===================================== **/

    /**
     * Group list
     *
     * @param key group field
     */
    <K> IFrame<FI2<K, List<T>>> group(Function<T, K> key);

    /**
     * Group summation
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, R extends Number> IFrame<FI2<K, BigDecimal>> groupBySum(Function<T, K> key, NumberFunction<T, R> value);

    /**
     * Group summation
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, R extends Number> IFrame<FI3<K, J, BigDecimal>> groupBySum(Function<T, K> key, Function<T, J> key2, NumberFunction<T, R> value);

    /**
     * Group summation
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param key3  third level group field
     * @param value Aggregated field
     */
    <K, J, H, R extends Number> IFrame<FI4<K, J, H, BigDecimal>> groupBySum(Function<T, K> key,
                                                                            Function<T, J> key2,
                                                                            Function<T, H> key3,
                                                                            NumberFunction<T, R> value);

    /**
     * Group count
     *
     * @param key group field
     */
    <K> IFrame<FI2<K, Long>> groupByCount(Function<T, K> key);

    /**
     * Group count
     *
     * @param key  group field
     * @param key2 secondary level group field
     */
    <K, J> IFrame<FI3<K, J, Long>> groupByCount(Function<T, K> key, Function<T, J> key2);

    /**
     * Group count
     *
     * @param key  group field
     * @param key2 secondary level group field
     * @param key3 third level group field
     */
    <K, J, H> IFrame<FI4<K, J, H, Long>> groupByCount(Function<T, K> key, Function<T, J> key2, Function<T, H> key3);

    /**
     * Group sum and count together
     *
     * @param key   group field
     * @param value Aggregated field
     * @return FItem3(key, Sum, Count)
     */
    <K, R extends Number> IFrame<FI3<K, BigDecimal, Long>> groupBySumCount(Function<T, K> key, NumberFunction<T, R> value);

    /**
     * Group sum and count together
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     * @return FItem4(key, ke2, Sum, Count)
     */
    <K, J, R extends Number> IFrame<FI4<K, J, BigDecimal, Long>> groupBySumCount(Function<T, K> key, Function<T, J> key2, NumberFunction<T, R> value);


    /**
     * Group average
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, R extends Number> IFrame<FI2<K, BigDecimal>> groupByAvg(Function<T, K> key, NumberFunction<T, R> value);

    /**
     * Group average
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, R extends Number> IFrame<FI3<K, J, BigDecimal>> groupByAvg(Function<T, K> key, Function<T, J> key2, NumberFunction<T, R> value);

    /**
     * Group average
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param key3  third level group field
     * @param value Aggregated field
     */
    <K, J, H, R extends Number> IFrame<FI4<K, J, H, BigDecimal>> groupByAvg(Function<T, K> key,
                                                                            Function<T, J> key2,
                                                                            Function<T, H> key3,
                                                                            NumberFunction<T, R> value);

    /**
     * Group max
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, V extends Comparable<? super V>> IFrame<FI2<K, T>> groupByMax(Function<T, K> key, Function<T, V> value);

    /**
     * Group max
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, V extends Comparable<? super V>> IFrame<FI3<K, J, T>> groupByMax(Function<T, K> key, Function<T, J> key2, Function<T, V> value);


    /**
     * Group max value
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, V extends Comparable<? super V>> IFrame<FI2<K, V>> groupByMaxValue(Function<T, K> key, Function<T, V> value);


    /**
     * Group max value
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, V extends Comparable<? super V>> IFrame<FI3<K, J, V>> groupByMaxValue(Function<T, K> key, Function<T, J> key2, Function<T, V> value);


    /**
     * Group min
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, V extends Comparable<? super V>> IFrame<FI2<K, T>> groupByMin(Function<T, K> key, Function<T, V> value);

    /**
     * Group min
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, V extends Comparable<? super V>> IFrame<FI3<K, J, T>> groupByMin(Function<T, K> key, Function<T, J> key2, Function<T, V> value);

    /**
     * Group min value
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, V extends Comparable<? super V>> IFrame<FI2<K, V>> groupByMinValue(Function<T, K> key, Function<T, V> value);

    /**
     * Group min value
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, V extends Comparable<? super V>> IFrame<FI3<K, J, V>> groupByMinValue(Function<T, K> key, Function<T, J> key2, Function<T, V> value);

    /**
     * Group max and min value
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, V extends Comparable<? super V>> IFrame<FI2<K, MaxMin<V>>> groupByMaxMinValue(Function<T, K> key, Function<T, V> value);

    /**
     * Group max and min value
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, V extends Comparable<? super V>> IFrame<FI3<K, J, MaxMin<V>>> groupByMaxMinValue(Function<T, K> key,
                                                                                            Function<T, J> key2,
                                                                                            Function<T, V> value);

    /**
     * Group max and min element
     *
     * @param key   group field
     * @param value Aggregated field
     */
    <K, V extends Comparable<? super V>> IFrame<FI2<K, MaxMin<T>>> groupByMaxMin(Function<T, K> key,
                                                                                 Function<T, V> value);

    /**
     * Group max and min element
     *
     * @param key   group field
     * @param key2  secondary level group field
     * @param value Aggregated field
     */
    <K, J, V extends Comparable<? super V>> IFrame<FI3<K, J, MaxMin<T>>> groupByMaxMin(Function<T, K> key,
                                                                                       Function<T, J> key2,
                                                                                       Function<T, V> value);

    /** ===========================   Other  ===================================== **/

    /**
     * Summarize all collectDim values, calculate the difference between them, and then add the missing difference to the Frame through getEmptyObject
     */
    <C> IFrame<T> replenish(Function<T, C> collectDim, List<C> allDim, Function<C, T> getEmptyObject);

    /**
     * Calculate the difference in groups and then add the difference to that group
     * <p>
     * according to the groupDim dimension, and then summarize all collectDim fields within each group
     * After summarizing, calculate the difference sets with allAbscissa, which are the entries that need to be supplemented.
     * Then, generate empty objects according to the ReplenishFunction logic and add them to the group
     *
     * @param groupDim       Dimension fields for grouping
     * @param collectDim     Data fields collected within the group
     * @param allDim         All dimensions that need to be displayed within the group
     * @param getEmptyObject Logic for generating empty objects
     * @param <G>            The type of grouping
     * @param <C>            type of collection within the group
     *                       <p>
     *                       The set supplemented by @ return
     */
    <G, C> IFrame<T> replenish(Function<T, G> groupDim, Function<T, C> collectDim, List<C> allDim, ReplenishFunction<G, C, T> getEmptyObject);

    /**
     * such as {@link IFrame#replenish(Function, Function, List, ReplenishFunction)}, but can not Specify allDim，
     * will auto generate allDim, The default allDim is the value of all collectDim fields in the set
     *
     * @param groupDim       Dimension fields for grouping
     * @param collectDim     Data fields collected within the group
     * @param getEmptyObject Logic for generating empty objects
     * @param <G>            The type of grouping
     * @param <C>            type of collection within the group
     */
    <G, C> IFrame<T> replenish(Function<T, G> groupDim, Function<T, C> collectDim, ReplenishFunction<G, C, T> getEmptyObject);
}
