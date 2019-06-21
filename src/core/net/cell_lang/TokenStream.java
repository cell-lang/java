package net.cell_lang;


public interface TokenStream {
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

  //////////////////////////////////////////////////////////////////////////////

  boolean eof();
  ParsingException fail();
  void bookmark();
  ParsingException failAtBookmark();

  //////////////////////////////////////////////////////////////////////////////

  //## TODO: THESE ARE OBSOLETE, REMOVE WHEN POSSIBLE
  Token read();
  Token peek(int off);
}
