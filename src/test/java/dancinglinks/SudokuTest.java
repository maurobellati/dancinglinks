package dancinglinks;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.common.base.Stopwatch;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class SudokuTest {

  @Nested
  public class CustomAlphabeth {

    @Nested
    public class Parse {
      @Test
      public void allVisible() {
        Sudoku actual = parse("@#$%",
                              "....",
                              "....",
                              "....");
        assertThat(actual.getAlphabeth()).containsExactly("#", "$", "%", "@");

      }

      @Test
      public void empty() {
        Sudoku actual = parse("...",
                              "...",
                              "...");
        assertThat(actual.getAlphabeth()).containsExactly("1", "2", "3");
      }

      @Test
      public void partialOverride() {
        Sudoku actual = parse("X...",
                              ".2..",
                              "..Y.",
                              "....");

        assertThat(actual.getAlphabeth()).containsExactly("1", "2", "X", "Y");
      }

      @Test
      public void tooManySymbols() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                                                  () -> parse("xyz",
                                                              "t..",
                                                              "..."));
        assertThat(e).hasMessageContaining("Too many symbols");
      }
    }
  }

  @Nested
  public class Size16x16 {

    @Test
    public void test() {
      assertSolution(parse("...D5B7...C63.41",
                           "5.8........F....",
                           ".3....E.0B2....5",
                           "7.B....0E.......",
                           "2...9..A.75..0.B",
                           "..9.4..E.2..C7..",
                           "B..F6..D.0E9.1.3",
                           "0...F...8.D...E.",
                           ".1...0C64...7F.D",
                           "....29...3..B...",
                           ".C7..3.....84.2.",
                           ".....AD...7C..69",
                           "3..E.C8.5A12..76",
                           "...B0..436..5..E",
                           ".05A...1..9...B4",
                           ".6.......8......"),
                     parse("EF0D5B78A9C63241",
                           "598CA612D43FEB07",
                           "13A4CDEF0B278695",
                           "72B63490E581DCAF",
                           "2EC1983AF75460DB",
                           "D593410E62ABC7F8",
                           "B84F672DC0E9A153",
                           "0A67F5BC81D394E2",
                           "A12980C64EB57F3D",
                           "FDE82947136AB5C0",
                           "6C7513FB9D084E2A",
                           "4B30EAD52F7C1869",
                           "34FEBC895A120D76",
                           "C71B02A436FD598E",
                           "805ADF617C9E23B4",
                           "96D27E53B840FA1C"));

    }
  }

  @Nested
  public class Size2x2 {

    @Test
    public void test() {
      assertSolution(parse("1.",
                           ".."),
                     parse("12",
                           "21"));
    }

  }

  @Nested
  public class Size3x3 {

    @Test
    public void test() {
      assertSolution(parse("12.",
                           "...",
                           "..."),
                     parse("123",
                           "231",
                           "312"),
                     parse("123",
                           "312",
                           "231"));
    }

  }

  @Nested
  public class Size4x4 {
    @Test
    public void test() {
      assertSolution(parse("1..4",
                           "....",
                           "....",
                           "..3."),
                     parse("1324",
                           "2413",
                           "3142",
                           "4231"),
                     parse("1324",
                           "2413",
                           "3241",
                           "4132"),
                     parse("1324",
                           "4213",
                           "3142",
                           "2431"));
    }

  }

  @Nested
  public class Size9x9 {
    @Test
    public void easy() {
      assertSolution(parse("..9748...",
                           "7........",
                           ".2.1.9...",
                           "..7...24.",
                           ".64.1.59.",
                           ".98...3..",
                           "...8.3.2.",
                           "........6",
                           "...2759.."),
                     parse("519748632",
                           "783652419",
                           "426139875",
                           "357986241",
                           "264317598",
                           "198524367",
                           "975863124",
                           "832491756",
                           "641275983"));
    }

    @Test
    public void hard() {
      assertSolution(parse("8........",
                           "..36.....",
                           ".7..9.2..",
                           ".5...7...",
                           "....457..",
                           "...1...3.",
                           "..1....68",
                           "..85...1.",
                           ".9....4.."),
                     parse("812753649",
                           "943682175",
                           "675491283",
                           "154237896",
                           "369845721",
                           "287169534",
                           "521974368",
                           "438526917",
                           "796318452"));
    }
  }

  private void assertSolution(final Sudoku input, final Sudoku... solutions) {
    assertThat(solve(input)).containsExactly(solutions);
  }

  private Sudoku parse(final String... input) {
    return Sudoku.parse(asList(input));
  }

  private List<Sudoku> solve(final Sudoku sudoku) {
    System.out.printf("%nSolving: %s%n", sudoku.toPrettyString());

    Stopwatch stopwatch = Stopwatch.createStarted();
    List<Sudoku> solutions = sudoku.solve();
    long ms = stopwatch.elapsed(TimeUnit.MILLISECONDS);

    System.out.printf("%nFound %d solutions in %s ms:%n", solutions.size(), ms);
    solutions.stream().map(Sudoku::toPrettyString).forEach(System.out::println);
    return solutions;
  }

}
