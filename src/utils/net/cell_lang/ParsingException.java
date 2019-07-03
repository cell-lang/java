package net.cell_lang;


class ParsingException extends RuntimeException {
  public int line;
  public int col;
  public int errorOffset;

  public ParsingException(int line, int col) {
    this.line = line;
    this.col = col;
    this.errorOffset = -1;
  }

  public ParsingException(int errorOffset) {
    this.line = -1;
    this.col = -1;
    this.errorOffset = errorOffset;
  }

  public String toString() {
    if (line != -1)
      return String.format("Parsing error at line %d, column %d", line, col);
    else
      return String.format("Parsing error at errorOffset %d", errorOffset);
  }
}
