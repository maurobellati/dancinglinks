package dancinglinks;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.List;

public class SudokuContraintsGeneratorTest {

  @Nested
  public class Size2x2 {
    @Nested
    public class WithOneNumber {
      @Test
      public void constraints() {
        assertThat(getRows()).isEqualTo(asList("r1c1#1: X1.1 R1#1 C1#1",

                                               "r1c2#1: X1.2 R1#1 C2#1",
                                               "r1c2#2: X1.2 R1#2 C2#2",

                                               "r2c1#1: X2.1 R2#1 C1#1",
                                               "r2c1#2: X2.1 R2#2 C1#2",

                                               "r2c2#1: X2.2 R2#1 C2#1",
                                               "r2c2#2: X2.2 R2#2 C2#2"));
      }

      @BeforeEach
      public void init() {
        actual = constraintsGenerator(2,
                                      asList(new Sudoku.Cell(1, 1, "1"))).generate();
      }

      @Test
      public void size() {
        assertThat(getRows()).size().isEqualTo(7);
      }

    }

    @Test
    public void columnNames() {
      assertThat(getColumnNames()).isEqualTo("X1.1 X1.2 X2.1 X2.2 R1#1 R1#2 R2#1 R2#2 C1#1 C1#2 C2#1 C2#2");
    }

    @Test
    public void constraints() {
      assertThat(getRows()).isEqualTo(asList("r1c1#1: X1.1 R1#1 C1#1",
                                             "r1c1#2: X1.1 R1#2 C1#2",

                                             "r1c2#1: X1.2 R1#1 C2#1",
                                             "r1c2#2: X1.2 R1#2 C2#2",

                                             "r2c1#1: X2.1 R2#1 C1#1",
                                             "r2c1#2: X2.1 R2#2 C1#2",

                                             "r2c2#1: X2.2 R2#1 C2#1",
                                             "r2c2#2: X2.2 R2#2 C2#2"));
    }

    @BeforeEach
    public void init() {
      actual = constraintsGenerator(2).generate();
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "r1c1#1",
      "r2c2#2"
    })
    public void rowNames(final String rowName) {
      assertThat(getRows()).anyMatch(it -> it.startsWith(rowName));
    }

    @Test
    public void size() {
      assertThat(getRows()).size().isEqualTo(8);
    }

  }

  @Nested
  public class Size9x9 {
    @ParameterizedTest
    @ValueSource(strings = {
      "X1.1",
      "X9.9",
      "R1#1",
      "R9#9",
      "C1#1",
      "C9#9",
      "S1#1",
      "S1#9",
      "S9#1",
      "S9#9",
    })
    public void columnNames(final String name) {
      assertThat(getColumnNames()).contains(name);
    }

    @ParameterizedTest
    @ValueSource(strings = {
      "r1c1#1: X1.1 R1#1 C1#1 S1#1",
      "r1c1#9: X1.1 R1#9 C1#9 S1#9",

      "r1c4#5: X1.4 R1#5 C4#5 S2#5",

      "r7c8#5: X7.8 R7#5 C8#5 S9#5",
      "r9c9#9: X9.9 R9#9 C9#9 S9#9",
    })
    public void constraints(final String line) {
      assertThat(getRows()).contains(line);
    }

    @BeforeEach
    public void init() {
      actual = constraintsGenerator(9).generate();

    }

    @ParameterizedTest
    @ValueSource(strings = {
      "r1c1#1",
      "r1c9#1",
      "r1c9#1",
      "r9c9#9",
    })
    public void rowNames(final String name) {
      assertThat(getRows()).anyMatch(it -> it.startsWith(name));
    }

    @Test
    public void size() {
      assertThat(getRows()).size().isEqualTo(729);
    }
  }

  private List<String> actual;

  private Sudoku.ConstraintsGenerator constraintsGenerator(final int size, final Collection<Sudoku.Cell> existingValues) {
    return new Sudoku.ConstraintsGenerator(size, existingValues, asList("123456789".split("")));
  }

  private Sudoku.ConstraintsGenerator constraintsGenerator(final int size) {
    return constraintsGenerator(size, emptyList());

  }

  private String getColumnNames() {
    return actual.get(0);
  }

  private List<String> getRows() {
    return actual.subList(1, actual.size());
  }
}
