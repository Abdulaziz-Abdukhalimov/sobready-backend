package com.sobready.backend.controller;

import com.sobready.backend.entity.Member;
import com.sobready.backend.enums.MemberStatus;
import com.sobready.backend.enums.MemberType;
import com.sobready.backend.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * NestJS equivalent:
 *
 *   @Controller('user')
 *   export class MemberController {
 *     @Post('signup')
 *     async signup(@Body() input: MemberInput, @Res() res: Response) { ... }
 *
 *     @Post('login')
 *     async login(@Body() input: LoginInput, @Res() res: Response) { ... }
 *
 *     @UseGuards(JwtAuthGuard)    ← protected endpoint
 *     @Post('update')
 *     async update(@Req() req, @Body() input: MemberUpdateInput) { ... }
 *   }
 *
 * @AuthenticationPrincipal = gets the logged-in user from the JWT token
 *   Same as @Req() req → req.user in NestJS
 */
@RestController
@RequestMapping("/user")
public class MemberController {

    @Autowired
    private MemberService memberService;

    /**
     * POST /user/signup
     * Body: { memberNick, memberPhone, memberPassword }
     *
     * HttpServletResponse = like @Res() res in NestJS — we need it to set the cookie
     */
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(
            @RequestBody Map<String, String> input,
            HttpServletResponse response
    ) {
        String memberNick = input.get("memberNick");
        String memberPhone = input.get("memberPhone");
        String memberPassword = input.get("memberPassword");

        // Validation
        if (memberNick == null || memberNick.trim().isEmpty()
                || memberPhone == null || memberPhone.trim().isEmpty()
                || memberPassword == null || memberPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
        }

        if (memberPassword.length() < 4) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password must be at least 4 characters"));
        }

        Member member = memberService.signup(memberNick.trim(), memberPhone.trim(),
                memberPassword, response);

        return ResponseEntity.ok(Map.of("member", member));
    }

    /**
     * POST /user/login
     * Body: { memberNick, memberPassword }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @RequestBody Map<String, String> input,
            HttpServletResponse response
    ) {
        String memberNick = input.get("memberNick");
        String memberPassword = input.get("memberPassword");

        if (memberNick == null || memberPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "All fields are required"));
        }

        Member member = memberService.login(memberNick, memberPassword, response);

        return ResponseEntity.ok(Map.of("member", member));
    }

    /**
     * POST /user/logout
     * Protected — only logged-in users can logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        memberService.logout(response);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * POST /user/update
     * Protected — only logged-in users can update their profile
     *
     * Uses multipart/form-data because of image upload.
     * @RequestParam here reads form fields (not query params)
     * — when Content-Type is multipart/form-data, @RequestParam reads form fields.
     *
     * @AuthenticationPrincipal = gets the current user from the JWT token
     *   Same as @Req() req → req.user in NestJS
     */
    /**
     * POST /user/update
     * React expects: result.data → Member object directly
     */
    @PostMapping("/update")
    public ResponseEntity<Member> updateMember(
            @AuthenticationPrincipal Member currentMember,
            @RequestParam(required = false) String memberNick,
            @RequestParam(required = false) String memberPhone,
            @RequestParam(required = false) String memberAddress,
            @RequestParam(required = false) String memberDesc,
            @RequestParam(required = false) MultipartFile memberImage
    ) throws IOException {
        Member updated = memberService.updateMember(currentMember, memberNick, memberPhone,
                memberAddress, memberDesc, memberImage);

        return ResponseEntity.ok(updated);
    }

    /**
     * GET /user/top-users
     * Public — no auth needed
     */
    @GetMapping("/top-users")
    public ResponseEntity<List<Member>> getTopUsers() {
        return ResponseEntity.ok(memberService.getTopUsers());
    }

    /**
     * GET /user/manager
     * Public — no auth needed
     */
    @GetMapping("/manager")
    public ResponseEntity<Member> getManager() {
        return ResponseEntity.ok(memberService.getManager());
    }

    // ===================== ADMIN ENDPOINTS =====================

    /**
     * GET /user/all — get all members (ADMIN only)
     */
    @GetMapping("/all")
    public ResponseEntity<?> getAllMembers(@AuthenticationPrincipal Member currentMember) {
        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }
        return ResponseEntity.ok(memberService.getAllMembers());
    }

    /**
     * POST /user/block — block/unblock a member (ADMIN only)
     */
    @PostMapping("/block")
    public ResponseEntity<?> blockMember(
            @AuthenticationPrincipal Member currentMember,
            @RequestBody Map<String, Object> input
    ) {
        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(Map.of("message", "Admin access required"));
        }

        Long memberId = Long.parseLong(input.get("memberId").toString());
        String status = input.get("memberStatus").toString();
        MemberStatus memberStatus = MemberStatus.valueOf(status);

        Member updated = memberService.updateMemberStatus(memberId, memberStatus);
        return ResponseEntity.ok(updated);
    }
}
