package net.cell_lang;


class CharStreamProcessor {
  private CharStream src;
  private int currChar;
  private int offset = 0;


  protected CharStreamProcessor(CharStream src) {
    this.src = src;
    currChar = src.read();
  }

  protected final int read() {
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

  protected final int peek() {
    return currChar;
  }

  protected final int peek(int idx) {
    return idx == 0 ? currChar : src.peek(idx - 1);
  }

  protected final int offset() {
    return offset;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int line() {
    return src.line();
  }

  public int column() {
    return src.column();
  }

  public final ParsingException fail() {
    return src.fail();
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

final class Tokenizer extends CharStreamProcessor implements TokenStream {
  final static int BUFFER_SIZE = 1024;
  byte[] buffer = new byte[BUFFER_SIZE]; // For reading symbols


  public Tokenizer(CharStream src) {
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
    boolean negate = tryConsuming('-');
    long natVal = readNat();
    return negate ? -natVal : natVal;
  }

  public double readDouble() {
    boolean negate = tryConsuming('-');
    double value = readNat();

    if (tryConsuming('.')) {
      int start = offset();
      long decIntVal = readNat();
      value += ((double) decIntVal) / Math.pow(10, offset() - start);
    }

    if (tryConsuming('e')) {
      boolean negExp = tryConsuming('-');
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

  public boolean nextIs(char ch) {
    consumeWhiteSpace();
    return peek() == ch;
  }

  public void consume(char ch) {
    consumeWhiteSpace();
    check(peek() == ch);
    skip(1);
  }

  public void consume(char ch1, char ch2) {
    consumeWhiteSpace();
    check(peek() == ch1);
    skip(1);
    check(peek() == ch2);
    skip(1);
  }

  public boolean tryConsuming(char ch) {
    consumeWhiteSpace();
    if (peek() == ch) {
      skip(1);
      return true;
    }
    else
      return false;
  }

  public boolean tryConsuming(char ch1, char ch2) {
    consumeWhiteSpace();
    if (peek() == ch1 && peek(1) == ch2) {
      skip(2);
      return true;
    }
    return false;
  }

  public boolean eof() {
    return peek() == CharStream.EOF;
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

  private void consumeWhiteSpace() {
    while (isWhiteSpace(peek()))
      skip(1);
  }

  private int readHex() {
    check(nextIsHex());
    return read();
  }

  private boolean nextIsDigit() {
    return isDigit(peek());
  }

  private boolean nextIsHex() {
    return isHex(peek());
  }

  private boolean nextIsLower() {
    return isLower(peek());
  }

  private boolean nextIsAlphaNum() {
    return isAlphaNum(peek());
  }

  private void checkNextIs(char ch) {
    check(peek() == ch);
  }

  private void checkNextIsDigit() {
    check(isDigit(peek()));
  }

  private void checkNextIsHex() {
    check(isHex(peek()));
  }

  private void checkNextIsAlphaNum() {
    check(isAlphaNum(peek()));
  }

  private void checkNextIsPrintable() {
    check(isPrintable(peek()));
  }

  private void check(boolean cond) {
    if (cond)
      fail();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private static boolean isDigit(int ch) {
    return ch >= '0' & ch <= '9';
  }

  private static boolean isHex(int ch) {
    return (ch >= '0' & ch <= '9') | (ch >= 'a' & ch <= 'f');
  }

  private static boolean isLower(int ch) {
    return ch >= 'a' & ch <= 'z';
  }

  private static boolean isAlphaNum(int ch) {
    return isDigit(ch) | isLower(ch);
  }

  private static boolean isPrintable(int ch) {
    return ch >= ' ' & ch <= '~';
  }

  private static boolean isWhiteSpace(int ch) {
    return ch == ' ' | ch == '\t' | ch == '\n' | ch == '\r';
  }

  private static int hexDigitValue(int ch) {
    return ch - (isDigit(ch) ? '0' : 'a');
  }
}
