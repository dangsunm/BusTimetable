package com.bustime.module.account;

import com.bustime.module.Tag.Tag;
import com.bustime.module.Tag.TagForm;
import com.bustime.module.Tag.TagRepository;
import com.bustime.module.Tag.TagService;
import com.bustime.module.account.form.*;
import com.bustime.module.account.validator.PasswordFormValidator;
import com.bustime.module.account.validator.UserNameValidator;
import com.bustime.module.route.BusRoute;
import com.bustime.module.route.BusRouteRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final AccountRepository accountRepository;
    private final BusRouteRepository routeRepository;
    private final TagService tagService;
    private final TagRepository tagRepository;
    private final ModelMapper modelMapper;
    private final UserNameValidator userNameValidator;
    private final ObjectMapper objectMapper;

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

    /* My Account Page */
    @GetMapping("/mypage")
    public String viewProfile(@CurrentUser Account account, Model model) {
        Account accountLoaded = accountRepository.findAccountWithTagssById(account.getId());
        model.addAttribute("account", accountLoaded);
        List<BusRoute> routeWatching = routeRepository.findByWatchersContaining(account);
        model.addAttribute("routeWatching", routeWatching);

        return "account/mypage";
    }

    /* 계정 설정 */
    @GetMapping ("settings/account")
    public String getSettingsAccountInfo (@CurrentUser Account account, Model model) {
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
        attributes.addFlashAttribute("message", "닉네임 수정을 완료했습니다!");
        return "redirect:/settings/account";
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

    /* 비밀번호 재설정 */
    @GetMapping("/lost-password")
    public String passwordResetRequestForm(Model model) {
        model.addAttribute(new PasswordResetRequestForm());
        return "account/lost-password";
    }

    @PostMapping("/lost-password")
    public String passwordResetRequestSubmit(@Valid PasswordResetRequestForm resetForm,
                                             Errors errors, RedirectAttributes attributes) {

        Account account = accountRepository.findByEmail(resetForm.getEmail());
        if (errors.hasErrors()) {
            return "account/lost-password";
        }
        if (!account.canSendPasswordResetToken()){
            attributes.addFlashAttribute("message", "아직 비밀번호 재전송 이메일을 전송할 수 없습니다.");
        }
        if (!accountService.isAccountExists(resetForm.getEmail())){
            attributes.addFlashAttribute("message", "해당 계정이 존재하지 않습니다.");
        }
        else{
            accountService.passwordResetRequest(resetForm);
            attributes.addFlashAttribute("message", "이메일을 발송했습니다.");
        }

        accountService.passwordResetRequest(resetForm);
        return "redirect:/account/lost-password";
    }

    @GetMapping("reset-password")
    public String pwdReset(String token, String email, Model model) {
        Account account = accountRepository.findByEmail(email);
        model.addAttribute(new PasswordForm());

        String view = "account/reset-password";
        if (account == null) {
            model.addAttribute("error", "wrong.email");
            return view;
        }

        if (!account.isValidToken(token)) {
            model.addAttribute("error", "wrong.token");
            return view;
        }

        if (!account.validResetPasswordTiming()){
            model.addAttribute("error", "token.expired");
            return view;
        }

        accountService.login(account);
        accountService.completeSignUp(account);
        return view;
    }

    @PostMapping("reset-password")
    public String processPwdReset(@CurrentUser Account account, @Valid PasswordForm passwordForm, Errors errors,
                                 Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "account/reset-password";
        }

        accountService.updatePassword(account, passwordForm.getNewPassword());
        attributes.addFlashAttribute("message", "패스워드를 변경했습니다.");
        return "redirect:/";
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

        accountService.sendEmailLinkForVerify(account,
                "check-email-token",
                "우리동네 버스 시간표 - 회원 가입 인증",
                "우리동네 버스 시간표 사이트에 가입을 환영합니다. 서비스를 사용하려면 링크를 클릭하세요."
        );
        return "redirect:/";
    }

    /* Tag 관련 추가 및 삭제 */
    @GetMapping("settings/tags")
    public String updateTags(@CurrentUser Account account, Model model) throws JsonProcessingException {
        model.addAttribute(account);

        Set<Tag> tags = accountService.getTags(account);
        model.addAttribute("tags", tags.stream().map(Tag::getTitle).collect(Collectors.toList()));

        List<String> allTags = tagRepository.findAll().stream().map(Tag::getTitle).collect(Collectors.toList());
        model.addAttribute("whitelist", objectMapper.writeValueAsString(allTags));

        return "account/settings/tags";
    }


    @PostMapping("settings/account/tags/add")
    @ResponseBody
    public ResponseEntity addTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        accountService.addTag(account, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping("settings/account/tags/remove")
    @ResponseBody
    public ResponseEntity removeTag(@CurrentUser Account account, @RequestBody TagForm tagForm) {
        String title = tagForm.getTagTitle();
        Tag tag = tagRepository.findByTitle(title);
        if (tag == null) {
            return ResponseEntity.badRequest().build();
        }

        accountService.removeTag(account, tag);
        return ResponseEntity.ok().build();
    }

    /* 알림관련 */
    @GetMapping("settings/notifications")
    public String updateNotificationsForm(@CurrentUser Account account, Model model) {
        model.addAttribute(account);
        model.addAttribute(modelMapper.map(account, NotificationsForm.class));
        return "account/settings/notifications";
    }

    @PostMapping("settings/notifications")
    public String updateNotifications(@CurrentUser Account account, @Valid NotificationsForm notifications, Errors errors,
                                      Model model, RedirectAttributes attributes) {
        if (errors.hasErrors()) {
            model.addAttribute(account);
            return "account/settings/notifications";
        }

        accountService.updateNotifications(account, notifications);
        attributes.addFlashAttribute("message", "알림 설정을 변경했습니다.");
        return "redirect:/settings/notifications";
    }
}
