package dancinglinks;

import static java.util.stream.Collectors.toList;

import com.google.common.base.MoreObjects;
import lombok.Value;

import java.util.List;

@Value
public class Solution {
  private final List<List<Matrix.Node>> nodes;

  public List<List<String>> getCoveredColumnNames() {
    return nodes.stream()
                .map(rowNodes ->
                       rowNodes.stream()
                               .map(Matrix.Node::getColumnHeader)
                               .map(Matrix.Node::getLabel)
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
