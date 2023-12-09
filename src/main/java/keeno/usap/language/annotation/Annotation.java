

package keeno.usap.language.annotation;

import keeno.usap.util.collection.Views;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * 表示程序中的注解。
 * 每个注解包含0个或多个命名元素。
 * 目前，我们使用{@code String}（而不是{@code JClass}或{@code ClassType}）来表示注解的类型。
 * 这使得前端更容易从程序中提取注解（类型字符串在程序中准备好，并且前端不需要将字符串解析为{@code JClass}）。
 */
public class Annotation implements Serializable {

    /**
     * 此注解类型的字符串表示形式。
     */
    private final String annotationType;

    /**
     * 从名称映射到此注解中的相应元素。
     */
    private final Map<String, Element> elements;

    public Annotation(String annotationType,
                      Map<String, Element> elements) {
        this.annotationType = annotationType;
        this.elements = Map.copyOf(elements);
    }

    public String getType() {
        return annotationType;
    }

    public boolean hasElement(String name) {
        return elements.containsKey(name);
    }

    @Nullable
    public Element getElement(String name) {
        return elements.get(name);
    }

    /**
     * @return all name-element entries in this annotation.
     */
    public Set<Entry> getElementEntries() {
        return Views.toMappedSet(elements.entrySet(),
                e -> new Entry(e.getKey(), e.getValue()));
    }

    /**
     * Represents name-element entries in annotations.
     */
    public record Entry(String name, Element element) {
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@");
        sb.append(annotationType);
        int elems = elements.size();
        if (elems > 0) {
            sb.append('(');
            if (elems == 1 && elements.containsKey("value")) {
                // 单元素注解的情况，如@Test("keeno")
                sb.append(elements.get("value"));
            } else {
                // normal annotation
                int count = 0;
                for (var e : elements.entrySet()) {
                    sb.append(e.getKey()).append('=').append(e.getValue());
                    if (++count < elems) {
                        sb.append(',');
                    }
                }
            }
            sb.append(')');
        }
        return sb.toString();
    }
}
