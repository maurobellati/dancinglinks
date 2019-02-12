package dancinglinks;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;

import dancinglinks.NQueen.Cell;
import dancinglinks.NQueen.ConstraintsGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

public class NQueenContraintsGeneratorTest {

  @Nested
  public class Size3x3 {
    @Nested
    public class WithOneNumber {
      @Test
      public void constraints() {
        assertThat(getRows()).isEqualTo(asList("1.1: R1 C1 A1 B3",
                                               "2.3: R2 C3 A4 B2",
                                               "3.2: R3 C2 A4 B4"));
      }

      @BeforeEach
      public void init() {
        actual = new ConstraintsGenerator(3, newHashSet(new Cell(1, 1))).generate();
      }

      @Test
      public void size() {
        assertThat(getRows()).size().isEqualTo(3);
      }

    }

    @Test
    public void columnNames() {
      assertThat(getColumnNames()).isEqualTo("R1 R2 R3 C1 C2 C3 | A1 A2 A3 A4 A5 B1 B2 B3 B4 B5");
    }

    @Test
    public void constraints() {
      assertThat(getRows()).isEqualTo(asList("1.1: R1 C1 A1 B3",
                                             "1.2: R1 C2 A2 B2",
                                             "1.3: R1 C3 A3 B1",

                                             "2.1: R2 C1 A2 B4",
                                             "2.2: R2 C2 A3 B3",
                                             "2.3: R2 C3 A4 B2",

                                             "3.1: R3 C1 A3 B5",
                                             "3.2: R3 C2 A4 B4",
                                             "3.3: R3 C3 A5 B3"));
    }

    @BeforeEach
    public void init() {
      actual = new ConstraintsGenerator(3, emptySet()).generate();
    }

    @Test
    public void size() {
      assertThat(getRows()).size().isEqualTo(9);
    }

  }

  private List<String> actual;

  private String getColumnNames() {
    return actual.get(0);
  }

  private List<String> getRows() {
    return actual.subList(1, actual.size());
  }

}
