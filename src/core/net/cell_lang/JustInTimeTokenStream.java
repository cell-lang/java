package net.cell_lang;


class JustInTimeTokenStream implements TokenStream {
  final static int POOL_SIZE = 16;

  Tokenizer tokenizer;
  Token[] tokens = new Token[POOL_SIZE];
  int offset = 0;
  int count = 0;


  public JustInTimeTokenStream(CharStream chars) {
    tokenizer = new Tokenizer(chars);
    for (int i=0 ; i < tokens.length ; i++)
      tokens[i] = new Token();
  }

  public Token read() {
    int idx = offset++ % POOL_SIZE;
    if (count > 0) {
      count--;
      return tokens[idx];
    }
    else {
      Token token = tokens[idx];
      tokenizer.readToken(token);
      return token;
    }
  }

  public Token peek(int idx) {
    Miscellanea._assert(idx < POOL_SIZE);
    while (count <= idx)
      tokenizer.readToken(tokens[(offset + count++) % POOL_SIZE]);
    return tokens[(offset + idx) % POOL_SIZE];
  }

  public boolean eof() {
    return count == 0 && tokenizer.eof();
  }

  public ParsingException fail() {
    return tokenizer.failHere();
  }
}