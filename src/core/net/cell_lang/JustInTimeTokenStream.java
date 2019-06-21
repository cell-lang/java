package net.cell_lang;


final class JustInTimeTokenStream implements TokenStream {
  final static int POOL_SIZE = 16;

  ReaderCharStream charStream;
  Tokenizer tokenizer;
  Token[] tokens = new Token[POOL_SIZE];
  int offset = 0;
  int line = -1;
  int col = -1;


  public JustInTimeTokenStream(ReaderCharStream charStream) {
    this.charStream = charStream;
    tokenizer = new Tokenizer(charStream);
    for (int i=0 ; i < tokens.length ; i++)
      tokens[i] = new Token();
  }

  public Token read() {
    int idx = offset++ % POOL_SIZE;
    Token token = tokens[idx];
    tokenizer.readToken(token);
    return token;
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
