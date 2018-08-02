package net.cell_lang;

import java.util.ArrayList;


class CodePointStream {
  public interface Source {
    long get(int idx);
  }

  Source src;
  int offset;
  int length;
  int nextChar;
  int checkpoint;

  final byte EOF = -1;

  protected CodePointStream(Source src, int len) {
    this.src = src;
    length = len;
    nextChar = length > 0 ? getChar(0) : EOF;
  }

  protected final String string(int first, int len) {
    char[] chs = new char[len];
    for (int i=0 ; i < len ; i++)
      chs[i] = (char) src.get(first+i);
    return new String(chs);
  }

  private final int getChar(int idx) {
    long ch = src.get(idx);
    if (ch < 0 | ch > 0x10FFFF)
      failHere();
    return (int) ch;
  }

  protected final int read() {
    failHereIf(nextChar == EOF);
    offset++;
    int result = nextChar;
    nextChar = offset < length ? getChar(offset) : EOF;
    return result;
  }

  protected final long peek() {
    failHereIf(nextChar == EOF);
    return nextChar;
  }

  protected final void setCheckpoint() {
    checkpoint = offset;
  }

  protected final void rewind() {
    offset = checkpoint;
    nextChar = offset < length ? getChar(offset) : EOF;
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
    while (isWhiteSpace(nextChar))
      read();
  }

  protected final boolean nextIs(char ch) {
    return nextChar == ch;
  }

  protected final boolean nextIsDigit() {
    return isDigit(nextChar);
  }

  protected final boolean nextIsHex() {
    return isHex(nextChar);
  }

  protected final boolean nextIsLower() {
    return isLower(nextChar);
  }

  protected final boolean nextIsAlphaNum() {
    return isAlphaNum(nextChar);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected final void checkNextIs(char ch) {
    failHereIf(nextChar != ch);
  }

  protected final void checkNextIsDigit() {
    failHereIf(!isDigit(nextChar));
  }

  protected final void checkNextIsHex() {
    failHereIf(!isHex(nextChar));
  }

  protected final void checkNextIsAlphaNum() {
    failHereIf(!isAlphaNum(nextChar));
  }

  protected final void checkNextIsPrintable() {
    failHereIf(!isPrintable(nextChar));
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
    return (int) (isDigit(ch) ? ch - '0' : ch - 'a');
  }
}


class Lexer extends CodePointStream {
  public static Token[] lex(Source src, int len) {
    Lexer lexer = new Lexer(src, len);
    return lexer.lex();
  }

  Lexer(Source src, int len) {
    super(src, len);
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
      // checkNextIsPrintable();

      int ch = read();

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
        int nextChar = read();
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