package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PanelAttributeDSL;

import java.util.List;
import java.util.Map;

public interface PanelAttributesDSLMapper {

    List<PanelAttributeDSL> toDSL(Map<String, String> panelAttributes);

    Map<String, String> fromDSL(List<PanelAttributeDSL> panelAttributesDSL);
}
