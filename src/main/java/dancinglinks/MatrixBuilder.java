package dancinglinks;

import static com.google.common.base.CharMatcher.whitespace;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class MatrixBuilder {
  private static final Splitter SPLITTER_ON_WHITESPACE = Splitter.on(whitespace()).omitEmptyStrings().trimResults();
  private static final String PRIMARY_SECONDARY_COLUMNS_SEPARATOR = "|";
  private static final String NAME_VALUE_ROW_SEPARATOR = ":";

  public static Matrix create(final List<String> lines, final Function<String, List<String>> lineToRowValuesMapper) {
    String header = lines.get(0);
    Matrix result = new Matrix(getPrimaryColumnNames(header), getSecondaryColumnNames(header));
    range(1, lines.size()).forEach(rowIndex -> {
      final String line = lines.get(rowIndex);
      result.addRow(getRowName(rowIndex, line), lineToRowValuesMapper.apply(line));
    });
    return result;
  }

  public static Matrix fromBooleanMatrix(final List<String> lines) {
    checkArgument(lines.size() > 1);
    List<String> columnNames = getAllColumnNames(lines.get(0));
    Function<String, List<String>> lineToRowValuesMapper = line -> convertBooleansToColumnNames(getRowValues(line), columnNames);
    return create(lines, lineToRowValuesMapper);
  }

  public static Matrix withConstraintsLines(final List<String> lines) {
    checkArgument(lines.size() > 1);
    return create(lines, MatrixBuilder::getRowValues);
  }

  private static String afterOptionalToken(final String input, final String token) {
    return afterToken(input, token).orElse(input);
  }

  private static Optional<String> afterToken(final String input, final String token) {
    int i = input.indexOf(token);
    return i < 0 ? Optional.empty() : Optional.of(input.substring(i + 1));
  }

  private static String beforeOptionalToken(final String input, final String token) {
    return beforeToken(input, token).orElse(input);
  }

  private static Optional<String> beforeToken(final String input, final String token) {
    int i = input.indexOf(token);
    return i < 0 ? Optional.empty() : Optional.of(input.substring(0, i));
  }

  private static List<String> convertBooleansToColumnNames(final List<String> booleansAsString, final List<String> columnNames) {
    checkArgument(columnNames.size() == booleansAsString.size(),
                  "Row %s is expected to have size equals to %s but it is %s", booleansAsString, columnNames.size(), booleansAsString.size());
    List<String> result = newLinkedList();
    for (int i = 0; i < booleansAsString.size(); i++) {
      if (parseInt(booleansAsString.get(i)) > 0) {
        result.add(columnNames.get(i));
      }
    }
    return result;
  }

  private static List<String> getAllColumnNames(final String header) {
    return Stream.concat(getPrimaryColumnNames(header).stream(),
                         getSecondaryColumnNames(header).stream())
                 .collect(toList());
  }

  private static List<String> getPrimaryColumnNames(final String input) {
    return splitOnSpace(beforeOptionalToken(input, PRIMARY_SECONDARY_COLUMNS_SEPARATOR));
  }

  private static String getRowName(final int rowIndex, final String line) {
    return beforeToken(line, NAME_VALUE_ROW_SEPARATOR).orElseGet(() -> "R" + rowIndex);
  }

  private static List<String> getRowValues(final String line) {
    return splitOnSpace(afterOptionalToken(line, NAME_VALUE_ROW_SEPARATOR));
  }

  private static List<String> getSecondaryColumnNames(final String input) {
    return afterToken(input, PRIMARY_SECONDARY_COLUMNS_SEPARATOR).map(MatrixBuilder::splitOnSpace).orElse(emptyList());
  }

  private static List<String> splitOnSpace(final String input) {
    return SPLITTER_ON_WHITESPACE.splitToList(input);
  }

}
