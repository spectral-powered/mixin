package mixins;

import api.TestApi;
import org.spectralpowered.mixin.annotations.Mixin;
import target.Test;

@Mixin(Test.class)
public class TestMixin implements TestApi {

}
