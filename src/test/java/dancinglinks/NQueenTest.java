package dancinglinks;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.base.Stopwatch;
import dancinglinks.NQueen.Cell;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class NQueenTest {

  @Nested
  public class Size10 {
    @Test
    public void findAll() {
      List<NQueen> actual = solve(new NQueen(10));
      actual.forEach(NQueenTest::prettyPrint);
      assertThat(actual).size().isEqualTo(724);
    }
  }

  @Nested
  public class Size4 {

    @Test
    public void solve() {
      assertSolution(new NQueen(4),
                     parse(".X..",
                           "...X",
                           "X...",
                           "..X."),
                     parse("..X.",
                           "X...",
                           "...X",
                           ".X.."));
    }
  }

  @Nested
  public class Size8 {
    @Test
    public void findAll() {
      List<NQueen> actual = solve(new NQueen(8));
      actual.forEach(NQueenTest::prettyPrint);
      assertThat(actual).size().isEqualTo(92);
    }

    @Test
    public void withOneCell() {
      List<NQueen> actual = solve(new NQueen(8,
                                             asList(new Cell(2, 2))));
      assertThat(actual).size().isEqualTo(16);
    }
  }

  private static void prettyPrint(final NQueen solution) {
    System.out.println(solution.toPrettyString());
  }

  @Test
  public void hasSolution_false() {
    assertThat(parse("X...",
                     "....",
                     "....",
                     "....").hasSolutions()).isFalse();
  }

  @Test
  public void hasSolutions_true() {
    assertThat(parse(".X..",
                     "....",
                     "....",
                     "....").hasSolutions()).isTrue();
  }

  @Test
  public void hasUniqueSolution_false() {
    assertThat(parse("....",
                     "....",
                     "....",
                     "....").hasUniqueSolution()).isFalse();
  }

  @Test
  public void hasUniqueSolution_true() {
    assertThat(parse(".X..",
                     "....",
                     "....",
                     "....").hasUniqueSolution()).isTrue();
  }

  @Test
  public void parseTest() {
    assertThat(parse(".X..",
                     "....",
                     "X...",
                     "....")).isEqualTo(new NQueen(4,
                                                   asList(new Cell(1, 2),
                                                          new Cell(3, 1))));
  }

  private void assertSolution(final NQueen input, final NQueen... solutions) {
    assertThat(solve(input)).containsExactly(solutions);
  }

  private NQueen parse(final String... input) {
    return NQueen.parse(asList(input));
  }

  private List<NQueen> solve(final NQueen input) {
    System.out.printf("%nSolving: %s%n", input.toPrettyString());

    Stopwatch stopwatch = Stopwatch.createStarted();
    List<NQueen> solutions = input.solve();
    long ms = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    System.out.printf("%nFound %d solutions in %s ms%n", solutions.size(), ms);
    return solutions;
  }

}
