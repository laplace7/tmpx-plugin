package at.laplace.tmpxplugin;

public class Templates {
    public final static String DEFAULT =
            """
<?xml version="1.0" encoding="UTF-8" standalone="no"?>
<templates>
    <template autoinsert="true" context="asm.editor.context" deleted="false" description="Basic Upstart." enabled="true" name="BasicUpstart">    *= $$0801
; SYS2061
    .byte $$0b,$$08,$$ea,$$07,$$9e
    .byte $$32,$$30,$$36,$$31,0,0,0

main
    .block
    rts
    .bend
    </template>
</templates>
            """;
}
