package mixins;

import org.objectweb.asm.tree.ClassNode;
import org.spectralpowered.mixin.annotations.RawMixin;
import org.spectralpowered.mixin.asm.tree.ClassGroup;
import target.Test;

@RawMixin
public class TestRawMixin {

    private ClassGroup targetGroup;

    public TestRawMixin(ClassGroup group) {
        this.targetGroup = group;

        System.out.println("Raw Injector: " + this.targetGroup.getClasses().size());
    }

}
