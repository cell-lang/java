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

tmp/codegen.cs: $(SRC-FILES)
	cellc-cs.exe projects/codegen.txt
	rm -rf tmp/*
	mv generated.cs tmp/codegen.cs

codegen.exe: tmp/codegen.cs
	mcs -nowarn:219 tmp/codegen.cs -out:codegen.exe

codegen.jar: $(SRC-FILES) $(RUNTIME-FILES)
	java -jar bin/compiler.jar projects/codegen.txt
	javac -d tmp/codegen/ Generated.java
	mv Generated.java tmp/codegen.java
	jar cfe codegen.jar net.cell_lang.Generated -C tmp/codegen/ net/

compiler.jar: $(SRC-FILES) $(RUNTIME-FILES)
	java -jar bin/compiler.jar projects/java-compiler-no-runtime.txt
	bin/apply-hacks < Generated.java > src/java/net/cell_lang/Generated.java
	# javac -g -d tmp/ src/java/net/cell_lang/Generated.java
	javac -d tmp/ src/java/net/cell_lang/Generated.java
	mv Generated.java tmp/
	jar cfe compiler.jar net.cell_lang.Generated -C tmp net/

inputs/tests.txt: tests.cell
	cellc-cs.exe -p projects/tests.txt
	mv dump-opt-code.txt inputs/tests.txt

tests.jar: codegen.exe inputs/tests.txt
	./codegen.exe inputs/tests.txt
	javac -g -d tmp/ Generated.java
	cp Generated.java src/java/net/cell_lang/
	mv Generated.java tmp/tests-by-codegen.exe.java

################################################################################
################################################################################

clean:
	@rm -f codegen.exe codegen codegen-dbg
	@make -s soft-clean

soft-clean:
	@rm -f generated.cpp generated.cs Generated.java
	@rm -f *.class
	@rm -f cellc-java cellcd-java
	@rm -rf tmp
	@mkdir tmp
	@mkdir tmp/codegen