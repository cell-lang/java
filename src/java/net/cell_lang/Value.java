package net.cell_lang;

import java.io.Writer;


interface Value {
  boolean isSymb();
  boolean isInt();
  boolean isFloat();
  boolean isSeq();
  boolean isSet();
  boolean isBinRel();
  boolean isTernRel();
  boolean isTagged();

  String asSymb();
  long   asLong();
  double asDouble();

  int size();
  Value item(int index);
  Value arg1(int index);
  Value arg2(int index);
  Value arg3(int index);

  String tag();
  Value untagged();

  boolean isString();
  boolean isRecord();

  String asString();
  Value lookup(String field);

  void print(Writer writer);
}
