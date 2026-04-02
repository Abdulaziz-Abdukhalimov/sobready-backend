package com.sobready.backend.repository;

import com.sobready.backend.entity.Member;
import com.sobready.backend.enums.MemberStatus;
import com.sobready.backend.enums.MemberType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Same pattern as ProductRepository — define the interface, Spring writes the queries.
 *
 * NestJS equivalent:
 *   @InjectRepository(Member)
 *   private memberRepo: Repository<Member>;
 *   await this.memberRepo.findOne({ where: { memberNick: "john" } });
 *
 * Spring reads method names:
 *   findByMemberNick → SELECT * FROM members WHERE member_nick = ?
 *   findByMemberType → SELECT * FROM members WHERE member_type = ?
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * For login — find member by nickname
     * Optional<Member> = might return null (like Member | null in TypeScript)
     */
    Optional<Member> findByMemberNick(String memberNick);

    /**
     * Check if nickname already exists (for signup validation)
     */
    boolean existsByMemberNick(String memberNick);

    /**
     * For "top users" endpoint — find active members sorted by points
     */
    List<Member> findTop4ByMemberStatusOrderByMemberPointsDesc(MemberStatus status);

    /**
     * For "manager" endpoint — find admin member
     */
    Optional<Member> findFirstByMemberType(MemberType memberType);
}
