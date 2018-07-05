package net.cell_lang;

import java.util.ArrayList;


class TokenStream {
  Token[] tokens;
  int offset = 0;
  int length;

  protected TokenStream(Token[] tokens) {
    this.tokens = tokens;
    this.length = tokens.length;
  }

  protected void checkEof() {
    failHereIf(offset != length);
  }

  protected final boolean nextIs(TokenType type) {
    return offset < length && tokens[offset].type == type;
  }

  protected final boolean nextIs(TokenType type, int delta) {
    int index = offset + delta;
    return index < length && tokens[index].type == type;
  }

  protected final Token read() {
    failHereIf(offset >= length);
    return tokens[offset++];
  }

  protected final Token forceRead(TokenType type) {
    failHereIf(offset >= length);
    Token token = tokens[offset++];
    failHereIf(token.type != type);
    return token;
  }

  protected final Token peek() {
    failHereIf(offset >= length);
    return tokens[offset];
  }

  protected final void consume(TokenType type) {
    failHereIf(offset >= length || tokens[offset].type != type);
    offset++;
  }

  protected final boolean tryConsuming(TokenType type) {
    if (offset < length && tokens[offset].type == type) {
      offset++;
      return true;
    }
    else
      return false;
  }

  protected final void failHereIf(boolean cond) {
    if (cond)
      failHere();
  }

  protected final ParsingException failHere() {
    int textOffset = 0;
    if (offset < length)
      textOffset = tokens[offset].offset;
    else if (length > 0)
      textOffset = tokens[length-1].offset + tokens[length-1].length;
    throw new ParsingException(textOffset);
  }
}


class Parser extends TokenStream {
  public static Obj parse(Token[] tokens) {
    Parser parser = new Parser(tokens);
    Obj obj = parser.parseObj();
    parser.checkEof();
    return obj;
  }

  Parser(Token[] tokens) {
    super(tokens);
  }

  Obj parseObj() {
    Token token = peek();

    switch (token.type) {
      case Comma:
      case Colon:
      case Semicolon:
      case Arrow:
      case ClosePar:
      case CloseBracket:
        throw failHere();

      case Int:
        read();
        return IntObj.get(((Long) token.value).longValue());

      case Float:
        read();
        return new FloatObj(((Double) token.value).doubleValue());

      case Symbol:
        return parseSymbOrTaggedObj();

      case OpenPar:
        if (isRecord())
          return parseRec();
        else
          return parseSeq();

      case OpenBracket:
        return parseUnordColl();

      case String:
        read();
        return Miscellanea.strToObj((String) token.value);

      default:
        throw new RuntimeException("Internal error"); // Unreachable code
    }
  }

  ////////////////////////////////////////////////////////////////////////////////

  Obj parseSeq() {
    consume(TokenType.OpenPar);

    if (tryConsuming(TokenType.ClosePar))
      return SeqObj.empty();

    ArrayList<Obj> elts = new ArrayList<Obj>();
    do {
      elts.add(parseObj());
    } while (tryConsuming(TokenType.Comma));

    consume(TokenType.ClosePar);

    return Builder.createSeq(elts);
  }

  ////////////////////////////////////////////////////////////////////////////////

  boolean isRecord() {
    Miscellanea._assert(nextIs(TokenType.OpenPar));
    return nextIs(TokenType.Symbol, 1) && nextIs(TokenType.Colon, 2);
  }

  Obj parseRec() {
    ArrayList<Obj> labels = new ArrayList<Obj>();
    ArrayList<Obj> values = new ArrayList<Obj>();

    consume(TokenType.OpenPar);

    for ( ; ; ) {
      labels.add((SymbObj) forceRead(TokenType.Symbol).value);
      consume(TokenType.Colon);
      values.add(parseObj());
      if (!tryConsuming(TokenType.Comma))
        break;
    }

    consume(TokenType.ClosePar);

    return Builder.createBinRel(labels, values); // Creating a binary relation instead of a map
  }

  ////////////////////////////////////////////////////////////////////////////////

  Obj parseSymbOrTaggedObj() {
    SymbObj symbObj = (SymbObj) forceRead(TokenType.Symbol).value;
    if (nextIs(TokenType.OpenPar)) {
      Obj innerObj = isRecord() ? parseRec() : parseSeq();
      if (innerObj.isSeq() && innerObj.getSize() == 1)
        innerObj = innerObj.getItem(0);
      return Builder.createTaggedObj(symbObj.getSymbId(), innerObj);
    }
    else
      return symbObj;
  }

  ////////////////////////////////////////////////////////////////////////////////

  Obj parseUnordColl() {
    consume(TokenType.OpenBracket);

    if (tryConsuming(TokenType.CloseBracket))
      return EmptyRelObj.singleton;

    ArrayList<Obj> objs = new ArrayList<Obj>();
    do {
      objs.add(parseObj());
    } while (tryConsuming(TokenType.Comma));

    if (tryConsuming(TokenType.CloseBracket))
      return Builder.createSet(objs);

    int len = objs.size();

    if (len == 1) {
      ArrayList<Obj> values = new ArrayList<Obj>();
      consume(TokenType.Arrow);
      values.add(parseObj());
      while (tryConsuming(TokenType.Comma)) {
        objs.add(parseObj());
        consume(TokenType.Arrow);
        values.add(parseObj());
      }
      consume(TokenType.CloseBracket);
      return Builder.createBinRel(objs, values); // Here we create a binary relation rather than a map
    }

    if (len == 2) {
      ArrayList<Obj> col1 = new ArrayList<Obj>();
      ArrayList<Obj> col2 = new ArrayList<Obj>();
      col1.add(objs.get(0));
      col2.add(objs.get(1));
      while (!tryConsuming(TokenType.CloseBracket)) {
        consume(TokenType.Semicolon);
        col1.add(parseObj());
        consume(TokenType.Comma);
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
      while (!tryConsuming(TokenType.CloseBracket)) {
        consume(TokenType.Semicolon);
        col1.add(parseObj());
        consume(TokenType.Comma);
        col2.add(parseObj());
        consume(TokenType.Comma);
        col3.add(parseObj());
      }
      return Builder.createTernRel(col1, col2, col3);
    }

    throw failHere();
  }
}
