package ee.bitweb.core.actuator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class ActuatorAuthenticationProviderTest {

    @Mock
    Authentication authentication;

    @Mock
    ActuatorSecurityProperties actuatorSecurityProperties;

    @Mock
    ActuatorSecurityProperties.User actuatorUser;

    @InjectMocks
    ActuatorAuthenticationProvider provider;

    @Test
    @DisplayName("Invalid username must throw an exception")
    void testInvalidUsernameThrowsException() {
        // given
        when(actuatorUser.getName()).thenReturn("user123");
        when(actuatorSecurityProperties.getUser()).thenReturn(actuatorUser);
        when(authentication.getPrincipal()).thenReturn("otherUser");
        when(authentication.getCredentials()).thenReturn("somePassword");

        // when
        var e = assertThrows(BadCredentialsException.class, () -> provider.authenticate(authentication));

        // then
        assertEquals("Invalid username", e.getMessage());
        verify(actuatorUser, times(1)).getName();
        verify(actuatorSecurityProperties, times(1)).getUser();
        verify(authentication, times(1)).getPrincipal();
        verify(authentication, times(1)).getCredentials();

        verifyNoMoreInteractions(actuatorUser, actuatorSecurityProperties, authentication);
    }

    @Test
    @DisplayName("Invalid password must throw an exception")
    void testInvalidPasswordThrowsException() {
        // given
        when(actuatorUser.getName()).thenReturn("user123");
        when(actuatorUser.getPassword()).thenReturn("password123");
        when(actuatorSecurityProperties.getUser()).thenReturn(actuatorUser);
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getCredentials()).thenReturn("somePassword");

        // when
        var e = assertThrows(BadCredentialsException.class, () -> provider.authenticate(authentication));

        // then
        assertEquals("Invalid password", e.getMessage());
        verify(actuatorUser, times(1)).getName();
        verify(actuatorUser, times(1)).getPassword();
        verify(actuatorSecurityProperties, times(1)).getUser();
        verify(authentication, times(1)).getPrincipal();
        verify(authentication, times(1)).getCredentials();

        verifyNoMoreInteractions(actuatorUser, actuatorSecurityProperties, authentication);
    }

    @Test
    @DisplayName("Authentication successful, correct authorities returned")
    void testSuccessfulAuthentication() {
        // given
        when(actuatorUser.getName()).thenReturn("user123");
        when(actuatorUser.getPassword()).thenReturn("password123");
        when(actuatorUser.getRoles()).thenReturn(List.of("actuator", "something"));
        when(actuatorSecurityProperties.getUser()).thenReturn(actuatorUser);
        when(authentication.getPrincipal()).thenReturn("user123");
        when(authentication.getCredentials()).thenReturn("password123");

        // when
        var auth = provider.authenticate(authentication);

        // then
        assertEquals("user123", auth.getPrincipal());
        assertEquals("password123", auth.getCredentials());
        assertEquals(2, auth.getAuthorities().size());
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ACTUATOR")));
        assertTrue(auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_SOMETHING")));

        verify(actuatorUser, times(1)).getName();
        verify(actuatorUser, times(1)).getPassword();
        verify(actuatorUser, times(1)).getRoles();
        verify(actuatorSecurityProperties, times(2)).getUser();
        verify(authentication, times(1)).getPrincipal();
        verify(authentication, times(1)).getCredentials();

        verifyNoMoreInteractions(actuatorUser, actuatorSecurityProperties, authentication);
    }
}
