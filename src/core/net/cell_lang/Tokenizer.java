package net.cell_lang;


class CharStreamProcessor {
  ReaderCharStream src;
  int currChar;
  int offset = 0;

  protected CharStreamProcessor(ReaderCharStream src) {
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

  protected final void consume(char ch) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    failHereIf(currChar != ch);
    currChar = src.read();
    offset++;
  }

  protected final void consume(char ch1, char ch2) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    failHereIf(currChar != ch1);
    currChar = src.read();
    offset++;
    failHereIf(currChar != ch2);
    currChar = src.read();
    offset++;
  }

  protected final boolean tryConsuming(char ch) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    if (currChar == ch) {
      currChar = src.read();
      offset++;
      return true;
    }
    else
      return false;
  }

  protected final boolean tryConsuming(char ch1, char ch2) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    if (currChar == ch1 && src.peek() == ch2) {
      src.read();
      currChar = src.read();
      offset += 2;
      return true;
    }
    return false;
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
    return src.fail();
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
  public Tokenizer(ReaderCharStream src) {
    super(src);
  }

  public boolean readToken(Token token) {
    consumeWhiteSpace();

    if (eof())
      return false;

    boolean negate = false;
    if (consumeNextIfItIs('-')) {
      // Arrow
      if (consumeNextIfItIs('>')) {
        token.set(TokenType.Arrow);
        return true;
      }

      checkNextIsDigit();
      negate = true;
    }

    // Integer and floating point numbers
    if (nextIsDigit()) {
      readNumber(token, negate);
      return true;
    }

    // Symbols
    if (nextIsLower()) {
      readSymbol(token);
      return true;
    }

    // Strings
    if (nextIs('"')) {
      readString(token);
      return true;
    }

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

    token.set(type);
    return true;
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

  void readNumber(Token token, boolean negate) {
    int startOffset = offset();

    long intValue = readNat();

    if (nextIsLower())
      checkNextIs('e');

    if (!nextIs('.') & !nextIs('e')) {
      token.set(negate ? -intValue : intValue);
      return;
    }

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

    token.set(negate ? -floatValue : floatValue);
  }

  void readSymbol(Token token) {
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
    token.set(obj);
  }

  void readString(Token token) {
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
      else if (ch == '"')
        break;

      if (len >= chars.length)
        chars = Array.extend(chars, 2 * chars.length);
      chars[len++] = (char) ch;
    }

    token.set(Builder.createString(chars, len));
  }
}
