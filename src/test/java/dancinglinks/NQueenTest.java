package dancinglinks;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Java6Assertions.assertThat;

import dancinglinks.NQueen.Cell;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NQueenTest {

  @Nested
  public class Size4x4 {

    @Test
    public void solve() {
      List<NQueen> solutions = new NQueen(4).solve();
      System.out.printf("Found %s solutions%n", solutions.size());
      solutions.forEach(System.out::println);
    }
  }

  @Test
  public void parse() {
    NQueen actual = NQueen.parse(asList(".X..",
                                        "....",
                                        "X...",
                                        "...."));
    NQueen expected = new NQueen(4, asList(new Cell(1, 2),
                                           new Cell(3, 1)));
    assertThat(actual).isEqualTo(expected);
  }

}

