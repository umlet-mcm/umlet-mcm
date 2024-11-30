package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PanelAttributeDSL;

import java.util.LinkedHashMap;
import java.util.List;

public interface PanelAttributesDSLMapper {

    List<PanelAttributeDSL> toDSL(LinkedHashMap<String, String> panelAttributes);

    LinkedHashMap<String, String> fromDSL(List<PanelAttributeDSL> panelAttributesDSL);
}
