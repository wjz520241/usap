

package keeno.usap.ir;

import keeno.usap.ir.exp.InvokeDynamic;
import keeno.usap.ir.exp.InvokeExp;
import keeno.usap.ir.exp.InvokeInstanceExp;
import keeno.usap.ir.exp.Literal;
import keeno.usap.ir.stmt.Invoke;
import keeno.usap.ir.stmt.Stmt;

import java.io.PrintStream;
import java.util.Formatter;
import java.util.stream.Collectors;

public class IRPrinter {

    public static void print(IR ir, PrintStream out) {
        // print method signature
        out.println("---------- " + ir.getMethod() + " ----------");
        // print parameters
        out.print("Parameters: ");
        out.println(ir.getParams()
                .stream()
                .map(p -> p.getType() + " " + p)
                .collect(Collectors.joining(", ")));
        // print all variables
        out.println("Variables:");
        ir.getVars().forEach(v -> out.println(v.getType() + " " + v));
        // print all statements
        out.println("Statements:");
        ir.forEach(s -> out.println(toString(s)));
        // print all try-catch blocks
        if (!ir.getExceptionEntries().isEmpty()) {
            out.println("Exception entries:");
            ir.getExceptionEntries().forEach(b -> out.println("  " + b));
        }
    }

    public static String toString(Stmt stmt) {
        if (stmt instanceof Invoke) {
            return toString((Invoke) stmt);
        } else {
            return String.format("%s %s;", position(stmt), stmt);
        }
    }

    public static String toString(Invoke invoke) {
        Formatter formatter = new Formatter();
        formatter.format("%s ", position(invoke));
        if (invoke.getResult() != null) {
            // some variable names contain '%', which will be treated as
            // format specifier by formatter, thus we need to escape it
            String lhs = invoke.getResult().toString().replace("%", "%%");
            formatter.format(lhs + " = ");
        }
        InvokeExp ie = invoke.getInvokeExp();
        formatter.format("%s ", ie.getInvokeString());
        if (ie instanceof InvokeDynamic indy) {
            formatter.format("%s \"%s\" <%s>[%s]%s;",
                    indy.getBootstrapMethodRef(),
                    indy.getMethodName(), indy.getMethodType(),
                    indy.getBootstrapArgs()
                            .stream()
                            .map(Literal::toString)
                            .collect(Collectors.joining(", ")),
                    indy.getArgsString());
        } else {
            if (ie instanceof InvokeInstanceExp) {
                formatter.format("%s.", ((InvokeInstanceExp) ie).getBase().getName());
            }
            formatter.format("%s%s;", ie.getMethodRef(), ie.getArgsString());
        }
        return formatter.toString();
    }

    public static String position(Stmt stmt) {
        return "[" +
                stmt.getIndex() +
                "@L" + stmt.getLineNumber() +
                ']';
    }
}
