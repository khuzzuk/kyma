package pl.khuzzuk.functions.collections;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * It returns only elements which match provided criteria. It also immutable collection.
 *
 * @param <K> keys
 * @param <V> values
 */
public class FilteredMapDecorator<K, V> implements Map<K, V> {
    private static final Predicate ALWAYS_TRUE = __ -> true;
    private final Map<K, V> source;
    private final Predicate<K> keyPredicate;
    private final Predicate<V> valuePredicate;

    private FilteredMapDecorator(Map<K, V> source, Predicate<K> keyPredicate, Predicate<V> valuePredicate) {
        this.source = source;
        this.keyPredicate = keyPredicate;
        this.valuePredicate = valuePredicate;
    }

    @SuppressWarnings("unchecked")
    public static <K, V> FilteredMapDecorator<K, V> createWithValueFilter(
            Map<K, V> source,
            Predicate<V> valuePredicate) {
        return new FilteredMapDecorator<>(source, ALWAYS_TRUE, valuePredicate);
    }

    @SuppressWarnings("unchecked")
    public static <K, V> FilteredMapDecorator<K, V> createWithKeyFilter(
            Map<K, V> source,
            Predicate<K> keyPredicate) {
        return new FilteredMapDecorator<>(source, keyPredicate, ALWAYS_TRUE);
    }

    public static <K, V> FilteredMapDecorator<K, V> create(
            Map<K, V> source,
            Predicate<K> keyPredicate,
            Predicate<V> valuePredicate) {
        return new FilteredMapDecorator<>(source, keyPredicate, valuePredicate);
    }

    private Map<? extends K, ? extends V> getFilteredMap(Map<? extends K, ? extends V> toFilter) {
        return toFilter.entrySet().stream()
                .filter(entry -> keyPredicate.test(entry.getKey()) && valuePredicate.test(entry.getValue()))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public int size() {
        return (int) source.entrySet().stream()
                .filter(entry -> keyPredicate.test(entry.getKey()) && valuePredicate.test(entry.getValue()))
                .count();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Will throw {@link ClassCastException} if key has different class than <code>K</code>
     *
     * @param key should bo of type <code>K</code>
     * @return true if key is in the map and match provided criteria
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean containsKey(Object key) {
        K castedKey = (K) key;
        return source.containsKey(key) && keyPredicate.test(castedKey);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean containsValue(Object value) {
        V castedValue = (V) value;
        return source.containsValue(value) && valuePredicate.test(castedValue);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        K castedKey = (K) key;
        return keyPredicate.test(castedKey) ? source.get(key) : null;
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return source.keySet().stream()
                .filter(keyPredicate)
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<V> values() {
        return source.entrySet().stream()
                .filter(this::entrySetTest)
                .map(Entry::getValue)
                .collect(Collectors.toList());
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return source.entrySet().stream()
                .filter(this::entrySetTest)
                .collect(Collectors.toSet());
    }

    private boolean entrySetTest(Entry<K, V> entry) {
        return keyPredicate.test(entry.getKey()) && valuePredicate.test(entry.getValue());
    }
}
