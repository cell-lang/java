package net.cell_lang;


public interface CharStream {
  final byte EOF = -1;

  int read();
  void fail();
}
