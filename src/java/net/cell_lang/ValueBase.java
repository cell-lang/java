package net.cell_lang;

import java.io.StringWriter;
import java.io.Writer;


abstract class ValueBase implements Value {
  public boolean IsSymb() {
    return false;
  }

  public boolean IsInt() {
    return false;
  }

  public boolean IsFloat() {
    return false;
  }

  public boolean IsSeq() {
    return false;
  }

  public boolean IsSet() {
    return false;
  }

  public boolean IsBinRel() {
    return false;
  }

  public boolean IsTernRel() {
    return false;
  }

  public boolean IsTagged() {
    return false;
  }

  public String AsSymb() {
    throw new UnsupportedOperationException();
  }

  public long AsLong() {
    throw new UnsupportedOperationException();
  }

  public double AsDouble() {
    throw new UnsupportedOperationException();
  }

  public int Size() {
    throw new UnsupportedOperationException();
  }

  public Value Item(int index) {
    throw new UnsupportedOperationException();
  }

  public Value Arg1(int index) {
    throw new UnsupportedOperationException();
  }

  public Value Arg2(int index) {
    throw new UnsupportedOperationException();
  }

  public Value Arg3(int index) {
    throw new UnsupportedOperationException();
  }

  public String Tag() {
    throw new UnsupportedOperationException();
  }

  public Value Untagged() {
    throw new UnsupportedOperationException();
  }

  public boolean IsString() {
    return false;
  }

  public boolean IsRecord() {
    return false;
  }

  public String AsString() {
    throw new UnsupportedOperationException();
  }

  public Value Lookup(String field) {
    throw new UnsupportedOperationException();
  }

  public void Print(Writer writer) {
    Obj obj = AsObj();
    obj.Print(writer, 90, true, 0);
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    Print(writer);
    return writer.toString();
  }

  public abstract Obj AsObj();
}
