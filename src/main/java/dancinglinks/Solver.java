package dancinglinks;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import dancinglinks.Matrix.Node;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Solver {

  @AllArgsConstructor
  public enum ColumnSelector {
    FIRST(columns -> columns.stream().findFirst()
                            .orElseThrow(IllegalStateException::new)),
    SMALLER(columns -> columns.stream().min(comparing(Node::getColumnCount))
                              .orElseThrow(IllegalStateException::new));

    private final Function<Collection<Node>, Node> selector;

    public Node select(final Collection<Node> nodes) {
      return selector.apply(nodes);
    }

  }

  @Value
  @Builder
  public static class Options {

    @Builder.Default
    @NonNull
    final ColumnSelector columnSelector = ColumnSelector.SMALLER;

    final Integer limit;

    final PrintStream logger;

    public static Options withLimit(final int limit) {
      return builder().limit(limit).build();
    }

    public Optional<Integer> getLimit() {
      return Optional.ofNullable(limit);
    }

    public Optional<PrintStream> getLogger() {
      return Optional.ofNullable(logger);
    }
  }

  private final Matrix matrix;
  private final Options options;
  private final List<Solution> solutions = newArrayList();

  public Solver(final Matrix matrix,
                final Options options) {
    this.matrix = matrix;
    this.options = options;
  }

  public List<Solution> solve() {
    log("Solving with %s", options);
    solutions.clear();
    search(newArrayList());
    return copyOf(solutions);
  }

  private Solution getSolution(final List<Node> progress) {

    return new Solution(progress.stream()
                                .map(node -> concat(Stream.of(node),
                                                    node.getAll(Node::getRight).stream())
                                  .filter(it -> !it.isHeader())
                                  .collect(toList()))
                                .collect(toList()));
  }

  private void log(final String message, final Object... args) {
    options.getLogger()
           .ifPresent(out -> out.printf(message + "%n", args));
  }

  private void saveSolution(final List<Node> progress) {
    solutions.add(getSolution(progress));
  }

  private boolean search(final List<Node> progress) {
    int level = progress.size();
    log("%s: searching level %s", level, level);
    if (matrix.isEmpty()) {
      log("%s: *** found solution: %s", level, progress);
      saveSolution(progress);
      return true;
    }
    log("%s: available columns: %s | %s", level, matrix.getUncoveredPrimaryColumns(), matrix.getUncoveredSecondaryColumns());

    Node column = options.getColumnSelector().select(matrix.getUncoveredPrimaryColumns());
    matrix.coverColumn(column);
    log("%s: choosed and covered column %s", level, column);

    for (Node rowNode : column.getAll(Node::getDown)) {
      progress.add(rowNode);
      log("%s: adding %s to progress", level, rowNode);

      for (Node node : rowNode.getAll(Node::getRight)) {
        if (!node.equals(rowNode.getRowHeader())) {
          matrix.coverColumn(node.getColumnHeader());
        }
      }

      boolean found = search(progress);
      Boolean reachedSolutionLimit = options.getLimit()
                                            .map(it -> it <= solutions.size())
                                            .orElse(false);
      if (found && reachedSolutionLimit) {
        return true;
      }

      progress.remove(rowNode);
      log("%s: removing %s from progress", level, rowNode);

      for (Node node : rowNode.getAll(Node::getLeft)) {
        if (!node.equals(rowNode.getRowHeader())) {
          matrix.uncoverColumn(node.getColumnHeader());
        }
      }
    }

    matrix.uncoverColumn(column);
    log("%s: uncovering column %s", level, column);
    return false;
  }

}
