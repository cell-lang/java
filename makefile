SRC-FILES=$(shell ls src/cell/*.cell src/cell/code-gen/*.cell)
RUNTIME-FILES=$(shell ls src/java/net/cell_lang/*)

################################################################################
###################### Level 3 AST -> Java code generator ######################

tmp/codegen.cpp: $(SRC-FILES)
	cellc projects/codegen.txt
	rm -rf tmp/*
	# ../build/bin/ren-fns < generated.cpp > tmp/cellc-java.cpp
	# echo >> tmp/cellc-java.cpp
	# echo >> tmp/cellc-java.cpp
	# cat ../build/src/hacks.cpp >> tmp/cellc-java.cpp
	mv generated.cpp tmp/codegen.cpp

codegen: tmp/codegen.cpp
	g++ -O1 tmp/codegen.cpp -o codegen

codegen-dbg: tmp/codegen.cpp
	g++ -ggdb -DNDEBUG tmp/codegen.cpp -o codegen

################################################################################
################################################################################

clean:
	@rm -rf tmp
	@rm -f codegen codegen-dbg
	@rm -f cellc-java cellcd-java
	@mkdir tmp