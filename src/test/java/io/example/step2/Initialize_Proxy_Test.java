package io.example.step2;

import static org.assertj.core.api.Assertions.assertThat;

import io.config.base.JpaTestBase;
import io.example.step2.domain.Member;
import io.example.step2.domain.Team;
import io.example.step2.repo.MemberRepo;
import io.example.step2.repo.TeamRepo;
import java.util.List;
import org.hibernate.Hibernate;
import org.hibernate.proxy.HibernateProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Step2: 프록시 객체 초기화 시점 알아보기")
class Initialize_Proxy_Test extends JpaTestBase {
    private final TeamRepo teamRepo;
    private final MemberRepo memberRepo;

    public Initialize_Proxy_Test(TeamRepo teamRepo, MemberRepo memberRepo) {
        this.teamRepo = teamRepo;
        this.memberRepo = memberRepo;
    }

    private Team team = null;
    private Member member1, member2 = null;

    @BeforeEach
    void setUp() {
        team = new Team("팀");
        teamRepo.save(team);

        member1 = new Member("멤버", team);
        member2 = new Member("멤버2", team);
        memberRepo.saveAll(List.of(member1, member2));

        flushAndClear();
    }

    @Nested
    @DisplayName("FetchType.LAZY 전략의")
    class proxyInitializationDetail {
        @Test
        @DisplayName("1. Member 조회 시, Member와 연관 관계를 가지는 Team은 프록시 객체 이다.")
        void beforeLazyTypeRelationalTeamIsInitialized() {
            // When
            /**    // LAZY 전략으로 인해 member만 조회하고, 연관관계를 가지는 Team은 조회 하지 않음
             *     select
             *         m1_0.id,
             *         m1_0.name,
             *         m1_0.team_id
             *     from
             *         member m1_0
             *     where
             *         m1_0.id=?
             */
            Member actual = memberRepo.findById(member1.getId()).orElseThrow();

            // Then
            assertThat(actual.getTeam())
                .as("지연 로딩으로 인해 Team 객체는 Proxy 객체로 채워지게 된다.")
                .isInstanceOf(HibernateProxy.class);

            assertThat(Hibernate.isInitialized(actual.getTeam()))
                .as("Proxy 객체인 Team 객체에 별도 접근이 없었으므로, 초기화 되지 않았다.")
                .isFalse();
        }

        @Test
        @DisplayName("2. Member 조회 후, Member와 연관 관계를 가지는 Team의 프록시 객체에 접근 하면 프록시 객체는 초기화 된다.")
        void afterLazyTypeRelationalTeamIsInitialized() {
            // When
            /**
             *     select
             *         m1_0.id,
             *         m1_0.name,
             *         m1_0.team_id
             *     from
             *         member m1_0
             *     where
             *         m1_0.id=?
             * ============================================== // printLine();
             * [Hibernate]
             *     // Proxy 객체 초기화를 위한 조회 쿼리 실행
             *     select
             *         t1_0.id,
             *         t1_0.name
             *     from
             *         team t1_0
             *     where
             *         t1_0.id=?
             */
            Member actual = memberRepo.findById(member1.getId()).orElseThrow();
            printLine();
            actual.getTeam().getName();

            // Then
            assertThat(actual.getTeam())
                .as("지연 로딩으로 인해 Team 객체는 Proxy 객체로 채워지게 된다.")
                .isInstanceOf(HibernateProxy.class);
            assertThat(Hibernate.isInitialized(actual.getTeam()))
                .as("Proxy 객체인 Team 객체에 접근 하는 경우 Proxy 객체는 초기화 된다.")
                .isTrue();
        }

        @Test
        @DisplayName("3. 연관관계를 가지는 객체에 접근 하면서, 의도치 않게 초기화를 위한 조회 쿼리가 실행 되는 현상에 주의")
        void dd() {
            // When
            Member actual = memberRepo.findById(member1.getId()).orElseThrow();
            printLine();
            actual.getTeam().getName();
        }
    }
}
