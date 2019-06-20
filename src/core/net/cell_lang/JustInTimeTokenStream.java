package net.cell_lang;


final class JustInTimeTokenStream implements TokenStream {
  final static int POOL_SIZE = 16;

  Tokenizer tokenizer;
  Token[] tokens = new Token[POOL_SIZE];
  int offset = 0;
  int count = 0;


  public JustInTimeTokenStream(ReaderCharStream chars) {
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
    while (count <= idx) {
      Token token = tokens[(offset + count++) % POOL_SIZE];
      if (!tokenizer.readToken(token))
        fail();
    }
    return tokens[(offset + idx) % POOL_SIZE];
  }

  public boolean eof() {
    return count == 0 && tokenizer.eof();
  }

  public ParsingException fail() {
    return tokenizer.failHere();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final boolean nextIs(TokenType type, int off) {
    Token token = peek(off);
    return token != null && token.type == type;
  }

  public final void bookmark() {
    //## IMPLEMENT IMPLEMENT IMPLEMENT
  }

  public final ParsingException failAtBookmark() {
    throw new RuntimeException(); //## IMPLEMENT IMPLEMENT IMPLEMENT
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final boolean nextIsCloseBracket() {
    if (count == 0) {
      tokenizer.consumeWhiteSpace();
      return tokenizer.nextIs(']');
    }
    else
      return nextIs(TokenType.CloseBracket, 0);
  }

  public final void consumeArrow() {
    if (count == 0)
      tokenizer.consume('-', '>');
    else
      consume(TokenType.Arrow);
  }

  public final void consumeCloseBracket() {
    if (count == 0)
      tokenizer.consume(']');
    else
      consume(TokenType.CloseBracket);
  }

  public final void consumeClosePar() {
    if (count == 0)
      tokenizer.consume(')');
    else
      consume(TokenType.ClosePar);
  }

  public final void consumeColon() {
    if (count == 0)
      tokenizer.consume(':');
    else
      consume(TokenType.Colon);
  }

  public final void consumeComma() {
    if (count == 0)
      tokenizer.consume(',');
    else
      consume(TokenType.Comma);
  }

  public final void consumeOpenBracket() {
    if (count == 0)
      tokenizer.consume('[');
    else
      consume(TokenType.OpenBracket);
  }

  public final void consumeOpenPar() {
    if (count == 0)
      tokenizer.consume('(');
    else
      consume(TokenType.OpenPar);
  }

  public final void consumeSemicolon() {
    if (count == 0)
      tokenizer.consume(';');
    else
      consume(TokenType.Semicolon);
  }

  public final boolean tryConsumingSemicolon() {
    if (count == 0)
      return tokenizer.tryConsuming(';');
    else
      return tryConsuming(TokenType.Semicolon);
  }

  public final boolean tryConsumingArrow() {
    if (count == 0)
      return tokenizer.tryConsuming('-', '>');
    else
      return tryConsuming(TokenType.Arrow);
  }

  public final boolean tryConsumingComma() {
    if (count == 0)
      return tokenizer.tryConsuming(',');
    else
      return tryConsuming(TokenType.Comma);
  }

  private void consume(TokenType type) {
    Token token = tokens[offset % POOL_SIZE];
    if (token.type != type)
      fail();
    offset++;
    count--;
  }

  private boolean tryConsuming(TokenType type) {
    Miscellanea._assert(count > 0);
    Token token = tokens[offset % POOL_SIZE];
    if (token.type == type) {
      offset++;
      count--;
      return true;
    }
    else
      return false;
  }
}
