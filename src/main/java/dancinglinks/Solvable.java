package dancinglinks;

import static dancinglinks.Solver.Options.withLimit;

import dancinglinks.Solver.Options;

import java.util.List;

public interface Solvable<SolutionT> {

  default boolean hasSolutions() {
    return !solve(withLimit(1)).isEmpty();
  }

  default boolean hasUniqueSolution() {
    return solve(withLimit(2)).size() == 1;
  }

  List<SolutionT> solve(Options options);

  default List<SolutionT> solve() {
    return solve(Options.builder().build());
  }

}
