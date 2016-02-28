// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, the
// program clears the screen, i.e. writes "white" in every pixel.

// Put your code here.

(START)
//initialize CurrentPos with SCREEN address(16384)
	@SCREEN
	D=A
	@CurrentPos
	M=D

//check if a key is pressed
	@KBD	
	D=M

//CurrentColor is set to corresponding color
	@BLACK
	D;JGT

(WHITE)
	@CurrentColor
	M=0
	@LOOP
	0;JMP

(BLACK)
	@CurrentColor
	M=-1

(LOOP)
	@CurrentColor
	D=M

	@CurrentPos		//CurrentPos=CurrentColor
	A=M
	M=D

	@CurrentPos		//CurrentPos=CurrentPos+1
	MD=M+1

	@KBD
	D=D-A
	@START
	D;JGE

	@LOOP
	0;JMP

(END)
	@END
	0;JMP