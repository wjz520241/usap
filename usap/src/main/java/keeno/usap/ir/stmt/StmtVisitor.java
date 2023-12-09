

package keeno.usap.ir.stmt;

/**
 * Stmt visitor which may return a result after the visit.
 *
 * @param <T> type of the return value
 */
public interface StmtVisitor<T> {

    default T visit(New stmt) {
        return visitDefault(stmt);
    }

    default T visit(AssignLiteral stmt) {
        return visitDefault(stmt);
    }

    default T visit(Copy stmt) {
        return visitDefault(stmt);
    }

    default T visit(LoadArray stmt) {
        return visitDefault(stmt);
    }

    default T visit(StoreArray stmt) {
        return visitDefault(stmt);
    }

    default T visit(LoadField stmt) {
        return visitDefault(stmt);
    }

    default T visit(StoreField stmt) {
        return visitDefault(stmt);
    }

    default T visit(Binary stmt) {
        return visitDefault(stmt);
    }

    default T visit(Unary stmt) {
        return visitDefault(stmt);
    }

    default T visit(InstanceOf stmt) {
        return visitDefault(stmt);
    }

    default T visit(Cast stmt) {
        return visitDefault(stmt);
    }

    default T visit(Goto stmt) {
        return visitDefault(stmt);
    }

    default T visit(If stmt) {
        return visitDefault(stmt);
    }

    default T visit(TableSwitch stmt) {
        return visitDefault(stmt);
    }

    default T visit(LookupSwitch stmt) {
        return visitDefault(stmt);
    }

    default T visit(Invoke stmt) {
        return visitDefault(stmt);
    }

    default T visit(Return stmt) {
        return visitDefault(stmt);
    }

    default T visit(Throw stmt) {
        return visitDefault(stmt);
    }

    default T visit(Catch stmt) {
        return visitDefault(stmt);
    }

    default T visit(Monitor stmt) {
        return visitDefault(stmt);
    }

    default T visit(Nop stmt) {
        return visitDefault(stmt);
    }

    default T visitDefault(Stmt stmt) {
        return null;
    }
}
