; Sprite Coordinates (0-7)
VIC_SPR0_X      = $D000
VIC_SPR0_Y      = $D001
VIC_SPR1_X      = $D002
VIC_SPR1_Y      = $D003
VIC_SPR2_X      = $D004
VIC_SPR2_Y      = $D005
VIC_SPR3_X      = $D006
VIC_SPR3_Y      = $D007
VIC_SPR4_X      = $D008
VIC_SPR4_Y      = $D009
VIC_SPR5_X      = $D00A
VIC_SPR5_Y      = $D00B
VIC_SPR6_X      = $D00C
VIC_SPR6_Y      = $D00D
VIC_SPR7_X      = $D00E
VIC_SPR7_Y      = $D00F
VIC_SPR_X_MSB   = $D010 ; Bit 0-7: X-coordinate MSB (bit 8) for Sprites 0-7

; Control & Raster
VIC_CTRL1       = $D011 ; Bit 7: Raster bit 8 | 6: ECM | 5: BMM | 4: DEN | 3: RSEL | 0-2: YSCROLL
VIC_RASTER      = $D012 ; Read: Current raster line (bits 0-7) / Write: Raster IRQ line
VIC_LPEN_X      = $D013 ; Light pen X
VIC_LPEN_Y      = $D014 ; Light pen Y
VIC_SPR_ENA     = $D015 ; Sprite enable bits (0-7)
VIC_CTRL2       = $D016 ; Bit 4: MCM (Multicolor) | 3: CSEL (40/38 cols) | 0-2: XSCROLL
VIC_SPR_EXP_Y   = $D017 ; Sprite vertical (2x) expansion (bits 0-7)
VIC_MEM_PTRS    = $D018 ; Bits 4-7: Screen base ($0400*n) | Bits 1-3: Char/Bitmap base ($0800*n)

; Interrupts
VIC_IRQ_FLAG    = $D019 ; IRQ Status/Ack (Bit 7: Any | 3: Lightpen | 2: Spr-Spr | 1: Spr-Bg | 0: Raster)
VIC_IRQ_MASK    = $D01A ; IRQ Enable Mask (Same bit layout as $D019)

; Sprite Priority, Modes & Collisions
VIC_SPR_PRIO    = $D01B ; Sprite-to-background priority (0 = Sprite in front, 1 = Behind)
VIC_SPR_MCOLOR  = $D01C ; Sprite multicolor mode enable (bits 0-7)
VIC_SPR_EXP_X   = $D01D ; Sprite horizontal (2x) expansion (bits 0-7)
VIC_SPR_COLL    = $D01E ; Sprite-to-sprite collision register (cleared on read)
VIC_BG_COLL     = $D01F ; Sprite-to-background collision register (cleared on read)

; Colors
VIC_BORDER_COL  = $D020 ; Border color
VIC_BG_COL0     = $D021 ; Background color 0
VIC_BG_COL1     = $D022 ; Background color 1 (Multicolor / ECM)
VIC_BG_COL2     = $D023 ; Background color 2 (Multicolor / ECM)
VIC_BG_COL3     = $D024 ; Background color 3 (ECM)
VIC_SPR_MCOL0   = $D025 ; Sprite multicolor 0
VIC_SPR_MCOL1   = $D026 ; Sprite multicolor 1
VIC_SPR0_COL    = $D027 ; Sprite 0 color
VIC_SPR1_COL    = $D028 ; Sprite 1 color
VIC_SPR2_COL    = $D029 ; Sprite 2 color
VIC_SPR3_COL    = $D02A ; Sprite 3 color
VIC_SPR4_COL    = $D02B ; Sprite 4 color
VIC_SPR5_COL    = $D02C ; Sprite 5 color
VIC_SPR6_COL    = $D02D ; Sprite 6 color
VIC_SPR7_COL    = $D02E ; Sprite 7 color

; Color RAM
COLOR_RAM       = $D800 ; 1000 bytes ($D800-$DBE7), lower 4 bits only

