; Voice 1
SID_V1_FREQ_LO  = $D400 ; Frequency low byte
SID_V1_FREQ_HI  = $D401 ; Frequency high byte
SID_V1_PW_LO    = $D402 ; Pulse width low byte
SID_V1_PW_HI    = $D403 ; Pulse width high nibble (bits 0-3)
SID_V1_CTRL     = $D404 ; Bit 7: Noise | 6: Pulse | 5: Saw | 4: Tri | 3: Test | 2: Ring | 1: Sync | 0: Gate
SID_V1_AD       = $D405 ; Bits 4-7: Attack | Bits 0-3: Decay
SID_V1_SR       = $D406 ; Bits 4-7: Sustain | Bits 0-3: Release

; Voice 2
SID_V2_FREQ_LO  = $D407
SID_V2_FREQ_HI  = $D408
SID_V2_PW_LO    = $D409
SID_V2_PW_HI    = $D40A
SID_V2_CTRL     = $D40B
SID_V2_AD       = $D40C
SID_V2_SR       = $D40D

; Voice 3
SID_V3_FREQ_LO  = $D40E
SID_V3_FREQ_HI  = $D40F
SID_V3_PW_LO    = $D410
SID_V3_PW_HI    = $D411
SID_V3_CTRL     = $D412
SID_V3_AD       = $D413
SID_V3_SR       = $D414

; Filter & Volume
SID_FC_LO       = $D415 ; Filter cutoff low (bits 0-2)
SID_FC_HI       = $D416 ; Filter cutoff high (bits 0-7)
SID_RES_FILT    = $D417 ; Bits 4-7: Resonance | Bit 3: Ext | Bit 2: V3 | Bit 1: V2 | Bit 0: V1
SID_MODE_VOL    = $D418 ; Bit 7: 3-Off | 6: HP | 5: BP | 4: LP | Bits 0-3: Master Volume

; Read-Only Registers
SID_POT_X       = $D419 ; Paddle X / Mouse X
SID_POT_Y       = $D41A ; Paddle Y / Mouse Y
SID_OSC3_RND    = $D41B ; Voice 3 oscillator output (often used for RNG)
SID_ENV3        = $D41C ; Voice 3 envelope output
