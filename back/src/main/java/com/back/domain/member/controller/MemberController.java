package com.back.domain.member.controller;

import com.back.domain.member.dto.MyInfoForm;
import com.back.domain.member.dto.SignupForm;
import com.back.domain.member.entity.Member;
import com.back.domain.member.service.MemberService;
import com.back.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/signup")
    public String signupForm(SignupForm signupForm) {
        return "member/signup";
    }

    @PostMapping("/signup")
    @Transactional
    public String signup(@Valid SignupForm signupForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "member/signup";
        }

        try {
            memberService.signup(signupForm.getUsername(), signupForm.getPassword(), signupForm.getNickname());
        } catch (IllegalStateException e) {
            bindingResult.reject("signupFailed", e.getMessage());
            return "member/signup";
        }

        return "redirect:/member/login";
    }

    @GetMapping("/login")
    public String loginForm() {
        return "member/login";
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public String myInfo(@AuthenticationPrincipal SecurityUser user, MyInfoForm myInfoForm, Model model) {
        Member member = memberService.findById(user.getMemberId());
        myInfoForm.setNickname(member.getNickname());
        model.addAttribute("member", member);
        return "member/me";
    }

    @PostMapping("/me")
    @Transactional
    public String updateMyInfo(@AuthenticationPrincipal SecurityUser user,
                               @Valid MyInfoForm myInfoForm, BindingResult bindingResult, Model model) {
        Member member = memberService.findById(user.getMemberId());

        String password = myInfoForm.getPassword();
        if (password != null && !password.isBlank() && password.length() < 4) {
            bindingResult.rejectValue("password", "Size", "비밀번호는 4자 이상이어야 합니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("member", member);
            return "member/me";
        }

        memberService.updateNickname(member, myInfoForm.getNickname());
        if (password != null && !password.isBlank()) {
            memberService.updatePassword(member, password);
        }

        return "redirect:/member/me";
    }
}
