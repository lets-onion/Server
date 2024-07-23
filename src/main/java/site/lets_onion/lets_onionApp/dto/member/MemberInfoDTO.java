package site.lets_onion.lets_onionApp.dto.member;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import site.lets_onion.lets_onionApp.domain.Member;

@Data
public class MemberInfoDTO {

    @JsonProperty("member_id")
    private Long memberId;
    private String nickname;

    public MemberInfoDTO(Member member) {
        this.memberId = member.getId();
        this.nickname = member.getNickname();
    }
}