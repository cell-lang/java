package net.cell_lang;


public interface CharStream {
  final byte EOF = -1;

  int read();
  int peek(int idx);

  int line();
  int column();

  ParsingException fail();
}
