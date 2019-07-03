package net.cell_lang;

import java.io.Writer;
import java.io.IOException;


public class TextWriter {
  public static void write(Writer writer, int field_symb_idx, UnaryTable[] tables, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
    String baseWs = new String(Array.repeat(' ', indentation));
    String entryWs = new String(Array.repeat(' ', indentation + 2));

    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (indentFirstLine)
      writer.write(baseWs);
    writer.write(SymbTable.idxToStr(field_symb_idx));
    writer.write(": [");

    if (count > 0) {
      writer.write("\n");

      int written = 0;
      for (int i=0 ; i < tables.length ; i++) {
        UnaryTable table = tables[i];
        SurrObjMapper mapper = table.mapper;
        UnaryTable.Iter it = table.getIter();
        while (!it.done()) {
          writer.write(entryWs);
          Obj obj = mapper.surrToObj(it.get());
          obj.print(writer, Integer.MAX_VALUE, true, 0);
          written++;
          writer.write(written < count ? ",\n" : "\n");
          it.next();
        }
      }
      Miscellanea._assert(written == count);

      writer.write(baseWs);
    }

    writer.write(writeSeparator ? "],\n" : "]\n");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static void write(Writer writer, int field_symb_idx, BinaryTable[] tables, boolean flipCols, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
    String baseWs = new String(Array.repeat(' ', indentation));
    String entryWs = new String(Array.repeat(' ', indentation + 2));

    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (indentFirstLine)
      writer.write(baseWs);
    writer.write(SymbTable.idxToStr(field_symb_idx));
    writer.write(": [");

    if (count > 0) {
      writer.write("\n");

      int written = 0;
      for (int i=0 ; i < tables.length ; i++) {
        BinaryTable table = tables[i];
        SurrObjMapper mapper1 = table.mapper1;
        SurrObjMapper mapper2 = table.mapper2;
        BinaryTable.Iter it = table.getIter();
        while (!it.done()) {
          writer.write(entryWs);
          Obj obj1 = mapper1.surrToObj(it.get1());
          Obj obj2 = mapper2.surrToObj(it.get2());
          if (flipCols) {
            Obj tmp = obj1;
            obj1 = obj2;
            obj2 = tmp;
          }
          obj1.print(writer, Integer.MAX_VALUE, true, 0);
          writer.write(", ");
          obj2.print(writer, Integer.MAX_VALUE, true, 0);
          written++;
          writer.write(written < count || count == 1 ? ";\n" : "\n");
          it.next();
        }
      }
      Miscellanea._assert(written == count);

      writer.write(baseWs);
    }

    writer.write(writeSeparator ? "],\n" : "]\n");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static void write(Writer writer, int field_symb_idx, ColumnBase[] columns, boolean flipCols, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
    String baseWs = new String(Array.repeat(' ', indentation));
    String entryWs = new String(Array.repeat(' ', indentation + 2));

    int count = 0;
    for (int i=0 ; i < columns.length ; i++)
      count += columns[i].size();

    if (indentFirstLine)
      writer.write(baseWs);
    writer.write(SymbTable.idxToStr(field_symb_idx));
    writer.write(": [");

    if (count > 0) {
      writer.write("\n");

      int written = 0;
      for (int i=0 ; i < columns.length ; i++) {
        ColumnBase col = columns[i];
        SurrObjMapper mapper = col.mapper;

        if (col instanceof IntColumn) {
          IntColumn intCol = (IntColumn) col;
          IntColumn.Iter it = intCol.getIter();
          while (!it.done()) {
            writer.write(entryWs);
            Obj key = mapper.surrToObj(it.getIdx());
            long value = it.getValue();
            if (flipCols) {
              writer.write(Long.toString(value));
              writer.write(", ");
              key.print(writer, Integer.MAX_VALUE, true, 0);
              written++;
              writer.write(written == 1 | written < count ? ";\n" : "\n");
            }
            else {
              key.print(writer, Integer.MAX_VALUE, true, 0);
              writer.write(" -> ");
              writer.write(Long.toString(value));
              written++;
              writer.write(written < count ? ",\n" : "\n");
            }
            it.next();
          }
        }
        else if (col instanceof FloatColumn) {
          FloatColumn floatCol = (FloatColumn) col;
          FloatColumn.Iter it = floatCol.getIter();
          while (!it.done()) {
            writer.write(entryWs);
            Obj key = mapper.surrToObj(it.getIdx());
            double value = it.getValue();
            if (flipCols) {
              writer.write(Double.toString(value));
              writer.write(", ");
              key.print(writer, Integer.MAX_VALUE, true, 0);
              written++;
              writer.write(written == 1 | written < count ? ";\n" : "\n");
            }
            else {
              key.print(writer, Integer.MAX_VALUE, true, 0);
              writer.write(" -> ");
              writer.write(Double.toString(value));
              written++;
              writer.write(written < count ? ",\n" : "\n");
            }
            it.next();
          }
        }
        else {
          ObjColumn objCol = (ObjColumn) col;
          ObjColumn.Iter it = objCol.getIter();
          while (!it.done()) {
            writer.write(entryWs);
            Obj key = mapper.surrToObj(it.getIdx());
            Obj value = it.getValue();
            if (flipCols) {
              value.print(writer, Integer.MAX_VALUE, true, 0);
              writer.write(", ");
              key.print(writer, Integer.MAX_VALUE, true, 0);
              written++;
              writer.write(written == 1 | written < count ? ";\n" : "\n");
            }
            else {
              key.print(writer, Integer.MAX_VALUE, true, 0);
              writer.write(" -> ");
              value.print(writer, Integer.MAX_VALUE, true, 0);
              written++;
              writer.write(written < count ? ",\n" : "\n");
            }
            it.next();
          }
        }
      }
      Miscellanea._assert(written == count);

      writer.write(baseWs);
    }

    writer.write(writeSeparator ? "],\n" : "]\n");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////


  public static void write(Writer writer, int field_symb_idx, TernaryTable[] tables, int col1, int col2, int col3, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
    String baseWs = new String(Array.repeat(' ', indentation));
    String entryWs = new String(Array.repeat(' ', indentation + 2));

    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (indentFirstLine)
      writer.write(baseWs);
    writer.write(SymbTable.idxToStr(field_symb_idx));
    writer.write(": [");

    if (count > 0) {
      writer.write("\n");

      int written = 0;
      for (int i=0 ; i < tables.length ; i++) {
        TernaryTable table = tables[i];
        SurrObjMapper mapper1 = table.mapper1;
        SurrObjMapper mapper2 = table.mapper2;
        SurrObjMapper mapper3 = table.mapper3;
        TernaryTable.Iter it = table.getIter();
        while (!it.done()) {
          writer.write(entryWs);
          Obj obj1 = mapper1.surrToObj(it.get1());
          Obj obj2 = mapper2.surrToObj(it.get2());
          Obj obj3 = mapper3.surrToObj(it.get3());

          if (col1 == 0)
            obj1.print(writer, Integer.MAX_VALUE, true, 0);
          else if (col1 == 1)
            obj2.print(writer, Integer.MAX_VALUE, true, 0);
          else
            obj3.print(writer, Integer.MAX_VALUE, true, 0);

          writer.write(", ");

          if (col2 == 0)
            obj1.print(writer, Integer.MAX_VALUE, true, 0);
          else if (col2 == 1)
            obj2.print(writer, Integer.MAX_VALUE, true, 0);
          else
            obj3.print(writer, Integer.MAX_VALUE, true, 0);

          writer.write(", ");

          if (col3 == 0)
            obj1.print(writer, Integer.MAX_VALUE, true, 0);
          else if (col3 == 1)
            obj2.print(writer, Integer.MAX_VALUE, true, 0);
          else
            obj3.print(writer, Integer.MAX_VALUE, true, 0);

          written++;
          writer.write(written < count || count == 1 ? ";\n" : "\n");
          it.next();
        }
      }
      Miscellanea._assert(written == count);

      writer.write(baseWs);
    }

    writer.write(writeSeparator ? "],\n" : "]\n");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static void write(Writer writer, int field_symb_idx, SymBinaryTable[] tables, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
    String baseWs = new String(Array.repeat(' ', indentation));
    String entryWs = new String(Array.repeat(' ', indentation + 2));

    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (indentFirstLine)
      writer.write(baseWs);
    writer.write(SymbTable.idxToStr(field_symb_idx));
    writer.write(": [");

    if (count > 0) {
      writer.write("\n");

      int written = 0;
      for (int i=0 ; i < tables.length ; i++) {
        SymBinaryTable table = tables[i];
        SurrObjMapper mapper = table.mapper;
        SymBinaryTable.Iter it = table.getIter();
        while (!it.done()) {
          writer.write(entryWs);
          Obj obj1 = mapper.surrToObj(it.get1());
          Obj obj2 = mapper.surrToObj(it.get2());
          obj1.print(writer, Integer.MAX_VALUE, true, 0);
          writer.write(", ");
          obj2.print(writer, Integer.MAX_VALUE, true, 0);
          written++;
          writer.write(written < count || count == 1 ? ";\n" : "\n");
          it.next();
        }
      }
      Miscellanea._assert(written == count);

      writer.write(baseWs);
    }

    writer.write(writeSeparator ? "],\n" : "]\n");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static void write(Writer writer, int field_symb_idx, Sym12TernaryTable[] tables, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
    String baseWs = new String(Array.repeat(' ', indentation));
    String entryWs = new String(Array.repeat(' ', indentation + 2));

    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].size();

    if (indentFirstLine)
      writer.write(baseWs);
    writer.write(SymbTable.idxToStr(field_symb_idx));
    writer.write(": [");

    if (count > 0) {
      writer.write("\n");

      int written = 0;
      for (int i=0 ; i < tables.length ; i++) {
        Sym12TernaryTable table = tables[i];
        SurrObjMapper mapper12 = table.mapper12;
        SurrObjMapper mapper3 = table.mapper3;
        Sym12TernaryTable.Iter it = table.getIter();
        while (!it.done()) {
          writer.write(entryWs);
          Obj obj1 = mapper12.surrToObj(it.get1());
          Obj obj2 = mapper12.surrToObj(it.get2());
          Obj obj3 = mapper3.surrToObj(it.get3());
          obj1.print(writer, Integer.MAX_VALUE, true, 0);
          writer.write(", ");
          obj2.print(writer, Integer.MAX_VALUE, true, 0);
          writer.write(", ");
          obj3.print(writer, Integer.MAX_VALUE, true, 0);
          written++;
          writer.write(written < count || count == 1 ? ";\n" : "\n");
          it.next();
        }
      }
      Miscellanea._assert(written == count);

      writer.write(baseWs);
    }

    writer.write(writeSeparator ? "],\n" : "]\n");
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // public static void write(Writer writer, int field_symb_idx, AssocTable[] tables, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
  //   throw new RuntimeException();
  // }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // public static void write(Writer writer, int field_symb_idx, SlaveTernTable[] tables, int indentation, boolean indentFirstLine, boolean writeSeparator) throws IOException {
  //   throw new RuntimeException();
  // }
}
