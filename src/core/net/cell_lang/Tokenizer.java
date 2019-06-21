package net.cell_lang;


class CharStreamProcessor {
  private ReaderCharStream src;
  private int currChar;
  private int offset = 0;

  final static int BUFFER_SIZE = 1024;
  byte[] buffer = new byte[BUFFER_SIZE]; // For reading symbols


  protected CharStreamProcessor(ReaderCharStream src) {
    this.src = src;
    currChar = src.read();
  }

  protected final int read() {
    check(currChar == CharStream.EOF);
    int result = currChar;
    currChar = src.read();
    offset++;
    return result;
  }

  protected final void skip(int count) {
    for (int i=0 ; i < count ; i++)
      currChar = src.read();
    offset += count;
  }

  protected final int readHex() {
    check(!nextIsHex());
    return read();
  }

  protected final int peek() {
    check(currChar == CharStream.EOF);
    return currChar;
  }

  protected final int peek(int idx) {
    return idx == 0 ? currChar : src.peek(idx - 1);
  }

  protected final int offset() {
    return offset;
  }

  public final boolean eof() {
    return currChar == CharStream.EOF;
  }

  public final void consume(char ch) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    check(currChar != ch);
    currChar = src.read();
    offset++;
  }

  public final void consume(char ch1, char ch2) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    check(currChar != ch1);
    currChar = src.read();
    offset++;
    check(currChar != ch2);
    currChar = src.read();
    offset++;
  }

  public final boolean tryConsuming(char ch) {
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

  public final boolean tryConsuming(char ch1, char ch2) {
    while (isWhiteSpace(currChar))
      currChar = src.read();
    if (currChar == ch1 && src.peek(0) == ch2) {
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

  public final boolean nextIs(char ch) {
    consumeWhiteSpace();
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
    check(currChar != ch);
  }

  protected final void checkNextIsDigit() {
    check(!isDigit(currChar));
  }

  protected final void checkNextIsHex() {
    check(!isHex(currChar));
  }

  protected final void checkNextIsAlphaNum() {
    check(!isAlphaNum(currChar));
  }

  protected final void checkNextIsPrintable() {
    check(!isPrintable(currChar));
  }

  //////////////////////////////////////////////////////////////////////////////

  public int line() {
    return src.line();
  }

  public int column() {
    return src.column();
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final void check(boolean cond) {
    if (cond)
      fail();
  }

  public final ParsingException fail() {
    return src.fail();
  }

  //////////////////////////////////////////////////////////////////////////////

  static boolean isDigit(int ch) {
    return ch >= '0' & ch <= '9';
  }

  static boolean isHex(int ch) {
    return (ch >= '0' & ch <= '9') | (ch >= 'a' & ch <= 'f');
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

class Tokenizer extends CharStreamProcessor implements TokenStream {
  public Tokenizer(ReaderCharStream src) {
    super(src);
  }

  public TokenType peekType() {
    consumeWhiteSpace();

    if (nextIsDigit())
      return numberType(1);

    if (nextIsLower())
      return TokenType.Symbol;

    switch (peek()) {
      case '"':     return TokenType.String;
      case ',':     return TokenType.Comma;
      case ':':     return TokenType.Colon;
      case ';':     return TokenType.Semicolon;
      case '(':     return TokenType.OpenPar;
      case ')':     return TokenType.ClosePar;
      case '[':     return TokenType.OpenBracket;
      case ']':     return TokenType.CloseBracket;
      case '-':     int ch = peek(1);
                    if (ch == '>')
                      return TokenType.Arrow;
                    if (isDigit(ch))
                      return numberType(2);
                    throw fail();
    }

    throw fail();
  }

  public long readLong() {
    boolean negate = consumeNextIfItIs('-');
    long natVal = readNat();
    return negate ? -natVal : natVal;
  }

  public double readDouble() {
    boolean negate = consumeNextIfItIs('-');
    double value = readNat();

    if (consumeNextIfItIs('.')) {
      int start = offset();
      long decIntVal = readNat();
      value += ((double) decIntVal) / Math.pow(10, offset() - start);
    }

    if (consumeNextIfItIs('e')) {
      boolean negExp = consumeNextIfItIs('-');
      checkNextIsDigit();
      long expValue = readNat();
      value *= Math.pow(10, negExp ? -expValue : expValue);
    }

    check(nextIsLower());
    return negate ? -value : value;
  }

  public int readSymbol() {
    Miscellanea._assert(nextIsAlphaNum());

    buffer[0] = (byte) read();
    for (int i=1 ; i < BUFFER_SIZE ; i++) {
      if (nextIsAlphaNum()) {
        buffer[i] = (byte) read();
      }
      else if (nextIs('_')) {
        buffer[i++] = (byte) read();
        if (nextIsAlphaNum())
          buffer[i] = (byte) read();
        else
          throw fail();
      }
      else {
        return SymbTable.bytesToIdx(buffer, i);
      }
    }

    // The symbol was too long, we give up
    throw fail();
  }

  public Obj readString() {
    Miscellanea._assert(nextIs('"'));

    int len = 0;
    char[] chars = new char[32];

    read();
    for ( ; ; ) {
      int ch = read();
      check(!Character.isBmpCodePoint(ch));

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
          check(!isHex(ch)); //## THIS ACTUALLY FAILS ONE CHARACTER AHEAD
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

    return Builder.createString(chars, len);
  }

  public int tryReadingLabel() {
    Miscellanea._assert(nextIsAlphaNum());

    buffer[0] = (byte) peek();
    for (int i=1 ; i < BUFFER_SIZE ; i++) {
      int ch = peek(i);

      if (isAlphaNum(ch)) {
        buffer[i] = (byte) ch;
      }
      else if (ch == '_') {
        buffer[i++] = (byte) ch;
        ch = peek(i);
        if (isAlphaNum(ch))
          buffer[i] = (byte) ch;
        else
          throw fail();
      }
      else if (ch == ':') {
        skip(i);
        return SymbTable.bytesToIdx(buffer, i);
      }
      else {
        return -1;
      }
    }

    // The label was too long, we give up
    throw fail();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private TokenType numberType(int idx) {
    while (isDigit(peek(idx)))
      idx++;
    int ch = peek(idx);
    return ch == '.' | ch == 'e' ? TokenType.Float : TokenType.Int;
  }

  private long readNat() {
    int count = 0;
    long value = 0;
    while (nextIsDigit()) {
      int digit = read() - '0';
      if (++count == 19)
        check(value > 922337203685477580L | (value == 922337203685477580L & digit > 7));
      value = 10 * value + digit;
    }
    return value;
  }
}
