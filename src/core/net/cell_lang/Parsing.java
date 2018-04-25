package net.cell_lang;


class Parsing {
  public static Obj parseSymb(Obj obj) {
    String str = obj.getString();
    int id = SymbTable.strToIdx(str);
    return SymbObj.get(id);
  }

  public static Obj parse(Obj text) {
    byte[] bytes = text.getInnerObj().getByteArray();
    try {
      Token[] tokens = Lexer.lex(bytes);
      Obj obj = Parser.parse(tokens);
      return new TaggedObj(SymbTable.SuccessSymbId, obj);
    }
    catch (ParsingException e) {
      return new TaggedObj(SymbTable.FailureSymbId, IntObj.get(e.errorOffset));
    }
  }
}