package net.cell_lang;


final class JustInTimeTokenStream implements TokenStream {
  ReaderCharStream charStream;
  Tokenizer tokenizer;
  int line = -1;
  int col = -1;


  public JustInTimeTokenStream(ReaderCharStream charStream) {
    this.charStream = charStream;
    tokenizer = new Tokenizer(charStream);
  }

  public Token read() {
    throw Miscellanea.internalFail();
  }

  public Token peek(int idx) {
    throw Miscellanea.internalFail();
  }

  public boolean eof() {
    return tokenizer.eof();
  }

  public ParsingException fail() {
    return tokenizer.failHere();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final void bookmark() {
    line = charStream.line();
    col = charStream.column();
  }

  public final ParsingException failAtBookmark() {
    Miscellanea._assert(line != -1 & col != -1);
    throw new ParsingException(line + 1, col + 1);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final long readLong() {
    return tokenizer.readLong();
  }

  public final double readDouble() {
    return tokenizer.readDouble();
  }

  public final int readSymbol() {
    return tokenizer.readSymbol();
  }

  public final Obj readString() {
    return tokenizer.readString();
  }

  public final int tryReadingLabel() {
    return tokenizer.tryReadingLabel();
  }

  public final TokenType peekType() {
    return tokenizer.peekType();
  }

  public final boolean nextIs(char ch) {
    tokenizer.consumeWhiteSpace();
    return tokenizer.nextIs(ch);
  }

  public final void consume(char ch) {
    tokenizer.consume(ch);
  }

  public final void consume(char ch1, char ch2) {
    tokenizer.consume(ch1, ch2);
  }

  public final boolean tryConsuming(char ch) {
    return tokenizer.tryConsuming(ch);
  }

  public final boolean tryConsuming(char ch1, char ch2) {
    return tokenizer.tryConsuming(ch1, ch2);
  }
}
