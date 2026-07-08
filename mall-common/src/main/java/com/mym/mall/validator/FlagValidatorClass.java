package com.mym.mall.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * 状态标记校验器
 * Created by macro on 2018/4/26.
 */
public class FlagValidatorClass implements ConstraintValidator<FlagValidator, Integer> {
    private String[] values;

    @Override
    public void initialize(FlagValidator flagValidator) {
        this.values = flagValidator.value();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext constraintValidatorContext) {
        boolean isValid = false;
        if (value == null) {
            return true;
        }
        for (String s : values) {
            if (s.equals(String.valueOf(value))) {
                isValid = true;
                break;
            }
        }
        return isValid;
    }
}
