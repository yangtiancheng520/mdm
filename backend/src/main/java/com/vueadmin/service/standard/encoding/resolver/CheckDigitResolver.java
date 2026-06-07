package com.vueadmin.service.standard.encoding.resolver;

import com.vueadmin.service.standard.encoding.EncodingContext;
import com.vueadmin.service.standard.encoding.SegmentResolver;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 校验位解析器
 * 根据已生成的编码计算校验位
 */
@Component
public class CheckDigitResolver implements SegmentResolver {

    @Override
    public String resolve(Map<String, Object> config, EncodingContext context) {
        // 获取算法类型
        String algorithm = (String) config.getOrDefault("algorithm", "mod10");

        // 获取当前已生成的编码
        String currentCode = context.getCurrentCode();
        if (currentCode == null || currentCode.isEmpty()) {
            return "";
        }

        // 根据算法计算校验位
        return switch (algorithm) {
            case "mod10" -> calculateMod10(currentCode);
            case "mod11" -> calculateMod11(currentCode);
            case "luhn" -> calculateLuhn(currentCode);
            case "iso7064" -> calculateISO7064(currentCode);
            default -> "";
        };
    }

    /**
     * Mod10 算法
     */
    private String calculateMod10(String code) {
        int sum = 0;
        boolean alternate = true;
        for (int i = code.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(code.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = digit / 10 + digit % 10;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return String.valueOf((10 - sum % 10) % 10);
    }

    /**
     * Mod11 算法
     */
    private String calculateMod11(String code) {
        int[] weights = {2, 3, 4, 5, 6, 7, 2, 3, 4, 5, 6, 7, 2, 3, 4, 5, 6, 7};
        int sum = 0;
        int weightIndex = 0;
        for (int i = code.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(code.charAt(i));
            sum += digit * weights[weightIndex % weights.length];
            weightIndex++;
        }
        int check = 11 - (sum % 11);
        if (check == 10) {
            return "X";
        } else if (check == 11) {
            return "0";
        }
        return String.valueOf(check);
    }

    /**
     * Luhn 算法
     */
    private String calculateLuhn(String code) {
        int sum = 0;
        boolean alternate = false;
        for (int i = code.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(code.charAt(i));
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
            alternate = !alternate;
        }
        return String.valueOf((10 - sum % 10) % 10);
    }

    /**
     * ISO 7064 Mod 11-2 算法
     */
    private String calculateISO7064(String code) {
        int sum = 0;
        int weight = 2;
        for (int i = code.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(code.charAt(i));
            sum += digit * weight;
            weight = (weight * 2) % 11;
        }
        int check = (12 - (sum % 11)) % 11;
        if (check == 10) {
            return "X";
        }
        return String.valueOf(check);
    }

    @Override
    public String getType() {
        return "checkDigit";
    }
}
