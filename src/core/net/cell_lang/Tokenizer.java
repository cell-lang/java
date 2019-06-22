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

  protected final void skip() {
    currChar = src.read();
    offset++;
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
  final static int BUFFER_SIZE = 256;
  byte[] buffer = new byte[BUFFER_SIZE]; // For reading symbols, it's not part of the state of the class


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

    check(!nextIsLower());
    return negate ? -value : value;
  }

  // public int readSymbol() {
  //   int idx = 0;
  //   buffer[0] = (byte) peek(idx++);
  //   for (int i=1 ; i < BUFFER_SIZE ; i++) {
  //     if (isAlphaNum(peek(idx))) {
  //       buffer[i] = (byte) peek(idx++);
  //     }
  //     else if (peek(idx) == '_') {
  //       buffer[i++] = (byte) peek(idx++);
  //       if (isAlphaNum(peek(idx)))
  //         buffer[i] = (byte) peek(idx++);
  //       else
  //         throw fail();
  //     }
  //     else {
  //       int legitIdx = SymbTable.bytesToIdx(buffer, i);
  //       int tentativeIdx = _readSymbol();
  //       if (legitIdx != tentativeIdx) {
  //         System.out.printf("\n\n%s - %s\n", SymbTable.idxToStr(legitIdx), SymbTable.idxToStr(tentativeIdx));
  //         System.exit(1);
  //       }
  //       return legitIdx;
  //     }
  //   }
  //
  //   // The symbol was too long, we give up
  //   throw fail();
  // }

  public int readSymbol() {
    Miscellanea._assert(nextIsLower());

    long encWord1 = SymbTableFastCache.encodedLetter(read());
    for (int i=0 ; i < 9 ; i++) {
      if (nextIsLower()) {
        int code =  SymbTableFastCache.encodedLetter(read());
        encWord1 = (encWord1 << 6) + code;
      }
      else if (nextIs('_')) {
        skip();
        if (nextIsLower()) {
          int code = SymbTableFastCache.encodedUnderscoredLetter(read());
          encWord1 = (encWord1 << 6) + code;
        }
        else if (nextIsDigit()) {
          encWord1 = (encWord1 << 6) + SymbTableFastCache.ENCODED_UNDERSCORE;
        }
        else
          throw fail();
      }
      else if (nextIsDigit()) {
        int code = SymbTableFastCache.encodedDigit(read());
        encWord1 = (encWord1 << 6) + code;
      }
      else {
        return SymbTableFastCache.encToIdx(encWord1);
      }
    }

    if (!nextIsAlphaNum() & !nextIs('_'))
      return SymbTableFastCache.encToIdx(encWord1);

    long encWord2 = 0;
    for (int i=0 ; i < 10 ; i++) {
      if (nextIsLower()) {
        int code =  SymbTableFastCache.encodedLetter(read());
        encWord2 = (encWord2 << 6) + code;
      }
      else if (nextIs('_')) {
        skip();
        if (nextIsLower()) {
          int code = SymbTableFastCache.encodedUnderscoredLetter(read());
          encWord2 = (encWord2 << 6) + code;
        }
        else if (nextIsDigit()) {
          encWord2 = (encWord2 << 6) + SymbTableFastCache.ENCODED_UNDERSCORE;
        }
        else
          throw fail();
      }
      else if (nextIsDigit()) {
        int code = SymbTableFastCache.encodedDigit(read());
        encWord2 = (encWord2 << 6) + code;
      }
      else {
        return SymbTableFastCache.encToIdx(encWord1, encWord2);
      }
    }

    if (!nextIsAlphaNum() & !nextIs('_'))
      return SymbTableFastCache.encToIdx(encWord1, encWord2);

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
      check(Character.isBmpCodePoint(ch));

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
          check(isHex(ch)); //## THIS ACTUALLY FAILS ONE CHARACTER AHEAD
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
    if (!cond)
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

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class SymbTableFastCache {
  private static final int SIZE = 4096;

  private static long[] encSymbs1     = new long[SIZE];
  private static int[]  encSymbsIdxs1 = new  int[SIZE];

  private static long[] encSymbs2     = new long[2 * SIZE];
  private static int[]  encSymbsIdxs2 = new  int[SIZE];


  public static int encToIdx(long encWord) {
    int hashcode = Hashing.hashcode64(encWord);
    int idx = hashcode % SIZE;
    if (idx < 0)
      idx = -idx;

    long storedEnc = encSymbs1[idx];
    if (storedEnc == encWord)
      return encSymbsIdxs1[idx];

    byte[] bytes = decode(encWord);
    int symbIdx = SymbTable.bytesToIdx(bytes);

    if (storedEnc == 0) {
      encSymbs1[idx] = encWord;
      encSymbsIdxs1[idx] = symbIdx;
    }

    return symbIdx;
  }

  public static int encToIdx(long encWord1, long encWord2) {
    int hashcode1 = Hashing.hashcode64(encWord1);
    int hashcode2 = Hashing.hashcode64(encWord2);
    int idx = (hashcode1 ^ hashcode2) % SIZE;
    if (idx < 0)
      idx = -idx;

    long storedEnc1 = encSymbs2[2 * idx];
    long storedEnc2 = encSymbs2[2 * idx + 1];

    if (storedEnc1 == encWord1 & storedEnc2 == encWord2)
      return encSymbsIdxs2[idx];

    byte[] bytes = decode(encWord1, encWord2);
    int symbIdx = SymbTable.bytesToIdx(bytes);

    if (storedEnc1 == 0) {
      encSymbs2[2 * idx] = encWord1;
      encSymbs2[2 * idx + 1] = encWord2;
      encSymbsIdxs2[idx] = symbIdx;
    }

    return symbIdx;
  }

  //////////////////////////////////////////////////////////////////////////////

  //  0         Empty
  //  1 - 26    Letter
  // 27 - 36    Digit
  // 37         Underscore (followed by a digit)
  // 38 - 63    Underscore + letter

  public static int ENCODED_UNDERSCORE = 37;

  public static int encodedLetter(int ch) {
    return ch - 'a' + 1;
  }

  public static int encodedDigit(int ch) {
    return ch - '0' + 27;
  }

  public static int encodedUnderscoredLetter(int ch) {
    return ch - 'a' + 38;
  }

  public static byte[] decode(long encWord) {
    int size = size(encWord);
    byte[] bytes = new byte[size];
    int idx = decode(encWord, bytes, size-1);
    Miscellanea._assert(idx == -1);
    return bytes;
  }


  public static byte[] decode(long encWord1, long encWord2) {
    int size = size(encWord1, encWord2);
    byte[] bytes = new byte[size];
    int idx = decode(encWord2, bytes, size-1);
    idx = decode(encWord1, bytes, idx);
    Miscellanea._assert(idx == -1);
    return bytes;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static int size(long word) {
    int size = 0;
    while (word != 0) {
      int code = (int) (word & 0x3F);
      size += code >= 38 ? 2 : 1;
      word = word >>> 6;
    }
    Miscellanea._assert(size > 0);
    return size;
  }

  private static int size(long word1, long word2) {
    return size(word1) + size(word2);
  }

  private static int decode(long word, byte[] bytes, int idx) {
    while (word != 0) {
      int code = (int) (word & 0x3F);
      Miscellanea._assert(code != 0);
      if (code <= 26) {
        bytes[idx--] = (byte) (code - 1 + 'a');
      }
      else if (code <= 36) {
        bytes[idx--] = (byte) (code - 27 + '0');
      }
      else if (code == 37) {
        bytes[idx--] = '_';
      }
      else {
        bytes[idx--] = (byte) (code - 38 + 'a');
        bytes[idx--] = '_';
      }
      word = word >>> 6;
    }
    return idx;
  }
}