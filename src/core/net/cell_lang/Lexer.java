package net.cell_lang;

import java.util.ArrayList;


class ByteStream {
  byte[] bytes;
  int offset;
  int length;
  byte nextByte;
  int checkpoint;

  final byte EOF = -1;

  protected ByteStream(byte[] bytes) {
    this.bytes = bytes;
    length = bytes.length;
    nextByte = bytes.length > 0 ? bytes[0] : EOF;
  }

  protected final String string(int first, int len) {
    return new String(bytes, first, len);
  }

  protected final byte read() {
    failHereIf(nextByte == EOF);
    offset++;
    byte result = nextByte;
    nextByte = offset < length ? bytes[offset] : EOF;
    return result;
  }

  protected final byte peek() {
    failHereIf(nextByte == EOF);
    return nextByte;
  }

  protected final void setCheckpoint() {
    checkpoint = offset;
  }

  protected final void rewind() {
    offset = checkpoint;
    nextByte = offset < length ? bytes[offset] : EOF;
  }

  protected final int offset() {
    return offset;
  }

  protected final boolean eof() {
    return offset >= length;
  }

  protected final boolean consumeNextIfItIs(char ch) {
    boolean res = nextIs(ch);
    if (res)
      read();
    return res;
  }

  protected void consumeWhiteSpace() {
    while (isWhiteSpace(nextByte))
      read();
  }

  protected final boolean nextIs(char ch) {
    return nextByte == ch;
  }

  protected final boolean nextIsDigit() {
    return isDigit(nextByte);
  }

  protected final boolean nextIsHex() {
    return isHex(nextByte);
  }

  protected final boolean nextIsLower() {
    return isLower(nextByte);
  }

  protected final boolean nextIsAlphaNum() {
    return isAlphaNum(nextByte);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final void checkNextIs(char ch) {
    failHereIf(nextByte != ch);
  }

  protected final void checkNextIsDigit() {
    failHereIf(!isDigit(nextByte));
  }

  protected final void checkNextIsHex() {
    failHereIf(!isHex(nextByte));
  }

  protected final void checkNextIsAlphaNum() {
    failHereIf(!isAlphaNum(nextByte));
  }

  protected final void checkNextIsPrintable() {
    failHereIf(!isPrintable(nextByte));
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final void failHereIf(boolean cond) {
    if (cond)
      failHere();
  }

  protected final ParsingException failHere() {
    throw new ParsingException(offset);
  }

  //////////////////////////////////////////////////////////////////////////////

  static boolean isDigit(byte ch) {
    return ch >= '0' & ch <= '9';
  }

  static boolean isHex(byte ch) {
    return (ch >= '0' & ch <= '9') | (ch >= 'a' & ch <= 'z');
  }

  static boolean isLower(byte ch) {
    return ch >= 'a' & ch <= 'z';
  }

  static boolean isAlphaNum(byte ch) {
    return isDigit(ch) | isLower(ch);
  }

  static boolean isPrintable(byte ch) {
    return ch >= ' ' & ch <= '~';
  }

  static boolean isWhiteSpace(byte ch) {
    return ch == ' ' | ch == '\t' | ch == '\n' | ch == '\r';
  }

  //////////////////////////////////////////////////////////////////////////////

  public static int hexDigitValue(byte ch) {
    return isDigit(ch) ? ch - '0' : ch - 'a';
  }
}


class Lexer extends ByteStream {
  public static Token[] lex(byte[] bytes) {
    Lexer lexer = new Lexer(bytes);
    return lexer.lex();
  }

  Lexer(byte[] bytes) {
    super(bytes);
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

    int startOffset = offset();
    for ( ; ; ) {
      if (consumeNextIfItIs('_'))
        checkNextIsAlphaNum();
      else if (nextIsAlphaNum())
        read();
      else
        break;
    }

    int len = offset() - startOffset;
    SymbObj obj = SymbObj.get(SymbTable.strToIdx(string(startOffset, len)));
    return new Token(offset, len, TokenType.Symbol, obj);
  }

  Token readString() {
    Miscellanea._assert(nextIs('"'));

    int startOffset = offset();
    read();
    setCheckpoint();

    int strLen = 0;
    for ( ; ; ) {
      checkNextIsPrintable();

      byte ch = read();

      if (ch == '"')
        break;

      strLen++;

      if (ch == '\\') {
        read();
        if (nextIs('\\') | nextIs('"') | nextIs('n') | nextIs('t')) {
          read();
          continue;
        }

        for (int i=0 ; i < 4 ; i++) {
          checkNextIsHex();
          read();
        }
      }
    }

    rewind();
    char[] chars = new char[strLen];
    for (int i=0 ; i < strLen ; i++) {
      char ch = (char) read();
      if (ch == '\\') {
        byte nextChar = read();
        if (nextChar == '\\' | nextChar == '"') {
          // Nothing to do here
        }
        else if (nextChar == 'n') {
          ch = '\n';
        }
        else if (nextChar == 't') {
          ch = '\t';
        }
        else {
          int d3 = hexDigitValue(nextChar);
          int d2 = hexDigitValue(read());
          int d1 = hexDigitValue(read());
          int d0 = hexDigitValue(read());

          ch = (char) (4096 * d3 + 256 * d2 + 16 * d1 + d0);
        }
      }

      chars[i] = ch;
    }
    Miscellanea._assert(nextIs('"'));
    read();

    return new Token(startOffset, offset() - startOffset, TokenType.String, new String(chars));
  }

  Token[] lex() {
    ArrayList<Token> tokens = new ArrayList<Token>();

    for ( ; ; ) {
      consumeWhiteSpace();

      if (eof())
        return tokens.toArray(new Token[tokens.size()]);

      int startOffset = offset();

      boolean negate = false;
      if (consumeNextIfItIs('-')) {
        // Arrow
        if (consumeNextIfItIs('>')) {
          tokens.add(new Token(offset() - 1, 2, TokenType.Arrow));
          continue;
        }

        checkNextIsDigit();
        negate = true;
      }

      // Integer and floating point numbers
      if (nextIsDigit()) {
        tokens.add(readNumber(negate));
        continue;
      }

      // Symbols
      if (nextIsLower()) {
        tokens.add(readSymbol());
        continue;
      }

      // Strings
      if (nextIs('"')) {
        tokens.add(readString());
        continue;
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

      tokens.add(new Token(offset()-1, 1, type));
    }
  }
}