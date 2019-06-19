package net.cell_lang;


class Token {
  public int offset;
  public int length;
  public TokenType type;
  public Obj objValue;
  public long longValue;
  public double doubleValue;

  public Object value; //## DEPRECATED, REMOVE

  public Token() {
    offset = -1;
    length = -1;
    type = null;
    objValue = null;
  }

  //## DEPRECATED, REMOVE
  public Token(int offset, int length, TokenType type, Object value) {
    this.offset = offset;
    this.length = length;
    this.type = type;
    this.value = value;
  }

  //## DEPRECATED, REMOVE
  public Token(int offset, int length, TokenType type) {
    this(offset, length, type, null);
  }

  //## DEPRECATED, REMOVE
  public Token(int offset, int length, TokenType type, long value) {
    this(offset, length, type, Long.valueOf(value));
  }

  //## DEPRECATED, REMOVE
  public Token(int offset, int length, TokenType type, double value) {
    this(offset, length, type, Double.valueOf(value));
  }

  public void set(TokenType type) {
    this.type = type;
  }

  public void set(long value) {
    type = TokenType.Int;
    longValue = value;
  }

  public void set(double value) {
    type = TokenType.Float;
    doubleValue = value;
  }

  public void set(SymbObj obj) {
    type = TokenType.Symbol;
    objValue = obj;
  }

  public void set(TaggedObj str) {
    type = TokenType.String;
    objValue = str;
  }

  // public String toString() {
  //   // return String.format("%4d  %-12s  %-12s", offset, type, value != null ? value : "");
  // }
};
