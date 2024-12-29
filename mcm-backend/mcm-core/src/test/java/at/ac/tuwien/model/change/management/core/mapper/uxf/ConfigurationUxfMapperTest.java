package at.ac.tuwien.model.change.management.core.mapper.uxf;

import at.ac.tuwien.model.change.management.core.model.Configuration;
import at.ac.tuwien.model.change.management.core.model.Model;
import at.ac.tuwien.model.change.management.core.model.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConfigurationUxfMapperTest {

    static Configuration conf = new Configuration();
    static Model m1 = new Model();
    static Model m2 = new Model();

    @BeforeAll
    static void init(){
        m1.setDescription("Description for model 1");
        m1.setId("Model1");
        Node n1 = new Node();
        Node n2 = new Node();
        m1.setNodes(Set.of(n1, n2));
        m1.setZoomLevel(5);

        m2.setDescription("Description for model 2");
        m2.setId("Model2");
        Node n3 = new Node();
        Node n4 = new Node();
        m2.setNodes(Set.of(n3, n4));
        m2.setZoomLevel(15);

        conf.setModels(Set.of(m1, m2));
    }

    @Test
    public void toConfigurationUxfTest(){
        var res = ConfigurationUxfMapper.toConfigurationUxf(conf);
        assertEquals(4, res.getElements().size());
        assertEquals(5, res.getZoomLevel());
    }

    @Test
    public void combineModelDescriptionsTest(){


        String exp = """
                Model Model1
                Description for model 1
                //////////
                Model Model2
                Description for model 2
                //////////
                """;

        LinkedHashSet<Model> models = new LinkedHashSet<>();
        models.add(m1);
        models.add(m2);
        assertEquals(exp, ConfigurationUxfMapper.combineModelDescriptions(models));
    }
}
