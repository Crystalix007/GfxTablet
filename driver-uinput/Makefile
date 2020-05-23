TARGET  := networktablet
LIBS    := cairo libpsx x11
CFLAGS  := `pkg-config --cflags ${LIBS}`
LDFLAGS := `pkg-config --libs ${LIBS}`
PREFIX  := /usr/local
BINDIR  := ${PREFIX}/bin

networktablet : networktablet.c

clean :
	rm -f networktablet

install :
	install -D networktablet ${DESTDIR}${BINDIR}/networktablet
