package net.cell_lang;

import java.io.Writer;


interface Value {
  boolean IsSymb();
  boolean IsInt();
  boolean IsFloat();
  boolean IsSeq();
  boolean IsSet();
  boolean IsBinRel();
  boolean IsTernRel();
  boolean IsTagged();

  String AsSymb();
  long   AsLong();
  double AsDouble();

  int Size();
  Value Item(int index);
  Value Arg1(int index);
  Value Arg2(int index);
  Value Arg3(int index);

  String Tag();
  Value Untagged();

  boolean IsString();
  boolean IsRecord();

  String AsString();
  Value Lookup(String field);

  void Print(Writer writer);
}
