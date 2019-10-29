#!/usr/bin/env python

################################################################################

src_ArrayObjs                     = 'core/net/cell_lang/ArrayObjs.java'
src_BinRelIter                    = 'core/net/cell_lang/BinRelIter.java'
src_BlankObj                      = 'core/net/cell_lang/BlankObj.java'
src_Builder                       = 'core/net/cell_lang/Builder.java'
src_Canonical                     = 'core/net/cell_lang/Canonical.java'
src_EmptyRelObj                   = 'core/net/cell_lang/EmptyRelObj.java'
src_EmptySeqObj                   = 'core/net/cell_lang/EmptySeqObj.java'
src_FloatArrayObjs                = 'core/net/cell_lang/FloatArrayObjs.java'
src_FloatObj                      = 'core/net/cell_lang/FloatObj.java'
src_IntArrayObjs                  = 'core/net/cell_lang/IntArrayObjs.java'
src_IntObj                        = 'core/net/cell_lang/IntObj.java'
src_NeBinRelObj                   = 'core/net/cell_lang/NeBinRelObj.java'
src_NeFloatSeqObj                 = 'core/net/cell_lang/NeFloatSeqObj.java'
src_NeIntSeqObj                   = 'core/net/cell_lang/NeIntSeqObj.java'
src_NeSeqObj                      = 'core/net/cell_lang/NeSeqObj.java'
src_NeSetObj                      = 'core/net/cell_lang/NeSetObj.java'
src_NeTernRelObj                  = 'core/net/cell_lang/NeTernRelObj.java'
src_NeTreeMapObj                  = 'core/net/cell_lang/NeTreeMapObj.java'
src_NeTreeSetObj                  = 'core/net/cell_lang/NeTreeSetObj.java'
src_NullObj                       = 'core/net/cell_lang/NullObj.java'
src_Obj                           = 'core/net/cell_lang/Obj.java'
src_OptTagRecObj                  = 'core/net/cell_lang/OptTagRecObj.java'
src_Procs                         = 'core/net/cell_lang/Procs.java'
src_RecordObj                     = 'core/net/cell_lang/RecordObj.java'
src_SeqIter                       = 'core/net/cell_lang/SeqIter.java'
src_SeqObj                        = 'core/net/cell_lang/SeqObj.java'
src_SetIter                       = 'core/net/cell_lang/SetIter.java'
src_SymbObj                       = 'core/net/cell_lang/SymbObj.java'
src_SymbTable                     = 'core/net/cell_lang/SymbTable.java'
src_TaggedIntObj                  = 'core/net/cell_lang/TaggedIntObj.java'
src_TaggedObj                     = 'core/net/cell_lang/TaggedObj.java'
src_TernRelIter                   = 'core/net/cell_lang/TernRelIter.java'

src_AbstractLongSorter            = 'algorithms/net/cell_lang/AbstractLongSorter.java'
src_Algs                          = 'algorithms/net/cell_lang/Algs.java'
src_Ints                          = 'algorithms/net/cell_lang/Ints.java'
src_Ints123                       = 'algorithms/net/cell_lang/Ints123.java'
src_Ints12                        = 'algorithms/net/cell_lang/Ints12.java'
src_Ints21                        = 'algorithms/net/cell_lang/Ints21.java'
src_Ints231                       = 'algorithms/net/cell_lang/Ints231.java'
src_Ints312                       = 'algorithms/net/cell_lang/Ints312.java'

src_Array                         = 'utils/net/cell_lang/Array.java'
src_AutoProcs                     = 'utils/net/cell_lang/AutoProcs.java'
src_CharStream                    = 'utils/net/cell_lang/CharStream.java'
src_Conversions                   = 'utils/net/cell_lang/Conversions.java'
src_DateTime                      = 'utils/net/cell_lang/DateTime.java'
src_Hashing                       = 'utils/net/cell_lang/Hashing.java'
src_IntCtrs                       = 'utils/net/cell_lang/IntCtrs.java'
src_IntIdxMap                     = 'utils/net/cell_lang/IntIdxMap.java'
src_IntIntMap                     = 'utils/net/cell_lang/IntIntMap.java'
src_IntLongMap                    = 'utils/net/cell_lang/IntLongMap.java'
src_IntObjMap                     = 'utils/net/cell_lang/IntObjMap.java'
src_Miscellanea                   = 'utils/net/cell_lang/Miscellanea.java'
src_ObjPrinter                    = 'utils/net/cell_lang/ObjPrinter.java'
src_ObjVisitor                    = 'utils/net/cell_lang/ObjVisitor.java'
src_Parser                        = 'utils/net/cell_lang/Parser.java'
src_ParsingException              = 'utils/net/cell_lang/ParsingException.java'
src_SurrObjMapper                 = 'utils/net/cell_lang/SurrObjMapper.java'
src_SurrSet                       = 'utils/net/cell_lang/SurrSet.java'
src_SymbTableFastCache            = 'utils/net/cell_lang/SymbTableFastCache.java'
src_TextWriter                    = 'utils/net/cell_lang/TextWriter.java'
src_Tokenizer                     = 'utils/net/cell_lang/Tokenizer.java'
src_TokenStream                   = 'utils/net/cell_lang/TokenStream.java'
src_TokenType                     = 'utils/net/cell_lang/TokenType.java'

src_ArrayIter                     = 'automata/net/cell_lang/ArrayIter.java'
src_ArraySliceAllocator           = 'automata/net/cell_lang/ArraySliceAllocator.java'
src_BinaryTable                   = 'automata/net/cell_lang/BinaryTable.java'
src_BinaryTableUpdater            = 'automata/net/cell_lang/BinaryTableUpdater.java'
src_ColumnBase                    = 'automata/net/cell_lang/ColumnBase.java'
src_FloatColumn                   = 'automata/net/cell_lang/FloatColumn.java'
src_FloatColumnUpdater            = 'automata/net/cell_lang/FloatColumnUpdater.java'
src_ForeignKeyViolationException  = 'automata/net/cell_lang/ForeignKeyViolationException.java'
src_Index                         = 'automata/net/cell_lang/Index.java'
src_IntColumn                     = 'automata/net/cell_lang/IntColumn.java'
src_IntColumnUpdater              = 'automata/net/cell_lang/IntColumnUpdater.java'
src_IntStore                      = 'automata/net/cell_lang/IntStore.java'
src_IntStoreUpdater               = 'automata/net/cell_lang/IntStoreUpdater.java'
src_KeyViolationException         = 'automata/net/cell_lang/KeyViolationException.java'
src_ObjColumn                     = 'automata/net/cell_lang/ObjColumn.java'
src_ObjColumnUpdater              = 'automata/net/cell_lang/ObjColumnUpdater.java'
src_ObjStore                      = 'automata/net/cell_lang/ObjStore.java'
src_ObjStoreUpdater               = 'automata/net/cell_lang/ObjStoreUpdater.java'
src_OneWayBinTable                = 'automata/net/cell_lang/OneWayBinTable.java'
src_OverflowTable                 = 'automata/net/cell_lang/OverflowTable.java'
src_RelAutoBase                   = 'automata/net/cell_lang/RelAutoBase.java'
src_RelAutoUpdaterBase            = 'automata/net/cell_lang/RelAutoUpdaterBase.java'
src_Sym12TernaryTable             = 'automata/net/cell_lang/Sym12TernaryTable.java'
src_Sym12TernaryTableUpdater      = 'automata/net/cell_lang/Sym12TernaryTableUpdater.java'
src_SymBinaryTable                = 'automata/net/cell_lang/SymBinaryTable.java'
src_SymBinaryTableUpdater         = 'automata/net/cell_lang/SymBinaryTableUpdater.java'
src_TernaryTable                  = 'automata/net/cell_lang/TernaryTable.java'
src_TernaryTableUpdater           = 'automata/net/cell_lang/TernaryTableUpdater.java'
src_UnaryTable                    = 'automata/net/cell_lang/UnaryTable.java'
src_UnaryTableUpdater             = 'automata/net/cell_lang/UnaryTableUpdater.java'
src_ValueStore                    = 'automata/net/cell_lang/ValueStore.java'
src_ValueStoreUpdater             = 'automata/net/cell_lang/ValueStoreUpdater.java'

################################################################################

std_sources = [
  src_ArrayObjs,
  src_BinRelIter,
  src_BlankObj,
  src_Builder,
  src_Canonical,
  src_EmptyRelObj,
  src_EmptySeqObj,
  src_FloatArrayObjs,
  src_FloatObj,
  src_IntArrayObjs,
  src_IntObj,
  src_NeBinRelObj,
  src_NeFloatSeqObj,
  src_NeIntSeqObj,
  src_NeSeqObj,
  src_NeSetObj,
  src_NeTernRelObj,
  src_NeTreeMapObj,
  src_NeTreeSetObj,
  src_NullObj,
  src_Obj,
  src_OptTagRecObj,
  src_Procs,
  src_RecordObj,
  src_SeqIter,
  src_SeqObj,
  src_SetIter,
  src_SymbObj,
  src_SymbTable,
  src_TaggedIntObj,
  src_TaggedObj,
  src_TernRelIter,

  src_AbstractLongSorter,
  src_Algs,
  src_Ints,
  src_Ints123,
  src_Ints12,
  src_Ints21,
  src_Ints231,
  src_Ints312,

  src_Array,
  src_CharStream,
  src_DateTime,
  src_Hashing,
  src_IntCtrs,
  src_IntIdxMap,
  src_IntIntMap,
  src_IntLongMap,
  src_IntObjMap,
  src_Miscellanea,
  src_ObjPrinter,
  src_ObjVisitor,
  src_Parser,
  src_ParsingException,
  src_SurrObjMapper,
  src_SurrSet,
  src_SymbTableFastCache,
  src_Tokenizer,
  src_TokenStream,
  src_TokenType
]

table_sources = [
  src_ArrayIter,
  src_ArraySliceAllocator,
  src_AutoProcs,
  src_BinaryTable,
  src_BinaryTableUpdater,
  src_ColumnBase,
  src_FloatColumn,
  src_FloatColumnUpdater,
  src_ForeignKeyViolationException,
  src_Index,
  src_IntColumn,
  src_IntColumnUpdater,
  src_IntStore,
  src_IntStoreUpdater,
  src_KeyViolationException,
  src_ObjColumn,
  src_ObjColumnUpdater,
  src_ObjStore,
  src_ObjStoreUpdater,
  src_OneWayBinTable,
  src_OverflowTable,
  src_RelAutoBase,
  src_RelAutoUpdaterBase,
  src_Sym12TernaryTable,
  src_Sym12TernaryTableUpdater,
  src_SymBinaryTable,
  src_SymBinaryTableUpdater,
  src_TernaryTable,
  src_TernaryTableUpdater,
  src_UnaryTable,
  src_UnaryTableUpdater,
  src_ValueStore,
  src_ValueStoreUpdater,

  src_TextWriter
]

interface_sources = [
  src_Conversions
]

################################################################################

num_of_tabs = 0

def escape(ch):
  if ch == ord('\\'):
    return '\\\\'
  elif ch == ord('"'):
    return '\\"'
  elif ch >= ord(' ') or ch <= ord('~'):
    return chr(ch)
  elif ch == ord('\t'):
    global num_of_tabs
    num_of_tabs += 1
    return '\\t'
  else:
    print 'Invalid character: ' + ch
    exit(1);


def merge_lines(lines):
  merged_lines = []
  curr_line = ""
  for l in lines:
    if l:
      if len(curr_line) + len(l) > 2000:
        merged_lines.append(curr_line)
        curr_line = ""
      if curr_line:
        curr_line += "\\n"
      curr_line += l
  if curr_line:
    merged_lines.append(curr_line);
  return merged_lines


def convert_file(file_name, keep_all):
  lines = []
  f = open(file_name)
  past_header = False
  header = []
  for l in f:
    l = l.rstrip()
    past_header = past_header or not (l == "" or l.startswith('package ') or l.startswith('import '))
    if keep_all or past_header:
      el = ''.join([escape(ord(ch)) for ch in l])
      if past_header:
        lines.append(el)
      else:
        header.append(el)

  return ['"' + l + '"' for l in header + merge_lines(lines)]


# def to_code(bytes):
#   count = len(bytes)
#   ls = []
#   l = ' '
#   for i, b in enumerate(bytes):
#     l += ' ' + str(b) + (',' if i < count-1 else '')
#     if len(l) > 80:
#       ls.append(l)
#       l = ' '
#   if l:
#     ls.append(l)
#   return ls


def convert_files(directory, file_names, keep_all):
  ls = []
  for i, f in enumerate(file_names):
    if i > 0:
      ls.extend(['""', '""'])
    ls.extend(convert_file(directory + '/' + f, keep_all))
  return ['  ' + l for l in ls]


def data_array_def(array_name, directory, file_names, keep_all):
  lines = convert_files(directory, file_names, keep_all)
  # code = to_code(lines)
  if len(lines) <= 500:
    lines = [l + (',' if i < len(lines) - 1 else '') for i, l in enumerate(lines)]
    return ['String* ' + array_name + ' = ('] + lines + [');']
  code = []
  count = (len(lines) + 499) / 500;
  for i in range(count):
    code += ['String* ' + array_name + '_' + str(i) + ' = (']
    chunk = lines[500 * i : 500 * (i + 1)]
    code += [l + (',' if i < len(chunk) - 1 else '') for i, l in enumerate(chunk)]
    code += [');', '', '']
  pieces = [array_name + '_' + str(i) for i in range(count)]
  code += ['String* ' + array_name + ' = ' + ' & '.join(pieces) + ';']
  return code

################################################################################

from sys import argv, exit

if len(argv) != 4:
  print 'Usage: ' + argv[0] + ' <input directory> <output file> <empty output file>'
  exit(0)

_, input_dir, out_fname, empty_out_fname = argv

file_data = [
  data_array_def('core_runtime', input_dir, std_sources, False),
  data_array_def('table_runtime', input_dir, table_sources, False),
  data_array_def('interface_runtime', input_dir, interface_sources, False)
]

out_file = open(out_fname, 'w')
for i, f in enumerate(file_data):
  if i > 0:
    out_file.write('\n\n')
  for l in f:
    out_file.write(l + '\n');

empty_file_data = [
  data_array_def('core_runtime', input_dir, [], False),
  data_array_def('table_runtime', input_dir, [], False),
  data_array_def('interface_runtime', input_dir, [], False)
]

empty_out_file = open(empty_out_fname, 'w')
for i, f in enumerate(empty_file_data):
  if i > 0:
    empty_out_file.write('\n\n')
  for l in f:
    empty_out_file.write(l + '\n')
