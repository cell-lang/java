package net.cell_lang;

import java.io.Reader;
import java.io.IOException;


final class ReaderCharStream implements CharStream {
  Reader reader;

  int line = 0;
  int col = 0;

  final static int BUFFER_SIZE = 4096;

  char[] buffer = new char[BUFFER_SIZE];
  int offset = 0;
  int count = 0;

  public ReaderCharStream(Reader reader) {
    this.reader = reader;
  }

  public final int read() {
    if (count == 0) {
      fill();
      if (count == 0)
        return EOF;
    }

    char ch = buffer[offset++];
    count--;

    if (ch == '\n') {
      line++;
      col = 0;
    }
    else
      col++;

    return ch;
  }

  public final int peek(int idx) {
    if (idx >= count) {
      fill();
      if (idx >= count)
        return EOF;
    }
    return buffer[offset + idx];
  }

  public final int line() {
    return line;
  }

  public final int column() {
    return col;
  }

  public ParsingException fail() {
    throw new ParsingException(line + 1, col + 1);
  }

  //////////////////////////////////////////////////////////////////////////////

  private void fill() {
    try {
      if (count == 0) {
        offset = 0;
        count = reader.read(buffer, 0, BUFFER_SIZE);
        if (count == -1)
          count = 0;
      }
      else {
        if (offset != 0)
          for (int i=0 ; i < count ; i++)
            buffer[i] = buffer[offset+i];
        offset = 0;
        int read = reader.read(buffer, count, BUFFER_SIZE - count);
        if (read != -1)
          count += read;
      }
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
