package site.lets_onion.lets_onionApp.domain.member;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import site.lets_onion.lets_onionApp.domain.DeviceToken;
import site.lets_onion.lets_onionApp.domain.onion.GrowingOnion;
import site.lets_onion.lets_onionApp.domain.onionBook.OnionBook;

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

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private GrowingOnion onions;

    @OneToOne(mappedBy = "member", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private OnionBook onionBook;

    private String userImageUrl;

    @Builder
    public Member(Long id, @NonNull Long kakaoId, String nickname) {
        this.id = id;
        this.kakaoId = kakaoId;
        this.nickname = nickname;
        pushNotification = new PushNotification();
        this.onions = GrowingOnion.builder().member(this).build();
        this.onionBook = OnionBook.builder().member(this).build();
        this.userImageUrl = "https://imgur.com/dNv03Iq.png"; // 기본 프로필 이미지를 깐양파로 설정
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    public void updateUserImageUrl(String userImageUrl){
        this.userImageUrl = userImageUrl;
    }

}
