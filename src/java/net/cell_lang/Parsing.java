package net.cell_lang;


class Parsing {
  public static Obj ParseSymb(Obj obj) {
    String str = obj.GetString();
    int id = SymbTable.StrToIdx(str);
    return SymbObj.Get(id);
  }

  public static Obj Parse(Obj text) {
    byte[] bytes = text.GetInnerObj().GetByteArray();
    Token[] tokens = Lexer.lex(bytes);
    return Parser.parse(tokens);
  }
}