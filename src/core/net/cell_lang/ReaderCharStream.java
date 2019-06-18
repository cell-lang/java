package net.cell_lang;

import java.io.Reader;
import java.io.IOException;


class ReaderCharStream implements CharStream {
  Reader reader;

  public ReaderCharStream(Reader reader) {
    this.reader = reader;
  }

  public int read() {
    try {
      return reader.read();
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void fail() {
    throw new RuntimeException();
  }
}
