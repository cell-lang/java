package net.cell_lang;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;


abstract class RelAutoBase {
  public abstract void loadState(Reader reader) throws IOException;
  public abstract void writeState(Writer writer) throws IOException;
  public abstract boolean fullCheck();
}