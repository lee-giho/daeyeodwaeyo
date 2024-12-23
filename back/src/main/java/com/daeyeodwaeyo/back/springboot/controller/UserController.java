package com.daeyeodwaeyo.back.springboot.controller;

import com.daeyeodwaeyo.back.springboot.config.JwtRequestFilter;
import com.daeyeodwaeyo.back.springboot.config.JwtUtil;
import com.daeyeodwaeyo.back.springboot.domain.User;
import com.daeyeodwaeyo.back.springboot.dto.*;
import com.daeyeodwaeyo.back.springboot.repository.UserRepository;
import com.daeyeodwaeyo.back.springboot.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

//UserController는 회원가입 및 로그인 요청은 처리하는 컨트롤러이다.
//클라이언트로부터 요청을 받아 서비스를 호출한다.
@RestController
@RequestMapping("/api/users")
public class UserController {

  @Autowired
  private UserService userService;

//  @Autowired
//  public UserController(UserService userService) {
//    this.userService = userService;
//  }

  @Autowired
  private JwtUtil jwtUtil;

  @Autowired
  private UserRepository userRepository;

//
//  // 이미지 임시 업로드
//  @PostMapping("/tempProfileImage")
//  public ResponseEntity<String> uploadTempProfileImage(@RequestParam("file")MultipartFile file) throws IOException {
//    String imageUrl = userService.saveTempImage(file); // 임시 이미지 저장 서비스 호출
//
////    확인용 코드
////    System.out.println(imageUrl);
//
//    return ResponseEntity.ok(imageUrl);
//  }
//

//  회원가입 엔드포인트
//  @param userRegisterDTO 클라이언트가 전달한 회원가입 정보
//  @return 성공 메시지 또는 에러 메시지
  @PostMapping("/signup")
  public ResponseEntity<String> registerUser(@ModelAttribute UserRegisterDTO userRegisterDTO) {
    try {
      String message = userService.registerUser(userRegisterDTO);
      return ResponseEntity.ok(message); // 회원가입 성공 시 성공 메시지 반환
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
  }

  // 아이디 중복확인 엔드포인트
  @GetMapping("/idDuplicate")
  public ResponseEntity<Boolean> checkIdDuplicate(@RequestParam String id) {
    boolean isIdDuplicate = userService.isIdDuplicate(id);
    return ResponseEntity.ok(isIdDuplicate);
  }

  // 닉네임 중복확인 엔드포인트
  @GetMapping("/nickNameDuplicate")
  public ResponseEntity<Boolean> checkNickNameDuplicate(@RequestParam String nickName) {
    boolean isNickNameDuplicate = userService.isNickNameDuplicate(nickName);
    return ResponseEntity.ok(isNickNameDuplicate);
  }

//  로그인 엔드포인트
//  @param userLoginDTO 클라이언트가 전달한 로그인 정보 (아이디와 비밀번호)
//  @return JWT 토큰 또는 에러 메시지
  @PostMapping("/login")
  public ResponseEntity<String> loginUser(@Valid @RequestBody UserLoginDTO userLoginDTO) {
    System.out.println("로그인 요청이 도착했습니다: " + userLoginDTO.getId());
    try {
      // 사용자 인증 (아이디 비밀번호 확인)
      User user = userService.loginUser(userLoginDTO);
      if(user != null) {
        // 인증 성공 시 JWT 토큰 발급
        String token = jwtUtil.generateToken(userLoginDTO.getId());
        System.out.println("token: " + token);
        return ResponseEntity.ok(token);
      } else {
        // 인증 실패 시 에러 응답
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디 또는 비밀번호가 일치하지 않습니다.");
      }
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
    }
//    String token = userService.loginUser(userLoginDTO);
//    return ResponseEntity.ok(token); // 로그인 성공 시 JWT 토큰 반환
  }

  // JWT 토큰을 통해 사용자 정보 제공
  @GetMapping("/headerUserInfo")
  public ResponseEntity<?> getHeaderUserInfo(@RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출
      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

      // 사용자 정보 반환
      UserInfoDTO userInfoDTO = new UserInfoDTO(user.getId(), user.getNickName(), user.getProfileImage());
      System.out.println("headerUserInfo / userinfoDTO.getProfileImage(): " + userInfoDTO.getProfileImage());
      return ResponseEntity.ok(userInfoDTO);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
    }
  }

  // JWT 토큰을 통해 사용자 정보 제공
  @GetMapping("/userInfo")
  public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출
      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

      // 사용자 정보 반환
      UserInfoDTO userInfoDTO = new UserInfoDTO(user.getId(), user.getName(), user.getPhoneNumber(), user.getAddress(), user.getEmail(), user.getProfileImage(), user.getNickName());
      System.out.println("userInfo / userinfoDTO.getProfileImage(): " + userInfoDTO.getProfileImage());
      return ResponseEntity.ok(userInfoDTO);
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("서버 오류 발생");
    }
  }

  // 아이디 찾기
  @PostMapping("/findId")
  public ResponseEntity<String> findIdByNameAndEmail(@RequestBody FindIdRequestDTO findIdRequestDTO) {
    String userId = userService.findId(findIdRequestDTO.getName(), findIdRequestDTO.getEmail());
    if (userId != null) {
      System.out.println("찾은 아이디" + userId);
      return ResponseEntity.ok(userId); // 성공 시 유저 아이디 반환
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 정보로 일치하는 아이디가 없습니다."); // 찾지 못했을 경우
    }
  }

  // 비밀번호 찾기 (임시 비밀번호 발급)
  @PostMapping("/findPassword")
  public ResponseEntity<String> findPassword(@RequestBody FindPasswordRequestDTO findPasswordRequestDTO) {
    String tempPassword = userService.generateTempPassword(findPasswordRequestDTO.getName(), findPasswordRequestDTO.getId(), findPasswordRequestDTO.getEmail());
    if (tempPassword != null) {
      System.out.println("임시 비밀번호: " + tempPassword);
      return ResponseEntity.ok(tempPassword); // 성공 시 임시 비밀번호 반환
    } else {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 정보로 일치하는 사용자를 찾을 수 없습니다."); // 찾지 못했을 경우
    }
  }

  // 마이페이지에서 현재 비밀번호 인증
  @PostMapping("/checkPassword")
  public ResponseEntity<Boolean> checkPassword(@RequestBody Map<String, String> inputPassword,
                                               @RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출

      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));
      // 테스트 출력
      System.out.println("userId: " + user.getId());

      // 사용자 정보 반환
      ChangeUserInfoDTO changeUserInfoDTO = new ChangeUserInfoDTO(user.getPassword());

      // 테스트 출력
      System.out.println("checkPassword / userinfoDTO.getPassword(): " + changeUserInfoDTO.getPassword());

      boolean matches = userService.checkPassword(changeUserInfoDTO, inputPassword.get("password")); // json 형식으로 inputPassword를 받기 때문에 .get() 사용

      // 테스트 출력
      System.out.println("비밀번호 일치 여부: " + matches);

      return ResponseEntity.ok(matches);
    } catch (UsernameNotFoundException e) {
      // 사용자 아이디가 존재하지 않는 경우 404 반환
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
    } catch (Exception e) {
      // 일반적인 서버 오류가 발생한 경우 500 반환
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }
  }

  // 마이페이지의 내정보 변경에서 비밀번호 변경
  @PostMapping("/changePassword")
  public ResponseEntity<?> changePassword(@RequestBody Map<String, String> changePassword,
                                          @RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출

      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

      // 테스트 출력
      System.out.println("userId: " + user.getId());

      // 사용자 정보 반환
      ChangeUserInfoDTO changeUserInfoDTO = new ChangeUserInfoDTO(user.getId(), changePassword.get("password"));

      // 테스트 출력
      System.out.println("changePassword / userinfoDTO.getPassword(): " + changeUserInfoDTO.getPassword());

      String result = userService.changePassword(changeUserInfoDTO);

      // 테스트 출력
      System.out.println("result: " + result);

      if (result.equals("success")) {
        // 태스트 출력
        System.out.println("비밀번호 변경에 성공했습니다.");

        return ResponseEntity.ok(true);
      } else {
        // 태스트 출력
        System.out.println("비밀번호 변경에 실패했습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
      }
    } catch (Exception e) {
      // 태스트 출력
      System.out.println("서버 오류");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }
  }

  // 마이페이지의 내정보 변경에서 주소 변경
  @PostMapping("/changeAddress")
  public ResponseEntity<?> changeAddress(@RequestBody Map<String, String> changeAddress,
                                         @RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출

      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

      // 테스트 출력
      System.out.println("userId: " + user.getId());

      // 사용자 정보 반환
      ChangeUserInfoDTO changeUserInfoDTO = new ChangeUserInfoDTO(user.getId(), changeAddress.get("address"));

      // 테스트 출력
      System.out.println("changeAddress / userinfoDTO.getAddress(): " + changeUserInfoDTO.getAddress());

      String result = userService.changeAddress(changeUserInfoDTO);

      // 테스트 출력
      System.out.println("result: " + result);

      if (result.equals("success")) {
        // 태스트 출력
        System.out.println("주소 변경에 성공했습니다.");

        return ResponseEntity.ok(true);
      } else {
        // 태스트 출력
        System.out.println("주소 변경에 실패했습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
      }
    } catch (Exception e) {
      // 태스트 출력
      System.out.println("서버 오류");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }
  }

  // 마이페이지의 내정보 변경에서 프로필사진 변경
  @PostMapping("/changeProfileImage")
  public ResponseEntity<?> changeProfileImage(@RequestParam("profileImage") MultipartFile profileImage,
                                              @RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출

      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

      // 테스트 출력
      System.out.println("userId: " + user.getId());

      // 사용자 정보 반환
      ChangeUserInfoDTO changeUserInfoDTO = new ChangeUserInfoDTO(user.getId(), profileImage);

      // 테스트 출력
      System.out.println("changeProfileImage / userinfoDTO.getProfileImage(): " + changeUserInfoDTO.getProfileImage());

      String result = userService.changeProfileImage(changeUserInfoDTO);

      // 테스트 출력
      System.out.println("result: " + result);

      if (result.equals("success")) {
        // 태스트 출력
        System.out.println("프로필사진 변경에 성공했습니다.");

        return ResponseEntity.ok(true);
      } else {
        // 태스트 출력
        System.out.println("프로필사진 변경에 실패했습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
      }
    } catch (Exception e) {
      // 태스트 출력
      System.out.println("서버 오류");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }
  }

  // 마이페이지의 내정보 변경에서 닉네임 변경
  @PostMapping("/changeNickName")
  public ResponseEntity<?> changeNickName(@RequestBody Map<String, String> changeNickName,
                                         @RequestHeader("Authorization") String token) {
    try {
      // 토큰에서 사용자 정보 추출

      String userId = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 부분 제거
      User user = userRepository.findById(userId).orElseThrow(() -> new UsernameNotFoundException("아이디가 존재하지 않습니다."));

      // 테스트 출력
      System.out.println("userId: " + user.getId());

      // 사용자 정보 반환
      ChangeUserInfoDTO changeUserInfoDTO = new ChangeUserInfoDTO(user.getId(), changeNickName.get("nickName"));

      // 테스트 출력
      System.out.println("changeNickName / userinfoDTO.getNickName(): " + changeUserInfoDTO.getNickName());

      String result = userService.changeNickName(changeUserInfoDTO);

      // 테스트 출력
      System.out.println("result: " + result);

      if (result.equals("success")) {
        // 태스트 출력
        System.out.println("닉네임 변경에 성공했습니다.");

        return ResponseEntity.ok(true);
      } else {
        // 태스트 출력
        System.out.println("닉네임 변경에 실패했습니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(false);
      }
    } catch (Exception e) {
      // 태스트 출력
      System.out.println("서버 오류");

      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
    }
  }

//  @GetMapping("/profileImage")
//  public ResponseEntity<?> returnImage(@RequestParam String imageName) {
//    String path = "/Users/giho/Desktop/anyang/graduationProject/daeyeodwaeyo/back/src/main/resources/static/images/profileImage/"; // 이미지가 저장된 위치
//    Resource resource = new FileSystemResource(path + imageName);
//    System.out.println("/profileImage resource : " + resource);
//    return new ResponseEntity<>(resource, HttpStatus.OK);
//  }

//  @GetMapping("/profileImage")
//  public ResponseEntity<Resource> display(@Param("fileName") String fileName) {
//    String path = "/Users/giho/Desktop/anyang/graduationProject/daeyeodwaeyo/resources/images/profileImage";
//
//    Resource resource = new FileSystemResource(path + fileName);
//
//    if(!resource.exists())
//      return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);
//
//    HttpHeaders header = new HttpHeaders();
//    Path filePath = null;
//
//    try {
//      filePath = Paths.get(path + fileName);
//      header.add("Content-Type", Files.probeContentType((filePath)));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
//    return new ResponseEntity<Resource>(resource, header, HttpStatus.OK);
//  }

  @GetMapping("/profileImage/{filename}")
  public ResponseEntity<Resource> display(@PathVariable("filename") String fileName) {
    String path = "/Users/giho/Desktop/anyang/graduationProject/daeyeodwaeyo/resources/images/profileImage";

    System.out.println("filename: " + fileName);

    Resource resource = new FileSystemResource(path + fileName);

    System.out.println(("resource url: " + resource));

    if(!resource.exists())
      return new ResponseEntity<Resource>(HttpStatus.NOT_FOUND);

    HttpHeaders header = new HttpHeaders();
    Path filePath = null;

    try {
      filePath = Paths.get(path + fileName);
      header.add("Content-Type", Files.probeContentType((filePath)));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new ResponseEntity<Resource>(resource, header, HttpStatus.OK);
  }

//  인증된 사용자만 접근 가능한 엔드포인트 (선택사항)
//  @return 인증된 사용자에게만 반환하는 메시지
  @GetMapping("/secure")
  public ResponseEntity<String> secureEndpoint() {
    return ResponseEntity.ok("인증된 사용자만 접근 가능합니다.");
  }
}
