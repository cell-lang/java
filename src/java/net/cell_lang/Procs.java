package net.cell_lang;

import java.nio.file.Paths;
import java.nio.file.Files;


class Procs {
  public static Obj FileRead_P(Obj fname, Object env) {
    String fnameStr = fname.getString();
    try {
      byte[] content = Files.readAllBytes(Paths.get(fnameStr));
      Obj bytesObj = Builder.buildConstIntSeq(content);
      return new TaggedObj(SymbTable.JustSymbId, bytesObj);
    }
    catch (Exception e) {
      return SymbObj.get(SymbTable.NothingSymbId);
    }
  }

  public static Obj FileWrite_P(Obj fname, Obj data, Object env) {
    String fnameStr = fname.getString();
    byte[] bytes = data.getByteArray();
    try {
      Files.write(Paths.get(fnameStr), bytes);
      return SymbObj.get(SymbTable.TrueSymbId);
    }
    catch (Exception e) {
      return SymbObj.get(SymbTable.FalseSymbId);
    }
  }

  public static void Print_P(Obj str, Object env) {
    System.out.print(str.getString());
  }

  public static Obj GetChar_P(Object env) {
    int ch;
    try {
      ch = System.in.read();
    }
    catch (Exception e) {
      ch = -1;
    }
    if (ch != -1)
      return new TaggedObj(SymbTable.JustSymbId, IntObj.get(ch));
    else
      return SymbObj.get(SymbTable.NothingSymbId);
  }
}
