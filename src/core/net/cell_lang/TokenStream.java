package net.cell_lang;


public interface TokenStream {
  Token read();
  Token peek(int off);
  boolean eof();
  ParsingException fail();

  //////////////////////////////////////////////////////////////////////////////

  boolean nextIs(TokenType type, int off);
  void bookmark();
  ParsingException failAtBookmark();

  //////////////////////////////////////////////////////////////////////////////

  boolean nextIsCloseBracket();

  void consumeArrow();
  void consumeCloseBracket();
  void consumeClosePar();
  void consumeColon();
  void consumeComma();
  void consumeOpenBracket();
  void consumeOpenPar();
  void consumeSemicolon();

  boolean tryConsumingSemicolon();
  boolean tryConsumingArrow();
  boolean tryConsumingComma();
}
