package com.sobready.backend.service;

import com.sobready.backend.config.JwtUtil;
import com.sobready.backend.entity.Member;
import com.sobready.backend.enums.MemberStatus;
import com.sobready.backend.enums.MemberType;
import com.sobready.backend.repository.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Handles all member-related business logic:
 * - Signup (register new user)
 * - Login (verify credentials, create JWT token)
 * - Logout (clear cookie)
 * - Update profile (including image upload)
 * - Get top users / manager
 *
 * NestJS equivalent:
 *
 *   @Injectable()
 *   export class AuthService {
 *     constructor(
 *       @InjectRepository(Member) private memberRepo: Repository<Member>,
 *       private jwtService: JwtService,
 *     ) {}
 *
 *     async signup(input: MemberInput) {
 *       const hashed = await bcrypt.hash(input.memberPassword, 10);
 *       return this.memberRepo.save({ ...input, memberPassword: hashed });
 *     }
 *   }
 */
@Service
public class MemberService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // Path where uploaded images are stored
    private final String uploadDir = "src/main/resources/static/uploads/";

    /**
     * Register a new member.
     *
     * Security steps:
     * 1. Check if nickname already exists
     * 2. Hash the password with BCrypt (NEVER store plain text!)
     * 3. Save to database
     * 4. Create JWT token and set as cookie
     * 5. Return member data (password is hidden by @JsonIgnore)
     */
    public Member signup(String memberNick, String memberPhone, String memberPassword,
                         HttpServletResponse response) {

        // 1. Check if nickname is taken
        if (memberRepository.existsByMemberNick(memberNick)) {
            throw new RuntimeException("This nickname is already taken");
        }

        // 2. Hash the password — "password123" becomes "$2a$10$xJ8k..."
        //    Even if someone steals the database, they can't reverse the hash
        String hashedPassword = passwordEncoder.encode(memberPassword);

        // 3. Build and save the member
        Member member = Member.builder()
                .memberNick(memberNick)
                .memberPhone(memberPhone)
                .memberPassword(hashedPassword)
                .build();

        member = memberRepository.save(member);

        // 4. Create JWT token and set as HTTP cookie
        setTokenCookie(member, response);

        return member;
    }

    /**
     * Log in an existing member.
     *
     * Security steps:
     * 1. Find member by nickname
     * 2. Check if account is active (not blocked/deleted)
     * 3. Verify password against the stored hash
     * 4. Create JWT token and set as cookie
     */
    public Member login(String memberNick, String memberPassword,
                        HttpServletResponse response) {

        // 1. Find by nickname
        Member member = memberRepository.findByMemberNick(memberNick)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        // 2. Check account status
        if (member.getMemberStatus() != MemberStatus.ACTIVE) {
            throw new RuntimeException("Account is not active");
        }

        // 3. Verify password — passwordEncoder.matches() compares plain text with hash
        //    "password123" vs "$2a$10$xJ8k..." → true or false
        if (!passwordEncoder.matches(memberPassword, member.getMemberPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        // 4. Set token cookie
        setTokenCookie(member, response);

        return member;
    }

    /**
     * Log out — just clear the cookie.
     *
     * Since JWT is stateless, there's no server-side session to destroy.
     * We just remove the cookie from the browser.
     *
     * In NestJS: res.clearCookie('accessToken')
     */
    public void logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("accessToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 0 = delete the cookie immediately
        response.addCookie(cookie);
    }

    /**
     * Update member profile.
     * Handles both text fields and image file upload.
     */
    public Member updateMember(Member currentMember, String memberNick, String memberPhone,
                               String memberAddress, String memberDesc,
                               MultipartFile memberImage) throws IOException {

        if (memberNick != null && !memberNick.isEmpty()) {
            // Check if the new nick is taken by someone else
            if (!memberNick.equals(currentMember.getMemberNick())
                    && memberRepository.existsByMemberNick(memberNick)) {
                throw new RuntimeException("This nickname is already taken");
            }
            currentMember.setMemberNick(memberNick);
        }

        if (memberPhone != null && !memberPhone.isEmpty()) {
            currentMember.setMemberPhone(memberPhone);
        }
        if (memberAddress != null) {
            currentMember.setMemberAddress(memberAddress);
        }
        if (memberDesc != null) {
            currentMember.setMemberDesc(memberDesc);
        }

        // Handle image upload
        if (memberImage != null && !memberImage.isEmpty()) {
            String imagePath = saveImage(memberImage);
            currentMember.setMemberImage(imagePath);
        }

        return memberRepository.save(currentMember);
    }

    /**
     * Get top 4 users by points — for the homepage "Top Users" section.
     */
    public List<Member> getTopUsers() {
        return memberRepository.findTop4ByMemberStatusOrderByMemberPointsDesc(MemberStatus.ACTIVE);
    }

    /**
     * Get the admin/manager — for the "About" section.
     */
    public Member getManager() {
        return memberRepository.findFirstByMemberType(MemberType.ADMIN)
                .orElseThrow(() -> new RuntimeException("No manager found"));
    }

    /**
     * Get all members (ADMIN).
     */
    public List<Member> getAllMembers() {
        return memberRepository.findAll();
    }

    /**
     * Update member status — block/unblock (ADMIN).
     */
    public Member updateMemberStatus(Long memberId, MemberStatus status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        member.setMemberStatus(status);
        return memberRepository.save(member);
    }

    /**
     * Create JWT token and set it as an HTTP-only cookie.
     *
     * HttpOnly cookie means:
     * - JavaScript (document.cookie) CANNOT read it → prevents XSS attacks
     * - Browser automatically sends it with every request → no manual handling needed
     *
     * Your React app uses withCredentials: true, which tells Axios
     * to include cookies automatically.
     */
    private void setTokenCookie(Member member, HttpServletResponse response) {
        String token = jwtUtil.generateToken(member.getId(), member.getMemberNick());

        Cookie cookie = new Cookie("accessToken", token);
        cookie.setHttpOnly(true);   // JavaScript can't access this cookie (XSS protection)
        cookie.setPath("/");        // Cookie is sent for all routes
        cookie.setMaxAge(86400);    // 24 hours in seconds
        // cookie.setSecure(true);  // Uncomment in production (HTTPS only)

        response.addCookie(cookie);
    }

    /**
     * Save uploaded image to the uploads directory.
     * Returns the relative path (e.g., "uploads/abc123.jpg")
     *
     * Security: we generate a UUID filename to prevent:
     * - Path traversal attacks (../../etc/passwd)
     * - Filename collisions
     * - Original filename exposure
     */
    private String saveImage(MultipartFile file) throws IOException {
        // Create uploads directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename: "a1b2c3d4.jpg"
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String newFilename = UUID.randomUUID() + extension;

        // Save file to disk
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

        // Return relative path (this is what gets stored in DB and sent to React)
        return "uploads/" + newFilename;
    }
}
