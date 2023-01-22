package net.purefunc.practice.member;

import org.springframework.stereotype.Service;

@Service
public class MemberService {

    private final MemberDAO memberDAO;

    public MemberService(MemberDAO memberDAO) {
        this.memberDAO = memberDAO;
    }
}
