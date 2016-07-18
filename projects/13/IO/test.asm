@cursorx
M=0

@cursory
M=0

@mask
M=0

@firsttime
M=1

	@io_readorwrite
	M=0

	@RETURN_FROM_IO_1
	D=A
	@io_retaddress
	M=D

	@IO
	0;JMP

	(RETURN_FROM_IO_1)

	@io_number
	D=M

	@num1
	M=D


	@io_readorwrite
	M=0

	@RETURN_FROM_IO_2
	D=A
	@io_retaddress
	M=D

	@IO
	0;JMP

	(RETURN_FROM_IO_2)

	@io_number
	D=M

	@num2
	M=D


	@sum
	M=D

	@num1
	D=M

	@sum
	MD=D+M

	@io_number
	M=D

	@io_readorwrite
	M=1
						
	@RETURN_FROM_IO_3
	D=A
	@io_retaddress
	M=D

	@IO
	0;JMP

	(RETURN_FROM_IO_3)

	@num1
	D=M

	@diff
	M=D

	@num2
	D=M

	@diff
	MD=M-D

	@io_number
	M=D

	@io_readorwrite
	M=1
						
	@RETURN_FROM_IO_4
	D=A
	@io_retaddress
	M=D

	@IO
	0;JMP

	(RETURN_FROM_IO_4)

						@io_number
						M=0

						@io_readorwrite
						M=0

						@RETURN_FROM_IO_5
						D=A
						@io_retaddress
						M=D

						@IO
						0;JMP

						(RETURN_FROM_IO_5)

						@io_number
						D=M

						@num3
						M=D


						@io_number
						M=0

						@io_readorwrite
						M=0

						@RETURN_FROM_IO_6
						D=A
						@io_retaddress
						M=D

						@IO
						0;JMP

						(RETURN_FROM_IO_6)

						@io_number
						D=M

						@num4
						M=D


						@sum2
						M=D

						@num3
						D=M

						@sum2
						MD=D+M

						@io_number
						M=D

						@io_readorwrite
						M=1
											
						@RETURN_FROM_IO_7
						D=A
						@io_retaddress
						M=D

						@IO
						0;JMP

						(RETURN_FROM_IO_7)

						@num3
						D=M

						@diff2
						M=D

						@num4
						D=M

						@diff2
						MD=M-D

						@io_number
						M=D

						@io_readorwrite
						M=1
											
						@RETURN_FROM_IO_8
						D=A
						@io_retaddress
						M=D

						@IO
						0;JMP

						(RETURN_FROM_IO_8)

(END)
	@END
	0;JMP


(IO)

@firsttime
D=M
@ELSE_TYPE
D;JEQ

	@io_readorwrite
	D=M
	
	@previoustype
	M=D

	@currenttype
	M=D

	@firsttime
	M=0

	@END_TYPE
	0;JMP

(ELSE_TYPE)

	@currenttype
	D=M

	@previoustype
	M=D

	@io_readorwrite
	D=M

	@currenttype
	M=D

(END_TYPE)

	@previoustype
	D=M
	@currenttype
	D=D-M
	@CONTINUE_LABEL_5
	D;JEQ
		
		@cursorx
		M=0

		@cursory
		M=M+1

		@mask
		M=0

	(CONTINUE_LABEL_5)


@256
D=A
@SP
M=D

@io_readorwrite
D=M

@READ
D;JEQ

@io_number
D=M

@PRINT_NUMBER
D;JGE

(PRINT_BMP_MINUS)
		
	@SP
	M=M+1
	A=M-1
	M=-1

	@RETURN_PRINT_CHAR_3
	D=A
	@SP
	M=M+1
	A=M-1
	M=D

	@PRINT_CHAR
	0;JMP

	(RETURN_PRINT_CHAR_3)

@io_number
M=-M

(PRINT_NUMBER)

@20000
D=A
@subtractor
M=D

@startprinting
M=0

(WRITE)

	(CHECK_20000)
	@20000
	D=A
	@subtractor
	D=D-M
	
	@CHECK_10000
	D;JNE

		@10000
		D=A
		@subtractor
		M=D

		@END_SET
		0;JMP

	(CHECK_10000)
	@10000
	D=A
	@subtractor
	D=D-M

	@CHECK_1000
	D;JNE

		@1000
		D=A
		@subtractor
		M=D

		@END_SET
		0;JMP

	(CHECK_1000)
	@1000
	D=A
	@subtractor
	D=D-M

	@CHECK_100
	D;JNE

		@100
		D=A
		@subtractor
		M=D

		@END_SET
		0;JMP

	(CHECK_100)
	@100
	D=A
	@subtractor
	D=D-M

	@CHECK_10
	D;JNE

		@10
		D=A
		@subtractor
		M=D

		@END_SET
		0;JMP

	(CHECK_10)
	@10
	D=A
	@subtractor
	D=D-M

	@CHECK_1
	D;JNE

		@subtractor
		M=1

		@startprinting
		M=1

		@END_SET
		0;JMP

	(CHECK_1)
	@subtractor
	M=0

	(END_SET)

	@subtractor
	D=M-1
	@END_WRITE
	D;JLT

		@io_number
		D=M

		@digit
		M=0

		(DIGIT_LOOP)
			@subtractor
			D=D-M

			@END_DIGIT_LOOP
			D;JLT

			@digit
			M=M+1

			@DIGIT_LOOP
			0;JMP
		(END_DIGIT_LOOP)

		@subtractor
		D=D+M
		@io_number
		M=D

		@digit
		D=M

			@CONTINUE_PRINTING
			D;JEQ

				(SET_START_PRINTING)
				@startprinting
				M=1

			(CONTINUE_PRINTING)

			@startprinting
			D=M-1

			@WRITE
			D;JNE

		@digit
		D=M
		@SP
		M=M+1
		A=M-1
		M=D

		@RETURN_PRINT_CHAR_1
		D=A
		@SP
		M=M+1
		A=M-1
		M=D

		@PRINT_CHAR
		0;JMP

		(RETURN_PRINT_CHAR_1)

		@WRITE
		0;JMP

(END_WRITE)

@END_IO
0;JMP

(READ)

	@io_number
	M=0

	(READ_LINE)

		(KEY_PRESSED_LOOP)
			@KBD
			D=M

			@END_KEY_PRESSED_LOOP
			D;JNE

			@KEY_PRESSED_LOOP
			0;JMP

		(END_KEY_PRESSED_LOOP)

		@key
		M=D

		(KEY_RELEASED_LOOP)
			@KBD
			D=M

			@END_KEY_RELEASED_LOOP
			D;JEQ

			@KEY_RELEASED_LOOP
			0;JMP

		(END_KEY_RELEASED_LOOP)	

		@128
		D=A
		@key
		D=D-M

		@END_READ_LINE
		D;JEQ

		@48
		D=A
		@key
		M=M-D

			@key
			D=M
			@SP
			M=M+1
			A=M-1
			M=D

			@RETURN_PRINT_CHAR_2
			D=A
			@SP
			M=M+1
			A=M-1
			M=D

			@PRINT_CHAR
			0;JMP

			(RETURN_PRINT_CHAR_2)


		@io_number
		D=M
		@SP
		M=M+1
		A=M-1
		M=D

		@10
		D=A
		@SP
		M=M+1
		A=M-1
		M=D

		@RET_FROM_MUL_1
		D=A
		@SP
		M=M+1
		A=M-1
		M=D

		@MULTIPLY
		0;JMP

		(RET_FROM_MUL_1)

			@SP
			AM=M-1
			D=M

			@io_number
			M=D

		@key
		D=M
		@io_number
		M=M+D

		@READ_LINE
		0;JMP

	(END_READ_LINE)

(END_WRITE)

(END_IO)

	@RET_FROM_MOVE_CURSOR_2
	D=A
	@SP
	M=M+1
	A=M-1
	M=D

	@MOVE_CURSOR
	0;JMP

	(RET_FROM_MOVE_CURSOR_2)

@io_retaddress
A=M
0;JMP


(PRINT_CHAR)
		
		@2
		D=A
		@SP
		A=M-D
		D=M

		@PUSH_BMP_MINUS
		D;JLT

		@PUSH_BMP_0
		D;JEQ

		D=D-1
		@PUSH_BMP_1
		D;JEQ

		D=D-1
		@PUSH_BMP_2
		D;JEQ

		D=D-1
		@PUSH_BMP_3
		D;JEQ

		D=D-1
		@PUSH_BMP_4
		D;JEQ

		D=D-1
		@PUSH_BMP_5
		D;JEQ

		D=D-1
		@PUSH_BMP_6
		D;JEQ

		D=D-1
		@PUSH_BMP_7
		D;JEQ

		D=D-1
		@PUSH_BMP_8
		D;JEQ

		D=D-1
		@PUSH_BMP_9
		D;JEQ

		@PUSH_BMP_NIL
		0;JMP

		(PUSH_BMP_MINUS)
				
				@SP
				M=M+1
				A=M-1
				M=0

				@SP
				M=M+1
				A=M-1
				M=0

				@SP
				M=M+1
				A=M-1
				M=0

				@SP
				M=M+1
				A=M-1
				M=0

				@SP
				M=M+1
				A=M-1
				M=0

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=0

				@SP
				M=M+1
				A=M-1
				M=0

				@SP
				M=M+1
				A=M-1
				M=0				

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_0)
				
				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_1)

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@14
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@15
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_2)

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@24
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@6
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@3
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_3)

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@28
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP
		
		(PUSH_BMP_4)

				@16
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@24
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@28
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@26
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@25
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@24
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@24
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@60
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_5)

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@3
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@3
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@31
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_6)

				@28
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@6
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@3
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@3
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@31
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_7)

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@49
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@24
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@12
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_8)

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_9)

				@30
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@51
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@62
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@48
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@24
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@14
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_BMP_NIL)

				@63
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

				@SP
				M=M+1
				A=M-1
				M=D

			@PUSH_GAP
			0;JMP

		(PUSH_GAP)

			@SP
			M=M+1
			A=M-1
			M=0

			@SP
			M=M+1
			A=M-1
			M=0

		(PRINT_BMP)

		@11
		D=A
		@SP
		D=M-D
		@row_bmp_location
		M=D

			@cursory
			D=M
			@SP
			M=M+1
			A=M-1
			M=D

			@352
			D=A
			@SP
			M=M+1
			A=M-1
			M=D

			@RET_FROM_MUL_2
			D=A
			@SP
			M=M+1
			A=M-1
			M=D

			@MULTIPLY
			0;JMP

			(RET_FROM_MUL_2)

			@SP
			AM=M-1
			D=M

			@address
			M=D

			@cursorx
			D=M
			@address
			M=M+D

		@i
		M=0

		(PRINT_LOOP)

			@i
			D=M
			@10
			D=D-A
			@END_LOOP
			D;JGT

			@mask
			D=M
			@CONTINUE_LABEL
			D;JEQ

				@row_bmp_location
				A=M
				D=M
				@SP
				M=M+1
				A=M-1
				M=D

				@256
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@RET_FROM_MUL_3
				D=A
				@SP
				M=M+1
				A=M-1
				M=D

				@MULTIPLY
				0;JMP

				(RET_FROM_MUL_3)

				@SP
				AM=M-1
				D=M

				@row_bmp_location
				A=M
				M=D

			(CONTINUE_LABEL)
			
			@address
			D=M
			@SCREEN
			D=A+D
			@R13
			M=D
				
				@mask
				D=M
				@ELSE_MASK_1
				D;JNE

					@256
					D=-A

					@END_MASK_1
					0;JMP

				(ELSE_MASK_1)

					@255
					D=A

				(END_MASK_1)			
			
			@R13
			A=M
			M=M&D
			@row_bmp_location
			A=M
			D=M
			@R13
			A=M
			M=M|D

			@32
			D=A
			@address
			M=M+D
			@i
			M=M+1
			@row_bmp_location
			M=M+1

			@PRINT_LOOP
			0;JMP

		(END_LOOP)

			@RET_FROM_MOVE_CURSOR_1
			D=A
			@SP
			M=M+1
			A=M-1
			M=D

			@MOVE_CURSOR
			0;JMP

			(RET_FROM_MOVE_CURSOR_1)

		@13
		D=A
		@SP
		M=M-D

		@SP
		A=M+1
		A=M
		0;JMP

(END_PRINT_CHAR)


(MULTIPLY)
	
	@3
	D=A
	@SP
	A=M-D
	D=M
	@x
	M=D

	@2
	D=A
	@SP
	A=M-D
	D=M
	@y
	M=D

	@j
	M=0

	@twoToThej
	M=1

	@sum
	M=0

	@x
	D=M
	@shiftedX
	M=D

	(MUL_LOOP)

		@16
		D=A
		@j
		D=M-D

		@END_MUL_LOOP
		D;JGE

		@y
		D=M
		@twoToThej
		D=D&M

		@SKIP_STEP
		D;JEQ

			@shiftedX
			D=M
			@sum
			M=M+D

		(SKIP_STEP)

		@j
		M=M+1

		@twoToThej
		D=M
		M=M+D

		@shiftedX
		D=M
		M=M+D

		@MUL_LOOP
		0;JMP		

	(END_MUL_LOOP)

	@SP
	A=M-1
	D=M
	@mul_retaddr
	M=D

	@3
	D=A
	@SP
	M=M-D

	@sum
	D=M
	@SP
	M=M+1
	A=M-1
	M=D

	@mul_retaddr
	A=M
	0;JMP

(END_MULTIPLY)


(MOVE_CURSOR)
	
	@mask
	D=M
	@ELSE_MASK
	D;JNE

		@mask
		M=1

		@END_MASK
		0;JMP

	(ELSE_MASK)

		@cursorx
		M=M+1

		@32
		D=A
		@cursorx
		D=D-M

		@CURSOR_LABEL
		D;JNE

			@cursorx
			M=0

			@cursory
			M=M+1

		(CURSOR_LABEL)

		@mask
		M=0

	(END_MASK)

	@SP
	AM=M-1
	A=M
	0;JMP

(END_MOVE_CURSOR)