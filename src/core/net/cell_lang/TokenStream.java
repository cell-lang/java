package net.cell_lang;


public interface TokenStream {
  Token read();
  Token peek(int off); //## TODO: THIS IS OBSOLETE, REMOVE WHEN POSSIBLE
  boolean eof();
  ParsingException fail();

  //////////////////////////////////////////////////////////////////////////////

  void bookmark();
  ParsingException failAtBookmark();

  //////////////////////////////////////////////////////////////////////////////

  long readLong();
  double readDouble();
  int readSymbol();

  int tryReadingLabel();

  TokenType peekType();

  boolean nextIs(char ch);

  void consume(char ch);
  void consume(char ch1, char ch2);

  boolean tryConsuming(char ch);
  boolean tryConsuming(char ch1, char ch2);
}
