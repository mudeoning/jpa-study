package io.example.step4;

import static org.assertj.core.api.Assertions.assertThat;

import io.config.base.JpaTestBase;
import io.example.step2.domain.Member;
import io.example.step2.domain.Team;
import io.example.step2.repo.MemberRepo;
import io.example.step2.repo.TeamRepo;
import io.example.step4.domain.Board;
import io.example.step4.domain.Comment;
import io.example.step4.repo.BoardRepo;
import io.example.step4.repo.CommentRepo;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("Step4: 1:N 연관 관계를 조회 하는 과정에서 발생하는 N+1 문제와 해결 방법")
public class N_Plus_1_Problem_And_Solution_Test extends JpaTestBase {
    private final TeamRepo teamRepo;
    private final MemberRepo memberRepo;
    private final BoardRepo boardRepo;
    private final CommentRepo commentRepo;

    public N_Plus_1_Problem_And_Solution_Test(
        TeamRepo teamRepo,
        MemberRepo memberRepo,
        BoardRepo boardRepo,
        CommentRepo commentRepo
    ) {
        this.teamRepo = teamRepo;
        this.memberRepo = memberRepo;
        this.boardRepo = boardRepo;
        this.commentRepo = commentRepo;
    }

    @BeforeEach
    void setUp() {
        dummyTeamAndMember(5);
        dummyBoardAndComment(5);
        flushAndClear();
    }


    @Nested
    @DisplayName("1:N 연관 관계 조회 시")
    class N_Plus_1_Problems {

        // 1:N Team: Member lazy
        // 팀목록 필요해서 -> 팀목록을 조회하게 되면
        //
        @Test
        @DisplayName("1. N+1 문제를 인지 하고")
        void Recognition() {
            // Given
            List<Team> teams = teamRepo.findAll();
            // 팀목록 조회 쿼리가 1번만 수행되기를 원했는데,
            // 데이터를 서버로 끄집어내서 각 팀 단건 데이터를 처리하는 과정에서
            // 팀에 소속된 멤버데이터가 필요한 경우가 발생하는 경우
            // 각 팀의 멤버를 초기화 하기 위한 쿼리가 추가적으로 N번 발생하는데
            // 이를 N + 1 문제라고 합니다.

            // When
            System.out.println("=============");
            teams.forEach(team -> team.getMembers()
                .forEach(member -> System.out.println(
                    team.getName() + " -> " + member.getName())
                ));

            // Then
            assertThat(teams).hasSize(5);
        }

        // 1:N Board: Comment
        @Test
        @DisplayName("2. N+1 문제 해결해 본다.")
        void Solution() {
            // Given
            List<Board> boards = boardRepo.findAll();

            // When
            System.out.println("=============");
            boards.forEach(board -> board.getComments().forEach(comment -> System.out.println(board.getTitle() + " -> " + comment.getContent())));

            // Then
            assertThat(boards).hasSize(5);
        }
    }

    private void dummyTeamAndMember(int count) {
        for (int i = 1; i <= count; i++) {
            String name = "팀:" + i;
            Team savedTeam = teamRepo.save(new Team(name));
            dummyMember(count, savedTeam);
        }
    }

    private void dummyMember(int count, Team team) {
        for (int i = 1; i <= count; i++) {
            String name = "회원:" + i;
            memberRepo.save(new Member(name, team));
        }
    }

    private void dummyBoardAndComment(int count) {
        for (int i = 1; i <= count; i++) {
            String title = "게시글 제목: " + i;
            String content = "게시글 내용:" + i;
            Board savedBoard = boardRepo.save(new Board(title, content));
            dummyComments(count, savedBoard);
        }
    }

    private void dummyComments(int count, Board board) {
        for (int i = 1; i <= count; i++) {
            String content = "댓글 내용:" + i;
            commentRepo.save(new Comment(content, board));
        }
    }
}
