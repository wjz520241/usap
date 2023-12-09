

package keeno.usap.frontend.soot;

import keeno.usap.ir.exp.ClassLiteral;
import keeno.usap.ir.exp.Literal;
import keeno.usap.ir.exp.NullLiteral;
import keeno.usap.ir.exp.StringLiteral;
import keeno.usap.ir.exp.Var;
import keeno.usap.language.classes.JMethod;
import keeno.usap.language.type.Type;
import keeno.usap.util.collection.Maps;
import soot.Local;
import soot.Value;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class VarManager {

    private static final String THIS = "%this";

    private static final String STRING_CONSTANT = "%stringconst";

    private static final String CLASS_CONSTANT = "%classconst";

    private static final String NULL_CONSTANT = "%nullconst";

    /**
     * The method which contains the variable managed by this VarManager.
     */
    private final JMethod method;

    private final Converter converter;

    private final Map<Local, Var> varMap = Maps.newLinkedHashMap();

    private final List<Var> vars = new ArrayList<>();

    private Var thisVar;

    private final List<Var> params = new ArrayList<>();

    private Var nullConst;

    /**
     * Counter for indexing all variables.
     */
    private int varCounter = 0;

    /**
     * Counter for naming temporary constant variables.
     */
    private int tempConstCounter = 0;

    public VarManager(JMethod method, Converter converter) {
        this.method = method;
        this.converter = converter;
    }

    void addThis(Local thisLocal) {
        thisVar = newVar(THIS, getTypeOf(thisLocal));
        varMap.put(thisLocal, thisVar);
    }

    void addParams(List<Local> paramLocals) {
        for (int i = 0; i < paramLocals.size(); i++) {
            Local paramLocal = paramLocals.get(i);
            // soot frontend cannot ensure availability of all parameter names
            String paramName = Objects.requireNonNullElse(
                    method.getParamName(i), paramLocal.getName());
            Var param = varMap.computeIfAbsent(paramLocal, l ->
                    newVar(paramName, getTypeOf(l)));
            params.add(param);
        }
    }

    Var getVar(Local local) {
        return varMap.computeIfAbsent(local, l ->
                newVar(l.getName(), getTypeOf(l)));
    }

    /**
     * @return a new temporary variable that holds given literal value.
     */
    Var newConstantVar(Literal literal) {
        String varName;
        if (literal instanceof StringLiteral) {
            varName = STRING_CONSTANT + tempConstCounter++;
        } else if (literal instanceof ClassLiteral) {
            varName = CLASS_CONSTANT + tempConstCounter++;
        } else if (literal instanceof NullLiteral) {
            // each method has at most one variable for null constant
            Var v = nullConst;
            if (v == null) {
                v = newVar(NULL_CONSTANT, literal.getType(), literal);
                nullConst = v;
            }
            return v;
        } else {
            varName = "%" + literal.getType().getName() +
                    "const" + tempConstCounter++;
        }
        return newVar(varName, literal.getType(), literal);
    }

    Var getThis() {
        return thisVar;
    }

    List<Var> getParams() {
        return params;
    }

    List<Var> getVars() {
        return vars;
    }

    private Var newVar(String name, Type type) {
        return newVar(name, type, null);
    }

    private Var newVar(String name, Type type, @Nullable Literal literal) {
        Var var = new Var(method, name, type, varCounter++, literal);
        vars.add(var);
        return var;
    }

    /**
     * Shortcut: obtains Jimple Value's Type and convert to Tai-e Type.
     */
    private Type getTypeOf(Value value) {
        return converter.convertType(value.getType());
    }
}
