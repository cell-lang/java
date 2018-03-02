package net.cell_lang;


class TaggedValue extends ValueBase {
  int tagId;
  ValueBase value;

  public TaggedValue(int tagId, ValueBase value) {
    this.tagId = tagId;
    this.value = value;
  }

  public boolean IsTagged() {
    return true;
  }

  public String Tag() {
    return SymbTable.IdxToStr(tagId);
  }

  public Value Untagged() {
    return value;
  }

  public boolean IsString() {
    if (tagId != SymbTable.StringSymbId)
      return false;
    if (!value.IsSeq())
      return false;
    int len = value.Size();
    for (int i=0 ; i < len ; i++) {
      Value item = value.Item(i);
      if (!item.IsInt() || item.AsLong() > 65535)
        return false;
    }
    return true;
  }

  public String AsString() {
    if (!IsString())
      throw new UnsupportedOperationException();
    int len = value.Size();
    char[] chars = new char[len];
    for (int i=0 ; i < len ; i++) {
      long code = value.Item(i).AsLong();
      // if (code > 65535)
      //  throw new UnsupportedOperationException(); // Char.ConvertFromUtf32
      chars[i] = (char) code;
    }
    return new String(chars);
  }

  public Value Lookup(String field) {
    return value.Lookup(field);
  }

  public Obj AsObj() {
    return new TaggedObj(tagId, value.AsObj());
  }
}
