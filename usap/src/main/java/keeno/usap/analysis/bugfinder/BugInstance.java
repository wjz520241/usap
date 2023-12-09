

package keeno.usap.analysis.bugfinder;

import keeno.usap.language.classes.JClass;
import keeno.usap.language.classes.JMethod;

import java.util.Objects;

// TODO: refactor it with more precise context information.
public class BugInstance implements Comparable<BugInstance> {

    private final BugType type;

    private final Severity severity;

    private final JClass jClass;

    private final JMethod jMethod;

    private int sourceLineStart = -1;

    private int sourceLineEnd = -1;

    public BugInstance(BugType type, Severity severity, JClass jClass) {
        this.type = type;
        this.severity = severity;
        this.jClass = jClass;
        this.jMethod = null;
    }

    public BugInstance(BugType type, Severity severity, JMethod jMethod) {
        this.type = type;
        this.severity = severity;
        this.jClass = jMethod.getDeclaringClass();
        this.jMethod = jMethod;
    }

    public BugType getType() {
        return type;
    }

    public Severity getSeverity() {
        return severity;
    }

    public BugInstance setSourceLine(int start, int end) {
        sourceLineStart = start;
        sourceLineEnd = end;
        return this;
    }

    public BugInstance setSourceLine(int num) {
        return setSourceLine(num, num);
    }

    public int getSourceLineStart() {
        return sourceLineStart;
    }

    public int getSourceLineEnd() {
        return sourceLineEnd;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BugInstance bugInstance)) {
            return false;
        }
        return type.equals(bugInstance.type)
                && Objects.equals(jClass, bugInstance.jClass)
                && Objects.equals(jMethod, bugInstance.jMethod)
                && sourceLineStart == bugInstance.sourceLineStart
                && sourceLineEnd == bugInstance.sourceLineEnd;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, jClass, jMethod, sourceLineStart, sourceLineEnd);
    }

    @Override
    public String toString() {
        String sourceLineRange = "null";
        if (sourceLineStart >= 0) {
            sourceLineRange = sourceLineStart == sourceLineEnd
                    ? String.valueOf(sourceLineStart)
                    : sourceLineStart + "---" + sourceLineEnd;
        }
        return String.format("Class: %s, Method: %s, LineNumber: %s, BugType: %s, Severity: %s",
                jClass, jMethod, sourceLineRange, type, severity);
    }

    @Override
    public int compareTo(BugInstance o) {
        if (jClass.equals(o.jClass)) {
            return Integer.compare(sourceLineStart, o.sourceLineStart);
        } else {
            return jClass.toString().compareTo(o.jClass.toString());
        }
    }
}
