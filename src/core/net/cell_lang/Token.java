package net.cell_lang;


class Token {
  public int offset;
  public int length;
  public TokenType type;
  public Object value;

  public Token(int offset, int length, TokenType type, Object value) {
    this.offset = offset;
    this.length = length;
    this.type = type;
    this.value = value;
  }

  public Token(int offset, int length, TokenType type) {
    this(offset, length, type, null);
  }

  public Token(int offset, int length, TokenType type, long value) {
    this(offset, length, type, Long.valueOf(value));
  }

  public Token(int offset, int length, TokenType type, double value) {
    this(offset, length, type, Double.valueOf(value));
  }
};
