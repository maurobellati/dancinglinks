package dancinglinks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.difference;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;
import static java.util.stream.Stream.concat;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

@EqualsAndHashCode(of = {"size", "existingValues"})
public class NQueen {

  @Value
  public static class Cell implements Comparable<Cell> {
    final Integer column;
    final Integer row;

    public Cell(final Integer row, final Integer column) {
      this.row = row;
      this.column = column;
    }

    @Override
    public int compareTo(final Cell other) {
      return ComparisonChain.start()
                            .compare(row, other.getRow())
                            .compare(column, other.getColumn())
                            .result();
    }

    public Cell down() {
      return new Cell(row + 1, column);
    }

    public Cell downLeft() {
      return new Cell(row + 1, column - 1);
    }

    public Cell downRight() {
      return new Cell(row + 1, column + 1);
    }

    public Cell left() {
      return new Cell(row, column - 1);
    }

    public Cell right() {
      return new Cell(row, column + 1);
    }

    public Cell up() {
      return new Cell(row - 1, column);
    }

    public Cell upLeft() {
      return new Cell(row - 1, column - 1);
    }

    public Cell upRight() {
      return new Cell(row - 1, column + 1);
    }

  }

  public static class ConstraintsGenerator {
    private static final String PRIMARY_SECONDARY_SEPARATOR = " | ";
    private final Set<Cell> allCells;
    private final List<Function<Cell, String>> primaryCellConstraintFormaters;
    private final Set<Cell> requireConstraintCells;
    private final List<Function<Cell, String>> secondaryCellConstraintFormaters;
    private final int size;
    private final Range<Integer> validIndexRange;

    public ConstraintsGenerator(final int size, final Set<Cell> existingValues) {
      this.size = size;
      validIndexRange = Range.closed(1, size);

      primaryCellConstraintFormaters = newArrayList(this::formatRowConstraint,
                                                    this::formatColumnConstraint);

      secondaryCellConstraintFormaters = newArrayList(this::formatForwardDiagonalConstraint,
                                                      this::formatReverseDiagonalConstraint);

      allCells = newHashSet();
      rangeClosed(1, size)
        .forEach(rowIndex -> {
          rangeClosed(1, size)
            .forEach(columnIndex -> allCells.add(new Cell(rowIndex, columnIndex)));
        });

      Set<Cell> forbiddenCells = existingValues.stream()
                                               .flatMap(it -> reachableFrom(it).stream())
                                               .collect(toSet());

      requireConstraintCells = difference(allCells, forbiddenCells);

    }

    public List<String> generate() {
      List<String> result = newArrayList(generateColumnNames());
      result.addAll(requireConstraintCells.stream()
                                          .sorted()
                                          .map(this::generate)
                                          .collect(toList()));

      return result;
    }

    private String formatColumnConstraint(final Cell input) {
      return format("C%d", input.getColumn());
    }

    private String formatForwardDiagonalConstraint(final Cell input) {
      int diagonalIndex = input.getRow() + input.getColumn() - 1;
      return format("A%d", diagonalIndex);
    }

    private String formatReverseDiagonalConstraint(final Cell input) {
      int diagonalIndex = (input.getRow() - 1) + (size - input.getColumn()) + 1;
      return format("B%d", diagonalIndex);
    }

    private String formatRowConstraint(final Cell input) {
      return format("R%d", input.getRow());
    }

    private String formatRowName(final Cell input) {
      return format("%d.%d:", input.getRow(), input.getColumn());
    }

    private String generate(final Cell input) {
      Stream<String> constraints = concat(primaryCellConstraintFormaters.stream(),
                                          secondaryCellConstraintFormaters.stream()).map(it -> it.apply(input));

      return concat(Stream.of(formatRowName(input)), constraints)
        .collect(joining(" "));
    }

    private String generateColumnNames() {
      return generateColumnNames(primaryCellConstraintFormaters) +
        PRIMARY_SECONDARY_SEPARATOR +
        generateColumnNames(secondaryCellConstraintFormaters);
    }

    private String generateColumnNames(final List<Function<Cell, String>> constraintFormatters) {
      return constraintFormatters.stream()
                                 .flatMap(formatter -> allCells.stream().map(formatter).sorted())
                                 .distinct()
                                 .collect(joining(" "));
    }

    private boolean isValid(final Cell input) {
      return validIndexRange.contains(input.getRow()) &&
        validIndexRange.contains(input.getColumn());
    }

    private Cell parseRowName(final String input) {
      String[] rowColumn = input.split(".");
      return new Cell(parseInt(rowColumn[0]),
                      parseInt(rowColumn[1]));

    }

    private List<Cell> reachableFrom(final Cell input) {
      List<Cell> result = newLinkedList();
      for (Function<Cell, Cell> direction : Lists.<Function<Cell, Cell>>newArrayList(Cell::up,
                                                                                     Cell::upRight,
                                                                                     Cell::right,
                                                                                     Cell::downRight,
                                                                                     Cell::down,
                                                                                     Cell::downLeft,
                                                                                     Cell::left,
                                                                                     Cell::upLeft)) {
        result.addAll(reachableFrom(input, direction));
      }

      return result;
    }

    private Collection<? extends Cell> reachableFrom(final Cell input, final Function<Cell, Cell> direction) {
      List<Cell> result = newLinkedList();
      Cell next = direction.apply(input);
      while (isValid(next)) {
        result.add(next);
        next = direction.apply(next);
      }
      return result;
    }
  }

  private static final String EMPTY_SYMBOL = ".";
  @Getter
  private final Set<Cell> existingValues;
  @Getter
  private final int size;

  public NQueen(final int size) {
    this(size, emptySet());
  }

  public NQueen(final int size, final Collection<Cell> existingValues) {
    this.size = size;
    this.existingValues = newHashSet(existingValues);

  }

  public static NQueen parse(final List<String> input) {
    int size = input.size();

    List<Cell> existingValues = newLinkedList();
    rangeClosed(1, size).forEach(rowIndex -> {
      String row = input.get(rowIndex - 1);
      checkArgument(row.length() == size, "Expected row %s to have size %s but it has size %s", rowIndex, size, row.length());

      rangeClosed(1, size).forEach(columnIndex -> {
        String value = String.valueOf(row.charAt(columnIndex - 1));
        if (!value.equals(EMPTY_SYMBOL)) {
          existingValues.add(new Cell(rowIndex, columnIndex));
        }
      });

    });

    return new NQueen(size, existingValues);
  }

  public List<NQueen> solve() {

    ConstraintsGenerator constraintsGenerator = new ConstraintsGenerator(size, existingValues);
    List<String> constraints = constraintsGenerator.generate();
    List<Solver.Solution> solutions = MatrixBuilder.withContraintsLines(constraints).solve(Solver.ColumnSelector.FIRST, false);

    return solutions.stream()
                    .map(it -> fromSolution(it, constraintsGenerator::parseRowName))
                    .collect(toList());
  }

  public Object toPrettyString() {
    StringBuilder result = new StringBuilder();
    result.append(getClass().getSimpleName())
          .append(": size ").append(size).append("x").append(size).append(lineSeparator());
    rangeClosed(1, size)
      .forEach(rowIndex -> {
        rangeClosed(1, size)
          .forEach(columnIndex -> {
            String value = existingValues.contains(new Cell(rowIndex, columnIndex)) ? "X" : EMPTY_SYMBOL;
            result.append(value);
          });
        result.append(lineSeparator());
      });

    return result.toString();
  }

  private NQueen fromSolution(final Solver.Solution solution, final Function<String, Cell> parser) {
    return new NQueen(size,
                      solution.getRowNames()
                              .stream()
                              .map(parser)
                              .collect(toList()));
  }
}
