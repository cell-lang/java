package net.cell_lang;


// interface TokenStream {
//   long readLong();
//   double readDouble();
//   int readSymbol();
//   Obj readString();

//   int tryReadingLabel();

//   TokenType peekType();

//   boolean nextIs(char ch);

//   void consume(char ch);
//   void consume(char ch1, char ch2);

//   boolean tryConsuming(char ch);
//   boolean tryConsuming(char ch1, char ch2);

//   boolean eof();

//   int line();
//   int column();

//   ParsingException fail();
// }


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
      case '`':     return TokenType.Literal;
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

  public int readSymbol() {
    Miscellanea._assert(nextIsLower());

    // long encWord1 = SymbTableFastCache.encodedLetter(read());
    // for (int i=0 ; i < 9 ; i++) {
    //   if (nextIsLower()) {
    //     int code =  SymbTableFastCache.encodedLetter(read());
    //     encWord1 = (encWord1 << 6) + code;
    //   }
    //   else if (nextIs('_')) {
    //     skip();
    //     if (nextIsLower()) {
    //       int code = SymbTableFastCache.encodedUnderscoredLetter(read());
    //       encWord1 = (encWord1 << 6) + code;
    //     }
    //     else if (nextIsDigit()) {
    //       encWord1 = (encWord1 << 6) + SymbTableFastCache.ENCODED_UNDERSCORE;
    //     }
    //     else
    //       throw fail();
    //   }
    //   else if (nextIsDigit()) {
    //     int code = SymbTableFastCache.encodedDigit(read());
    //     encWord1 = (encWord1 << 6) + code;
    //   }
    //   else {
    //     return SymbTableFastCache.encToIdx(encWord1);
    //   }
    // }

    // if (!nextIsAlphaNum() & !nextIs('_'))
    //   return SymbTableFastCache.encToIdx(encWord1);

    // long encWord2 = 0;
    // for (int i=0 ; i < 10 ; i++) {
    //   if (nextIsLower()) {
    //     int code =  SymbTableFastCache.encodedLetter(read());
    //     encWord2 = (encWord2 << 6) + code;
    //   }
    //   else if (nextIs('_')) {
    //     skip();
    //     if (nextIsLower()) {
    //       int code = SymbTableFastCache.encodedUnderscoredLetter(read());
    //       encWord2 = (encWord2 << 6) + code;
    //     }
    //     else if (nextIsDigit()) {
    //       encWord2 = (encWord2 << 6) + SymbTableFastCache.ENCODED_UNDERSCORE;
    //     }
    //     else
    //       throw fail();
    //   }
    //   else if (nextIsDigit()) {
    //     int code = SymbTableFastCache.encodedDigit(read());
    //     encWord2 = (encWord2 << 6) + code;
    //   }
    //   else {
    //     return SymbTableFastCache.encToIdx(encWord1, encWord2);
    //   }
    // }

    // if (!nextIsAlphaNum() & !nextIs('_'))
    //   return SymbTableFastCache.encToIdx(encWord1, encWord2);


    long encWord1 = readEncSymbWord();
    if (!nextIsAlphaNum() & !nextIs('_'))
      return SymbTableFastCache.encToIdx(encWord1);

    long encWord2 = readEncSymbWord();
    if (!nextIsAlphaNum() & !nextIs('_'))
      return SymbTableFastCache.encToIdx(encWord1, encWord2);

    long encWord3 = readEncSymbWord();
    if (!nextIsAlphaNum() & !nextIs('_'))
      return SymbTableFastCache.encToIdx(encWord1, encWord2, encWord3);

    long[] encWords = new long[8];
    encWords[0] = encWord1;
    encWords[1] = encWord2;
    encWords[2] = encWord3;

    for (int i=3 ; i < 64 ; i++) {
      if (i >= encWords.length)
        encWords = Array.extend(encWords, 2 * encWords.length);
      encWords[i] = readEncSymbWord();
      if (!nextIsAlphaNum() & !nextIs('_'))
        return SymbTableFastCache.encToIdx(encWords, i+1);
    }

    // The symbol was too long, we give up
    throw fail();
  }

  private long readEncSymbWord() {
    long encWord = 0;
    for (int i=0 ; i < 10 ; i++) {
      if (nextIsLower()) {
        int code =  SymbTableFastCache.encodedLetter(read());
        encWord = (encWord << 6) + code;
      }
      else if (nextIs('_')) {
        skip();
        if (nextIsLower()) {
          int code = SymbTableFastCache.encodedUnderscoredLetter(read());
          encWord = (encWord << 6) + code;
        }
        else if (nextIsDigit()) {
          encWord = (encWord << 6) + SymbTableFastCache.ENCODED_UNDERSCORE;
        }
        else
          throw fail();
      }
      else if (nextIsDigit()) {
        int code = SymbTableFastCache.encodedDigit(read());
        encWord = (encWord << 6) + code;
      }
      else {
        return encWord;
      }
    }
    return encWord;
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

  public Obj readLiteral() {
    Miscellanea._assert(nextIs('`'));

    read();
    int ch1 = read();
    int ch2 = read();
    if (ch1 == '\\') {
      read('`');
      if (ch2 == 'n')
        return IntObj.get('\n');
      else if (ch2 == '`')
        return IntObj.get('`');
      else if (ch2 == 't')
        return IntObj.get('\t');
      else if (ch2 == '\\')
        return IntObj.get('\\');
      else
        throw fail();
    }
    else if (ch2 == '`') {
      return IntObj.get(ch1);
    }
    else {
      check(isDigit(ch1) & isDigit(ch2));

      int year = 1000 * (ch1 - '0') + 100 * (ch2 - '0') + 10 * readDigit() + readDigit();
      read('-');
      int month = 10 * readDigit() + readDigit();
      read('-');
      int day = 10 * readDigit() + readDigit();

      check(DateTime.isValidDate(year, month, day));

      int daysSinceEpoc = DateTime.daysSinceEpoc(year, month, day);

      if (tryReading('`'))
        return Builder.createTaggedIntObj(SymbTable.DateSymbId, daysSinceEpoc);

      read(' ');
      int hours = 10 * readDigit() + readDigit();
      check(hours >= 0 & hours < 24);
      read(':');
      int minutes = 10 * readDigit() + readDigit();
      check(minutes >= 0 & minutes < 60);
      read(':');
      int seconds = 10 * readDigit() + readDigit();
      check(seconds >= 0 & minutes < 60);

      int nanosecs = 0;
      if (tryReading('.')) {
        int pow10 = 100000000;
        for (int i=0 ; i < 10 && nextIsDigit() ; i++) {
          nanosecs = nanosecs + pow10 * readDigit();
          pow10 /= 10;
        }
      }

      read('`');

      long dayTimeNs = 1000000000L * (60L * (60L * hours + minutes) + seconds) + nanosecs;
      check(DateTime.isWithinRange(daysSinceEpoc, dayTimeNs));

      long epocTimeNs = DateTime.epocTimeNs(daysSinceEpoc, dayTimeNs);
      return Builder.createTaggedIntObj(SymbTable.TimeSymbId, epocTimeNs);
    }
  }

  public int tryReadingLabel() {
    consumeWhiteSpace();

    if (!nextIsAlphaNum())
      return -1;

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
        skip(i+1);
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
    consumeWhiteSpace();
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
      if (++count == 19) {
        check(value < 922337203685477580L | (value == 922337203685477580L & digit <= 7));
        check(!nextIsDigit());
      }
      value = 10 * value + digit;
    }
    return value;
  }

  private void consumeWhiteSpace() {
    while (isWhiteSpace(peek()))
      skip(1);
  }

  private void read(char ch) {
    check(read() == ch);
  }

  private int readHex() {
    check(nextIsHex());
    return read();
  }

  private int readDigit() {
    int ch = read();
    check(isDigit(ch));
    return ch - '0';
  }

  private boolean tryReading(char ch) {
    if (peek() == ch) {
      read();
      return true;
    }
    else
      return false;
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
