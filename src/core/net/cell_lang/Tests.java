package net.cell_lang;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;


public class Tests {
  public static void main(String[] args) throws Exception {
    byte[] data_A = {10, 42, 42, 32, 42, 42, 32, 42, 42, 10};
    byte[] data_B = {10, 45, 45, 32, 45, 45, 32, 45, 45, 32, 45, 45, 10};
    byte[] data_C = {
      70, 105, 108, 101,  32, 110, 111, 116,  32, 102, 111, 117, 110, 100,  58,  32
    };
    byte[] data_D = {
       67,  97, 110,  39, 116,  32, 114, 101,  97, 100,  32, 102, 105, 108, 101,  58,  32
    };
    byte[] data_E = {
       85, 115,  97, 103, 101,  58,  32,  60,  99, 111, 109, 109,  97, 110, 100,  62,  32,  60, 112, 114, 
      111, 106, 101,  99, 116,  32, 102, 105, 108, 101,  62,  10
    };

    Obj str_A = new TaggedObj(SymbObj.get(SymbTable.StringSymbId), Builder.buildConstIntSeq(data_A));
    Obj str_B = new TaggedObj(SymbObj.get(SymbTable.StringSymbId), Builder.buildConstIntSeq(data_B));
    Obj str_C = new TaggedObj(SymbObj.get(SymbTable.StringSymbId), Builder.buildConstIntSeq(data_C));
    Obj str_D = new TaggedObj(SymbObj.get(SymbTable.StringSymbId), Builder.buildConstIntSeq(data_D));
    Obj str_E = new TaggedObj(SymbObj.get(SymbTable.StringSymbId), Builder.buildConstIntSeq(data_E));

    str_A.print();
    str_B.print();
    str_C.print();
    str_D.print();
    str_E.print();

    System.out.println("Hello world!");
  }

  // public static void main(String[] args) throws Exception {
  //   // System.out.println("Hello world!");

  //   if (args.length != 1) {
  //     System.err.println("Usage: javac <class name> <input file>");
  //     System.exit(1);
  //   }
  //   String filename = args[0];

  //   // String filename = "dump-syn-prg.txt";

  //   byte[] bytes = Files.readAllBytes(Paths.get(filename));
  //   Token[] tokens = Lexer.lex(bytes);
  //   Obj obj = Parser.parse(tokens);

  //   obj.print();

  //   // String str = obj.tostring();
  //   // System.out.println(str);

  //   // System.out.println("\n\nHello again world!");
  // }
}