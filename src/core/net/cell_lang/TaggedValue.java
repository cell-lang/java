package net.cell_lang;


class TaggedValue extends ValueBase {
  int tagId;
  ValueBase value;

  public TaggedValue(int tagId, ValueBase value) {
    this.tagId = tagId;
    this.value = value;
  }

  public boolean isTagged() {
    return true;
  }

  public String tag() {
    return SymbTable.idxToStr(tagId);
  }

  public Value untagged() {
    return value;
  }

  public boolean isString() {
    if (tagId != SymbTable.StringSymbId)
      return false;
    if (!value.isSeq())
      return false;
    int len = value.size();
    for (int i=0 ; i < len ; i++) {
      Value item = value.item(i);
      if (!item.isInt() || item.asLong() > 65535)
        return false;
    }
    return true;
  }

  public String asString() {
    if (!isString())
      throw new UnsupportedOperationException();
    int len = value.size();
    char[] chars = new char[len];
    for (int i=0 ; i < len ; i++) {
      long code = value.item(i).asLong();
      // if (code > 65535)
      //  throw new UnsupportedOperationException(); // Char.ConvertFromUtf32
      chars[i] = (char) code;
    }
    return new String(chars);
  }

  public Value lookup(String field) {
    return value.lookup(field);
  }

  public Obj asObj() {
    return new TaggedObj(tagId, value.asObj());
  }
}
