package net.cell_lang;


class ParsingException extends RuntimeException {
  public int line;
  public int col;

  public ParsingException(int line, int col) {
    this.line = line;
    this.col = col;
  }

  public String toString() {
    return String.format("Parsing error at line %d, column %d", line, col);
  }
}
