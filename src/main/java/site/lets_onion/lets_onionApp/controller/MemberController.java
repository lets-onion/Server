package site.lets_onion.lets_onionApp.controller;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Nullable;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import site.lets_onion.lets_onionApp.dto.integration.KakaoScopesDTO;
import site.lets_onion.lets_onionApp.dto.jwt.LogoutDTO;
import site.lets_onion.lets_onionApp.dto.jwt.RefreshTokenDTO;
import site.lets_onion.lets_onionApp.dto.jwt.TokenDTO;
import site.lets_onion.lets_onionApp.dto.member.AppLoginDTO;
import site.lets_onion.lets_onionApp.dto.member.LoginDTO;
import site.lets_onion.lets_onionApp.dto.member.MemberInfoDTO;
import site.lets_onion.lets_onionApp.dto.member.NicknameDTO;
import site.lets_onion.lets_onionApp.dto.member.StatusMessageDTO;
import site.lets_onion.lets_onionApp.dto.push.DeviceTokenRequestDTO;
import site.lets_onion.lets_onionApp.service.member.MemberService;
import site.lets_onion.lets_onionApp.service.member.Redirection;
import site.lets_onion.lets_onionApp.util.exception.ExceptionDTO;
import site.lets_onion.lets_onionApp.util.jwt.JwtProvider;
import site.lets_onion.lets_onionApp.util.response.ResponseDTO;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final JwtProvider jwtProvider;


    @GetMapping("/oauth/kakao/login")
    @Operation(summary = "백엔드 로그인", description = "백엔드 개발 시 로그인에 사용되는 API입니다.")
    @ApiResponse(responseCode = "301", description = "리다이렉트 성공")
    public ResponseEntity<?> getRedirect(HttpServletRequest request)
    {
        String addr = request.getRemoteAddr();
        Redirection redirection;
        if ("127.0.0.1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr)) {
            redirection = Redirection.LOCAL;
        } else {
            redirection = Redirection.SERVER;
        }
        String redirectUri = memberService.getRedirectUri(redirection);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUri));
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }


    @GetMapping("/oauth/kakao/callback")
    @Operation(summary = "카카오 인증 API", description = "카카오 인가 코드를 받아 인증 처리를 하는 API입니다.")
    @ApiResponse(responseCode = "200", description = "기존 사용자 로그인 성공")
    @ApiResponse(responseCode = "201", description = "신규 사용자 가입 및 로그인 성공",
    content = @Content(examples = @ExampleObject("""
            {
                "msg":"string",
                "code": 0,
                "data": {
                    "member": {
                        "nickname":"string",
                        "member_id": Long
                    },
                    "access_token":"string",
                    "refresh_token": "string",
                    "exist_user": 신규 유저일 시 false, 기존 유저일 시 true
                }
            }
            """)))
    @ApiResponse(responseCode = "40x", description = "에러",
            content = @Content(schema = @Schema(implementation = ExceptionDTO.class)))
    public ResponseDTO<LoginDTO> localLogin(
            HttpServletRequest request,
            @Parameter(description = "카카오 서버에서 발급 받은 인가코드입니다.")
            @RequestParam String code)
    {
        String addr = request.getRemoteAddr();
        Redirection redirection;
        if ("127.0.0.1".equals(addr) || "0:0:0:0:0:0:0:1".equals(addr)) {
            redirection = Redirection.LOCAL;
        } else {
            redirection = Redirection.SERVER;
        }
        return memberService.login(code, redirection);
    }


    @PostMapping("/oauth/kakao/login/v2")
    @Operation(summary = "앱 로그인", description = "앱에서 로그인하는 API입니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    public ResponseDTO<LoginDTO> loginInApp(
            @RequestBody AppLoginDTO request)
    {
        return memberService.loginInApp(request);
    }


    @PostMapping("/token/refresh")
    @Operation(summary = "토큰 리프레시",
            description = "리프레시 토큰을 통해 새로운 액세스/리프레시 토큰을 발급받는 API입니다.")
    @ApiResponse(responseCode = "200", description = "토큰 리프레시 성공")
    @ApiResponse(responseCode = "40x", description = "에러", content = @Content(schema =
    @Schema(implementation = ExceptionDTO.class)))
    public ResponseDTO<TokenDTO> tokenReissue(
            @RequestBody RefreshTokenDTO request)
    {
        return memberService.tokenReissue(request.getRefreshToken());
    }


    @PostMapping("/auth/logout")
    @Operation(summary = "로그아웃", description = "로그아웃을 처리하는 API입니다.")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    public ResponseDTO<Boolean> logout(HttpServletRequest request
    , @RequestBody LogoutDTO logoutDTO)
    {
        Long memberId = jwtProvider.getMemberId(request);
        return memberService.logout(memberId, logoutDTO);
    }


    @PutMapping("/nickname/update")
    @Operation(summary = "닉네임 수정", description = "닉네임을 수정하는 API입니다.")
    @ApiResponse(responseCode = "200", description = "닉네임 수정 성공")
    public ResponseDTO<MemberInfoDTO> updateNickname(
            HttpServletRequest request, @RequestBody NicknameDTO dto)
    {
        Long memberId = jwtProvider.getMemberId(request);
        return memberService.updateNickname(memberId, dto.getNickname());
    }


    @PostMapping("/status-message/update")
    @Operation(summary = "상태메시지 수정",
            description = "상태메시지를 작성하는 API입니다.24시간 후 삭제됩니다.")
    @ApiResponse(responseCode = "200", description = "상태메시지 작성 성공")
    public ResponseDTO<StatusMessageDTO> updateStatusMessage(
            HttpServletRequest request, @RequestBody StatusMessageDTO dto
    ) {
        Long memberId = jwtProvider.getMemberId(request);
        return memberService
            .updateStatusMessage(memberId,dto.getStatusMessage());
    }


    @GetMapping("/status-message/get")
    @Operation(summary = "상태메시지 조회",
    description = "상태메시지를 조회하는 API입니다. 쿼리파라미터가 없으면 자신의 상태메시지를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "상태메시지 조회 성공")
    public ResponseDTO<StatusMessageDTO> getStatusMessage(
            HttpServletRequest request,
            @Nullable @RequestParam(name = "member_id") Long memberId
    ) {
        Long id;
        if (memberId == null) {
            id = jwtProvider.getMemberId(request);
        } else {
            id = memberId;
        }
        return memberService.getStatusMessage(id);
    }


    @GetMapping("/info/get")
    @Operation(summary = "유저 정보 조회",
    description = "유저 정보를 조회하는 API입니다. 쿼리파라미터가 없으면 자신의 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "유저 정보 조회 성공")
    public ResponseDTO<MemberInfoDTO> getMemberInfo(
            HttpServletRequest request,
            @Nullable @RequestParam(name = "member_id") Long memberId
    ) {
        Long id;
        if (memberId == null) {
            id = jwtProvider.getMemberId(request);
        } else {
            id = memberId;
        }
        return memberService.getMemberInfo(id);
    }


    @PostMapping("/device-token/save")
    @Operation(summary = "디바이스 토큰 업데이트",
            description = "디바이스 토큰을 서버에 전송하는 API입니다. 기존 토큰과 같을 시 200이 응답됩니다.")
    @ApiResponse(responseCode = "200", description = "디바이스 토큰 업데이트 성공")
    public ResponseDTO<Boolean> updateDeviceToken(
            HttpServletRequest request,
            @RequestBody DeviceTokenRequestDTO dto)
    {
        Long memberId = jwtProvider.getMemberId(request);
        return memberService.saveDeviceToken(memberId, dto.getDeviceToken());
    }

    @GetMapping("/kakao/scope")
    @Operation(summary = "카카오 동의항목 확인",
    description = "유저의 카카오 정보 동의 내역을 확인하는 API입니다.")
    @ApiResponse(responseCode = "200", description = """
    자세한 내용은
    https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#check-consent-response-body-scope
    """)
    public ResponseDTO<KakaoScopesDTO> kakaoTest(
            HttpServletRequest request
    ) {
        Long memberId = jwtProvider.getMemberId(request);
        return memberService.checkKakaoScopes(memberId);
    }


    @GetMapping("/kakao/friends")
    @Operation(summary = "카카오톡 친구&&앱 회원 리스트 조회",
    description = "한 번에 100페이지씩 가져옵니다. offset을 0부터 100씩 늘려가며 요청해주세요.")
    @ApiResponse(responseCode = "200", description = """
    자세한 내용은
    https://developers.kakao.com/docs/latest/ko/kakaotalk-social/rest-api#get-friends
    """)
    public ResponseDTO<List<MemberInfoDTO>> requestKakaoFriends(
            HttpServletRequest request,
            @RequestParam int offset
    ) {
        Long memberId = jwtProvider.getMemberId(request);
        return memberService.requestKakaoFriends(memberId, offset);
    }
}
