package dancinglinks;

import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

import com.google.common.base.MoreObjects;
import dancinglinks.Matrix.Node;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collection;
import java.util.List;
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
  public static class Solution {
    private final List<List<Node>> nodes;

    public List<List<String>> getCoveredColumnNames() {
      return nodes.stream()
                  .map(rowNodes ->
                         rowNodes.stream()
                                 .map(Node::getColumnHeader)
                                 .map(Node::getLabel)
                                 .collect(toList()))
                  .collect(toList());
    }

    public List<String> getRowNames() {
      return nodes.stream()
                  .map(list -> list.get(0).getRowHeader().getLabel())
                  .collect(toList());
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this)
                        .add("nodes", nodes)
                        .add("rowNames", getRowNames())
                        .add("coveredColumnNames", getCoveredColumnNames())
                        .toString();
    }
  }

  private final ColumnSelector columnSelector;
  private final boolean findOneSolution;
  private final Matrix matrix;
  private final List<Solution> solutions = newArrayList();

  public Solver(final Matrix matrix,
                final ColumnSelector columnSelector,
                final boolean findOneSolution) {
    this.matrix = matrix;
    this.columnSelector = columnSelector;
    this.findOneSolution = findOneSolution;
  }

  public List<Solution> solve() {
    solutions.clear();
    search(newArrayList());
    return copyOf(solutions);
  }

  private Solver.Solution getSolution(final List<Node> progress) {

    return new Solver.Solution(progress.stream()
                                       .map(node -> concat(Stream.of(node),
                                                           node.getAll(Node::getRight).stream())
                                         .filter(it -> !it.isHeader())
                                         .collect(toList()))
                                       .collect(toList()));
  }

  private void log(final String message, final Object... args) {
    //        System.out.printf(message + "%n", args);
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

    Node column = columnSelector.select(matrix.getUncoveredColumns());
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
      if (found && findOneSolution) {
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
