package net.cell_lang;

import java.io.Reader;
import java.io.IOException;


final class ReaderCharStream implements CharStream {
  Reader reader;

  int line = 0;
  int col = 0;

  int buffChar = -2;

  public ReaderCharStream(Reader reader) {
    this.reader = reader;
  }

  public int read() {
    if (buffChar != -2) {
      int ch = buffChar;
      buffChar = -2;
      return ch;
    }

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

  public int peek() {
    if (buffChar == -2)
      try {
        buffChar = reader.read();
      }
      catch (IOException e) {
        throw new RuntimeException(e);
      }
    return buffChar;
  }

  public ParsingException fail() {
    throw new ParsingException(line, col);
  }
}
