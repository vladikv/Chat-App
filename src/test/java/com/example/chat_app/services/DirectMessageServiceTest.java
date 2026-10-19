package com.example.chat_app.services;

import com.example.chat_app.dto.dm.DMItemDTO;
import com.example.chat_app.entities.DirectMessageEntity;
import com.example.chat_app.entities.UserEntity;
import com.example.chat_app.repositories.DirectMessageRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

    @Mock private DirectMessageRepository directMessageRepository;
    @Mock private UserService userService;

    @InjectMocks private DirectMessageService directMessageService;

    @Test
    void getConversationPage_returnsOldestToNewest() {
        when(directMessageRepository.findConversationPage(eq("alice"), eq("bob"), any(Pageable.class)))
                .thenReturn(List.of(dm(2L), dm(1L))); // repository returns newest first

        List<DMItemDTO> result = directMessageService.getConversationPage("alice", "bob", null, 30);

        assertThat(result).extracting(DMItemDTO::getId).containsExactly(1L, 2L);
    }

    @Test
    void getConversationPage_withCursor_usesBeforeQuery() {
        when(directMessageRepository.findConversationBefore(eq("alice"), eq("bob"), eq(10L), any(Pageable.class)))
                .thenReturn(List.of(dm(9L)));

        List<DMItemDTO> result = directMessageService.getConversationPage("alice", "bob", 10L, 30);

        assertThat(result).hasSize(1);
        verify(directMessageRepository, never())
                .findConversationPage(any(), any(), any(Pageable.class));
    }

    @Test
    void getConversationPage_capsLimitAt100() {
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        when(directMessageRepository.findConversationPage(eq("alice"), eq("bob"), captor.capture()))
                .thenReturn(Collections.emptyList());

        directMessageService.getConversationPage("alice", "bob", null, 1000);

        assertThat(captor.getValue().getPageSize()).isEqualTo(100);
    }

    private DirectMessageEntity dm(Long id) {
        UserEntity alice = new UserEntity();
        alice.setUsername("alice");
        UserEntity bob = new UserEntity();
        bob.setUsername("bob");

        DirectMessageEntity m = new DirectMessageEntity();
        m.setId(id);
        m.setSender(alice);
        m.setReceiver(bob);
        m.setContent("hi " + id);
        return m;
    }
}