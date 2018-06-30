package net.cell_lang;

import java.util.IdentityHashMap;


class Hacks {
  static IdentityHashMap<Obj, Obj> attachments = new IdentityHashMap<Obj, Obj>();

  static public void attach(Obj target, Obj attachment) {
    attachments.remove(target);
    attachments.put(target, attachment);
  }

  static public Obj fetch(Obj target) {
    Obj attachment = attachments.get(target);
    if (attachment != null)
      return Builder.createTaggedObj(SymbTable.JustSymbId, attachment);
    else
      return SymbObj.get(SymbTable.NothingSymbId);
  }

  //////////////////////////////////////////////////////////////////////////////

  static IdentityHashMap<Obj, Obj> cachedSourceFileLocation = new IdentityHashMap<Obj, Obj>();

  static public void setSourceFileLocation(Obj ast, Obj value) {
    cachedSourceFileLocation.put(ast, value);
  }

  static public Obj getSourceFileLocation(Obj ast) {
    return cachedSourceFileLocation.get(ast);
  }
}
