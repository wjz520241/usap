

/**
 * This package implements modification side-effect analysis (MOD) which
 * computes the objects that may be modified by each method and statement.
 * <p>
 * The analysis was defined in paper:
 * Ana Milanova, Atanas Rountev, and Barbara G. Ryder.
 * Parameterized Object Sensitivity for Points-to Analysis for Java.
 * In TOSEM 2005.
 * <p>
 * However, the algorithm described in the paper is very inefficient.
 * Therefore, we have designed and implemented a new and efficient
 * algorithm to compute modification information.
 */
package keeno.usap.analysis.sideeffect;
