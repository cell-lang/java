package net.cell_lang;

import java.util.ArrayList;


class TokenStreamProcessor {
  private TokenStream tokens;
  private int line = -1;
  private int col = -1;

  protected TokenStreamProcessor(TokenStream tokens) {
    this.tokens = tokens;
  }

  protected void checkEof() {
    if (!tokens.eof())
      fail();
  }

  public final TokenType peekType() {
    return tokens.peekType();
  }

  protected final ParsingException fail() {
    return tokens.fail();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public final void bookmark() {
    line = tokens.line();
    col = tokens.column();
  }

  public final ParsingException failAtBookmark() {
    Miscellanea._assert(line != -1 & col != -1);
    throw new ParsingException(line + 1, col + 1);
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

  public final Obj readString() {
    return tokens.readString();
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

  public final boolean tryConsumingOpenBracket() {
    return tokens.tryConsuming('[');
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
        throw fail();

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
        return readString();

      default:
        throw new RuntimeException("Internal error"); // Unreachable code
    }
  }

  ////////////////////////////////////////////////////////////////////////////////

  // The opening parenthesis must have already been consumed
  Obj parseSeqOrRecord(TokenType firstTokenType) {
    if (firstTokenType == TokenType.Symbol) {
      int labelId = tryReadingLabel();
      if (labelId != -1)
        return parseRec(labelId);
    }
    else if (firstTokenType == TokenType.ClosePar) {
      consumeClosePar();
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
        labels = Array.extend(labels, 2 * labels.length);
        values = Array.extend(values, 2 * values.length);
      }
      int labelId = tryReadingLabel();
      if (labelId == -1)
        throw fail();
      //## BAD BAD BAD: WITH A LARGE RECORD...
      for (int j=0 ; j < i ; j++)
        if (labels[j] == labelId)
          throw fail();
      labels[i] = labelId;
      values[i++] = parseObj();
    }
    consumeClosePar();

    Obj[] labelObjs = new Obj[i];
    for (int j=0 ; j < i ; j++)
      labelObjs[j] = SymbTable.get(labels[j]);

    //## IT WOULD BE BETTER TO CREATE A RecordObj, BUT THE LABELS WOULD NEED TO BE SORTED FIRST
    return Builder.createMap(labelObjs, values, i);
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
        return createTaggedObj(symbId, parseRec(labelId));
      firstValue = parseObj();
    }
    else {
      firstValue = parseObj();
    }

    if (tryConsumingClosePar())
      return createTaggedObj(symbId, firstValue);

    Obj[] elts = new Obj[16];
    elts[0] = firstValue;

    int i = 1;
    while (tryConsumingComma()) {
      if (i >= elts.length)
        elts = Array.extend(elts, 2 * elts.length);
      elts[i++] = parseObj();
    }
    consumeClosePar();

    return createTaggedObj(symbId, Builder.createSeq(elts, i));
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

    throw fail();
  }
}
