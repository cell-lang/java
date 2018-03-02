package net.cell_lang;


class ParsingException extends RuntimeException {
  public int errorOffset;

  public ParsingException(int errorOffset) {
    this.errorOffset = errorOffset;
  }
}
