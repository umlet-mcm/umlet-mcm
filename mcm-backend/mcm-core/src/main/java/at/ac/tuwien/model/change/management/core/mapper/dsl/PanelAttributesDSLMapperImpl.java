package at.ac.tuwien.model.change.management.core.mapper.dsl;

import at.ac.tuwien.model.change.management.core.model.dsl.PanelAttributeDSL;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class PanelAttributesDSLMapperImpl implements PanelAttributesDSLMapper {
    @Override
    public List<PanelAttributeDSL> toDSL(LinkedHashMap<String, String> umletAttribute) {
        if (umletAttribute == null) return null;

        return umletAttribute.entrySet().stream().map(entry -> new PanelAttributeDSL(entry.getKey(), entry.getValue())).toList();
    }

    @Override
    public LinkedHashMap<String, String> fromDSL(List<PanelAttributeDSL> panelAttributesDSL) {
        if (panelAttributesDSL == null) return null;

        return panelAttributesDSL.stream()
                .collect(Collectors.toMap(
                        PanelAttributeDSL::getKey,
                        PanelAttributeDSL::getValue,
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }
}
