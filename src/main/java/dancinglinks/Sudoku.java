package dancinglinks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Sets.union;
import static com.google.common.math.DoubleMath.isMathematicalInteger;
import static java.lang.Math.sqrt;
import static java.lang.String.format;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;
import static java.util.stream.Stream.concat;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@EqualsAndHashCode(of = {"size", "alphabeth", "existingValues"})
public class Sudoku implements Solvable<Sudoku> {
  @Value
  @AllArgsConstructor
  public static class Cell implements Comparable<Cell> {
    @NonNull
    final Coordinates coordinates;

    @NonNull
    final String value;

    public Cell(final Integer row, final Integer column, final String value) {
      this(new Coordinates(row, column), value);
    }

    @Override
    public int compareTo(final Cell other) {
      checkNotNull(other);
      return ComparisonChain.start()
                            .compare(coordinates, other.getCoordinates())
                            .compare(value, other.getValue())
                            .result();
    }

    public Integer getColumn() {
      return coordinates.getColumn();
    }

    public Integer getRow() {
      return coordinates.getRow();
    }
  }

  public static class ConstraintsGenerator {

    private final List<Cell> allPossibleCells;
    private final List<Function<Cell, String>> cellConstraintFormaters;
    private final Map<Coordinates, Cell> existingValues;
    private final int size;

    public ConstraintsGenerator(final int size, final Collection<Cell> existingValues, List<String> alphabeth) {
      checkArgument(alphabeth.size() >= size,
                    "Current alpabeth [%s] should contain at least %s symbols", alphabeth, size);
      this.size = size;
      this.existingValues = Maps.uniqueIndex(existingValues, Cell::getCoordinates);

      cellConstraintFormaters = newArrayList(this::formatCellConstraint,
                                             this::formatRowConstraint,
                                             this::formatColumnConstraint);
      if (isPerfectSquare(size)) {
        cellConstraintFormaters.add(this::formatSectorConstraint);
      }

      allPossibleCells = newLinkedList();

      rangeClosed(1, size)
        .forEach(rowIndex ->
                   rangeClosed(1, size)
                     .forEach(columnIndex ->
                                rangeClosed(1, size)
                                  .forEach(number ->
                                             allPossibleCells.add(new Cell(rowIndex, columnIndex, alphabeth.get(number - 1))))));
    }

    private static boolean isPerfectSquare(final int input) {
      return isMathematicalInteger(sqrt(input));
    }

    public Cell parseRowName(final String name) {
      Pattern compile = Pattern.compile("r(\\d+)c(\\d+)#(\\w+)");
      Matcher matcher = compile.matcher(name);
      checkState(matcher.matches(), "Unable to parse row name '%s'", name);
      String value = matcher.group(3);
      return new Cell(Integer.parseInt(matcher.group(1)),
                      Integer.parseInt(matcher.group(2)),
                      value);
    }

    List<String> generate() {
      List<String> result = newLinkedList();

      result.add(generateColumnNames());

      Stream<Cell> existingCells = existingValues.values().stream();
      Stream<Cell> emptyCells = allPossibleCells.stream()
                                                .filter(cell -> !existingValues.containsKey(cell.getCoordinates()));
      concat(existingCells, emptyCells)
        .sorted()
        .map(this::generate)
        .forEach(result::add);

      return result;
    }

    private String formatCellConstraint(final Cell cell) {
      return format("X%d.%d", cell.getRow(), cell.getColumn());
    }

    private String formatColumnConstraint(final Cell cell) {
      return format("C%d#%s", cell.getColumn(), cell.getValue());
    }

    private String formatRowConstraint(final Cell cell) {
      return format("R%d#%s", cell.getRow(), cell.getValue());
    }

    private String formatRowName(final Cell cell) {
      return format("r%dc%d#%s", cell.getRow(), cell.getColumn(), cell.getValue()) + ':';
    }

    private String formatSectorConstraint(final Cell cell) {
      checkState(isPerfectSquare(size), "Can not generate sector constraints when size (%d) is not a perfect square", this.size);

      int sectorSize = (int) sqrt(size);
      int sectorRow = ((cell.getRow() - 1) / sectorSize) * sectorSize;
      int sectorColumn = (cell.getColumn() - 1) / sectorSize;
      int sectorNumber = sectorRow + sectorColumn + 1;

      return format("S%d#%s", sectorNumber, cell.getValue());
    }

    private String generate(final Cell cell) {
      Stream<String> cellConstraints = cellConstraintFormaters.stream().map(it -> it.apply(cell));

      return concat(Stream.of(formatRowName(cell)),
                    cellConstraints).collect(joining(" "));

    }

    private String generateColumnNames() {
      return cellConstraintFormaters.stream()
                                    .flatMap(formatter ->
                                               allPossibleCells.stream()
                                                               .map(formatter))
                                    .distinct()
                                    .collect(joining(" "));
    }

  }

  @Value
  public static class Coordinates implements Comparable<Coordinates> {
    final Integer column;
    final Integer row;

    public Coordinates(final Integer row, final Integer column) {
      this.row = row;
      this.column = column;
    }

    @Override
    public int compareTo(final Coordinates other) {
      checkNotNull(other);

      return ComparisonChain.start()
                            .compare(row, other.getRow())
                            .compare(column, other.getColumn())
                            .result();
    }
  }

  private static final List<String> DEFAULT_ALPHABETH = Arrays.asList("123456789ABCDEFGHIJKLMNOPQRSTUVZ".split(""));
  private static final String EMPTY_SYMBOL = ".";
  @Getter
  private final List<String> alphabeth;
  private final Map<Coordinates, Cell> existingValues;
  @Getter
  private final int size;

  public Sudoku(final int size, List<Cell> existingValues) {
    this(size, existingValues, DEFAULT_ALPHABETH);
  }

  public Sudoku(final int size, List<Cell> existingValues, Collection<String> alphabeth) {
    checkArgument(alphabeth.size() >= size,
                  "Current alpabeth [%s] should contain at least %s symbols", alphabeth, size);
    this.size = size;
    this.alphabeth = alphabeth.stream()
                              .limit(size)
                              .sorted()
                              .collect(toList());

    this.existingValues = Maps.uniqueIndex(existingValues, Cell::getCoordinates);
  }

  public static Sudoku parse(final List<String> input) {
    int size = input.size();

    List<Cell> cells = newLinkedList();
    rangeClosed(1, size).forEach(rowIndex -> {
      String row = input.get(rowIndex - 1);
      checkArgument(row.length() == size, "Expected row %s to have size %s but it has size %s", rowIndex, size, row.length());

      rangeClosed(1, size).forEach(columnIndex -> {
        String value = String.valueOf(row.charAt(columnIndex - 1));
        if (!value.equals(EMPTY_SYMBOL)) {
          cells.add(new Cell(rowIndex, columnIndex, value));
        }
      });

    });

    return new Sudoku(size, cells, inferAlphabet(size, input));
  }

  private static Collection<String> inferAlphabet(final int size, final List<String> input) {
    Set<String> seen = input.stream()
                            .flatMap(line -> Arrays.stream(line.split("")))
                            .filter(symbol -> !symbol.equals(EMPTY_SYMBOL))
                            .collect(toSet());
    checkArgument(seen.size() <= size, "Too many symbols %s for size %s", seen, size);

    int missingSymbolsCount = size - seen.size();

    Set<String> missing = DEFAULT_ALPHABETH.stream()
                                           .filter(it -> !seen.contains(it))
                                           .limit(missingSymbolsCount)
                                           .collect(toSet());
    Set<String> result = union(seen, missing);
    checkState(result.size() == size);
    return result;
  }

  public List<Sudoku> solve(final Solver.Options options) {
    ConstraintsGenerator constraintsGenerator = new ConstraintsGenerator(size, existingValues.values(), alphabeth);
    Function<Solution, Sudoku> solutionParser = solution -> new Sudoku(size,
                                                                       solution.getRowNames()
                                                                               .stream()
                                                                               .map(constraintsGenerator::parseRowName)
                                                                               .collect(toList()),
                                                                       alphabeth);
    return MatrixBuilder.withConstraintsLines(constraintsGenerator.generate())
                        .solve(options)
                        .stream()
                        .map(solutionParser)
                        .collect(toList());
  }

  public Object toPrettyString() {
    return getClass().getSimpleName() + ": size " + size + "x" + size + lineSeparator() +
      rangeClosed(1, size)
        .mapToObj(rowIndex -> getRow(rowIndex).map(cell -> cell.map(Cell::getValue)
                                                               .map(Object::toString)
                                                               .orElse(EMPTY_SYMBOL))
                                              .collect(joining("")))
        .collect(joining(lineSeparator()));
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
                      .add("alphabeth", alphabeth)
                      .add("existingValues", existingValues)
                      .add("size", size)
                      .add("matrix", toPrettyString())
                      .toString();
  }

  private Stream<Optional<Cell>> getRow(final int rowIndex) {
    return rangeClosed(1, size).mapToObj(
      columnIndex -> Optional.ofNullable(existingValues.get(new Coordinates(rowIndex, columnIndex))));

  }

}
