package dancinglinks;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Lists.newLinkedList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.lang.System.lineSeparator;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Matrix {
  @Data
  @EqualsAndHashCode(of = "id")
  public static class Node {
    private Integer columnCount;
    private Node columnHeader;
    private Node down;
    private UUID id = UUID.randomUUID();
    private String label;
    private Node left;
    private Node right;
    private Node rowHeader;
    private Node up;

    public Node(final String label) {
      this();
      this.label = label;
    }

    private Node() {
      left = this;
      right = this;
      up = this;
      down = this;
      rowHeader = this;
      columnHeader = this;
      columnCount = 0;
    }

    public List<Node> getAll(Function<Node, Node> next) {
      List<Node> result = newLinkedList();
      Node node = next.apply(this);
      while (!node.equals(this)) {
        result.add(node);
        node = next.apply(node);
      }
      return result;
    }

    @Override
    public String toString() {
      return nonNull(label) ? label : rowHeader + ":" + columnHeader;
    }

    void insertDown(final Node value) {
      insert(value, Node::getUp, Node::setUp, Node::getDown, Node::setDown);
      value.setColumnHeader(getColumnHeader());
      getColumnHeader().incrementCount();
    }

    void insertLeft(final Node value) {
      insert(value, Node::getRight, Node::setRight, Node::getLeft, Node::setLeft);
      value.setRowHeader(getRowHeader());
    }

    void insertRight(final Node value) {
      insert(value, Node::getLeft, Node::setLeft, Node::getRight, Node::setRight);
      value.setRowHeader(getRowHeader());
    }

    void insertUp(final Node value) {
      insert(value, Node::getDown, Node::setDown, Node::getUp, Node::setUp);
      value.setColumnHeader(getColumnHeader());
      getColumnHeader().incrementCount();
    }

    boolean isHeader() {
      return Objects.equals(rowHeader, this) || Objects.equals(columnHeader, this);
    }

    void relinkLR() {
      getRight().setLeft(this);
      getLeft().setRight(this);
    }

    void relinkUD() {
      getDown().setUp(this);
      getUp().setDown(this);
      getColumnHeader().incrementCount();
    }

    void unlinkLR() {
      getLeft().setRight(getRight());
      getRight().setLeft(getLeft());
    }

    void unlinkUD() {
      getUp().setDown(getDown());
      getDown().setUp(getUp());
      getColumnHeader().decrementCount();
    }

    private void decrementCount() {
      columnCount--;
    }

    private void incrementCount() {
      columnCount++;
    }

    private void insert(final Node value,
                        final Function<Node, Node> getPrevious,
                        final BiConsumer<Node, Node> setPrevious,
                        final Function<Node, Node> getNext,
                        final BiConsumer<Node, Node> setNext) {
      Node thisNext = getNext.apply(this);
      setNext.accept(value, thisNext);
      setNext.accept(this, value);
      setPrevious.accept(thisNext, value);
      setPrevious.accept(value, this);
    }
  }

  private final Map<String, Node> allColumns = newLinkedHashMap();
  private final Map<String, Node> primaryColumns = newLinkedHashMap();
  private final Node primaryRoot;
  private final Map<String, Node> secondaryColumns = newLinkedHashMap();
  private final Node secondaryRoot;

  public Matrix(final List<String> primaryColumnNames, final List<String> secondaryColumnNames) {
    primaryRoot = new Node("--");
    secondaryRoot = new Node("|");

    primaryColumnNames.forEach(name -> {
      Node columnHeader = new Node(name);
      primaryRoot.getLeft().insertRight(columnHeader);
      primaryColumns.put(name, columnHeader);
    });
    secondaryColumnNames.forEach(name -> {
      Node columnHeader = new Node(name);
      secondaryRoot.getLeft().insertRight(columnHeader);
      secondaryColumns.put(name, columnHeader);
    });

    allColumns.putAll(primaryColumns);
    allColumns.putAll(secondaryColumns);
  }

  public void addRow(final String rowName, final List<String> columnNames) {
    Node rowHeader = new Node(rowName);
    primaryRoot.getUp().insertDown(rowHeader);
    columnNames.forEach(columnName -> {
      Node columnHeader = allColumns.get(columnName);
      checkState(columnHeader != null, "Column %s does not exist", columnName);
      Node node = new Node();
      rowHeader.getLeft().insertRight(node);
      columnHeader.getUp().insertDown(node);
    });
  }

  public final List<Node> getPrimaryColumns() {
    return newLinkedList(primaryColumns.values());
  }

  public final List<Node> getSecondaryColumns() {
    return newLinkedList(secondaryColumns.values());
  }

  public boolean isEmpty() {
    return getUncoveredPrimaryColumns().isEmpty();
  }

  public List<Solver.Solution> solve(final Solver.ColumnSelector columnSelector, final boolean findOneSolution) {
    return new Solver(this, columnSelector, findOneSolution).solve();
  }

  public List<Solver.Solution> solve() {
    return solve(Solver.ColumnSelector.SMALLER, false);
  }

  public String toString() {
    StringBuilder result = new StringBuilder();
    String separator = " ";
    result.append(primaryRoot).append(separator);
    Collection<Node> columnHeaders = allColumns.values();
    result.append(columnHeaders.stream().map(Objects::toString).collect(joining(separator))).append(lineSeparator());
    for (Node rowHeader : getUncoveredRows()) {
      result.append(rowHeader).append(separator);
      Node node = rowHeader.getRight();
      for (Node columnHeader : columnHeaders) {
        if (node.getColumnHeader().equals(columnHeader)) {
          result.append(1);
          node = node.getRight();
        } else {
          result.append(0);
        }
        result.append(separator);
      }
      result.append(lineSeparator());
    }
    return result.toString();
  }

  void coverColumn(final Node input) {
    checkArgument(getUncoveredColumns().contains(input), "Column %s is already covered", input);
    input.unlinkLR();
    input.getAll(Node::getDown).forEach(this::coverRow);
  }

  List<Node> getUncoveredColumns() {
    List<Node> result = newLinkedList();
    result.addAll(getUncoveredPrimaryColumns());
    result.addAll(getUncoveredSecondaryColumns());
    return result;
  }

  List<Node> getUncoveredNodes() {
    return getUncoveredColumns().stream().flatMap(column -> column.getAll(Node::getDown).stream()).collect(toList());
  }

  List<Node> getUncoveredPrimaryColumns() {
    return primaryRoot.getAll(Node::getRight);
  }

  List<Node> getUncoveredRows() {
    return primaryRoot.getAll(Node::getDown);
  }

  List<Node> getUncoveredSecondaryColumns() {
    return secondaryRoot.getAll(Node::getRight);
  }

  void uncoverColumn(final Node input) {
    checkArgument(!getUncoveredColumns().contains(input), "Column %s is not covered", input);
    input.getAll(Node::getUp).forEach(this::uncoverRow);
    input.relinkLR();
  }

  private void coverRow(final Node input) {
    input.getAll(Node::getRight).forEach(Node::unlinkUD);
  }

  private void uncoverRow(final Node input) {
    input.getAll(Node::getLeft).forEach(Node::relinkUD);
  }
}
