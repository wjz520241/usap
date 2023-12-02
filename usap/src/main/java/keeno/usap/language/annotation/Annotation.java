package keeno.usap.language.annotation;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.Map;

/**
 *表示程序中的注解。每个注解包含0个或多个元素
 * 目前，我们使用{@code String}（而不是{@code JClass}或{@code ClassType}）来表示注释的类型。
 * 这使得前端更容易从程序中提取注解（类型字符串已在程序中准备好，并且前端不需要将字符串解析为{@code JClass}）。
 */
public class Annotation implements Serializable {
    private final String annotationType;

    /**
     * 从名称映射到此注解中的相应元素
     */
    private final Map<String, Element> elements;

    public Annotation(String annotationType, Map<String, Element> elements) {
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


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("@");
        sb.append(annotationType);
        int elems = elements.size();
        if (elems > 0) {
            sb.append('(');
            if (elems == 1 && elements.containsKey("value")) {
                // 单元素注解，如@Test(123)
                sb.append(elements.get("value"));
            } else {
                // 普通注解
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
