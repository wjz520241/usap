

/**
 * This package provides functionality to analyze exceptions.
 * <p>
 * Here we use the term "exception" by convention, actually, we handle
 * all subclasses of {@link java.lang.Throwable}, including both
 * {@link java.lang.Exception} and {@link java.lang.Error}.
 * <p>
 * We classify exceptions into four categories:
 * <p>
 * (1) VM errors, i.e., subclasses of {@link java.lang.VirtualMachineError}
 * defined below:
 * {@link java.lang.InternalError}
 * {@link java.lang.OutOfMemoryError}
 * {@link java.lang.StackOverflowError}
 * {@link java.lang.UnknownError}
 * According to JVM Spec., Chapter 6.3, the above errors may be thrown
 * at any time during the operation of the Java Virtual Machine.
 * <p>
 * (2) Exceptions that may be implicitly thrown by JVM when executing
 * each instruction. See JVM Spec., Chapter 6.5 for more details.
 * <p>
 * (3) Exceptions that are explicitly thrown by throw statements.
 * <p>
 * (4) Exceptions that are explicitly thrown by method invocations.
 * <p>
 * Generally, Tai-e ignores (1), and provides different strategies
 * to handle exceptions in (2)-(4).
 */
package keeno.usap.analysis.exception;
