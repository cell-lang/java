package net.cell_lang;


class TokenArray implements TokenStream {
  Token[] tokens;
  int offset = 0;


  public TokenArray(Token[] tokens) {
    this.tokens = tokens;
  }

  public Token read() {
    if (offset < tokens.length)
      return tokens[offset++];
    else
      throw fail();
  }

  public Token peek(int delta) {
    int idx = offset + delta;
    return idx < tokens.length ? tokens[idx] : null;
  }

  public boolean eof() {
    return offset >= tokens.length;
  }

  public ParsingException fail() {
    int len = tokens.length;
    int textOffset = 0;
    if (offset < len) {
      textOffset = tokens[offset].offset;
      printState();
    }
    else if (len > 0) {
      Token lastToken = tokens[len - 1];
      textOffset = lastToken.offset + lastToken.length;
    }
    throw new ParsingException(textOffset);
  }

  public void printState() {
    System.out.print("\n\n\n");
    for (int i=0 ; i <= offset ; i++)
      System.out.println(tokens[i].toString());
    System.out.print("\n\n\n");
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean nextIs(TokenType type, int off) {
    throw Miscellanea.internalFail();
  }

  public void bookmark() {
    throw Miscellanea.internalFail();
  }

  public ParsingException failAtBookmark() {
    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean nextIsCloseBracket() {
    throw Miscellanea.internalFail();
  }

  public void consumeArrow() {
    throw Miscellanea.internalFail();
  }

  public void consumeCloseBracket() {
    throw Miscellanea.internalFail();
  }

  public void consumeClosePar() {
    throw Miscellanea.internalFail();
  }

  public void consumeColon() {
    throw Miscellanea.internalFail();
  }

  public void consumeComma() {
    throw Miscellanea.internalFail();
  }

  public void consumeOpenBracket() {
    throw Miscellanea.internalFail();
  }

  public void consumeOpenPar() {
    throw Miscellanea.internalFail();
  }

  public void consumeSemicolon() {
    throw Miscellanea.internalFail();
  }

  public boolean tryConsumingSemicolon() {
    throw Miscellanea.internalFail();
  }

  public boolean tryConsumingArrow() {
    throw Miscellanea.internalFail();
  }

  public boolean tryConsumingComma() {
    throw Miscellanea.internalFail();
  }
}
