; CIA 1 ($DC00 - IRQ, Keyboard, Joysticks)
CIA1_PRA        = $DC00 ; Data Port A (Keyboard matrix cols / Joystick Port 2)
CIA1_PRB        = $DC01 ; Data Port B (Keyboard matrix rows / Joystick Port 1)
CIA1_DDRA       = $DC02 ; Data Direction Register A
CIA1_DDRB       = $DC03 ; Data Direction Register B
CIA1_TA_LO      = $DC04 ; Timer A low byte
CIA1_TA_HI      = $DC05 ; Timer A high byte
CIA1_TB_LO      = $DC06 ; Timer B low byte
CIA1_TB_HI      = $DC07 ; Timer B high byte
CIA1_TOD_10TH   = $DC08 ; Time of Day: 1/10 seconds
CIA1_TOD_SEC    = $DC09 ; Time of Day: Seconds
CIA1_TOD_MIN    = $DC0A ; Time of Day: Minutes
CIA1_TOD_HR     = $DC0B ; Time of Day: Hours (Bit 7: AM/PM)
CIA1_SDR        = $DC0C ; Serial Data Register
CIA1_ICR        = $DC0D ; Interrupt Control & Status Register (Read clears flags)
CIA1_CRA        = $DC0E ; Control Register A
CIA1_CRB        = $DC0F ; Control Register B

; CIA 2 ($DD00 - NMI, VIC Bank, Serial IEC Bus, User Port)
CIA2_PRA        = $DD00 ; Bits 0-1: VIC Bank (0=$C000, 1=$8000, 2=$4000, 3=$0000) | Bits 3-7: Serial IEC
CIA2_PRB        = $DD01 ; User Port Data / RS-232
CIA2_DDRA       = $DD02 ; Data Direction Register A
CIA2_DDRB       = $DD03 ; Data Direction Register B
CIA2_TA_LO      = $DD04 ; Timer A low byte
CIA2_TA_HI      = $DD05 ; Timer A high byte
CIA2_TB_LO      = $DD06 ; Timer B low byte
CIA2_TB_HI      = $DD07 ; Timer B high byte
CIA2_TOD_10TH   = $DD08 ; Time of Day: 1/10 seconds
CIA2_TOD_SEC    = $DD09 ; Time of Day: Seconds
CIA2_TOD_MIN    = $DD0A ; Time of Day: Minutes
CIA2_TOD_HR     = $DD0B ; Time of Day: Hours
CIA2_SDR        = $DD0C ; Serial Data Register
CIA2_ICR        = $DD0D ; NMI Control & Status Register (Read clears flags)
CIA2_CRA        = $DD0E ; Control Register A
CIA2_CRB        = $DD0F ; Control Register B
