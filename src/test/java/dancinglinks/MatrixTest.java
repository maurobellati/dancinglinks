package dancinglinks;

import static com.google.common.collect.Lists.newArrayList;
import static dancinglinks.Solver.Options.withLimit;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

import dancinglinks.Matrix.Node;
import dancinglinks.Solver.ColumnSelector;
import dancinglinks.Solver.Options;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MatrixTest {
  @Nested
  public class Builder {
    @Test
    public void fromConstraintLines() {
      Matrix actual = MatrixBuilder.withConstraintsLines(asList("A B C D",
                                                                "B D",
                                                                "A C",
                                                                "A D",
                                                                "B C"));

      assertColumnNamesAre(actual.getPrimaryColumns(), asList("A", "B", "C", "D"));
      assertColumnNamesAre(actual.getSecondaryColumns(), emptyList());
      assertRowNamesAre(actual, asList("R1", "R2", "R3", "R4"));
      assertRowNIs(actual, 4, asList("B", "C"));
    }

    @Test
    public void fromConstraintLinesWithRowNames() {
      Matrix actual = MatrixBuilder.withConstraintsLines(asList("A B C D",
                                                                "r1: B D",
                                                                "r2: A C",
                                                                "r3: A D",
                                                                "r4: B C"));

      assertColumnNamesAre(actual.getPrimaryColumns(), asList("A", "B", "C", "D"));
      assertColumnNamesAre(actual.getSecondaryColumns(), emptyList());
      assertRowNamesAre(actual, asList("r1", "r2", "r3", "r4"));
      assertRowNIs(actual, 4, asList("B", "C"));
    }

    @Test
    public void fromConstraintLines_withOptionalColumns() {
      Matrix actual = MatrixBuilder.withConstraintsLines(asList("A B | C D",
                                                                "B D",
                                                                "A C",
                                                                "A D",
                                                                "B C"));
      assertColumnNamesAre(actual.getPrimaryColumns(), asList("A", "B"));
      assertColumnNamesAre(actual.getSecondaryColumns(), asList("C", "D"));
      assertRowNamesAre(actual, asList("R1", "R2", "R3", "R4"));
      assertRowNIs(actual, 4, asList("B", "C"));
    }

    @Test
    public void fromMatrix() {
      Matrix actual = MatrixBuilder.fromBooleanMatrix(asList("A B C D",
                                                             "0 1 0 1",
                                                             "1 0 1 0",
                                                             "1 0 0 1",
                                                             "0 1 1 0"));

      assertColumnNamesAre(actual.getPrimaryColumns(), asList("A", "B", "C", "D"));
      assertColumnNamesAre(actual.getSecondaryColumns(), emptyList());
      assertRowNamesAre(actual, asList("R1", "R2", "R3", "R4"));
      assertRowNIs(actual, 1, asList("B", "D"));
    }

    void assertRowNIs(final Matrix matrix, final int rowIndex, final List<String> expected) {
      Node row = matrix.getUncoveredRows().get(rowIndex - 1);
      assertThat(row.getAll(Node::getRight)).extracting(it -> it.getColumnHeader().getLabel())
                                            .isEqualTo(expected);
    }

    void assertRowNamesAre(final Matrix matrix, final List<String> expected) {
      assertThat(matrix.getUncoveredRows()).extracting(Node::getLabel)
                                           .isEqualTo(expected);
    }

    private void assertColumnNamesAre(final List<Node> columns, final List<String> expected) {
      assertThat(columns).extracting(Node::getLabel)
                         .isEqualTo(expected);
    }
  }

  @Nested
  public class KnuthConfiguration {
    @Nested
    public class CoverColumn {
      @Nested
      public class UncoverColumn {
        @Test
        public void allValuesAreBack() {
          assertThat(matrix.getUncoveredColumns()).isEqualTo(allColumns);
          assertThat(matrix.getUncoveredRows()).isEqualTo(allRows);
          assertThat(matrix.getUncoveredNodes()).isEqualTo(allNodes);
        }

        @Test
        public void columnCount() {
          assertThat(allColumns).extracting(Node::getColumnCount)
                                .isEqualTo(asList(2, 2, 2, 3, 2, 2, 3));
        }

        @BeforeEach
        public void init() {
          matrix.uncoverColumn(coveredColumn);
        }
      }

      private Node coveredColumn;

      @Test
      public void columnCount() {
        assertThat(allColumns).extracting(Node::getColumnCount)
                              .isEqualTo(asList(2, 2, 2, 1, 2, 2, 2));
      }

      @BeforeEach
      public void init() {
        coveredColumn = allColumns.get(0);
        matrix.coverColumn(coveredColumn);
      }

      @Test
      public void uncoveredColumns() {
        assertThat(matrix.getUncoveredColumns()).doesNotContain(coveredColumn);
      }

      @Test
      public void uncoveredNodes() {
        assertThat(matrix.getUncoveredNodes()).extracting(Node::getRowHeader)
                                              .doesNotContain(allRows.get(1),
                                                              allRows.get(3));
      }
    }

    private List<Node> allColumns;
    private List<Node> allNodes;
    private List<Node> allRows;
    private Matrix matrix;

    @Test
    public void columnCount() {
      assertThat(matrix.getPrimaryColumns()).extracting(Node::getColumnCount)
                                            .isEqualTo(asList(2, 2, 2, 3, 2, 2, 3));
    }

    @BeforeEach
    public void init() {
      matrix = MatrixBuilder.fromBooleanMatrix(newArrayList("A B C D E F G",
                                                            "0 0 1 0 1 1 0",
                                                            "1 0 0 1 0 0 1",
                                                            "0 1 1 0 0 1 0",
                                                            "1 0 0 1 0 0 0",
                                                            "0 1 0 0 0 0 1",
                                                            "0 0 0 1 1 0 1"));
      allColumns = matrix.getUncoveredColumns();
      assertThat(allColumns).size().isEqualTo(7);
      allRows = matrix.getUncoveredRows();
      assertThat(allRows).size().isEqualTo(6);
      allNodes = matrix.getUncoveredNodes();
      assertThat(allNodes).size().isEqualTo(16);
    }

    @Test
    public void solveFirst() {
      List<Solution> solutions = matrix.solve(Options.builder().columnSelector(ColumnSelector.FIRST).build());
      assertThat(solutions).size().isEqualTo(1);
      Solution actual = solutions.get(0);
      assertThat(actual.getCoveredColumnNames()).isEqualTo(asList(asList("A", "D"),
                                                                  asList("B", "G"),
                                                                  asList("C", "E", "F")));
    }

    @Test
    public void solveSmaller() {
      List<Solution> solutions = matrix.solve(Options.builder().columnSelector(ColumnSelector.SMALLER).build());
      assertThat(solutions).size().isEqualTo(1);
      Solution actual = solutions.get(0);
      assertThat(actual.getCoveredColumnNames()).isEqualTo(asList(asList("A", "D"),
                                                                  asList("E", "F", "C"),
                                                                  asList("B", "G")));
    }
  }

  @Nested
  public class MultipleSolutions {
    private Matrix matrix;

    @Test
    public void hasSolutions() {
      assertThat(matrix.hasSolutions()).isTrue();
    }

    @Test
    public void hasUniqueSolution() {
      assertThat(matrix.hasUniqueSolution()).isFalse();
    }

    @BeforeEach
    public void init() {
      matrix = MatrixBuilder.fromBooleanMatrix(newArrayList("A B C D",
                                                            "0 1 0 1",
                                                            "1 0 1 0",
                                                            "1 0 0 1",
                                                            "0 1 1 0",
                                                            "1 1 0 0",
                                                            "0 0 1 1"));
    }

    @Test
    public void limit() {
      List<Solution> solutions = matrix.solve(withLimit(2));
      assertThat(solutions).size().isEqualTo(2);
    }

    @Test
    public void solve() {
      List<Solution> solutions = matrix.solve();
      assertThat(solutions).size().isEqualTo(3);
      assertThat(solutions).extracting(Solution::getCoveredColumnNames)
                           .containsAll(asList(asList(asList("A", "D"),
                                                      asList("B", "C")),
                                               asList(asList("A", "C"),
                                                      asList("B", "D"))));
    }
  }

  @Nested
  public class OptionalColumn {

    @BeforeEach
    public void init() {
      Matrix matrix = new Matrix(asList("A", "B"), asList("C", "D"));
    }
  }

}
