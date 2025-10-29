package br.com.devquote.utils;

import br.com.devquote.enums.FlowType;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class FlowTypeUtils {

    public static List<FlowType> convertToFlowTypeList(List<String> flowTypes) {
        if (flowTypes == null || flowTypes.isEmpty()) {
            return null;
        }

        return flowTypes.stream()
                .map(FlowType::fromString)
                .toList();
    }
}
