package net.cell_lang;


class JustInTimeTokenStream implements TokenStream {
  Tokenizer tokenizer;
  Token[] buffer = new Token[16];
  int offset = 0;
  int count = 0;


  public JustInTimeTokenStream(CharStream chars) {
    tokenizer = new Tokenizer(chars);
  }

  public Token read() {
    if (count > 0) {
      count--;
      return buffer[offset++ % 16];
    }
    else
      return tokenizer.readToken();
  }

  public Token peek(int idx) {
    Miscellanea._assert(idx < 16);
    while (count <= idx)
      buffer[(offset + count++) % 16] = tokenizer.readToken();
    return buffer[(offset + idx) % 16];
  }

  public boolean eof() {
    return count == 0 && tokenizer.eof();
  }

  public ParsingException fail() {
    return tokenizer.failHere();
  }
}