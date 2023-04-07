package com.bustime.module.account;

import com.bustime.module.account.form.PasswordForm;
import com.bustime.module.account.form.SignUpForm;
import com.bustime.module.account.form.UsernameForm;
import com.bustime.module.account.validator.PasswordFormValidator;
import com.bustime.module.account.validator.UserNameValidator;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.User;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final ModelMapper modelMapper;
    private final UserNameValidator userNameValidator;

    @InitBinder("passwordForm")
    public void passwordFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(new PasswordFormValidator());
    }

    @InitBinder("userNameForm")
    public void usernameFormInitBinder(WebDataBinder webDataBinder) {
        webDataBinder.addValidators(userNameValidator);
    }

    /* User 등록 Sign up*/
    @GetMapping("/sign-up")
    public String signUpForm(Model model) {
        model.addAttribute(new SignUpForm());
        return "account/sign-up";
    }

    @PostMapping("/sign-up")
    public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors) {
        if (errors.hasErrors()) {
            return "account/sign-up";
        }

        Account account = accountService.processNewAccount(signUpForm);
        accountService.login(account);
        return "redirect:/";
    }

    /* My Account info */
    @GetMapping("/profile/{username}")
    public String viewProfile(@PathVariable String username, Model model, @CurrentUser Account account) {
        Account accountToView = accountService.getAccount(username);
        model.addAttribute(accountToView);
        model.addAttribute("isOwner", accountToView.equals(account));
        return "account/profile";
    }

    @GetMapping ("settings/account")
    public String getSettingsAccountInfo (@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, UsernameForm.class));
        return "account/settings/account";
    }

    @PostMapping("settings/account")
    public String updateAccount(@CurrentUser Account account, @Valid UsernameForm usernameForm, Errors errors,
                                Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "account/settings/account";
        }

        accountService.updateUserName(account, usernameForm.getUsername());
        attributes.addFlashAttribute("message", "닉네임을 수정했습니다.");
        return "redirect:/account/settings/account";
    }

    /* 비밀번호 변경 */
    @GetMapping("settings/password")
    public String updatePasswordForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(new PasswordForm());
        return "account/settings/password";
    }

    @PostMapping("settings/password")
    public String updatePassword(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "account/settings/account";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/settings/password";
    }

    /* Email Confirmation */
    @GetMapping("/check-email-token")
    public String checkEmailToken(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        String view = "account/checked-email";
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        accountService.completeSignUp(account);
        model.addAttribute("numberOfUser", accountRepository.count());
        model.addAttribute("username", account.getUsername());
        return view;
    }

    @GetMapping("/check-email")
    public String checkEmail(@CurrentUser Account account, Model model) {
        model.addAttribute("email", account.getEmail());
        return "account/check-email";
    }

    @GetMapping("/resend-confirm-email")
    public String resendConfirmEmail(@CurrentUser Account account, Model model) {
        if (!account.canSendConfirmEmail()) {
            model.addAttribute("error", "인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
            model.addAttribute("email", account.getEmail());
            return "account/check-email";
        }

        accountService.sendSignUpConfirmEmail(account);
        return "redirect:/";
    }
}
