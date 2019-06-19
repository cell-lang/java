package net.cell_lang;

import java.io.Reader;
import java.io.IOException;


final class ReaderCharStream implements CharStream {
  Reader reader;

  int line = 0;
  int col = 0;

  public ReaderCharStream(Reader reader) {
    this.reader = reader;
  }

  public int read() {
    try {
      int ch = reader.read();
      if (ch == '\n') {
        line++;
        col = 0;
      }
      else
        col++;
      return ch;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public ParsingException fail() {
    throw new ParsingException(line, col);
  }
}
