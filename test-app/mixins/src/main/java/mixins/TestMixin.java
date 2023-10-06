package mixins;

import api.TestApi;
import org.spectralpowered.mixin.annotations.Inject;
import org.spectralpowered.mixin.annotations.Mixin;
import org.spectralpowered.mixin.annotations.Overwrite;
import org.spectralpowered.mixin.annotations.Shadow;
import target.Test;

@Mixin(Test.class)
public abstract class TestMixin implements TestApi {

    @Shadow
    public abstract void shadow$start();

    @Inject
    private void injectedPrintHello() {
        System.out.println("Hello World!!! From Mixin!");
    }

    @Overwrite
    @Override
    public void start() {
        System.out.println("Overwritten by mixin.");
        this.injectedPrintHello();
        shadow$start();
    }
}
