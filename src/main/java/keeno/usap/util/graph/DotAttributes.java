

package keeno.usap.util.graph;

import keeno.usap.util.collection.CollectionUtils;
import keeno.usap.util.collection.Maps;
import keeno.usap.util.collection.MultiMap;

import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

/**
 * Represents dot attributes.
 */
public class DotAttributes {

    private static final DotAttributes EMPTY = new DotAttributes(Maps.newMultiMap());

    /**
     * Stores attributes, i.e,. name-value pairs.
     */
    private final MultiMap<String, String> attrs;

    /**
     * The string representation of attributes of this object that Dot can recognize.
     */
    private final String attrsString;

    private DotAttributes(MultiMap<String, String> attrs) {
        this.attrs = attrs;
        this.attrsString = toString(attrs);
    }

    /**
     * Converts attributes in given multimap to Dot-recognizable string.
     */
    private static String toString(MultiMap<String, String> attrs) {
        StringJoiner joiner = new StringJoiner(",");
        attrs.keySet().forEach(name -> {
            Set<String> values = attrs.get(name);
            if (values.size() == 1) {
                joiner.add(name + '=' + CollectionUtils.getOne(values));
            } else {
                String value = values.stream()
                        .collect(Collectors.joining(",", "\"", "\""));
                joiner.add(name + '=' + value);
            }
        });
        return joiner.toString();
    }

    /**
     * @return a new {@link DotAttributes} with attributed updated by given input.
     */
    public DotAttributes update(String... input) {
        if ((input.length & 1) != 0) { // implicit nullcheck of input
            throw new IllegalArgumentException("input.length should be even");
        }
        MultiMap<String, String> newAttrs = Maps.newMultiMap();
        for (int i = 0; i < input.length; i += 2) {
            newAttrs.put(input[i], input[i + 1]);
        }
        attrs.keySet().forEach(name -> {
            if (!newAttrs.containsKey(name)) {
                newAttrs.putAll(name, attrs.get(name));
            }
        });
        return new DotAttributes(newAttrs);
    }

    /**
     * @return a new {@link DotAttributes} with attributed in given input added.
     */
    public DotAttributes add(String... input) {
        if ((input.length & 1) != 0) { // implicit nullcheck of input
            throw new IllegalArgumentException("input.length should be even");
        }
        MultiMap<String, String> newAttrs = Maps.newMultiMap();
        newAttrs.putAll(attrs);
        for (int i = 0; i < input.length; i += 2) {
            newAttrs.put(input[i], input[i + 1]);
        }
        return new DotAttributes(newAttrs);
    }

    /**
     * @return a {@link DotAttributes} containing attributes specified by input.
     */
    public static DotAttributes of(String... input) {
        return input.length == 0 ? EMPTY : EMPTY.add(input);
    }

    @Override
    public String toString() {
        return attrsString;
    }
}
