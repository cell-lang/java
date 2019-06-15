package net.cell_lang;


public interface TokenStream {
  Token read();
  Token peek(int off);
  boolean eof();
  ParsingException fail();
}
