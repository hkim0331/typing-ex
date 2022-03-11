all: run

out.txt:
	bb excerpt.clj *.typ > out.txt

run: out.txt
	bb partition.clj 4 $^

clean:
	${RM} out.txt out/*.txt

