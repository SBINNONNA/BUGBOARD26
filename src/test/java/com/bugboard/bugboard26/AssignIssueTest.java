package com.bugboard.bugboard26;

import com.bugboard.bugboard26.model.Issue;
import com.bugboard.bugboard26.model.Project;
import com.bugboard.bugboard26.model.User;
import com.bugboard.bugboard26.repository.IssueRepository;
import com.bugboard.bugboard26.repository.ProjectRepository;
import com.bugboard.bugboard26.repository.UserRepository;
import com.bugboard.bugboard26.service.IssueService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per il metodo {@code assignIssue(Long issueId, Long userId, String requesterEmail)}
 * di {@link IssueService}.
 *
 * Classi di equivalenza individuate:
 *  - CE1: richiedente è ADMIN         → assegnazione consentita
 *  - CE2: richiedente non è ADMIN     → assegnazione negata (RuntimeException)
 *  - CE3: assegnatario era UNASSIGNED → viene promosso ad ASSIGNED_USER
 *  - CE4: issue aveva già un assegnatario diverso → vecchio assegnatario retrocesso
 */
@ExtendWith(MockitoExtension.class)
class AssignIssueTest {

    @Mock private IssueRepository   issueRepository;
    @Mock private UserRepository    userRepository;
    @Mock private ProjectRepository projectRepository;

    @InjectMocks
    private IssueService issueService;

    private User makeUser(Long id, String email, User.Role role) {
        User u = new User();
        u.setId(id);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }

    private Issue makeIssue(User assignedTo) {
        Project p = new Project();
        p.setMembers(new ArrayList<>());
        Issue i = new Issue();
        i.setId(1L);
        i.setStatus(Issue.Status.TODO);
        i.setAssignedTo(assignedTo);
        i.setProject(p);
        return i;
    }

    /**
     * CE1 + CE3: admin assegna issue a utente non assegnato.
     * Atteso: status IN_PROGRESS, assegnatario impostato, ruolo promosso.
     */
    @Test
    void assignIssue_adminAssegnaAUnassigned_promuoveEAggiorna() {
        User admin    = makeUser(1L, "admin@test.com", User.Role.ADMIN);
        User assignee = makeUser(2L, "user@test.com",  User.Role.UNASSIGNED_USER);
        Issue issue   = makeIssue(null);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(2L)).thenReturn(Optional.of(assignee));
        when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Issue result = issueService.assignIssue(1L, 2L, "admin@test.com");

        assertEquals(Issue.Status.IN_PROGRESS, result.getStatus());
        assertEquals(assignee, result.getAssignedTo());
        assertEquals(User.Role.ASSIGNED_USER, assignee.getRole());
    }

    /**
     * CE2: utente non admin tenta di assegnare una issue.
     * Atteso: RuntimeException lanciata.
     */
    @Test
    void assignIssue_nonAdmin_lanciaEccezione() {
        User user  = makeUser(1L, "user@test.com", User.Role.UNASSIGNED_USER);
        Issue issue = makeIssue(null);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> issueService.assignIssue(1L, 2L, "user@test.com"));
        verify(issueRepository, never()).save(any());
    }

    /**
     * CE4: admin riassegna issue, il vecchio assegnatario viene retrocesso.
     * Atteso: oldAssignee torna UNASSIGNED_USER se non ha altre issue attive.
     */
    @Test
    void assignIssue_riassegna_retrocedeVecchioAssegnatario() {
        User admin      = makeUser(1L, "admin@test.com", User.Role.ADMIN);
        User oldAssignee = makeUser(2L, "old@test.com",  User.Role.ASSIGNED_USER);
        User newAssignee = makeUser(3L, "new@test.com",  User.Role.UNASSIGNED_USER);
        Issue issue      = makeIssue(oldAssignee);

        when(issueRepository.findById(1L)).thenReturn(Optional.of(issue));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.findById(3L)).thenReturn(Optional.of(newAssignee));
        when(issueRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(issueRepository.existsByAssignedToAndStatusNot(oldAssignee, Issue.Status.DONE))
                .thenReturn(false);

        issueService.assignIssue(1L, 3L, "admin@test.com");

        assertEquals(User.Role.UNASSIGNED_USER, oldAssignee.getRole());
    }
}
