package com.crackview.repository;

import com.crackview.model.InterviewMessage;
import com.crackview.model.InterviewSession;
import com.crackview.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class InterviewMessageRepositoryTest {

    @Autowired
    private InterviewMessageRepository messageRepository;

    @Autowired
    private InterviewSessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    private InterviewSession session;

    @BeforeEach
    void setUp() {
        messageRepository.deleteAll();
        sessionRepository.deleteAll();
        userRepository.deleteAll();

        User user = userRepository.save(User.builder()
                .username("chatter").email("chat@test.com").build());

        session = sessionRepository.save(InterviewSession.builder()
                .user(user).targetCompany("Google").domain("java").build());

        messageRepository.save(InterviewMessage.builder()
                .session(session).role("agent")
                .content("请介绍一下HashMap的底层实现").build());

        messageRepository.save(InterviewMessage.builder()
                .session(session).role("user")
                .content("HashMap基于数组+链表+红黑树实现...")
                .score(75f).build());

        messageRepository.save(InterviewMessage.builder()
                .session(session).role("agent")
                .content("那HashMap什么时候会转换为红黑树？").build());
    }

    @Test
    @DisplayName("findBySessionId - should return all messages for a session")
    void findBySessionId_shouldReturnAll() {
        List<InterviewMessage> messages = messageRepository.findBySessionId(session.getId());
        assertThat(messages).hasSize(3);
    }

    @Test
    @DisplayName("findBySessionIdOrderByCreatedAtAsc - should return messages in chronological order")
    void findBySessionIdOrdered_shouldReturnAsc() {
        List<InterviewMessage> messages = messageRepository
                .findBySessionIdOrderByCreatedAtAsc(session.getId());
        assertThat(messages).hasSize(3);
        assertThat(messages.get(0).getRole()).isEqualTo("agent");
        assertThat(messages.get(1).getRole()).isEqualTo("user");
        assertThat(messages.get(2).getRole()).isEqualTo("agent");
    }

    @Test
    @DisplayName("countBySessionId - should return correct message count")
    void countBySessionId_shouldReturnCount() {
        long count = messageRepository.countBySessionId(session.getId());
        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("findByRelatedNodeId - should return empty when no node linked")
    void findByRelatedNodeId_shouldReturnEmpty() {
        List<InterviewMessage> messages = messageRepository.findByRelatedNodeId(999L);
        assertThat(messages).isEmpty();
    }

    @Test
    @DisplayName("save - should persist message with auto-generated id and uuid")
    void save_shouldPersist() {
        InterviewMessage msg = messageRepository.save(InterviewMessage.builder()
                .session(session).role("user")
                .content("链表长度超过8且数组长度>=64时转红黑树")
                .score(90f).build());

        assertThat(msg.getId()).isNotNull();
        assertThat(msg.getUuid()).isNotNull().hasSize(36);
        assertThat(msg.getCreatedAt()).isNotNull();
    }
}
