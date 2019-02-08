

package dancinglinks;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import dancinglinks.Matrix.Node;
import dancinglinks.Solver.Solution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MatrixTest {

    @Nested
    public class Builder {

        @Test
        public void fromConstraintLines() {
            Matrix actual = MatrixBuilder.withContraintsLines(asList("A B C D",
                                                                     "B D",
                                                                     "A C",
                                                                     "A D",
                                                                     "B C"));

            assertColumnNamesAre(actual, asList("A", "B", "C", "D"));
            assertRowNamesAre(actual, asList("R1", "R2", "R3", "R4"));
            assertRowNIs(actual, 4, asList("B", "C"));
        }

        @Test
        public void fromConstraintLinesWithRowNames() {
            Matrix actual = MatrixBuilder.withContraintsLines(asList("A B C D",
                                                                     "r1: B D",
                                                                     "r2: A C",
                                                                     "r3: A D",
                                                                     "r4: B C"));

            assertColumnNamesAre(actual, asList("A", "B", "C", "D"));
            assertRowNamesAre(actual, asList("r1", "r2", "r3", "r4"));
            assertRowNIs(actual, 4, asList("B", "C"));
        }

        @Test
        public void fromMatrix() {
            Matrix actual = MatrixBuilder.fromBooleanMatrix(asList("A B C D",
                                                                   "0 1 0 1",
                                                                   "1 0 1 0",
                                                                   "1 0 0 1",
                                                                   "0 1 1 0"));

            assertColumnNamesAre(actual, asList("A", "B", "C", "D"));
            assertRowNamesAre(actual, asList("R1", "R2", "R3", "R4"));
            assertRowNIs(actual, 1, asList("B", "D"));
        }

        void assertColumnNamesAre(final Matrix matrix, final List<String> expected) {
            assertThat(matrix.getUncoveredColumns()).extracting(Node::getLabel)
                                                    .isEqualTo(expected);
        }

        void assertRowNIs(final Matrix matrix, final int rowIndex, final List<String> expected) {
            Node row = matrix.getUncoveredRows().get(rowIndex - 1);
            assertThat(matrix.getNodesAfter(row, Node::getRight)).extracting(it -> it.getColumnHeader().getLabel())
                                                                 .isEqualTo(expected);
        }

        void assertRowNamesAre(final Matrix matrix, final List<String> expected) {
            assertThat(matrix.getUncoveredRows()).extracting(Node::getLabel)
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
            assertThat(matrix.getUncoveredColumns()).extracting(Node::getColumnCount)
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
            List<Solution> solutions = matrix.solve(Solver.ColumnSelector.FIRST, false);
            assertThat(solutions).size().isEqualTo(1);

            Solution actual = solutions.get(0);
            assertThat(actual.getCoveredColumnNames()).isEqualTo(asList(asList("A", "D"),
                                                                        asList("B", "G"),
                                                                        asList("C", "E", "F")));

        }

        @Test
        public void solveSmaller() {
            List<Solution> solutions = matrix.solve(Solver.ColumnSelector.SMALLER, false);
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

        @BeforeEach
        public void init() {
            matrix = MatrixBuilder.fromBooleanMatrix(newArrayList("A B C D",
                                                                  "0 1 0 1",
                                                                  "1 0 1 0",
                                                                  "1 0 0 1",
                                                                  "0 1 1 0"));
        }

        @Test
        public void test() {
            List<Solution> solutions = matrix.solve();

            assertThat(solutions).size().isEqualTo(2);

            assertThat(solutions).extracting(Solution::getCoveredColumnNames)
                                 .contains(asList(asList("A", "D"),
                                                  asList("B", "C")),
                                           asList(asList("A", "C"),
                                                  asList("B", "D")));
        }

    }

    @Nested
    public class Sudoku2x2 {

        private Matrix matrix;

        @BeforeEach
        public void init() {
            matrix = MatrixBuilder.withContraintsLines(asList("X1.1 X1.2 X2.1 X2.2 R1#1 R1#2 R2#1 R2#2 C1#1 C1#2 C2#1 C2#2",
                                                              "r1c1#1: X1.1 R1#1 C1#1",
                                                              "r1c1#2: X1.1 R1#2 C1#2",

                                                              "r1c2#1: X1.2 R1#1 C2#1",
                                                              "r1c2#2: X1.2 R1#2 C2#2",

                                                              "r2c1#1: X2.1 R2#1 C1#1",
                                                              "r2c1#2: X2.1 R2#2 C1#2",

                                                              "r2c2#1: X2.2 R2#1 C2#1",
                                                              "r2c2#2: X2.2 R2#2 C2#2"));
        }

        @Test
        public void solve() {
            List<Solution> solutions = matrix.solve();

            assertThat(solutions).size().isEqualTo(2);

            assertThat(solutions).extracting(Solution::getRowNames)
                                 .contains(asList("r1c1#1", "r1c2#2", "r2c1#2", "r2c2#1"),
                                           asList("r1c1#2", "r1c2#1", "r2c1#1", "r2c2#2"));
        }
    }

}
