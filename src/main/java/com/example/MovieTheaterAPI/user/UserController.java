package com.example.MovieTheaterAPI.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {
    MemberService memberService;
    UserService userService;

    @GetMapping("")
    public ResponseEntity<List<User>> getAllUser() {
        return new ResponseEntity<>(userService.getAllUser(), HttpStatus.OK);
    }

    @PostMapping("/")
    public ResponseEntity<User> createMember(@RequestBody User user) {
        return new ResponseEntity<>(userService.createMember(user), HttpStatus.CREATED);
    }

    @PostMapping("/admin")
    public ResponseEntity<User> createAdmin(@RequestBody User user) {
        return new ResponseEntity<>(userService.createEmployee(user), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/changepw")
    public ResponseEntity<HttpStatus> changePassword(
            @PathVariable long id,
            @RequestBody PasswordChangeRequest req) {
        //Todo: verify old password
        User user;
        try {
            user = userService.getUser(id);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        if (!user.getPassword().equals(req.getPassword())) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        userService.changePassword(user, req.getNewPassword());

        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/update-info")
    public ResponseEntity<HttpStatus> updateInfo(
            @PathVariable long id,
            @RequestBody(required = false) InfoChangeRequest req) {
        User user;
        try {
            user = userService.getUser(id);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        userService.updateInfo(user, req.getFirstname(), req.getLastname(), req.getEmail());
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @PutMapping("/{id}/upgradeAccount")
    public ResponseEntity<HttpStatus> upgradeToPremium(@PathVariable long id) {
        try {
            memberService.upgradePremium(id);
        } catch (MemberNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<HttpStatus> downgradeToRegular(@PathVariable long id) {
        try {
            memberService.cancelPremium(id);
        } catch (MemberNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }
}

@Getter
@Setter
@NoArgsConstructor
class PasswordChangeRequest {
    @NonNull
    @JsonProperty("password")
    private String password;

    @NonNull
    @JsonProperty("new-password")
    private String newPassword;
}

@Getter
@Setter
@NoArgsConstructor
class InfoChangeRequest {
    private String firstname;
    private String lastname;
    private String email;
}