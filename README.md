# dancinglinks

This is my implementation of [Dancing Links](https://en.wikipedia.org/wiki/Dancing_Links) data structure and [Algorithm_X](https://en.wikipedia.org/wiki/Knuth%27s_Algorithm_X):

> In computer science, dancing links is a technique for reverting the operation of deleting a node from a circular doubly linked list. It is particularly useful for efficiently implementing backtracking algorithms, such as Donald Knuth's Algorithm X for the exact cover problem.[1] Algorithm X is a recursive, nondeterministic, depth-first, backtracking algorithm that finds all solutions to the exact cover problem.

[This video](https://www.youtube.com/watch?v=_cR9zDlvP88&t) has a great explanation of the algorithm and various use cases.

### Sudoku

```java
Sudoku.parse(
   "..9748...",
   "7........",
   ".2.1.9...",
   "..7...24.",
   ".64.1.59.",
   ".98...3..",
   "...8.3.2.",
   "........6",
   "...2759.."
).solve()
 .toPrettyString();
```

See [Sudoku Tests](https://github.com/maurobellati/dancinglinks/blob/master/src/test/java/dancinglinks/SudokuTest.java) for more examples.


### N-Queen

```java
NQueen.parse(
  ".X..",
 "....",
 "....",
 "...."
).solve()
 .toPrettyString();
```

See [NQueen Tests](https://github.com/maurobellati/dancinglinks/blob/master/src/test/java/dancinglinks/NQueenTest.java) for more examples.
