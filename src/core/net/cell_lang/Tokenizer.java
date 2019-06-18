package net.cell_lang;


class CharStreamProcessor {
  CharStream src;
  int currChar;
  int offset = 0;

  protected CharStreamProcessor(CharStream src) {
    this.src = src;
    currChar = src.read();
  }

  protected final int read() {
    failHereIf(currChar == CharStream.EOF);
    int result = currChar;
    currChar = src.read();
    offset++;
    return result;
  }

  protected final int readHex() {
    failHereIf(!nextIsHex());
    return read();
  }

  protected final long peek() {
    failHereIf(currChar == CharStream.EOF);
    return currChar;
  }

  protected final int offset() {
    return offset;
  }

  protected final boolean eof() {
    return currChar == CharStream.EOF;
  }

  protected final boolean consumeNextIfItIs(char ch) {
    boolean res = nextIs(ch);
    if (res)
      read();
    return res;
  }

  protected void consumeWhiteSpace() {
    while (isWhiteSpace(currChar))
      read();
  }

  protected final boolean nextIs(char ch) {
    return currChar == ch;
  }

  protected final boolean nextIsDigit() {
    return isDigit(currChar);
  }

  protected final boolean nextIsHex() {
    return isHex(currChar);
  }

  protected final boolean nextIsLower() {
    return isLower(currChar);
  }

  protected final boolean nextIsAlphaNum() {
    return isAlphaNum(currChar);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final void checkNextIs(char ch) {
    failHereIf(currChar != ch);
  }

  protected final void checkNextIsDigit() {
    failHereIf(!isDigit(currChar));
  }

  protected final void checkNextIsHex() {
    failHereIf(!isHex(currChar));
  }

  protected final void checkNextIsAlphaNum() {
    failHereIf(!isAlphaNum(currChar));
  }

  protected final void checkNextIsPrintable() {
    failHereIf(!isPrintable(currChar));
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final void failHereIf(boolean cond) {
    if (cond)
      failHere();
  }

  protected final ParsingException failHere() {
    src.fail();
    throw (RuntimeException) null;
  }

  //////////////////////////////////////////////////////////////////////////////

  static boolean isDigit(int ch) {
    return ch >= '0' & ch <= '9';
  }

  static boolean isHex(int ch) {
    return (ch >= '0' & ch <= '9') | (ch >= 'a' & ch <= 'z');
  }

  static boolean isLower(int ch) {
    return ch >= 'a' & ch <= 'z';
  }

  static boolean isAlphaNum(int ch) {
    return isDigit(ch) | isLower(ch);
  }

  static boolean isPrintable(int ch) {
    return ch >= ' ' & ch <= '~';
  }

  static boolean isWhiteSpace(int ch) {
    return ch == ' ' | ch == '\t' | ch == '\n' | ch == '\r';
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int hexDigitValue(int ch) {
    return ch - (isDigit(ch) ? '0' : 'a');
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class Tokenizer extends CharStreamProcessor {
  public Tokenizer(CharStream src) {
    super(src);
  }

  public Token readToken() {
    consumeWhiteSpace();

    if (eof())
      return null;

    boolean negate = false;
    if (consumeNextIfItIs('-')) {
      // Arrow
      if (consumeNextIfItIs('>'))
        return new Token(offset() - 1, 2, TokenType.Arrow);

      checkNextIsDigit();
      negate = true;
    }

    // Integer and floating point numbers
    if (nextIsDigit())
      return readNumber(negate);

    // Symbols
    if (nextIsLower())
      return readSymbol();

    // Strings
    if (nextIs('"'))
      return readString();

    // Single character tokens
    TokenType type;
    switch (read()) {
      case ',':
        type = TokenType.Comma;
        break;

      case ':':
        type = TokenType.Colon;
        break;

      case ';':
        type = TokenType.Semicolon;
        break;

      case '(':
        type = TokenType.OpenPar;
        break;

      case ')':
        type = TokenType.ClosePar;
        break;

      case '[':
        type = TokenType.OpenBracket;
        break;

      case ']':
        type = TokenType.CloseBracket;
        break;

      default:
        throw failHere();
    }

    return new Token(offset()-1, 1, type);
  }

  long readNat() {
    int count = 0;
    long value = 0;
    while (nextIsDigit()) {
      int digit = read() - '0';
      if (++count == 19)
        failHereIf(value > 922337203685477580L | (value == 922337203685477580L & digit > 7));
      value = 10 * value + digit;
    }
    return value;
  }

  Token readNumber(boolean negate) {
    int startOffset = offset();

    long intValue = readNat();

    if (nextIsLower())
      checkNextIs('e');

    if (!nextIs('.') & !nextIs('e'))
      return new Token(startOffset, offset - startOffset, TokenType.Int, negate ? -intValue : intValue);

    double floatValue = intValue;
    if (consumeNextIfItIs('.')) {
      int start = offset();
      long decIntVal = readNat();
      floatValue += ((double) decIntVal) / Math.pow(10, offset() - start);
    }

    if (consumeNextIfItIs('e')) {
      boolean negExp = consumeNextIfItIs('-');
      checkNextIsDigit();
      long expValue = readNat();
      floatValue *= Math.pow(10, negExp ? -expValue : expValue);
    }

    failHereIf(nextIsLower());

    return new Token(startOffset, offset() - startOffset, TokenType.Float, negate ? -floatValue : floatValue);
  }

  Token readSymbol() {
    Miscellanea._assert(nextIsLower());

    int offset = offset();

    int len = 0;
    byte[] chars = new byte[32];

    for ( ; ; ) {
      int ch;
      if (consumeNextIfItIs('_')) {
        ch = '_';
        checkNextIsAlphaNum();
      }
      else if (nextIsAlphaNum())
        ch = read();
      else
        break;
      if (len >= chars.length)
        chars = Array.extend(chars, 2 * len);
      chars[len++] = (byte) ch;
    }

    SymbObj obj = SymbObj.get(SymbTable.strToIdx(new String(chars, 0, len)));
    return new Token(offset, len, TokenType.Symbol, obj);
  }

  Token readString() {
    Miscellanea._assert(nextIs('"'));

    int offset = offset();

    int len = 0;
    char[] chars = new char[32];

    read();
    for ( ; ; ) {
      int ch = read();
      failHereIf(!Character.isBmpCodePoint(ch));

      if (ch == '\\') {
        ch = read();
        if (ch == '\\' | ch == '"') {
          // Nothing to do here
        }
        else if (ch == 'n') {
          ch = '\n';
        }
        else if (ch == 't') {
          ch = '\t';
        }
        else {
          failHereIf(!isHex(ch)); //## THIS ACTUALLY FAILS ONE CHARACTER AHEAD
          int d3 = hexDigitValue(ch);
          int d2 = hexDigitValue(readHex());
          int d1 = hexDigitValue(readHex());
          int d0 = hexDigitValue(readHex());
          ch = (char) (4096 * d3 + 256 * d2 + 16 * d1 + d0);
        }
      }

      if (ch == '"')
        break;

      if (len >= chars.length)
        chars = Array.extend(chars, 2 * chars.length);
      chars[len++] = (char) ch;
    }

    return new Token(offset, offset() - offset, TokenType.String, new String(chars, 0, len));
  }
}
