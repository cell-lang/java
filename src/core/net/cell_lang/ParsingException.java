package net.cell_lang;


class ParsingException extends RuntimeException {
  public int errorOffset;

  public ParsingException(int errorOffset) {
    this.errorOffset = errorOffset;
  }

  public String toString() {
    return String.format("Parsing error at byte %d", errorOffset);
  }
}
