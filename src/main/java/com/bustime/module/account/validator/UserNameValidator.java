package com.bustime.module.account.validator;

import com.bustime.module.account.Account;
import com.bustime.module.account.AccountRepository;
import com.bustime.module.account.form.UsernameForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class UserNameValidator implements Validator {

    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return UsernameForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        UsernameForm usernameForm = (UsernameForm) target;
        Account byNickname = accountRepository.findByUsername(usernameForm.getUsername());
        if (byNickname != null) {
            errors.rejectValue("nickname", "wrong.value", "입력하신 닉네임을 사용할 수 없습니다.");
        }
    }
}
