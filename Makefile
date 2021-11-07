JFLAGS = -cp "bin;."
PROCESADOR = src/*.java
APP_RUN = Procesador

.PHONY: compile
compile:
	@javac -d bin $(JFLAGS) $(PROCESADOR)

run-app:
	@java $(JFLAGS) $(APP_RUN)

.PHONY: clean
clean:
	rm -r bin