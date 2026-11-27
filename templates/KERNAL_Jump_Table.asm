SCINIT  = $FF81 ; Initialize VIC; restore default input/output to keyboard/screen
IOINIT  = $FF84 ; Initialize CIAs, SID volume; setup IRQ timer
RAMTAS  = $FF87 ; Clear memory pointers; test RAM; set top/bottom of RAM
RESTOR  = $FF8A ; Restore default I/O vectors ($0314-$0333)
VECTOR  = $FF8D ; Read/set I/O vector table (X/Y = pointer, Carry: 0=set, 1=read)
SETMSG  = $FF90 ; Control KERNAL messages (A: Bit 7=Errors, Bit 6=Control)
SECOND  = $FF93 ; Send secondary address after LISTEN (A = secondary addr)
TKSA    = $FF96 ; Send secondary address after TALK (A = secondary addr)
MEMTOP  = $FF99 ; Read/set top of memory (X/Y = addr, Carry: 0=set, 1=read)
MEMBOT  = $FF9C ; Read/set bottom of memory (X/Y = addr, Carry: 0=set, 1=read)
SCNKEY  = $FF9F ; Scan keyboard; put key in buffer
SETTMO  = $FFA2 ; Set IEEE timeout flag (A)
ACPTR   = $FFA5 ; Input byte from serial bus (returns byte in A)
CIOUT   = $FFA8 ; Output byte to serial bus (A = byte)
UNTLK   = $FFAB ; Command serial bus device to UNTALK
UNLSN   = $FFAE ; Command serial bus device to UNLISTEN
LISTEN  = $FFB1 ; Command serial bus device to LISTEN (A = device #)
TALK    = $FFB4 ; Command serial bus device to TALK (A = device #)
READST  = $FFB7 ; Read I/O status word (returns status in A)
SETLFS  = $FFBA ; Set logical file parameters (A = logical #, X = device #, Y = secondary #)
SETNAM  = $FFBD ; Set filename parameters (A = length, X/Y = pointer to string)
OPEN    = $FFC0 ; Open logical file
CLOSE   = $FFC3 ; Close logical file (A = logical #)
CHKIN   = $FFC6 ; Define file as default input (X = logical #)
CHKOUT  = $FFC9 ; Define file as default output (X = logical #)
CLRCHN  = $FFCC ; Close default input/output files; restore keyboard/screen
CHRIN   = $FFCF ; Input character from default input (returns char in A)
CHROUT  = $FFD2 ; Output character to default output (A = PETSCII char)
LOAD    = $FFD5 ; Load or verify from file (A: 0=Load, 1=Verify; X/Y = load addr)
SAVE    = $FFD8 ; Save memory to file (A = zero-page ptr to start addr, X/Y = end addr + 1)
SETTIM  = $FFDB ; Set real-time clock (A/X/Y = jiffies)
RDTIM   = $FFDE ; Read real-time clock (returns jiffies in A/X/Y)
STOP    = $FFE1 ; Check RUN/STOP key (Z=1 if pressed)
GETIN   = $FFE4 ; Get character from keyboard buffer (returns char in A, 0 if empty)
CLALL   = $FFE7 ; Close all open files and channels
UDTIM   = $FFEA ; Increment real-time clock
SCREEN  = $FFED ; Return screen organization (X = columns, Y = rows)
PLOT    = $FFF0 ; Read/set cursor position (X = row, Y = col; Carry: 0=set, 1=read)
IOBASE  = $FFF3 ; Return base address of I/O devices in X/Y ($DC00)
