package com.bustime.module.account;

import com.bustime.config.AppProperties;
import com.bustime.config.mail.EmailMessage;
import com.bustime.config.mail.EmailService;
import com.bustime.module.Tag.Tag;
import com.bustime.module.account.form.PasswordResetRequestForm;
import com.bustime.module.account.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final JavaMailSender javaMailSender;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final TemplateEngine templateEngine;
    private final AppProperties appProperties;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Account account = accountRepository.findByEmail(email);
        if (account == null) {
            throw new UsernameNotFoundException(email);
        }
        return new UserAccount(account);
    }

    public void login(Account account) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                new UserAccount(account),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(token);
    }

    //유저 등록과 관련된 Service
    @Transactional
    public Account processNewAccount(@Valid SignUpForm signUpForm) {
        signUpForm.setPassword(passwordEncoder.encode(signUpForm.getPassword()));
        Account account = modelMapper.map(signUpForm, Account.class);
        account.generateEmailCheckToken();
        Account newAccount = accountRepository.save(account);
        sendEmailLink(newAccount,
                "check-email-token",
                "우리동네 버스 시간표 - 회원 가입 인증",
                "우리동네 버스 시간표 사이트에 가입을 환영합니다. 서비스를 사용하려면 링크를 클릭하세요."
        );
        return newAccount;
    }

   public void sendEmailLink(Account newAccount, String toLink, String subject, String msg ) {

        Context context = new Context();
        context.setVariable("link", "/"+toLink+"?token=" + newAccount.getEmailCheckToken() +
                "&email=" + newAccount.getEmail());
        context.setVariable("nickname", newAccount.getUsername());
        context.setVariable("linkName", "인증 링크");
        context.setVariable("message", msg);
        context.setVariable("host", appProperties.getHost());
        String message = templateEngine.process("mail/email-link-simple", context);

        EmailMessage emailMessage = EmailMessage.builder()
                .to(newAccount.getEmail())
                .subject(subject)
                .message(message)
                .build();
        emailService.sendEmail(emailMessage);
    }

    public void completeSignUp(Account account) {
        account.completeSignUp();
        login(account);
    }

    public Account getAccount(String username) {
        Account account = accountRepository.findByUsername(username);
        if (account == null) {
            throw new IllegalArgumentException(username + "에 해당하는 사용자가 없습니다.");
        }
        return account;
    }

    public void updatePassword(Account account, String newPassword) {
        account.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(account);
    }

    public void updateUserName(Account account, String username) {
        account.setUsername(username);
        accountRepository.save(account);
        login(account); // 다시 로그인해서 세션 갱신
    }

    public void addTag(Account account, Tag tag) {
        accountRepository.findById(account.getId())
                .ifPresent(a -> a.getTags().add(tag));
    }

    public Set<Tag> getTags(Account account) {
        return accountRepository.findById(account.getId()).orElseThrow().getTags();
    }

    public void removeTag(Account account, Tag tag) {
        accountRepository.findById(account.getId())
                .map(Account::getTags)
                .ifPresent(tags -> tags.remove(tag));
    }

    @Transactional
    public Account passwordResetRequest(@Valid PasswordResetRequestForm resetForm) {
        Account accountToReset = accountRepository.findByEmail(resetForm.getEmail());
        accountToReset.generatePasswordRestToken();

        sendEmailLink(accountToReset,
                "reset-password",
                "우리동네 버스 시간표 - 비밀번호 재설정",
                "비밀번호 재설정을 위해 링크를 클릭하세요, 본 링크는 24시간동안만 유효합니다."
        );
        return accountToReset;
    }

    public boolean isAccountExists(String email) {
        if (accountRepository.findByEmail(email) == null){
            return false;
        }
        return true;
    }

    public boolean isAdmin (Account account){
        return account.getUsertype().equals("A");
    }
}
