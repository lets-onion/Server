package site.lets_onion.lets_onionApp.domain.member;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<DeviceToken> deviceTokens = new ArrayList<>();

    private Long kakaoId;
    private String nickname;

    @Embedded
    private PushNotification pushNotification;

    @Builder
    public Member(@NonNull Long kakaoId) {
        this.kakaoId = kakaoId;
        pushNotification = new PushNotification();
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }
}