package net.cell_lang;

import java.util.ArrayList;


class TokenStreamProcessor {
  private TokenStream tokens;

  protected TokenStreamProcessor(TokenStream tokens) {
    this.tokens = tokens;
  }

  protected void checkEof() {
    failHereIf(!tokens.eof());
  }

  protected final Token read() {
    return tokens.read();
  }

  public final TokenType peekType() {
    return tokens.peekType();
  }

  protected final void failHereIf(boolean cond) {
    if (cond)
      failHere();
  }

  protected final ParsingException failHere() {
    return tokens.fail();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final void bookmark() {
    tokens.bookmark();
  }

  public final ParsingException failAtBookmark() {
    throw tokens.failAtBookmark();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final long readLong() {
    return tokens.readLong();
  }

  public final double readDouble() {
    return tokens.readDouble();
  }

  public final int readSymbol() {
    return tokens.readSymbol();
  }

  public final int tryReadingLabel() {
    return tokens.tryReadingLabel();
  }

  public final boolean nextIsCloseBracket() {
    return tokens.nextIs(']');
  }

  public final void consumeArrow() {
    tokens.consume('-', '>');
  }

  public final void consumeCloseBracket() {
    tokens.consume(']');
  }

  public final void consumeClosePar() {
    tokens.consume(')');
  }

  public final void consumeColon() {
    tokens.consume(':');
  }

  public final void consumeComma() {
    tokens.consume(',');
  }

  public final void consumeOpenBracket() {
    tokens.consume('[');
  }

  public final void consumeOpenPar() {
    tokens.consume('(');
  }

  public final void consumeSemicolon() {
    tokens.consume(';');
  }

  public final boolean tryConsumingSemicolon() {
    return tokens.tryConsuming(';');
  }

  public final boolean tryConsumingArrow() {
    return tokens.tryConsuming('-', '>');
  }

  public final boolean tryConsumingComma() {
    return tokens.tryConsuming(',');
  }

  protected final boolean tryConsumingOpenPar() {
    return tokens.tryConsuming('(');
  }

  protected final boolean tryConsumingClosePar() {
    return tokens.tryConsuming(')');
  }

  protected final boolean tryConsumingCloseBracket() {
    return tokens.tryConsuming(']');
  }
}

////////////////////////////////////////////////////////////////////////////////

abstract class Parser extends TokenStreamProcessor {
  Parser(TokenStream tokens) {
    super(tokens);
  }

  //## THIS IS HERE ONLY FOR BACKWARD COMPATIBILITY WITH THE OLD COMPILER.
  //## REMOVE AS SOON AS POSSIBLE, ALONG WITH TokenArray
  Parser(Token[] tokens) {
    super(new TokenArray(tokens));
  }

  void skipValue() {
    parseObj(); //## IMPLEMENT FOR REAL
  }

  Obj parseObj() {
    TokenType type = peekType();

    switch (type) {
      case Comma:
      case Colon:
      case Semicolon:
      case Arrow:
      case ClosePar:
      case CloseBracket:
        throw failHere();

      case Int:
        return IntObj.get(readLong());

      case Float:
        return new FloatObj(readDouble());

      case Symbol:
        return parseSymbOrTaggedObj();

      case OpenPar:
        consumeOpenPar();
        return parseSeqOrRecord(peekType());

      case OpenBracket:
        return parseUnordColl();

      case String:
        return read().objValue;

      default:
        throw new RuntimeException("Internal error"); // Unreachable code
    }
  }

  ////////////////////////////////////////////////////////////////////////////////

  // The opening parenthesis must have already been consumed
  Obj parseSeqOrRecord(TokenType firsTokenType) {
    if (firsTokenType == TokenType.Symbol) {
      int labelId = tryReadingLabel();
      if (labelId != -1)
        return parseRec(labelId);
    }
    else if (firsTokenType == TokenType.ClosePar) {
      return EmptySeqObj.singleton;
    }

    return parseNeSeq();
  }

  ////////////////////////////////////////////////////////////////////////////////

  // The opening parenthesis must have already been consumed
  Obj parseNeSeq() {
    ArrayList<Obj> elts = new ArrayList<Obj>();

    do {
      elts.add(parseObj());
    } while (tryConsumingComma());

    consumeClosePar();
    return Builder.createSeq(elts);
  }

  ////////////////////////////////////////////////////////////////////////////////

  // The opening parenthesis and the first label including
  // its trailing colon must have already been consumed
  Obj parseRec(int firstLabelId) {
    int[] labels = new int[8];
    Obj[] values = new Obj[8];

    labels[0] = firstLabelId;
    values[0] = parseObj();

    int i = 1;
    while (tryConsumingComma()) {
      if (i >= labels.length) {
        Array.extend(labels, 2 * labels.length);
        Array.extend(values, 2 * values.length);
      }
      int labelId = tryReadingLabel();
      if (labelId == -1)
        failHere();
      //## BAD BAD BAD: WITH A HUGE RECORD...
      for (int j=0 ; j < i ; j++)
        if (labels[i] == labelId)
          failHere();
      labels[i] = labelId;
      values[i++] = parseObj();
    }
    consumeClosePar();

    if (i < labels.length) {
      labels = Array.take(labels, i);
      values = Array.take(values, i);
    }
    return new RecordObj(labels, values);
  }

  ////////////////////////////////////////////////////////////////////////////////

  Obj parseSymbOrTaggedObj() {
    int symbId = readSymbol();

    if (!tryConsumingOpenPar())
      return SymbObj.get(symbId);

    TokenType type = peekType();

    Obj firstValue = null;

    if (type == TokenType.Int) {
      long value = readLong();
      if (tryConsumingClosePar())
        return Builder.createTaggedIntObj(symbId, value);
      // Here we've consumed the opening parenthesis and the integer
      // Since the opening parenthesis was not follow by a label,
      // we're dealing with a sequence, possibly a sequence of integers
      //## OPTIMIZE FOR SEQUENCES OF INTEGERS
      firstValue = IntObj.get(value);
    }
    else if (type == TokenType.Symbol) {
      int labelId = tryReadingLabel();
      if (labelId != -1)
        return Builder.createTaggedObj(symbId, parseRec(labelId));
      firstValue = parseObj();
    }
    else {
      firstValue = parseObj();
    }

    if (tryConsumingClosePar())
      return Builder.createTaggedObj(symbId, firstValue);

    Obj[] elts = new Obj[16];
    elts[0] = firstValue;

    int i = 1;
    while (tryConsumingComma()) {
      if (i >= elts.length)
        elts = Array.extend(elts, 2 * elts.length);
      elts[i++] = parseObj();
    }
    consumeClosePar();

    return Builder.createTaggedObj(symbId, Builder.createSeq(elts, i));
  }

  abstract Obj createTaggedObj(int tagId, Obj obj);

  ////////////////////////////////////////////////////////////////////////////////

  Obj parseUnordColl() {
    consumeOpenBracket();

    if (tryConsumingCloseBracket())
      return EmptyRelObj.singleton;

    ArrayList<Obj> objs = new ArrayList<Obj>();
    do {
      objs.add(parseObj());
    } while (tryConsumingComma());

    if (tryConsumingCloseBracket())
      return Builder.createSet(objs);

    int len = objs.size();

    if (len == 1) {
      ArrayList<Obj> values = new ArrayList<Obj>();
      consumeArrow();
      values.add(parseObj());
      while (tryConsumingComma()) {
        objs.add(parseObj());
        consumeArrow();
        values.add(parseObj());
      }
      consumeCloseBracket();
      return Builder.createBinRel(objs, values); // Here we create a binary relation rather than a map
    }

    if (len == 2) {
      ArrayList<Obj> col1 = new ArrayList<Obj>();
      ArrayList<Obj> col2 = new ArrayList<Obj>();
      col1.add(objs.get(0));
      col2.add(objs.get(1));
      while (!tryConsumingCloseBracket()) {
        consumeSemicolon();
        col1.add(parseObj());
        consumeComma();
        col2.add(parseObj());
      }
      return Builder.createBinRel(col1, col2);
    }

    if (len == 3) {
      ArrayList<Obj> col1 = new ArrayList<Obj>();
      ArrayList<Obj> col2 = new ArrayList<Obj>();
      ArrayList<Obj> col3 = new ArrayList<Obj>();
      col1.add(objs.get(0));
      col2.add(objs.get(1));
      col3.add(objs.get(2));
      while (!tryConsumingCloseBracket()) {
        consumeSemicolon();
        col1.add(parseObj());
        consumeComma();
        col2.add(parseObj());
        consumeComma();
        col3.add(parseObj());
      }
      return Builder.createTernRel(col1, col2, col3);
    }

    throw failHere();
  }
}
