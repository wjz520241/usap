

package keeno.usap.language.type;

/**
 * This type means that the expression, e.g., a variable, is untyped (i.e., has no type).
 * Usually, it should not appear in IR, however, currently Tai-e uses Soot as front end
 * which fails to type some variables, thus it stays in IR for some cases.
 */
public enum BottomType implements Type {

    BOTTOM;

    @Override
    public String getName() {
        return "bottom-type";
    }

    @Override
    public String toString() {
        return getName();
    }
}
