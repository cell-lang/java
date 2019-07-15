package net.cell_lang;


interface TokenStream {
  long readLong();
  double readDouble();
  int readSymbol();
  Obj readString();

  int tryReadingLabel();

  TokenType peekType();

  boolean nextIs(char ch);

  void consume(char ch);
  void consume(char ch1, char ch2);

  boolean tryConsuming(char ch);
  boolean tryConsuming(char ch1, char ch2);

  boolean eof();

  int line();
  int column();

  ParsingException fail();
}
