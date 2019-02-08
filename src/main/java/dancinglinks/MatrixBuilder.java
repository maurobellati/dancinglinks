
package dancinglinks;

import static com.google.common.base.CharMatcher.whitespace;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newLinkedList;
import static java.lang.Integer.parseInt;
import static java.util.stream.IntStream.range;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.function.Function;

public class MatrixBuilder {
    private static final Splitter SPLITTER_ON_WHITESPACE = Splitter.on(whitespace()).omitEmptyStrings().trimResults();

    public static Matrix create(final List<String> lines, final Function<String, List<String>> lineToRowValuesMapper) {

        Matrix result = new Matrix(getColumnNames(lines.get(0)));

        range(1, lines.size())
                .forEach(rowIndex -> {
                    final String line = lines.get(rowIndex);
                    result.addRow(getRowName(rowIndex, line), lineToRowValuesMapper.apply(line));
                });
        return result;
    }

    public static Matrix fromBooleanMatrix(final List<String> lines) {
        checkArgument(lines.size() > 1);
        List<String> columnNames = getColumnNames(lines.get(0));
        Function<String, List<String>> lineToRowValuesMapper = line -> convertBooleansToColumnNames(getRowValues(line), columnNames);

        return create(lines, lineToRowValuesMapper);
    }

    public static Matrix withContraintsLines(final List<String> lines) {
        checkArgument(lines.size() > 1);
        return create(lines, MatrixBuilder::getRowValues);
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

    private static List<String> getColumnNames(final String input) {
        return SPLITTER_ON_WHITESPACE.splitToList(input);
    }

    private static String getRowName(final int rowIndex, final String line) {
        int i = line.indexOf(':');
        return i < 0 ? ("R" + rowIndex) : line.substring(0, i);
    }

    private static List<String> getRowValues(final String line) {
        String afterOptionalColon = line.substring(Math.max(line.indexOf(':') + 1, 0));
        return SPLITTER_ON_WHITESPACE.splitToList(afterOptionalColon);
    }

}

