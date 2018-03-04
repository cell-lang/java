package net.cell_lang;


class Parsing {
  public static Obj parseSymb(Obj obj) {
    String str = obj.getString();
    int id = SymbTable.strToIdx(str);
    return SymbObj.get(id);
  }

  public static Obj parse(Obj text) {
    byte[] bytes = text.getInnerObj().getByteArray();
    Token[] tokens = Lexer.lex(bytes);
    return Parser.parse(tokens);
  }
}