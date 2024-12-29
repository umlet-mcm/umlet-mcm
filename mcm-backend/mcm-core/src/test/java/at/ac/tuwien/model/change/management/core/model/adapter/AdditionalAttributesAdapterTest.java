package at.ac.tuwien.model.change.management.core.model.adapter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class AdditionalAttributesAdapterTest {

    private String testAttributes1 = "10;10;120;290";
    private List<Object> exp1 = List.of(10,10,120,290);
    private AdditionalAttributesAdapter testAdapter = new AdditionalAttributesAdapter();

    @Test
    void testUnmarshal(){
        Assertions.assertNull(testAdapter.unmarshal(""));

        var res = testAdapter.unmarshal(testAttributes1);
        Assertions.assertEquals(exp1, res);
    }

    @Test
    void testMarshal(){
        String exp = "1;2;3;4;5;6";
        String res = testAdapter.marshal(List.of(1,2,3,4,5,6));
        Assertions.assertEquals(exp, res);
    }
}
